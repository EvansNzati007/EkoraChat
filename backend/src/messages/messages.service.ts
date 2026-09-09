import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { ConversationsService } from '../conversations/conversations.service';
import { GeminiService } from './gemini.service';
import { MessagesGateway } from './messages.gateway';
import { PushService } from '../push/push.service';
import { MessageStatus, MessageType } from '../generated/prisma/client';

function previewOf(message: { type: string; content: string | null }): string {
  switch (message.type) {
    case 'IMAGE':
      return '📷 Photo';
    case 'AUDIO':
      return '🎤 Message vocal';
    case 'FILE':
      return `📎 ${message.content ?? 'Fichier'}`;
    default:
      return message.content ?? '';
  }
}

@Injectable()
export class MessagesService {
  constructor(
    private prisma: PrismaService,
    private convs: ConversationsService,
    private gemini: GeminiService,
    private gateway: MessagesGateway,
    private push: PushService,
  ) {}

  private async notifyOfflineParticipants(
    conversationId: string,
    senderId: string,
    senderUsername: string,
    message: { id: string; type: string; content: string | null },
  ) {
    const [participants, presentUserIds] = await Promise.all([
      this.prisma.conversationParticipant.findMany({
        where: { conversationId, userId: { not: senderId } },
        include: {
          user: { select: { id: true, fcmToken: true, isBot: true } },
        },
      }),
      this.gateway.getRoomMemberIds(conversationId),
    ]);

    const body = previewOf(message);
    for (const p of participants) {
      const { user } = p;
      if (user.isBot || !user.fcmToken || presentUserIds.has(user.id)) continue;
      await this.push.notifyNewMessage(user.fcmToken, senderUsername, body, {
        conversationId,
        otherUserId: senderId,
      });
    }
  }

  async send(userId: string, conversationId: string, content: string) {
    const conv = await this.convs.assertMember(conversationId, userId);

    const message = await this.prisma.message.create({
      data: { conversationId, senderId: userId, content },
      include: {
        sender: { select: { id: true, username: true, isBot: true } },
      },
    });
    this.gateway.emitNewMessage(conversationId, message);
    await this.notifyOfflineParticipants(
      conversationId,
      userId,
      message.sender.username,
      message,
    );

    await this.prisma.conversation.update({
      where: { id: conversationId },
      data: { updatedAt: new Date() },
    });

    if (conv.type === 'AI') {
      const bot = await this.prisma.user.findUnique({
        where: { username: 'ekora-ai' },
      });
      const aiText = await this.gemini.reply(content);
      const aiMessage = await this.prisma.message.create({
        data: { conversationId, senderId: bot!.id, content: aiText },
        include: {
          sender: { select: { id: true, username: true, isBot: true } },
        },
      });
      this.gateway.emitNewMessage(conversationId, aiMessage);
      await this.notifyOfflineParticipants(
        conversationId,
        bot!.id,
        aiMessage.sender.username,
        aiMessage,
      );
      return { message, aiMessage };
    }

    return { message };
  }

  async sendMedia(
    userId: string,
    conversationId: string,
    type: MessageType,
    mediaUrl: string,
    content?: string,
  ) {
    await this.convs.assertMember(conversationId, userId);

    const message = await this.prisma.message.create({
      data: { conversationId, senderId: userId, type, mediaUrl, content },
      include: {
        sender: { select: { id: true, username: true, isBot: true } },
      },
    });

    await this.prisma.conversation.update({
      where: { id: conversationId },
      data: { updatedAt: new Date() },
    });

    this.gateway.emitNewMessage(conversationId, message);
    await this.notifyOfflineParticipants(
      conversationId,
      userId,
      message.sender.username,
      message,
    );
    return { message };
  }

  async list(userId: string, conversationId: string) {
    await this.convs.assertMember(conversationId, userId);

    // Ouvrir la conversation vaut accusé de lecture pour les messages reçus.
    const { count } = await this.prisma.message.updateMany({
      where: {
        conversationId,
        senderId: { not: userId },
        status: { not: MessageStatus.READ },
      },
      data: { status: MessageStatus.READ },
    });
    if (count > 0) {
      this.gateway.emitMessagesRead(conversationId, userId);
    }

    return this.prisma.message.findMany({
      where: { conversationId },
      include: {
        sender: { select: { id: true, username: true, isBot: true } },
      },
      orderBy: { createdAt: 'asc' },
    });
  }
}

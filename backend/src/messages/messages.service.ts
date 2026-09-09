import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { ConversationsService } from '../conversations/conversations.service';
import { GeminiService } from './gemini.service';
import { MessagesGateway } from './messages.gateway';
import { MessageType } from '../generated/prisma/client';

@Injectable()
export class MessagesService {
  constructor(
    private prisma: PrismaService,
    private convs: ConversationsService,
    private gemini: GeminiService,
    private gateway: MessagesGateway,
  ) {}

  async send(userId: string, conversationId: string, content: string) {
    const conv = await this.convs.assertMember(conversationId, userId);

    const message = await this.prisma.message.create({
      data: { conversationId, senderId: userId, content },
      include: {
        sender: { select: { id: true, username: true, isBot: true } },
      },
    });
    this.gateway.emitNewMessage(conversationId, message);

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
    return { message };
  }

  async list(userId: string, conversationId: string) {
    await this.convs.assertMember(conversationId, userId);
    return this.prisma.message.findMany({
      where: { conversationId },
      include: {
        sender: { select: { id: true, username: true, isBot: true } },
      },
      orderBy: { createdAt: 'asc' },
    });
  }
}

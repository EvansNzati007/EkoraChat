import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import * as bcrypt from 'bcrypt';

@Injectable()
export class ConversationsService {
  constructor(private prisma: PrismaService) {}

  private async ensureBot() {
    return this.prisma.user.upsert({
      where: { username: 'ekora-ai' },
      update: {},
      create: {
        username: 'ekora-ai',
        email: 'ai@ekorachat.local',
        password: await bcrypt.hash('not-used-' + Date.now(), 10),
        isBot: true,
      },
    });
  }

  async createPrivate(userId: string, participantId: string) {
    // Conversation PRIVATE existante entre ces deux users ?
    const existing = await this.prisma.conversation.findFirst({
      where: {
        type: 'PRIVATE',
        AND: [
          { participants: { some: { userId } } },
          { participants: { some: { userId: participantId } } },
        ],
      },
    });
    if (existing) return existing;

    return this.prisma.conversation.create({
      data: {
        type: 'PRIVATE',
        participants: { create: [{ userId }, { userId: participantId }] },
      },
      include: {
        participants: {
          include: { user: { select: { id: true, username: true } } },
        },
      },
    });
  }

  async createAI(userId: string) {
    const bot = await this.ensureBot();
    const existing = await this.prisma.conversation.findFirst({
      where: {
        type: 'AI',
        AND: [
          { participants: { some: { userId } } },
          { participants: { some: { userId: bot.id } } },
        ],
      },
    });
    if (existing) return existing;

    return this.prisma.conversation.create({
      data: {
        type: 'AI',
        participants: { create: [{ userId }, { userId: bot.id }] },
      },
    });
  }

  list(userId: string) {
    return this.prisma.conversation.findMany({
      where: { participants: { some: { userId } } },
      include: {
        participants: {
          include: {
            user: {
              select: {
                id: true,
                username: true,
                avatar: true,
                isBot: true,
                isOnline: true,
                lastSeenAt: true,
              },
            },
          },
        },
        messages: { orderBy: { createdAt: 'desc' }, take: 1 },
      },
      orderBy: { updatedAt: 'desc' },
    });
  }

  async assertMember(conversationId: string, userId: string) {
    const member = await this.prisma.conversationParticipant.findUnique({
      where: { userId_conversationId: { userId, conversationId } },
    });
    if (!member)
      throw new ForbiddenException('Pas membre de cette conversation');
    const conv = await this.prisma.conversation.findUnique({
      where: { id: conversationId },
    });
    if (!conv) throw new NotFoundException('Conversation introuvable');
    return conv;
  }
}

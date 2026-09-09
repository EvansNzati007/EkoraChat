import { Logger } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import {
  OnGatewayConnection,
  OnGatewayDisconnect,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { ConversationsService } from '../conversations/conversations.service';
import { PrismaService } from '../prisma/prisma.service';

interface AuthenticatedSocket extends Socket {
  data: { userId?: string };
}

@WebSocketGateway({ cors: { origin: '*' } })
export class MessagesGateway
  implements OnGatewayConnection, OnGatewayDisconnect
{
  @WebSocketServer()
  server: Server;

  private logger = new Logger(MessagesGateway.name);
  // Compteur de connexions par utilisateur : gère proprement le
  // multi-device/reconnexion sans le marquer hors ligne trop tôt.
  private online = new Map<string, number>();

  constructor(
    private jwtService: JwtService,
    private convs: ConversationsService,
    private prisma: PrismaService,
  ) {}

  async handleConnection(client: AuthenticatedSocket) {
    const token =
      client.handshake.auth?.token ??
      client.handshake.headers?.authorization?.replace('Bearer ', '');

    if (!token) {
      client.disconnect();
      return;
    }

    try {
      const payload = this.jwtService.verify<{ sub: string }>(token);
      const userId = payload.sub;
      client.data.userId = userId;

      const count = (this.online.get(userId) ?? 0) + 1;
      this.online.set(userId, count);
      if (count === 1) {
        await this.prisma.user.update({
          where: { id: userId },
          data: { isOnline: true },
        });
        this.server.emit('presence', { userId, isOnline: true, lastSeenAt: null });
      }
    } catch {
      client.disconnect();
    }
  }

  async handleDisconnect(client: AuthenticatedSocket) {
    const userId = client.data.userId;
    if (!userId) return;

    const count = (this.online.get(userId) ?? 1) - 1;
    if (count <= 0) {
      this.online.delete(userId);
      const lastSeenAt = new Date();
      await this.prisma.user.update({
        where: { id: userId },
        data: { isOnline: false, lastSeenAt },
      });
      this.server.emit('presence', {
        userId,
        isOnline: false,
        lastSeenAt: lastSeenAt.toISOString(),
      });
    } else {
      this.online.set(userId, count);
    }
  }

  @SubscribeMessage('joinConversation')
  async joinConversation(
    client: AuthenticatedSocket,
    conversationId: string,
  ) {
    const userId = client.data.userId;
    if (!userId) return;

    try {
      await this.convs.assertMember(conversationId, userId);
      await client.join(`conversation:${conversationId}`);
    } catch (err) {
      this.logger.warn(
        `Refus joinConversation ${conversationId} pour ${userId}: ${err}`,
      );
    }
  }

  /**
   * Utilisateurs ayant actuellement cette conversation ouverte (room
   * Socket.IO) — sert à ne pousser une notification qu'à ceux qui ne la
   * verraient pas déjà passer en temps réel.
   */
  async getRoomMemberIds(conversationId: string): Promise<Set<string>> {
    const sockets = await this.server
      .in(`conversation:${conversationId}`)
      .fetchSockets();
    return new Set(
      sockets
        .map((s) => (s.data as { userId?: string }).userId)
        .filter((id): id is string => !!id),
    );
  }

  emitNewMessage(conversationId: string, payload: unknown) {
    this.server.to(`conversation:${conversationId}`).emit('newMessage', payload);
  }

  emitMessagesRead(conversationId: string, readerId: string) {
    this.server
      .to(`conversation:${conversationId}`)
      .emit('messagesRead', { conversationId, readerId });
  }
}

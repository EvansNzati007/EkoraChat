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

  constructor(
    private jwtService: JwtService,
    private convs: ConversationsService,
  ) {}

  handleConnection(client: AuthenticatedSocket) {
    const token =
      client.handshake.auth?.token ??
      client.handshake.headers?.authorization?.replace('Bearer ', '');

    if (!token) {
      client.disconnect();
      return;
    }

    try {
      const payload = this.jwtService.verify<{ sub: string }>(token);
      client.data.userId = payload.sub;
    } catch {
      client.disconnect();
    }
  }

  handleDisconnect() {}

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

  emitNewMessage(conversationId: string, payload: unknown) {
    this.server.to(`conversation:${conversationId}`).emit('newMessage', payload);
  }
}

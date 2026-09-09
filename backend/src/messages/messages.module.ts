import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtModule } from '@nestjs/jwt';
import { ConversationsModule } from '../conversations/conversations.module';
import { MessagesService } from './messages.service';
import { GeminiService } from './gemini.service';
import { MessagesGateway } from './messages.gateway';
import { MessagesController } from './messages.controller';
import { PrismaModule } from '../prisma/prisma.module';
import { PushModule } from '../push/push.module';

@Module({
  imports: [
    ConversationsModule,
    PrismaModule,
    PushModule,
    JwtModule.registerAsync({
      inject: [ConfigService],
      useFactory: async (configService: ConfigService) => ({
        secret: configService.get<string>('JWT_SECRET'),
      }),
    }),
  ],
  providers: [MessagesService, GeminiService, MessagesGateway],
  controllers: [MessagesController],
})
export class MessagesModule {}

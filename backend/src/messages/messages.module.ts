import { Module } from '@nestjs/common';
import { ConversationsModule } from '../conversations/conversations.module';
import { MessagesService } from './messages.service';
import { GeminiService } from './gemini.service';
import { MessagesController } from './messages.controller';
import { PrismaModule } from '../prisma/prisma.module';

@Module({
  imports: [ConversationsModule, PrismaModule],
  providers: [MessagesService, GeminiService],
  controllers: [MessagesController],
})
export class MessagesModule {}

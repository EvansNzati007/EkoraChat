import {
  Body,
  Controller,
  Get,
  Post,
  Request,
  UseGuards,
} from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { ConversationsService } from './conversations.service';

@Controller('conversations')
@UseGuards(JwtAuthGuard)
export class ConversationsController {
  constructor(private convs: ConversationsService) {}

  @Post()
  createPrivate(@Request() req, @Body('participantId') participantId: string) {
    return this.convs.createPrivate(req.user.userId, participantId);
  }

  @Post('ai')
  createAI(@Request() req) {
    return this.convs.createAI(req.user.userId);
  }

  @Get()
  list(@Request() req) {
    return this.convs.list(req.user.userId);
  }
}

import {
  Body,
  Controller,
  Get,
  Param,
  Post,
  Request,
  UseGuards,
} from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { MessagesService } from './messages.service';

@Controller('conversations/:id/messages')
@UseGuards(JwtAuthGuard)
export class MessagesController {
  constructor(private messages: MessagesService) {}

  @Post()
  send(
    @Request() req,
    @Param('id') id: string,
    @Body('content') content: string,
  ) {
    return this.messages.send(req.user.userId, id, content);
  }

  @Get()
  list(@Request() req, @Param('id') id: string) {
    return this.messages.list(req.user.userId, id);
  }
}

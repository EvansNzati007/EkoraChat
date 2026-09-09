import {
  BadRequestException,
  Body,
  Controller,
  Get,
  Param,
  Post,
  Request,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { MessagesService } from './messages.service';
import { MessageType } from '../generated/prisma/client';
import { uploadInterceptorOptions } from '../common/upload.util';
import { SendMessageDto } from './dto/send-message.dto';

const MAX_MEDIA_CAPTION_LENGTH = 500;

@Controller('conversations/:id/messages')
@UseGuards(JwtAuthGuard)
export class MessagesController {
  constructor(private messages: MessagesService) {}

  @Post()
  send(@Request() req, @Param('id') id: string, @Body() dto: SendMessageDto) {
    return this.messages.send(req.user.userId, id, dto.content);
  }

  @Post('media')
  @UseInterceptors(FileInterceptor('file', uploadInterceptorOptions('messages')))
  sendMedia(
    @Request() req,
    @Param('id') id: string,
    @UploadedFile() file: Express.Multer.File,
    @Body('type') type: string,
    @Body('content') content?: string,
  ) {
    if (!file) throw new BadRequestException('Fichier manquant ou type non autorisé');
    content = content?.slice(0, MAX_MEDIA_CAPTION_LENGTH);
    const messageType = (
      Object.values(MessageType).includes(type as MessageType)
        ? type
        : 'FILE'
    ) as MessageType;
    return this.messages.sendMedia(
      req.user.userId,
      id,
      messageType,
      `/uploads/messages/${file.filename}`,
      content,
    );
  }

  @Get()
  list(@Request() req, @Param('id') id: string) {
    return this.messages.list(req.user.userId, id);
  }
}

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
import { diskStorage } from 'multer';
import { extname, join } from 'path';
import { randomUUID } from 'crypto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { MessagesService } from './messages.service';
import { MessageType } from '../generated/prisma/client';

const ALLOWED_MIME = /^(image\/|audio\/)|^application\/(pdf|msword|vnd\.)/;

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

  @Post('media')
  @UseInterceptors(
    FileInterceptor('file', {
      storage: diskStorage({
        destination: join(process.cwd(), 'uploads'),
        filename: (_req, file, cb) =>
          cb(null, `${randomUUID()}${extname(file.originalname)}`),
      }),
      limits: { fileSize: 20 * 1024 * 1024 },
      fileFilter: (_req, file, cb) => {
        cb(null, ALLOWED_MIME.test(file.mimetype));
      },
    }),
  )
  sendMedia(
    @Request() req,
    @Param('id') id: string,
    @UploadedFile() file: Express.Multer.File,
    @Body('type') type: string,
    @Body('content') content?: string,
  ) {
    if (!file) throw new BadRequestException('Fichier manquant ou type non autorisé');
    const messageType = (
      Object.values(MessageType).includes(type as MessageType)
        ? type
        : 'FILE'
    ) as MessageType;
    return this.messages.sendMedia(
      req.user.userId,
      id,
      messageType,
      `/uploads/${file.filename}`,
      content,
    );
  }

  @Get()
  list(@Request() req, @Param('id') id: string) {
    return this.messages.list(req.user.userId, id);
  }
}

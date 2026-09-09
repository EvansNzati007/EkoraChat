import {
  Body,
  Controller,
  Get,
  Patch,
  Post,
  Query,
  Request,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { BadRequestException } from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { ChangePasswordDto } from './dto/change-password.dto';
import { UpdateFcmTokenDto } from './dto/update-fcm-token.dto';
import { imageUploadInterceptorOptions } from '../common/upload.util';

@Controller('users')
@UseGuards(JwtAuthGuard)
export class UsersController {
  constructor(private users: UsersService) {}

  @Get('me')
  me(@Request() req) {
    return this.users.me(req.user.userId);
  }

  @Get('search')
  search(@Query('username') username: string) {
    return this.users.search(username);
  }

  @Patch('me')
  updateProfile(@Request() req, @Body() dto: UpdateProfileDto) {
    return this.users.updateProfile(req.user.userId, dto);
  }

  @Patch('me/password')
  changePassword(@Request() req, @Body() dto: ChangePasswordDto) {
    return this.users.changePassword(req.user.userId, dto);
  }

  @Patch('me/fcm-token')
  updateFcmToken(@Request() req, @Body() dto: UpdateFcmTokenDto) {
    return this.users.setFcmToken(req.user.userId, dto.token);
  }

  @Post('me/avatar')
  @UseInterceptors(
    FileInterceptor('file', imageUploadInterceptorOptions('avatars')),
  )
  uploadAvatar(@Request() req, @UploadedFile() file: Express.Multer.File) {
    if (!file) throw new BadRequestException('Image manquante ou type non autorisé');
    return this.users.setAvatar(req.user.userId, `/uploads/avatars/${file.filename}`);
  }
}

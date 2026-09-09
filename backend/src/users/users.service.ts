import {
  ConflictException,
  Injectable,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { ChangePasswordDto } from './dto/change-password.dto';

const PUBLIC_SELECT = {
  id: true,
  username: true,
  email: true,
  avatar: true,
  isBot: true,
  isOnline: true,
  lastSeenAt: true,
};

@Injectable()
export class UsersService {
  constructor(private prisma: PrismaService) {}

  async search(username: string) {
    const user = await this.prisma.user.findUnique({
      where: { username },
      select: PUBLIC_SELECT,
    });
    if (!user)
      throw new NotFoundException("Cet utilisateur n'est pas sur EkoraChat");
    return user;
  }

  me(userId: string) {
    return this.prisma.user.findUnique({
      where: { id: userId },
      select: PUBLIC_SELECT,
    });
  }

  async updateProfile(userId: string, dto: UpdateProfileDto) {
    if (dto.username) {
      const exists = await this.prisma.user.findFirst({
        where: { username: dto.username, NOT: { id: userId } },
      });
      if (exists) throw new ConflictException("Nom d'utilisateur déjà pris");
    }
    return this.prisma.user.update({
      where: { id: userId },
      data: { username: dto.username },
      select: PUBLIC_SELECT,
    });
  }

  async changePassword(userId: string, dto: ChangePasswordDto) {
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    if (!user || !(await bcrypt.compare(dto.currentPassword, user.password)))
      throw new UnauthorizedException('Mot de passe actuel incorrect');

    await this.prisma.user.update({
      where: { id: userId },
      data: { password: await bcrypt.hash(dto.newPassword, 10) },
    });
    return { success: true };
  }

  async setAvatar(userId: string, url: string) {
    return this.prisma.user.update({
      where: { id: userId },
      data: { avatar: url },
      select: PUBLIC_SELECT,
    });
  }

  async setFcmToken(userId: string, token: string) {
    await this.prisma.user.update({
      where: { id: userId },
      data: { fcmToken: token },
    });
    return { success: true };
  }
}

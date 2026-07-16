import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class UsersService {
  constructor(private prisma: PrismaService) {}

  async search(username: string) {
    const user = await this.prisma.user.findUnique({
      where: { username },
      select: {
        id: true,
        username: true,
        email: true,
        avatar: true,
        isBot: true,
      },
    });
    if (!user)
      throw new NotFoundException("Cet utilisateur n'est pas sur EkoraChat");
    return user;
  }

  me(userId: string) {
    return this.prisma.user.findUnique({
      where: { id: userId },
      select: { id: true, username: true, email: true, avatar: true },
    });
  }
}

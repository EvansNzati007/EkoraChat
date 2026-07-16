import {
  BadRequestException,
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateContactDto } from './dto/create-contact.dto';

@Injectable()
export class ContactsService {
  constructor(private prisma: PrismaService) {}

  async add(userId: string, dto: CreateContactDto) {
    const target = await this.prisma.user.findUnique({
      where: { username: dto.username },
    });
    if (!target)
      throw new NotFoundException("Cet utilisateur n'est pas sur EkoraChat");
    if (target.id === userId)
      throw new BadRequestException("Impossible de s'ajouter soi-même");

    const exists = await this.prisma.contact.findUnique({
      where: { userId_contactId: { userId, contactId: target.id } },
    });
    if (exists) throw new ConflictException('Déjà dans vos contacts');

    return this.prisma.contact.create({
      data: { userId, contactId: target.id, nickname: dto.nickname },
      include: {
        contact: {
          select: { id: true, username: true, avatar: true, isBot: true },
        },
      },
    });
  }

  list(userId: string) {
    return this.prisma.contact.findMany({
      where: { userId },
      include: {
        contact: {
          select: { id: true, username: true, avatar: true, isBot: true },
        },
      },
      orderBy: { createdAt: 'desc' },
    });
  }
}

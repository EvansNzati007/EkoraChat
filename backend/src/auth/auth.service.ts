import {
  ConflictException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { PrismaService } from '../prisma/prisma.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private jwt: JwtService,
  ) {}

  async register(dto: RegisterDto) {
    const exists = await this.prisma.user.findFirst({
      where: { OR: [{ email: dto.email }, { username: dto.username }] },
    });
    if (exists) throw new ConflictException('Email ou username déjà utilisé');

    const user = await this.prisma.user.create({
      data: {
        email: dto.email,
        username: dto.username,
        password: await bcrypt.hash(dto.password, 10),
      },
    });
    return this.buildToken(user.id, user.username);
  }

  async login(dto: LoginDto) {
    const user = await this.prisma.user.findUnique({
      where: { email: dto.email },
    });
    if (!user || !(await bcrypt.compare(dto.password, user.password)))
      throw new UnauthorizedException('Identifiants invalides');

    return this.buildToken(user.id, user.username);
  }

  private buildToken(sub: string, username: string) {
    return {
      accessToken: this.jwt.sign({ sub, username }),
      userId: sub,
      username,
    };
  }
}

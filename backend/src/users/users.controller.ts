import { Controller, Get, Query, Request, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { UsersService } from './users.service';

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
}

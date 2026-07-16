import {
  Body,
  Controller,
  Get,
  Post,
  Request,
  UseGuards,
} from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { ContactsService } from './contacts.service';
import { CreateContactDto } from './dto/create-contact.dto';

@Controller('contacts')
@UseGuards(JwtAuthGuard)
export class ContactsController {
  constructor(private contacts: ContactsService) {}

  @Post()
  add(@Request() req, @Body() dto: CreateContactDto) {
    return this.contacts.add(req.user.userId, dto);
  }

  @Get()
  list(@Request() req) {
    return this.contacts.list(req.user.userId);
  }
}

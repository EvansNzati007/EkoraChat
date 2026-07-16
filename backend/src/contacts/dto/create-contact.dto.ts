import { IsOptional, IsString, MinLength } from 'class-validator';

export class CreateContactDto {
  @IsString()
  @MinLength(3)
  username: string;

  @IsOptional()
  @IsString()
  nickname?: string;
}

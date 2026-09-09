import { Transform } from 'class-transformer';
import { IsEmail, IsString, Matches, MaxLength, MinLength } from 'class-validator';

export class RegisterDto {
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsEmail()
  @MaxLength(255)
  email: string;

  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @MinLength(3)
  @MaxLength(30)
  @Matches(/^[a-zA-Z0-9_.-]+$/, {
    message:
      "Le nom d'utilisateur ne peut contenir que lettres, chiffres, points, tirets et underscores",
  })
  username: string;

  @IsString()
  @MinLength(6)
  @MaxLength(72) // bcrypt tronque silencieusement au-delà de 72 octets
  password: string;
}

import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class GeminiService {
  private logger = new Logger(GeminiService.name);

  constructor(private configService: ConfigService) {}

  async reply(userMessage: string): Promise<string> {
    try {
      const apiKey = this.configService.get<string>('GEMINI_API_KEY');

      const res = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${apiKey}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            system_instruction: {
              parts: [
                {
                  text: "Tu es l'assistant IA d'EkoraChat, une messagerie gabonaise. Réponds en français, de façon utile et concise.",
                },
              ],
            },
            contents: [{ parts: [{ text: userMessage }] }],
            generationConfig: {
              thinkingConfig: { thinkingBudget: 0 },
              maxOutputTokens: 1024,
            },
          }),
        },
      );

      const data = await res.json();

      if (!res.ok) {
        this.logger.error(`Gemini ${res.status}: ${JSON.stringify(data)}`);
        return "Désolé, une erreur est survenue lors de la communication avec l'IA.";
      }

      // 💡 Extraction ultra-sécurisée : on cherche l'élément qui contient réellement le texte
      const parts = data?.candidates?.[0]?.content?.parts;
      if (parts && Array.isArray(parts)) {
        const textPart = parts.find(p => p.text);
        if (textPart && textPart.text) {
          return textPart.text;
        }
      }

      // Si Google renvoie une structure inattendue, on log pour comprendre
      this.logger.warn(`Structure JSON inattendue reçue : ${JSON.stringify(data)}`);
      return "Désolé, je n'ai pas pu analyser la réponse de l'IA.";

    } catch (e) {
      this.logger.error(e);
      return 'Désolé, une erreur est survenue côté IA.';
    }
  }
}

import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as admin from 'firebase-admin';
import { existsSync, readFileSync } from 'fs';
import { join } from 'path';
import { PrismaService } from '../prisma/prisma.service';

const LOCAL_SERVICE_ACCOUNT_FILE =
  'ekora-chat-firebase-adminsdk-fbsvc-fa49e83616.json';

@Injectable()
export class PushService {
  private logger = new Logger(PushService.name);
  private enabled = false;

  constructor(
    private configService: ConfigService,
    private prisma: PrismaService,
  ) {
    this.init();
  }

  private init() {
    if (admin.apps.length > 0) {
      this.enabled = true;
      return;
    }

    try {
      const inlineJson = this.configService.get<string>(
        'FIREBASE_SERVICE_ACCOUNT_JSON',
      );
      const localPath = join(process.cwd(), LOCAL_SERVICE_ACCOUNT_FILE);

      let serviceAccount: admin.ServiceAccount | undefined;
      if (inlineJson) {
        serviceAccount = JSON.parse(inlineJson);
      } else if (existsSync(localPath)) {
        serviceAccount = JSON.parse(readFileSync(localPath, 'utf8'));
      }

      if (!serviceAccount) {
        this.logger.warn(
          'Aucune configuration Firebase trouvée : notifications push désactivées',
        );
        return;
      }

      admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
      this.enabled = true;
    } catch (e) {
      this.logger.error('Échec initialisation Firebase Admin', e);
    }
  }

  async notifyNewMessage(
    token: string,
    title: string,
    body: string,
    data: Record<string, string>,
  ) {
    if (!this.enabled) return;

    try {
      await admin.messaging().send({
        token,
        data: { title, body, ...data },
        android: { priority: 'high' },
      });
    } catch (e: any) {
      if (e?.code === 'messaging/registration-token-not-registered') {
        await this.prisma.user
          .updateMany({ where: { fcmToken: token }, data: { fcmToken: null } })
          .catch(() => {});
      } else {
        this.logger.warn(`Échec envoi push: ${e?.message ?? e}`);
      }
    }
  }
}

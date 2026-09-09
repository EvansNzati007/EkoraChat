import { diskStorage } from 'multer';
import { extname, join } from 'path';
import { randomUUID } from 'crypto';

const ALLOWED_MIME = /^(image\/|audio\/)|^application\/(pdf|msword|vnd\.)/;
const ALLOWED_IMAGE_MIME = /^image\//;

export function uploadInterceptorOptions(
  destSubdir: string,
  allowedMime: RegExp = ALLOWED_MIME,
) {
  return {
    storage: diskStorage({
      destination: join(process.cwd(), 'uploads', destSubdir),
      filename: (_req, file, cb) =>
        cb(null, `${randomUUID()}${extname(file.originalname)}`),
    }),
    limits: { fileSize: 20 * 1024 * 1024 },
    fileFilter: (_req, file, cb) => {
      cb(null, allowedMime.test(file.mimetype));
    },
  };
}

export function imageUploadInterceptorOptions(destSubdir: string) {
  return uploadInterceptorOptions(destSubdir, ALLOWED_IMAGE_MIME);
}

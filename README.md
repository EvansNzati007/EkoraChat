# EkoraChat 💬

Application de messagerie Android en temps réel avec agent IA conversationnel.

## Architecture

```
ekorachat/
├── backend/          # API NestJS (TypeScript)
├── android/          # App Android (Kotlin)
└── docs/             # Documentation technique
    ├── uml/            # Diagrammes UML (architecture, modèle de données, séquence)
    ├── screenshots/     # Captures d'écran de l'application
    ├── ARCHITECTURE.md  # Détail de l'architecture
    └── RAPPORT_PROJET.pdf
```

## Stack technique

### Backend
- NestJS (TypeScript)
- Prisma ORM + PostgreSQL
- Socket.IO (WebSocket, authentifié par JWT)
- API Gemini (agent IA)
- Firebase Admin SDK (notifications push)
- Multer (upload de fichiers)

### Android
- Kotlin
- Jetpack Compose (UI, Material 3)
- Socket.IO Client
- Retrofit + OkHttp (HTTP)
- Firebase Cloud Messaging (notifications push)
- Coil (chargement d'images)

### Déploiement
- Backend : [Railway](https://ekorachat-production.up.railway.app)
- BDD : PostgreSQL (managé par Railway)
- Fichiers uploadés (photos, audio) : volume persistant Railway

## Fonctionnalités
- [x] Inscription / Connexion sécurisée (JWT, bcrypt, validation stricte des champs)
- [x] Chat temps réel (WebSocket)
- [x] Envoi de médias (photos, messages vocaux, fichiers)
- [x] Accusés de réception (envoyé / lu)
- [x] Présence en ligne / dernière connexion
- [x] Notifications push (Firebase), avec ouverture directe de la conversation au tap
- [x] Agent IA conversationnel (API Gemini)
- [x] Page profil (photo, nom d'utilisateur, mot de passe)
- [ ] Chiffrement de bout en bout (non implémenté)

## Équipe
- **NZATI DOUMBI Evans** - Développeur (Génie Logiciel)

## Installation

### Backend
Créer un fichier `backend/.env` :
```
DATABASE_URL="postgresql://user:password@localhost:5432/ekorachat"
JWT_SECRET="votre_secret"
JWT_EXPIRES_IN="1d"
GEMINI_API_KEY="votre_cle_gemini"
```

```bash
cd backend
npm install
npx prisma migrate dev
npm run start:dev
```

Une base PostgreSQL locale peut être lancée avec `docker compose up -d`
(voir `backend/docker-compose.yml`).

### Android
Ouvrir le dossier `android/` avec Android Studio.

Pour que les notifications push fonctionnent, ajouter le fichier
`android/app/google-services.json` (généré depuis la console Firebase du
projet — non versionné car spécifique à chaque environnement).

## Documentation
- [Architecture détaillée](docs/ARCHITECTURE.md)
- [Rapport de projet (PDF)](docs/RAPPORT_PROJET.pdf)
- [Diagrammes UML](docs/uml/)

## Branches
- `main` — production, déployée automatiquement sur Railway

Les fonctionnalités sont développées sur des branches dédiées puis
fusionnées sur `main` via pull request (ex. `feature/websocket-media`).

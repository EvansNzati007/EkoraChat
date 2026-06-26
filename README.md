# EkoraChat 💬

Application de messagerie Android en temps réel avec agent IA conversationnel.

## Architecture
ekorachat/

├── backend/          # API NestJS (TypeScript)

├── android/          # App Android (Kotlin)

└── docs/             # Documentation technique

└── uml/          # Diagrammes UML

## Stack technique

### Backend
- NestJS (TypeScript)
- Prisma ORM + PostgreSQL
- Socket.IO (WebSockets)
- API Claude (Agent IA)
- Firebase Cloud Messaging (Notifications push)

### Android
- Kotlin
- Jetpack Compose (UI)
- Socket.IO Client
- Retrofit (HTTP)
- Room (stockage local chiffré)

### Déploiement
- Backend : Render
- BDD : PostgreSQL (Render)

## Fonctionnalités
- [x] Inscription / Connexion sécurisée
- [x] Chat temps réel (WebSocket)
- [x] Envoi de médias (photos, audio)
- [x] Accusés de réception (envoyé / lu)
- [x] Notifications push
- [x] Agent IA conversationnel
- [x] Chiffrement de bout en bout

## Équipe
- **NZATI DOUMBI Evans** - Développeur (Génie Logiciel)
- [Membre 2] - (Réseaux)
- [Membre 3] - (Réseaux)

## Installation

### Backend
```bash
cd backend
npm install
npx prisma migrate dev
npm run start:dev
```

### Android
Ouvrir le dossier `android/` avec Android Studio

## Branches
- `main` - Production
- `feature/auth` - Authentification
- `feature/chat` - Messagerie temps réel
- `feature/media` - Envoi de médias
- `feature/ai-agent` - Agent IA
- `feature/notifications` - Push notifications
- `feature/e2e-encryption` - Chiffrement

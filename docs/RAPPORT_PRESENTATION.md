# EkoraChat 💬 — Bilan de cette itération

## Fonctionnalités livrées

- [x] Chat temps réel (WebSocket)
- [x] Envoi de médias (photos, audio, fichiers)
- [x] Accusés de réception (envoyé / lu)
- [x] Présence en ligne / dernière connexion
- [x] Page profil (photo, nom d'utilisateur, mot de passe)
- [x] Agent IA conversationnel
- [x] Formulaires sécurisés (validation stricte des champs)
- [x] Notifications push (Firebase Cloud Messaging)

## Stack technique (ajouts de cette itération)

### Backend
- Socket.IO (`@nestjs/websockets`) — authentifié par JWT au handshake
- Multer — upload audio/images/fichiers, stockage sur volume persistant
- class-validator / class-transformer — validation stricte des DTOs

### Android
- socket.io-client — connexion temps réel
- MediaRecorder + sélecteur de fichiers système — audio et médias
- Coil — affichage des avatars et images
- Thème clair/sombre automatique (suit le système)
- Firebase Cloud Messaging — réception des notifications push

## Points forts pour la démo

- Message envoyé sur un téléphone → reçu instantanément sur l'autre, sans rafraîchir
- Message vocal : enregistrement, envoi, lecture avec barre de progression
- Envoi d'image ou de fichier depuis la galerie
- Coche « envoyé » → « lu » en temps réel
- Point vert « en ligne » / « vu à HH:mm »
- Page profil : changer photo, nom d'utilisateur, mot de passe
- Formulaires robustes : email invalide, champs vides, saisie énorme ou farfelue → rejetés proprement
- Notification reçue app fermée ou en arrière-plan → tap dessus ouvre directement la bonne conversation
- N'envoie une notification que si le destinataire n'a pas déjà la conversation ouverte (pas de doublon avec le temps réel)

## Architecture en un mot

Backend NestJS + PostgreSQL (Railway), app Android Kotlin/Jetpack Compose,
connexion temps réel WebSocket + REST, agent IA Gemini branché sur les
conversations dédiées.

## Installer l'app sur un nouveau téléphone (le jour J)

```bash
cd android
./gradlew installDebug
```

Avant de lancer cette commande :
1. Câble USB branché entre le PC et le téléphone
2. Débogage USB activé sur le téléphone (Paramètres → Options pour les
   développeurs → Débogage USB — si le menu développeur n'existe pas :
   Paramètres → À propos du téléphone → taper 7 fois sur "Numéro de build")
3. Autoriser la popup "Autoriser le débogage USB ?" qui apparaît sur le
   téléphone
4. Vérifier que l'appareil est bien détecté : `adb devices` doit l'afficher
   avec le statut `device` (pas `unauthorized`)

La commande compile et installe directement l'APK, pas de fichier à
transférer. Compter 1-2 minutes.

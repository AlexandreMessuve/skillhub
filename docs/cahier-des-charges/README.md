# Cahier des Charges — Projet SkillHub (Microservices)

## 1. Présentation du projet

SkillHub est une plateforme web et mobile de gestion des compétences destinée aux entreprises.
Elle permet aux utilisateurs de créer leur entreprise, d'y ajouter des RH et des collaborateurs, de déclarer et gérer leurs compétences, de consulter les formations disponibles et de s'y inscrire.
Les RH et Admin peuvent suivre les compétences de leurs équipes, valider les actions des collaborateurs et visualiser des dashboards statistiques.

## 2. Objectifs du projet

### Objectifs fonctionnels :
- Mettre en place une validation de l'email lors de l'inscription (envoi d'un lien de confirmation par email).
- Permettre l'activation d'une authentification à deux facteurs (2FA) ou la possibilité de se connecter avec une clé de sécurité pour renforcer la sécurité des comptes utilisateurs.
- Permettre à un utilisateur de créer son entreprise lors de l'inscription.
- Permettre à un Admin de gérer son entreprise : ajouter des RH et des collaborateurs.
- Centraliser les informations de compétences des collaborateurs.
- Proposer des formations adaptées.
- Suivre les évolutions des compétences dans le temps.
- Fournir un tableau de bord RH complet.
- Ajouter un système de validation par un RH ou Admin pour les compétences ajoutées ou les inscriptions aux formations des collaborateurs.

### Objectifs techniques :
- Architecture en microservices.
- API REST performante pour chaque microservice.
- Frontend moderne en ReactJS.
- Application mobile en React Native.
- Conteneurisation complète avec Docker et orchestration avec Kubernetes.
- Pipeline CI/CD avec GitHub Actions.

## 3. Utilisateurs & Rôles

| Rôle | Description | Droits principaux |
|------|-------------|-------------------|
| Collaborateur | Employé classique | Gérer son profil / compétences / formations (avec validation RH ou Admin) |
| RH | Responsable RH | Gérer utilisateurs / compétences / formations / stats / valider les demandes |
| Admin | Super Admin | Gérer l'entreprise / Ajouter RH et collaborateurs / Tous les droits (gestion globale, configuration, validation) |

## 4. Parcours Utilisateurs

### À l'inscription :
1. Création de compte utilisateur
2. Choix entre :
   - Créer une nouvelle entreprise (devient Admin de l'entreprise)
   - Rejoindre une entreprise existante via un lien d'invitation envoyé par un Admin ou RH
3. En fonction du choix, accès aux fonctionnalités associées

### Admin :
1. Connexion
2. Accès au Dashboard Admin
3. Gestion de son entreprise
4. Ajout des RH et Collaborateurs
5. Gestion des utilisateurs (CRUD)
6. Gestion des compétences (CRUD)
7. Gestion des formations (CRUD)
8. Validation des compétences ajoutées par les collaborateurs
9. Validation des inscriptions aux formations
10. Accès aux statistiques (visualisation des gaps de compétences)

### Collaborateur :
1. Connexion
2. Accès au Dashboard personnel
3. Gestion de son profil
4. Déclaration et modification de ses compétences (validation requise)
5. Consultation des formations disponibles
6. Inscription à une formation (validation requise)

### RH :
1. Connexion
2. Accès au Dashboard RH
3. Gestion des utilisateurs (CRUD)
4. Gestion des compétences (CRUD)
5. Gestion des formations (CRUD)
6. Validation des compétences ajoutées par les collaborateurs
7. Validation des inscriptions aux formations
8. Accès aux statistiques

## 5. Modules Fonctionnels

- Validation de l'adresse email lors de l'inscription (l'utilisateur doit confirmer son email avant de pouvoir se connecter).
- Authentification forte (optionnelle) via 2FA (Two Factor Authentication) ou possibilité de se connecter avec une clé de sécurité (security key).
- Authentification JWT (Spring Security)
- Gestion des entreprises (création et gestion par Admin)
- Gestion des utilisateurs (CRUD par Admin et RH)
- Gestion des rôles et permissions
- Gestion des compétences (CRUD + Validation RH/Admin)
- Gestion des formations (CRUD + Validation inscription RH/Admin)
- Dashboard statistiques
- Application mobile compagnon
- Notifications email (optionnel)
- Dark Mode / Responsive Design

## 6. Architecture en Microservices

| Microservice | Description | Technologie |
|--------------|-------------|-------------|
| Service Utilisateur | Gestion des utilisateurs, authentification et autorisation | Java 21 + Spring Boot |
| Service Entreprise | Gestion des entreprises et des rôles | Java 21 + Spring Boot |
| Service Compétences | Gestion des compétences des collaborateurs | Java 21 + Spring Boot |
| Service Formations | Gestion des formations et des inscriptions | Java 21 + Spring Boot |
| Service Notifications | Gestion des notifications par email ou autres canaux | Java 21 + Spring Boot |
| Service Statistiques | Génération des rapports et tableaux de bord | Java 21 + Spring Boot |

## 7. Contraintes Techniques

| Élément | Technologie choisie |
|---------|---------------------|
| Backend | Java 21 + Spring Boot 3.x + Maven |
| Frontend Web | ReactJS + Vite + TailwindCSS |
| Mobile | React Native (Expo) |
| BDD | MySQL, PostgreSQL, MongoDB |
| Conteneurisation | Docker + Kubernetes |
| CI/CD | GitHub Actions |
| Environnement Dev | Ubuntu 22.04 WSL2 |

## 8. Risques et Contraintes

- Synchronisation des microservices et gestion des dépendances.
- Gestion propre des rôles et permissions.
- Mise en place correcte de l'authentification JWT.
- Optimisation des performances BDD pour les dashboards.
- Gestion des validations RH/Admin efficace sans ralentir l'expérience utilisateur.
- Gestion multi-entreprises avec isolation des données.

## 9. Glossaire

- CRUD : Create / Read / Update / Delete
- JWT : Json Web Token (authentification sécurisée)
- API REST : Interface de communication web
- RH : Ressources Humaines
- MCD / MLD : Modélisation base de données

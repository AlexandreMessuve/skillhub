# **Cahier des Charges — Projet SkillHub**

## 1. Présentation du projet
SkillHub est une plateforme web et mobile de gestion des compétences destinée aux entreprises. Elle permet de centraliser les compétences des collaborateurs, de proposer des formations et de suivre l'évolution des talents au sein d'une organisation. Le modèle est multi-entreprises, chaque entité gérant ses propres données de manière isolée.

---

## 2. Objectifs du projet

### Objectifs fonctionnels :
- Mettre en place une validation de l'e-mail lors de l'inscription via un lien de confirmation.
- Proposer une authentification forte optionnelle (2FA ou clé de sécurité).
- Permettre à un utilisateur de créer sa propre entreprise et d'en devenir le dirigeant (PDG).
- Permettre à un PDG ou un RH d'inviter des collaborateurs à rejoindre une entreprise.
- Centraliser et suivre les compétences des collaborateurs avec un système de validation.
- Proposer un catalogue de formations et gérer les inscriptions (avec validation).
- Fournir des tableaux de bord statistiques pour identifier les manques et les forces.

### Objectifs techniques :
- Développer une API REST performante et sécurisée (Spring Boot).
- Créer un frontend web moderne et réactif (ReactJS).
- Développer une application mobile compagnon (React Native).
- Assurer une conteneurisation complète pour un déploiement simplifié (Docker).
- Mettre en place un pipeline d'intégration et de déploiement continus (CI/CD) avec GitHub Actions.

---

## 3. Utilisateurs & Rôles
La plateforme définit une hiérarchie de rôles claire pour une gestion des permissions précise.

| Rôle | Description | Droits & Périmètre |
| :--- | :--- | :--- |
| **User** | État initial d'un utilisateur après inscription, avant d'être rattaché à une entreprise. | Gérer son profil, créer une nouvelle entreprise (devient PDG) ou accepter une invitation. |
| **Employee** | Le collaborateur standard au sein d'une entreprise. | **Périmètre individuel :** Gérer son profil, ses compétences et ses inscriptions aux formations (soumis à validation). |
| **RH** | Le responsable des Ressources Humaines au sein d'une entreprise. | **Périmètre de l'entreprise :** Tous les droits de l'Employé, plus la gestion des employés (CRUD), du catalogue de compétences/formations et la validation des demandes. |
| **PDG** | Le dirigeant et administrateur principal d'une entreprise spécifique. | **Périmètre de l'entreprise :** Tous les droits du RH, plus la gestion des utilisateurs RH et la configuration de l'entreprise. |
| **Admin** | Le super-administrateur de la plateforme SkillHub. | **Périmètre global :** Accès et gestion de toutes les données de la plateforme, gestion des configurations globales, supervision des entreprises. |

---

## 4. Parcours Utilisateurs

### Inscription et Rattachement
1.  Un individu crée un compte et obtient le rôle **User**.
2.  Il doit valider son adresse e-mail.
3.  Depuis son profil, il peut :
    - **Option A : Créer une nouvelle entreprise.** Il devient alors le **PDG** de cette entreprise.
    - **Option B : Rejoindre une entreprise existante** en utilisant un lien d'invitation unique envoyé par un **PDG** ou un **RH**. Le rôle (`Employee` ou `RH`) est défini dans l'invitation.

### Parcours par Rôle

#### Employee :
- Consulte son tableau de bord personnel.
- Gère son profil (informations, mot de passe, 2FA).
- Déclare ses compétences (soumis à la validation d'un RH/PDG).
- Consulte le catalogue des formations de l'entreprise.
- S'inscrit à une formation (soumis à la validation d'un RH/PDG).

#### RH :
- Accède à un tableau de bord de gestion RH.
- Gère les profils des **Employees** (inviter, modifier, désactiver).
- Gère le catalogue de **compétences** et de **formations** de l'entreprise (CRUD).
- **Valide** ou refuse les nouvelles compétences et les inscriptions aux formations soumises par les Employees.
- Visualise les statistiques sur les compétences de l'entreprise.

#### PDG :
- Dispose de **tous les droits du RH**.
- Gère les profils des utilisateurs **RH** (inviter, modifier, révoquer).
- Configure les paramètres de son entreprise.

#### Admin (Super Admin) :
- Accède à un "back-office" global de la plateforme.
- Supervise l'ensemble des entreprises et des utilisateurs.
- Gère la configuration globale de l'application.
- Peut intervenir pour du support ou de la maintenance.

---

## 5. Modules Fonctionnels Clés
- **Validation d'e-mail & Sécurité :** Processus de confirmation et options 2FA / clé de sécurité.
- **Authentification JWT :** Système de token pour sécuriser l'API.
- **Gestion Multi-Entreprises :** Isolation stricte des données entre les différentes entreprises.
- **Gestion des Utilisateurs & Invitations :** CRUD complet avec un système d'invitation par lien.
- **Gestion des Rôles et Permissions :** Mécanisme robuste pour appliquer les droits décrits ci-dessus.
- **Gestion des Compétences :** Catalogue de compétences et système de validation.
- **Gestion des Formations :** Catalogue de formations et système de validation des inscriptions.
- **Dashboard & Statistiques :** Visualisation des données pour les RH et PDG.
- **Application Mobile :** Version allégée pour les collaborateurs.

---

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
| :--- | :--- |
| **Backend** | Java 21 + Spring Boot 3.4.4 + Maven |
| **Frontend Web** | ReactJS + Vite + TailwindCSS |
| **Mobile** | React Native (Expo) |
| **Base de Données** | MySQL |
| **Conteneurisation** | Docker + Docker Compose |
| **CI/CD** | GitHub Actions |
| **Environnement Dev** | Ubuntu 22.04 WSL2 |

---

## 8. Risques et Contraintes
- **Complexité des Permissions :** La gestion des différents niveaux de rôles (notamment la distinction PDG/Admin) doit être rigoureuse.
- **Isolation des Données :** La séparation des données entre les entreprises (multi-tenancy) doit être parfaite pour éviter toute fuite.
- **Synchronisation API/Frontend :** Maintenir une communication fluide et cohérente.
- **Performance des Dashboards :** Optimiser les requêtes pour l'agrégation de données statistiques.
- **Expérience Utilisateur :** Le processus de validation par les RH/PDG ne doit pas créer de friction excessive pour les employés.

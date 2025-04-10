# Cahier des Charges — Projet SkillHub

## 1. Présentation du projet

SkillHub est une plateforme web et mobile de gestion des compétences destinée aux entreprises.  
Elle permet aux collaborateurs de déclarer et gérer leurs compétences, de consulter les formations disponibles et de s'y inscrire.  
Les RH et Admin peuvent suivre les compétences de leurs équipes, identifier les besoins en formation, et visualiser des dashboards statistiques.

---

## 2. Objectifs du projet

### Objectifs fonctionnels :
- Centraliser les informations de compétences des collaborateurs.
- Proposer des formations adaptées.
- Suivre les évolutions des compétences dans le temps.
- Fournir un tableau de bord RH complet.

### Objectifs techniques :
- API REST performante (Spring Boot).
- Frontend moderne en ReactJS.
- Application mobile en React Native.
- Conteneurisation complète avec Docker.
- Pipeline CI/CD avec GitHub Actions.

---

## 3. Utilisateurs & Rôles

| Rôle | Description | Droits principaux |
|------|-------------|-------------------|
| Collaborateur | Employé classique | Gérer son profil / compétences / formations |
| RH | Responsable RH | Gérer utilisateurs / compétences / formations / stats |
| Admin | Super Admin | Tous les droits (gestion globale, configuration) |

---

## 4. Parcours Utilisateurs

### Collaborateur :
1. Connexion
2. Accès au Dashboard personnel
3. Gestion de son profil
4. Déclaration et modification de ses compétences
5. Consultation des formations disponibles
6. Inscription à une formation

### RH / Admin :
1. Connexion
2. Accès au Dashboard RH/Admin
3. Gestion des utilisateurs (CRUD)
4. Gestion des compétences (CRUD)
5. Gestion des formations (CRUD)
6. Accès aux statistiques (visualisation des gaps de compétences)

---

## 5. Modules Fonctionnels

- Authentification JWT (Spring Security)
- Gestion des utilisateurs (CRUD)
- Gestion des rôles et permissions
- Gestion des compétences (CRUD)
- Gestion des formations (CRUD)
- Dashboard statistiques
- Application mobile compagnon
- Notifications email (optionnel)
- Dark Mode / Responsive Design

---

## 6. Contraintes Techniques

| Elément | Technologie choisie |
|---------|---------------------|
| Backend | Java 21 + Spring Boot 3.x + Maven |
| Frontend Web | ReactJS + Vite + TailwindCSS |
| Mobile | React Native (Expo) |
| BDD | MySQL |
| Conteneurisation | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Environnement Dev | Ubuntu 22.04 WSL2 |

---


## 7. Risques et Contraintes

- Synchronisation des frontends avec l'API
- Gestion propre des rôles et permissions
- Mise en place correcte de l'authentification JWT
- Optimisation des performances BDD pour les dashboards

---

## 8. Glossaire

- CRUD : Create / Read / Update / Delete
- JWT : Json Web Token (authentification sécurisée)
- API REST : Interface de communication web
- RH : Ressources Humaines
- MCD / MLD : Modélisation base de données

---

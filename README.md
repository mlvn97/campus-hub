# Campus Hub
Une école souhaite centraliser la vie étudiante sur une plateforme unique. Aujourd'hui les évènements sont annoncés par email, les actualités dispersées sur plusieurs canaux, et les étudiants n'ont aucune visibilité sur ce qui se passe sur le campus. CampusHub regroupe tout ça en un seul endroit.

<br>

# Architecture
Dans ce projet, nous adoptons une architecture <b>microservices</b>. <br>
Le backend est réparti en quatre API distinctes qui contiennent un dossier models pour les entités comme <i>utilisateur</i> ou <i>association</i>,
repository pour les requêtes en base de données, service pour la logique métier, controller pour les requêtes HTTP, dto pour le transfert de données propre
et exception pour gérer les erreurs et exceptions. 
<br>
Les API sont reliées à une seule et même base de données.
<br>
Chaque API tourne sur son propre port:
- User API port 8080
- Association API port 8081
- Event API port 8082
- News API port 8083

Le frontend est développé avec le framework Angular 22.

# Fonctionnalités

## User API
Le socle commun de la plateforme.
Elle permet:
- l'inscription et la connexion
- la consultation et la modification de son profil
- la recherche d'utilisateurs (paginée et filtrable par rôle)

## Association API
Gestion de la vie des associations étudiantes, leurs membres et les demandes d'adhésion.
Elle permet:
- CRUD complet sur les associations
- la gestion des membres (ajouter, retirer, changer le rôle), réservée aux rôles `ADMIN_ASSOCIATION` et `ADMIN_CAMPUS`
- la soumission d'une demande d'adhésion, dont l'acceptation ou le refus est réservé au président de l'association
- le départ du président, nécessitant la désignation d'un successeur membre de l'association
- une seule demande d'adhésion en attente par étudiant et par association
- un utilisateur ne peut être président que d'une seule association à la fois
- accéder à la liste des associations (paginée et filtrable par catégorie et statut)
- accéder à la liste des membres d'une association

## Event API
Gestion des évènements organise*és par les associations, les inscriptions et la liste d'attente automatique.
Elle permet:
- CRUD complet sur les évènements
- l'inscription à un évènement (bascule en liste d'attente si complet)
- la désinscription d'un évènement
- la consultation des inscrits et la liste d'attente d'un évènement
- accéder à la liste des évènements (paginés, filtrables par statut, par association, par date)
- l'annulation d'un évènement

## News API
Gestion du fil d'actualités publié par les associations et l'administration.
Elle permet:
- CRUD complet sur les actualités
- publication et archivage d'une actualité
- fil d'actualités (paginé et filtrable par catégorie et par association)

## Frontend
Le frontend permet:
- l'inscription et connexion
- la consultation et la modification de son profil
- la consultation de l'annuaire des associations avec détail et membres
- la soumission d'une demande d'adhésion à une association
- la consultation du calendrier ou de la liste des évènements avec filtre par statut et association
- l'inscription et la désincription d'un évènement
- l'affichage du fil d'actualités avec filtre par catégorie

<br>

# Lancement en local

1. Installer Docker et Docker Compose sur sa machine.
2. Se créer un token JWT:
```bash
openssl rand -base64 32
```
3. Configurer un fichier .env dans chaque service API (voir .env.example).
4. Se placer à la racine du projet puis exécuter:
```bash
docker compose -f compose.dev.yaml up -d --build
```
5. Aller sur http://localhost:4200 pour accéder à l'application.

# Déploiement et lancement sur AWS EC2

## 1. Prérequis
- Une instance AWS EC2 sous Amazon Linux 2023.
- Docker et Docker Compose installés sur la machine.
- Un runner Github Actions configuré sur l'instance EC2 (Voir étapes sur github.com)
- Les images Docker du projet publiées sur GitHub Container Registry (GHCR).
- Les ports nécessaires autorisés dans le Security Group AWS.

## 2. Préparer le serveur (première installation uniquement)
Créer le répertoire de déploiement:
```bash
sudo mkdir -p /opt/campus-hub
sudo chown ec2-user:ec2-user /opt/campus-hub
```

Créer le fichier .env dans /opt/campus-hub :
```bash
cd /opt/campus-hub
nano .env
```

Configurer les variables d'environnement nécessaires:
```.env
DB_URL=jdbc:postgresql://adresse_rds_postgres:port/nom_de_la_base
DB_USERNAME=postgres
DB_PASSWORD=mot_de_passe
JWT_SECRET=token_jwt
```
Ne pas publier ce fichier sur Github.

## 3. Déploiement
Le déploiement est automatisé par Github Actions.

Lorsqu'un changement est poussé sur la branche `main`:
1. GitHub Actions exécute les tests.
2. Les images Docker des différentes applications sont construites.
3. Les images sont publiées sur GHCR.
4. Le runner installé sur EC2 récupère les nouvelles images.
5. Docker Compose recrée les conteneurs avec les nouvelles versions.

Le workflow de déploiement exécute notamment:
```bash
cd /opt/campus-hub
IMAGE_TAG=<COMMIT_SHA> docker compose pull
IMAGE_TAG=<COMMIT_SHA> docker compose up -d --remove-orphans
```

## 4. Vérifier le lancement
Afficher l'état des conteneurs:
```bash
cd /opt/campus-hub
docker ps
```

Afficher les logs:
```bash
cd /opt/campus-hub
docker logs nom_de_l_image
```

## 5. Accéder à l'application
L'application est accessible depuis l'adresse publique de l'instance EC2 via HTTP.

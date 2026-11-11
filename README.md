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
- la gestion des membres (ajouter, retirer, changer le rôle)
- la soumission, l'acceptation ou le refus d'une demande d'adhésion
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

# Consignes de lancement

## Local
1. Installer Java 25 et Maven sur sa machine.
2. Installer postgresql sur sa machine.
3. Configurer un fichier .env dans chaque service API (voir .env.example) pour se connecter à la base de données de développement.
4. Dans chaque service API, exécuter:

```bash
./mvnw spring-boot:run
```

## Machine distante EC2
Compiler chaque API avec Maven:

```bash
cd "api_cible"
./mvnw clean install
```

Puis copier le .jar `nom-api-0.0.1-SNAPSHOT.jar` (généralement) de chaque service API vers la machine distante:
```bash
scp -i "ma_cle_api.pem" "chemin_du_jar" "ec2-user@adresse:~/nom-api.jar"
```

Ensuite, depuis la machine distante, configurer le script de lancement `start.sh` en modifiant les variables d'environnement DB_URL, DB_USER et DB_PASSWORD:
```bash
export DB_URL=""
export DB_USERNAME=""
export DB_PASSWORD=""
```

Enfin, toujours depuis la machine distante, se placer à la racine (dossier ~/) et exécuter:
```bash
./start.sh &
```

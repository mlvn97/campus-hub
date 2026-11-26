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

# Fonctionnalités disponibles

Les sections API décrivent les opérations exposées par les services. La section frontend se limite aux parcours raccordés à ces API. Toutes les routes métier nécessitent un jeton JWT, sauf l'inscription et la connexion.

## User API
- Créer un compte étudiant et se connecter pour obtenir un jeton JWT.
- Consulter un utilisateur par identifiant.
- Consulter son profil ou un profil par identifiant, et modifier son propre profil (bio, genre et photo).
- Un administrateur du campus peut modifier un profil par identifiant.

## Association API
- Lister les associations avec pagination et filtres par catégorie et statut, ou consulter une association par identifiant.
- Créer une association (`ADMIN_ASSOCIATION`, `ADMIN_CAMPUS`), modifier ou supprimer une association (`ADMIN_ASSOCIATION`), et suspendre ou réactiver une association (les deux rôles administratifs).
- Consulter les membres d'une association et sa propre adhésion.
- Soumettre une demande d'adhésion. Les administrateurs ne peuvent pas déposer de demande; une même personne ne peut avoir qu'une demande en attente par association ni demander à rejoindre une association dont elle est déjà membre.
- Les présidents et les administrateurs peuvent consulter, accepter ou refuser les demandes. L'acceptation ajoute la personne comme membre.
- Quitter une association. Un président doit désigner comme successeur un autre membre qui n'est pas déjà président.
- Les rôles administratifs peuvent retirer un membre et modifier son rôle. Un utilisateur ne peut pas présider plusieurs associations.

## Event API
- Lister les événements avec pagination et filtres par statut, association et date, ou consulter un événement par identifiant.
- Créer un événement en brouillon. Seul un brouillon peut être modifié; le président de l'association peut changer son statut, notamment le publier ou l'annuler.
- S'inscrire à un événement et consulter sa propre inscription. Le président de l'association organisatrice ne peut pas s'inscrire à son événement.
- Quand la capacité est atteinte, les nouvelles inscriptions passent en attente. L'annulation d'une inscription confirmée fait passer en confirmé la première inscription en attente.
- Annuler sa propre inscription.

L'API n'expose pas d'opération pour supprimer un événement ni pour consulter la liste complète de ses inscrits.

## News API
- Consulter le fil des actualités publiées, paginé et filtrable par catégorie et association, ou consulter une actualité par identifiant.
- Consulter un fil de gestion des actualités accessibles à l'utilisateur.
- Créer une actualité en brouillon; modifier un brouillon ou une actualité archivée.
- Publier un brouillon, republier une actualité archivée, archiver une actualité publiée et supprimer une actualité gérée par l'utilisateur.
- Un président peut créer des actualités pour son association et gérer celles qu'il a rédigées. L'administrateur du campus peut gérer toutes les actualités.

## Frontend
- Créer un compte, se connecter, consulter et modifier son profil.
- Parcourir les actualités, événements et associations après connexion; les pages de détail utilisent les identifiants fournis par les API.
- Filtrer les actualités par catégorie et association, les événements par statut et association, et les associations par catégorie et statut.
- Depuis une association, envoyer une demande d'adhésion ou quitter l'association.
- Depuis un événement, consulter son inscription, s'inscrire ou se désinscrire.
- Selon ses droits, créer, suspendre ou réactiver une association; consulter les membres, en retirer ou modifier leur rôle; et traiter les demandes d'adhésion.
- Selon ses droits, créer et gérer les événements. Pour les actualités, créer un brouillon, modifier ou supprimer une actualité gérée, publier, archiver et republier une actualité archivée.

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

## Documentation Swagger

Après le démarrage des services locaux avec Docker Compose, chaque API expose son interface Swagger et son contrat OpenAPI.

| API | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| Comptes et profils | [Swagger](http://localhost:8080/swagger-ui.html) | [OpenAPI](http://localhost:8080/v3/api-docs) |
| Associations | [Swagger](http://localhost:8081/swagger-ui.html) | [OpenAPI](http://localhost:8081/v3/api-docs) |
| Événements | [Swagger](http://localhost:8082/swagger-ui.html) | [OpenAPI](http://localhost:8082/v3/api-docs) |
| Actualités | [Swagger](http://localhost:8083/swagger-ui.html) | [OpenAPI](http://localhost:8083/v3/api-docs) |

1. Dans Swagger Comptes et profils, exécute `POST /api/users/login` avec l’adresse e-mail et le mot de passe d’un compte local. Tu peux créer un compte avec `POST /api/users/register`.
2. Copie la valeur de `token` renvoyée par la connexion.
3. Dans le Swagger de l’API à tester, sélectionne **Authorize** et colle le jeton seul. Swagger ajoute le préfixe `Bearer`.
4. Ouvre une opération et utilise **Try it out**. Les opérations d’écriture restent soumises aux permissions du compte.

Les listes Associations et Événements utilisent les paramètres Spring `page` (à partir de 0), `size` et `sort`. Le fil `GET /api/news` utilise `page` (à partir de 1), `limit` et les filtres facultatifs `categorie` et `association`.

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
`JWT_SECRET` doit être une clé encodée en Base64 qui représente au moins 32 octets.
Pour la production, générez une clé unique avec `openssl rand -base64 32`.
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

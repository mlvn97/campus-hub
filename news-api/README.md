# API d’actualités

Le service démarre sur le port **8083** et partage la base PostgreSQL de Campus Hub. Configure les variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` et `JWT_SECRET` à partir de `.env.example`. Renseigne le même secret JWT encodé en Base64 que dans `user-api`. Les variables doivent être chargées dans le terminal avant de démarrer le service.

Depuis `news-api`, lance `./mvnw spring-boot:run` ou `.\mvnw.cmd spring-boot:run` sous Windows. Hibernate crée la table `news` au démarrage avec la configuration actuelle. Pour les opérations liées aux associations, news-api transmet le JWT à association-api. Configure ASSOCIATION_API_URL avec http://localhost:8081 hors Docker. Sous Docker, la valeur par défaut est http://association-api:8081.

Ouvre Swagger à l’adresse [localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html). La spécification OpenAPI est disponible à [localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs). Utilise **Authorize** avec un JWT délivré par `user-api` pour exécuter les requêtes.

| Méthode | Route | Résultat |
| --- | --- | --- |
| GET | `/api/news` | Fil des actualités publiées |
| GET | /api/news/gestion | Fil de gestion des actualités autorisées |
| GET | `/api/news/{id}` | Détail d’une actualité |
| POST | `/api/news` | Création d’un brouillon, avec son URL dans `Location` |
| PATCH | `/api/news/{id}` | Modification du titre, du contenu ou de la catégorie |
| PATCH | `/api/news/{id}/publier` | Publication d’un brouillon |
| PATCH | `/api/news/{id}/archiver` | Archivage d’une actualité publiée |
| DELETE | `/api/news/{id}` | Suppression, avec une réponse `204` |

Pour créer une actualité liée à une association, envoie le corps JSON suivant.

```json
{
  "title": "Forum des associations",
  "content": "Rendez-vous jeudi à 14 h dans le hall.",
  "category": "Vie étudiante",
  "associationId": 12
}
```

Un `ADMIN_CAMPUS` peut gérer toutes les actualités et omettre `associationId` pour publier au nom du campus. Un `ADMIN_ASSOCIATION` doit être président de l’association concernée. L’association est fixée à la création. Tout utilisateur authentifié peut lire les actualités publiées. Le détail d’un brouillon ou d’une actualité archivée est réservé aux personnes qui peuvent la gérer, avec une réponse `404` pour les autres lecteurs. Une écriture interdite renvoie `403` et une transition invalide renvoie `409`.

Le fil accepte `page` (à partir de 1, défaut 1), `limit` (de 1 à 100, défaut 20), `categorie` (correspondance exacte) et `association` (identifiant). Les filtres se combinent. Le tri place les publications les plus récentes en premier. La réponse contient `items`, `page`, `limit`, `totalItems` et `totalPages`.

## Tests d’intégration

Utilise une base PostgreSQL **vide et jetable**. Les tests recréent la table `news` et simulent les réponses d’association-api sur un serveur HTTP local. Configure `NEWS_TEST_DB_URL` (par exemple `jdbc:postgresql://localhost:5432/campus_news_test`), `NEWS_TEST_DB_USERNAME` et, si nécessaire, `NEWS_TEST_DB_PASSWORD`. Lance ensuite `./mvnw test` ou `.\mvnw.cmd test` sous Windows. Sans `NEWS_TEST_DB_URL`, Maven compile le projet et ignore la suite d’intégration.

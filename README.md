# KPI Analysis Platform - 🚧 En Cours de Développement

> **Note** : Ce projet est actuellement en cours de développement. Certaines fonctionnalités peuvent être incomplètes ou sujettes à des modifications.

## 📋 Description du Projet

Plateforme d'analyse intelligente des KPIs collaborateurs avec module d'intelligence artificielle intégré, capable d'analyser automatiquement les indicateurs de performance des collaborateurs (assiduité, vélocité, qualité, productivité, efficacité) et de fournir des interprétations et recommandations synthétiques pour soutenir la prise de décision managériale.

## 🛠️ Stack Technologique

- **Backend** : Spring Boot 3.5.7
- **Frontend** : Angular 19.2.0
- **Base de données** : PostgreSQL
- **IA** : Analyse basée sur des règles (à améliorer avec ML/AI avancé)

## 📁 Structure du Projet

```
KPI-Insight/
├── kpi-analysis-Backend/          # Application Spring Boot
│   └── kpi-analysis-Backend/
│       ├── src/main/java/
│       │   └── com/entreprise/kpi_analysis_Backend/
│       │       ├── controller/    # REST Controllers
│       │       ├── service/        # Business Logic
│       │       ├── repository/    # Data Access Layer
│       │       ├── entity/        # JPA Entities
│       │       ├── dto/           # Data Transfer Objects
│       │       ├── config/        # Configuration Classes
│       │       └── exception/     # Exception Handlers
│       └── src/main/resources/
│           └── application*.properties
│
└── kpi-analysis-frontend/         # Application Angular
    └── src/app/
        ├── components/             # Composants de pages
        ├── services/               # Services HTTP
        ├── models/                 # Interfaces TypeScript
        └── app.component.ts       # Composant principal
```

## ✅ Backend - Fonctionnalités Implémentées

### Architecture et Structure
- ✅ Architecture en couches (Controller → Service → Repository → Entity)
- ✅ Séparation des responsabilités selon les best practices Spring Boot
- ✅ Utilisation de DTOs pour l'API (jamais les entités directement)
- ✅ Gestion centralisée des exceptions avec `@RestControllerAdvice`

### Entités et Modèles
- ✅ **Employee** : Gestion des employés (nom, prénom, email, département, poste)
- ✅ **KPI** : Indicateurs de performance avec périodes et scores
- ✅ **KPIMetric** : Métriques détaillées (assiduité, vélocité, qualité, productivité, efficacité)
- ✅ Relations JPA correctement configurées (OneToMany, ManyToOne)

### API REST
- ✅ **EmployeeController** : CRUD complet pour les employés
  - `GET /api/employees` - Liste tous les employés
  - `GET /api/employees/{id}` - Détails d'un employé
  - `POST /api/employees` - Créer un employé
  - `PUT /api/employees/{id}` - Modifier un employé
  - `DELETE /api/employees/{id}` - Supprimer un employé

- ✅ **KPIController** : CRUD complet pour les KPIs
  - `GET /api/kpis` - Liste tous les KPIs
  - `GET /api/kpis/{id}` - Détails d'un KPI
  - `GET /api/kpis/employee/{employeeId}` - KPIs d'un employé
  - `POST /api/kpis` - Créer un KPI
  - `PUT /api/kpis/{id}` - Modifier un KPI
  - `DELETE /api/kpis/{id}` - Supprimer un KPI

- ✅ **KPIMetricController** : CRUD complet pour les métriques
  - `GET /api/kpi-metrics` - Liste toutes les métriques
  - `GET /api/kpi-metrics/{id}` - Détails d'une métrique
  - `GET /api/kpi-metrics/kpi/{kpiId}` - Métriques d'un KPI
  - `POST /api/kpi-metrics` - Créer une métrique
  - `PUT /api/kpi-metrics/{id}` - Modifier une métrique
  - `DELETE /api/kpi-metrics/{id}` - Supprimer une métrique

- ✅ **AnalysisController** : Analyse IA
  - `GET /api/analysis/employee/{employeeId}` - Analyser la performance d'un employé
  - `GET /api/analysis/employee/{employeeId}/recommendations` - Obtenir des recommandations
  - `POST /api/analysis/kpi/{kpiId}/analyze` - Déclencher l'analyse d'un KPI

### Best Practices Implémentées
- ✅ **Validation** : `@Valid` et `@Validated` sur tous les endpoints
- ✅ **Validation des path variables** : `@Min(1)` pour les IDs
- ✅ **Gestion d'erreurs** : `ErrorResponse` DTO avec structure cohérente
- ✅ **Exception Handler Global** : Gestion de toutes les exceptions
  - `ResourceNotFoundException`
  - `MethodArgumentNotValidException` (erreurs de validation)
  - `MethodArgumentTypeMismatchException`
  - `IllegalArgumentException`
  - Exceptions génériques
- ✅ **CORS Configuration** : Configuration centralisée dans `CorsConfig`
- ✅ **Transactions** : `@Transactional(readOnly = true)` pour les opérations de lecture
- ✅ **Logging** : SLF4J avec niveaux appropriés (debug, info, warn, error)
- ✅ **Documentation** : JavaDoc sur les classes principales
- ✅ **Codes HTTP appropriés** : 200, 201, 204, 400, 404, 500

### Configuration
- ✅ **Profils Spring** : `prod` (PostgreSQL) et `test` (PostgreSQL pour tests)
- ✅ **Base de données** : PostgreSQL configuré pour dev et test
- ✅ **Port** : 8082 (configurable)
- ✅ **CORS** : Configuré pour `localhost:4200` et `localhost:3000`

## ✅ Frontend - Fonctionnalités Implémentées

### Architecture et Structure
- ✅ Architecture modulaire avec composants standalone
- ✅ Services HTTP pour la communication avec le backend
- ✅ Models TypeScript pour le typage fort
- ✅ Navigation avec `NavigationExtras` et `skipLocationChange`

### Composants
- ✅ **EmployeeListComponent** : Page principale avec liste des employés
  - Affichage de tous les employés
  - Actions : Voir détails, Modifier, Supprimer
  - Bouton pour ajouter un nouvel employé
  - Rafraîchissement des données

- ✅ **EmployeeDetailComponent** : Page de détails d'un employé
  - Informations complètes de l'employé
  - Liste des KPIs avec scores
  - Métriques détaillées pour chaque KPI
  - Analyse IA affichée si disponible
  - Bouton pour analyser avec IA
  - Navigation vers formulaire d'édition

- ✅ **EmployeeFormComponent** : Formulaire d'ajout/modification
  - Formulaire réactif avec validation
  - Mode création et édition
  - Validation en temps réel
  - Messages d'erreur clairs

### Navigation
- ✅ **Navigation sans changement d'URL** : Utilisation de `skipLocationChange: true`
- ✅ **État centralisé** : Gestion de l'état dans `AppComponent`
- ✅ **Communication entre composants** : Via `@Input` et `@Output`
- ✅ **Transitions fluides** : Changement de contenu sans rechargement

### Services
- ✅ **EmployeeService** : CRUD complet pour les employés
- ✅ **KPIService** : CRUD complet pour les KPIs
- ✅ **KPIMetricService** : CRUD complet pour les métriques
- ✅ **AnalysisService** : Appels API pour l'analyse IA

### UI/UX
- ✅ **Design moderne** : Gradients, animations, ombres
- ✅ **Responsive** : Adapté aux différentes tailles d'écran
- ✅ **Icônes améliorées** : Bouton de suppression plus visible
- ✅ **Feedback visuel** : Loading states, error messages
- ✅ **Animations** : Transitions fluides entre les vues
- ✅ **Couleurs conditionnelles** : Scores et pourcentages avec codes couleur

### Intégration Backend
- ✅ **Configuration API** : URL configurée dans `environment.ts`
- ✅ **HttpClient** : Configuré avec interceptors
- ✅ **Gestion d'erreurs** : Affichage des erreurs API
- ✅ **Types TypeScript** : Interfaces alignées avec les DTOs backend

## 🚀 Démarrage du Projet

### Prérequis
- Java 17+
- Node.js 18+
- PostgreSQL 12+
- Maven 3.6+

### Backend

1. **Configurer PostgreSQL**
   ```sql
   CREATE DATABASE kpi_analysis;
   ```

2. **Configurer les credentials** dans `application-prod.properties`
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5433/kpi_analysis
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

3. **Lancer l'application**
   ```bash
   cd kpi-analysis-Backend/kpi-analysis-Backend
   mvn spring-boot:run
   ```
   L'API sera disponible sur `http://localhost:8082`

### Frontend

1. **Installer les dépendances**
   ```bash
   cd kpi-analysis-frontend
   npm install
   ```

2. **Lancer l'application**
   ```bash
   npm start
   ```
   L'application sera disponible sur `http://localhost:4200`

## 📊 Fonctionnalités IA (Actuellement Basiques)

### Analyse de Performance
- Calcul des scores moyens par type de métrique
- Classification des performances (Excellent, Bon, Moyen, Faible)
- Identification des points à améliorer
- Score global de performance

### Recommandations
- Recommandations basées sur les métriques sous-performantes
- Suggestions spécifiques par type de métrique
- Messages personnalisés selon le niveau de performance

**Note** : L'analyse IA actuelle est basée sur des règles. Une amélioration future pourrait intégrer du Machine Learning pour des analyses plus avancées.

## 🔄 Workflow de Navigation

1. **Page Liste** (`/`) → Affiche tous les employés
2. **Clic sur "Voir détails"** → Affiche les détails sans changer l'URL
3. **Clic sur "Modifier"** → Affiche le formulaire sans changer l'URL
4. **Clic sur "Ajouter"** → Affiche le formulaire vide sans changer l'URL
5. **Bouton "Retour"** → Retourne à la liste sans changer l'URL

L'URL reste toujours `localhost:4200` grâce à `skipLocationChange: true`.

## 🎨 Améliorations UI/UX

- ✅ Design moderne avec gradients et animations
- ✅ Icône de suppression améliorée (fond rouge, bordure, ombre)
- ✅ Transitions fluides entre les pages
- ✅ Feedback visuel pour toutes les actions
- ✅ Responsive design pour mobile et desktop
- ✅ Codes couleur pour les scores (vert, bleu, orange, rouge)

## 📝 Prochaines Étapes (À Faire)

### Backend
- [ ] Implémenter l'authentification et l'autorisation (JWT)
- [ ] Ajouter la pagination pour les listes
- [ ] Implémenter le cache avec Spring Cache
- [ ] Ajouter des tests unitaires et d'intégration
- [ ] Documenter l'API avec Swagger/OpenAPI
- [ ] Améliorer l'analyse IA avec du Machine Learning
- [ ] Ajouter la gestion des fichiers (upload de documents)

### Frontend
- [ ] Créer les composants pour gérer les KPIs (formulaire KPI)
- [ ] Créer les composants pour gérer les métriques (formulaire métrique)
- [ ] Implémenter l'authentification côté frontend
- [ ] Ajouter des graphiques pour visualiser les KPIs
- [ ] Implémenter le lazy loading pour les grandes listes
- [ ] Ajouter des filtres et recherche
- [ ] Améliorer l'analyse IA avec visualisations

## 🐛 Problèmes Connus

- L'analyse IA est basique (basée sur des règles)
- Pas de pagination pour les grandes listes
- Pas d'authentification implémentée
- Les formulaires KPI et métrique ne sont pas encore dans des pages séparées

## 📚 Technologies Utilisées

### Backend
- Spring Boot 3.5.7
- Spring Data JPA
- Spring Security (configuration basique)
- PostgreSQL Driver
- Lombok
- SLF4J (Logging)

### Frontend
- Angular 19.2.0
- TypeScript 5.7.2
- RxJS 7.8.0
- Angular Reactive Forms
- Angular Router

## 👥 Auteur

**Chaima Fouzri**

Ce projet a été développé dans le cadre d'une pratique personnelle et d'auto-apprentissage pour améliorer mes compétences en développement full-stack avec Spring Boot et Angular.

## 📄 Licence

Ce projet est en cours de développement.

---

**Dernière mise à jour** : Novembre 2024

**Statut** : 🚧 En cours de développement


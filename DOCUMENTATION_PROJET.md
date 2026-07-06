# Documentation du Projet : Application de Gestion des Prescriptions et Commissions

Ce document contient l'architecture complète, la structure des données et les spécifications de l'interface utilisateur pour recréer cette application mobile Android (Kotlin + Jetpack Compose) depuis zéro. Vous pouvez fournir ce document à un agent IA pour qu'il recrée l'application.

## 1. Vue d'ensemble du projet
L'application permet :
- Aux **Médecins** de créer des prescriptions médicales pour leurs patients, de suivre les commissions générées par ces prescriptions, et de visualiser leurs paiements reçus via un portefeuille (Wallet) et un tableau de bord.
- Aux **Administrateurs** de gérer les médecins, de valider les prescriptions (action qui génère les commissions pour les médecins), et d'enregistrer les paiements versés.

## 2. Stack Technologique
- **Plateforme** : Android natif
- **Langage** : Kotlin
- **Interface Utilisateur** : Jetpack Compose (Material Design 3)
- **Architecture** : Clean Architecture simplifiée (MVVM - Model-View-ViewModel)
- **Base de données locale** : Room Database (SQLite)
- **Asynchronisme & Flux** : Kotlin Coroutines, StateFlow / Flow

## 3. Schéma de Base de Données (Room Entities)

### `DoctorEntity` (Table: doctors)
- `id` (String, PrimaryKey) : UUID
- `name` (String) : Nom complet du médecin
- `specialization` (String) : Spécialité (ex: Généraliste, Pédiatre)
- `phone` (String) : Numéro de téléphone
- `email` (String) : Adresse email
- `createdAt` (Long) : Timestamp

### `PrescriptionEntity` (Table: prescriptions)
- `id` (String, PrimaryKey) : UUID
- `doctorId` (String, ForeignKey) : Référence au médecin
- `patientName` (String) : Nom du patient
- `patientAge` (Int) : Âge du patient
- `prescriptionDate` (String) : Format "YYYY-MM-DD"
- `notes` (String) : Notes complémentaires
- `diagnosis` (String) : Diagnostic médical
- `status` (String) : Statut de la prescription (`PENDING`, `VALIDATED`, `REJECTED`)

### `MedicationEntity` (Table: medications)
- `id` (String, PrimaryKey) : UUID
- `prescriptionId` (String, ForeignKey) : Référence à la prescription
- `name` (String) : Nom du médicament
- `dosage` (String) : Dosage (ex: "500mg")
- `frequency` (String) : Fréquence (ex: "3 fois par jour")
- `duration` (String) : Durée (ex: "7 jours")
- `quantity` (Int) : Quantité prescrite

### `CommissionEntity` (Table: commissions)
- `id` (String, PrimaryKey) : UUID
- `prescriptionId` (String?, ForeignKey nullable) : Prescription ayant généré la commission
- `doctorId` (String, ForeignKey) : Référence au médecin
- `commissionAmount` (Double) : Montant de la commission
- `status` (String) : Statut de la commission (`PENDING`, `PAID`, `CANCELLED`)
- `createdAt` (Long) : Timestamp

### `WalletEntity` (Table: wallets)
- `id` (String, PrimaryKey) : UUID
- `doctorId` (String, ForeignKey) : Référence au médecin
- `balance` (Double) : Solde actuel disponible (`totalEarned` - `totalWithdrawn`)
- `totalEarned` (Double) : Total des commissions cumulées
- `totalWithdrawn` (Double) : Total des paiements déjà versés
- `updatedAt` (Long) : Timestamp

### `PaymentEntity` (Table: payments)
- `id` (String, PrimaryKey) : UUID
- `doctorId` (String, ForeignKey) : Référence au médecin
- `amount` (Double) : Montant payé par l'administrateur
- `paymentDate` (String) : Date du paiement ("YYYY-MM-DD")
- `reference` (String) : Numéro ou référence de la transaction
- `status` (String) : Statut (`COMPLETED`, `FAILED`)
- `createdAt` (Long) : Timestamp

## 4. Architecture et Logique Métier (ViewModel & Repository)

- **Relations Room (`@Relation`)** : Créer des classes de données "Full" comme `FullPrescription` qui intègre la `PrescriptionEntity` ainsi que sa liste de `MedicationEntity`. De même pour `FullDoctor` (Doctor + Wallet + Commissions + Payments).
- **Logique de Validation de Prescription** : Lorsqu'un admin passe une prescription en `VALIDATED`, le ViewModel doit automatiquement calculer une commission et insérer une `CommissionEntity` (liée au médecin), puis mettre à jour le `totalEarned` et le `balance` dans la `WalletEntity` du médecin.
- **Logique de Paiement** : Lorsqu'un administrateur effectue un paiement (insertion d'une `PaymentEntity`), le ViewModel doit déduire le montant du `balance` de la `WalletEntity` du médecin, incrémenter son `totalWithdrawn`, et passer les statuts des `CommissionEntity` concernées de `PENDING` à `PAID`.

## 5. Interface Utilisateur (Écrans Jetpack Compose)

### 1. Écran d'accueil (Sélection du Rôle)
- Interface pour choisir entre le mode **"Administrateur"** et le mode **"Médecin"**.
- Pour le mode médecin, afficher une liste déroulante ou une grille des médecins inscrits dans la base de données pour simuler la connexion à un compte spécifique.

### 2. Tableau de bord Administrateur
- **Statistiques Globales** : Nombre de médecins, montant total des commissions dues, prescriptions en attente.
- **Gestion** : Formulaire pour ajouter un nouveau médecin.
- **Validation** : Liste des prescriptions avec un bouton pour Approuver/Valider (déclenche la création de commission).
- **Paiements** : Interface pour sélectionner un médecin et lui enregistrer un versement financier.

### 3. Tableau de bord Médecin
- **Tableau de Bord Financier (Wallet)** : Affichage clair du solde disponible (`balance`), des revenus générés (`totalEarned`), et de l'historique des retraits.
- **Graphique Mensuel (`MonthlyFinancialBarChart`)** : Un composant visuel (diagramme à barres) comparant pour chaque mois :
  - **En Vert** : La somme des versements reçus (`PaymentEntity` sur le mois).
  - **En Orange/Rouge** : La somme des commissions en attente (`CommissionEntity` avec le statut `PENDING` sur le mois).
- **Création de Prescription** : Un formulaire complet permettant au médecin d'ajouter un patient, de saisir un diagnostic, et d'ajouter dynamiquement (avec des boutons "+" ) une liste de médicaments (`MedicationEntity`).
- **Historique** : Un onglet listant toutes les prescriptions passées de ce médecin, avec un indicateur visuel de statut (En attente, Validé, Rejeté).

## 6. Composants UI et Design System
- **Couleurs & Typographie** : Utiliser `MaterialTheme.colorScheme` (Dynamic Colors).
- **Graphique en barres personnalisé** : Utiliser l'API Canvas de Compose (`Modifier.drawBehind` ou `Box` avec `fillMaxHeight()`) pour animer la hauteur des barres financières (ex: `animateFloatAsState`).
- **Cartes** : Utiliser le composant `Card` avec des coins arrondis (ex: `RoundedCornerShape(16.dp)`) et des ombres légères.
- **Icônes** : Utiliser de façon intensive `Icons.Default` et `Icons.Filled` (ex: `AttachMoney`, `PendingActions`, `MedicalServices`, `TrendingUp`) pour améliorer l'ergonomie.

---
**Note pour l'IA exécutante** : Suivez strictement cette documentation. Commencez par les entités Room, puis le Repository, le ViewModel, et enfin construisez la hiérarchie UI Compose en utilisant des composants Material 3.

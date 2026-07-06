# Implémentation Flutter : Gestion de Prescriptions et Commissions

Voici une proposition complète pour l'architecture et le code de votre application Flutter.
Étant donné que cet environnement (Google AI Studio) est configuré exclusivement pour **Android natif (Kotlin / Jetpack Compose)**, je ne peux pas compiler ni prévisualiser de code Flutter ici. 

Cependant, vous pouvez copier cette implémentation dans votre projet Flutter local. Elle utilise **Riverpod** pour la gestion d'état et **sqflite** pour la base de données locale.

## 1. Dépendances (`pubspec.yaml`)

Ajoutez ces dépendances à votre fichier `pubspec.yaml` :

```yaml
dependencies:
  flutter:
    sdk: flutter
  flutter_riverpod: ^2.4.3
  sqflite: ^2.3.0
  path: ^1.8.3
  fl_chart: ^0.65.0
  intl: ^0.18.1
  uuid: ^4.2.2
```

## 2. Modèles de Données (`lib/models/models.dart`)

```dart
import 'package:uuid/uuid.dart';

const uuid = Uuid();

class DoctorModel {
  final String id;
  final String name;
  final String specialization;
  final String phone;
  final String email;
  final int createdAt;

  DoctorModel({String? id, required this.name, required this.specialization, required this.phone, required this.email, int? createdAt}) 
      : id = id ?? uuid.v4(), createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toMap() => {'id': id, 'name': name, 'specialization': specialization, 'phone': phone, 'email': email, 'createdAt': createdAt};
  
  factory DoctorModel.fromMap(Map<String, dynamic> map) => DoctorModel(
    id: map['id'], name: map['name'], specialization: map['specialization'], phone: map['phone'], email: map['email'], createdAt: map['createdAt']
  );
}

class PrescriptionModel {
  final String id;
  final String doctorId;
  final String patientName;
  final int patientAge;
  final String prescriptionDate;
  final String notes;
  final String diagnosis;
  final String status;

  PrescriptionModel({String? id, required this.doctorId, required this.patientName, required this.patientAge, required this.prescriptionDate, required this.notes, required this.diagnosis, this.status = 'PENDING'})
      : id = id ?? uuid.v4();

  Map<String, dynamic> toMap() => {'id': id, 'doctorId': doctorId, 'patientName': patientName, 'patientAge': patientAge, 'prescriptionDate': prescriptionDate, 'notes': notes, 'diagnosis': diagnosis, 'status': status};
  
  factory PrescriptionModel.fromMap(Map<String, dynamic> map) => PrescriptionModel(
    id: map['id'], doctorId: map['doctorId'], patientName: map['patientName'], patientAge: map['patientAge'], prescriptionDate: map['prescriptionDate'], notes: map['notes'], diagnosis: map['diagnosis'], status: map['status']
  );
}

class MedicationModel {
  final String id;
  final String prescriptionId;
  final String name;
  final String dosage;
  final String frequency;
  final String duration;
  final int quantity;

  MedicationModel({String? id, required this.prescriptionId, required this.name, required this.dosage, required this.frequency, required this.duration, required this.quantity})
      : id = id ?? uuid.v4();

  Map<String, dynamic> toMap() => {'id': id, 'prescriptionId': prescriptionId, 'name': name, 'dosage': dosage, 'frequency': frequency, 'duration': duration, 'quantity': quantity};
  
  factory MedicationModel.fromMap(Map<String, dynamic> map) => MedicationModel(
    id: map['id'], prescriptionId: map['prescriptionId'], name: map['name'], dosage: map['dosage'], frequency: map['frequency'], duration: map['duration'], quantity: map['quantity']
  );
}

class CommissionModel {
  final String id;
  final String? prescriptionId;
  final String doctorId;
  final double commissionAmount;
  final String status;
  final int createdAt;

  CommissionModel({String? id, this.prescriptionId, required this.doctorId, required this.commissionAmount, this.status = 'PENDING', int? createdAt})
      : id = id ?? uuid.v4(), createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toMap() => {'id': id, 'prescriptionId': prescriptionId, 'doctorId': doctorId, 'commissionAmount': commissionAmount, 'status': status, 'createdAt': createdAt};
  
  factory CommissionModel.fromMap(Map<String, dynamic> map) => CommissionModel(
    id: map['id'], prescriptionId: map['prescriptionId'], doctorId: map['doctorId'], commissionAmount: map['commissionAmount'], status: map['status'], createdAt: map['createdAt']
  );
}

class WalletModel {
  final String id;
  final String doctorId;
  final double balance;
  final double totalEarned;
  final double totalWithdrawn;
  final int updatedAt;

  WalletModel({String? id, required this.doctorId, this.balance = 0.0, this.totalEarned = 0.0, this.totalWithdrawn = 0.0, int? updatedAt})
      : id = id ?? uuid.v4(), updatedAt = updatedAt ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toMap() => {'id': id, 'doctorId': doctorId, 'balance': balance, 'totalEarned': totalEarned, 'totalWithdrawn': totalWithdrawn, 'updatedAt': updatedAt};
  
  factory WalletModel.fromMap(Map<String, dynamic> map) => WalletModel(
    id: map['id'], doctorId: map['doctorId'], balance: map['balance'], totalEarned: map['totalEarned'], totalWithdrawn: map['totalWithdrawn'], updatedAt: map['updatedAt']
  );
}

class PaymentModel {
  final String id;
  final String doctorId;
  final double amount;
  final String paymentDate;
  final String reference;
  final String status;
  final int createdAt;

  PaymentModel({String? id, required this.doctorId, required this.amount, required this.paymentDate, required this.reference, this.status = 'COMPLETED', int? createdAt})
      : id = id ?? uuid.v4(), createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toMap() => {'id': id, 'doctorId': doctorId, 'amount': amount, 'paymentDate': paymentDate, 'reference': reference, 'status': status, 'createdAt': createdAt};
  
  factory PaymentModel.fromMap(Map<String, dynamic> map) => PaymentModel(
    id: map['id'], doctorId: map['doctorId'], amount: map['amount'], paymentDate: map['paymentDate'], reference: map['reference'], status: map['status'], createdAt: map['createdAt']
  );
}
```

## 3. Base de Données (`lib/data/database_helper.dart`)

```dart
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/models.dart';

class DatabaseHelper {
  static final DatabaseHelper instance = DatabaseHelper._init();
  static Database? _database;

  DatabaseHelper._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('prescriptions.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);
    return await openDatabase(path, version: 1, onCreate: _createDB);
  }

  Future _createDB(Database db, int version) async {
    await db.execute('''CREATE TABLE doctors (id TEXT PRIMARY KEY, name TEXT, specialization TEXT, phone TEXT, email TEXT, createdAt INTEGER)''');
    await db.execute('''CREATE TABLE prescriptions (id TEXT PRIMARY KEY, doctorId TEXT, patientName TEXT, patientAge INTEGER, prescriptionDate TEXT, notes TEXT, diagnosis TEXT, status TEXT)''');
    await db.execute('''CREATE TABLE medications (id TEXT PRIMARY KEY, prescriptionId TEXT, name TEXT, dosage TEXT, frequency TEXT, duration TEXT, quantity INTEGER)''');
    await db.execute('''CREATE TABLE commissions (id TEXT PRIMARY KEY, prescriptionId TEXT, doctorId TEXT, commissionAmount REAL, status TEXT, createdAt INTEGER)''');
    await db.execute('''CREATE TABLE wallets (id TEXT PRIMARY KEY, doctorId TEXT, balance REAL, totalEarned REAL, totalWithdrawn REAL, updatedAt INTEGER)''');
    await db.execute('''CREATE TABLE payments (id TEXT PRIMARY KEY, doctorId TEXT, amount REAL, paymentDate TEXT, reference TEXT, status TEXT, createdAt INTEGER)''');
  }

  // Exemple de méthodes CRUD
  Future<void> insertDoctor(DoctorModel doctor) async {
    final db = await instance.database;
    await db.insert('doctors', doctor.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
    await db.insert('wallets', WalletModel(doctorId: doctor.id).toMap()); // Créer le wallet
  }

  Future<List<DoctorModel>> getDoctors() async {
    final db = await instance.database;
    final result = await db.query('doctors');
    return result.map((json) => DoctorModel.fromMap(json)).toList();
  }
  
  // Implémentez ici les autres méthodes (getPrescriptions, updateWallet, etc.)
}
```

## 4. Logique Métier (Riverpod) (`lib/providers/app_providers.dart`)

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database_helper.dart';
import '../models/models.dart';

final databaseProvider = Provider((ref) => DatabaseHelper.instance);

final doctorsProvider = FutureProvider<List<DoctorModel>>((ref) async {
  return ref.read(databaseProvider).getDoctors();
});

class AdminController extends StateNotifier<AsyncValue<void>> {
  final DatabaseHelper db;
  AdminController(this.db) : super(const AsyncValue.data(null));

  Future<void> validatePrescription(PrescriptionModel prescription) async {
    state = const AsyncValue.loading();
    try {
      final updatedPrescription = PrescriptionModel(
        id: prescription.id, doctorId: prescription.doctorId, patientName: prescription.patientName, patientAge: prescription.patientAge, prescriptionDate: prescription.prescriptionDate, notes: prescription.notes, diagnosis: prescription.diagnosis, status: 'VALIDATED'
      );
      final database = await db.database;
      
      await database.transaction((txn) async {
        await txn.update('prescriptions', updatedPrescription.toMap(), where: 'id = ?', whereArgs: [prescription.id]);
        
        // 1. Générer la commission (ex: 15.0 par défaut)
        final commission = CommissionModel(prescriptionId: prescription.id, doctorId: prescription.doctorId, commissionAmount: 15.0);
        await txn.insert('commissions', commission.toMap());

        // 2. Mettre à jour le Wallet
        final walletMaps = await txn.query('wallets', where: 'doctorId = ?', whereArgs: [prescription.doctorId]);
        if (walletMaps.isNotEmpty) {
          final wallet = WalletModel.fromMap(walletMaps.first);
          final updatedWallet = WalletModel(id: wallet.id, doctorId: wallet.doctorId, balance: wallet.balance + 15.0, totalEarned: wallet.totalEarned + 15.0, totalWithdrawn: wallet.totalWithdrawn, updatedAt: DateTime.now().millisecondsSinceEpoch);
          await txn.update('wallets', updatedWallet.toMap(), where: 'id = ?', whereArgs: [wallet.id]);
        }
      });
      state = const AsyncValue.data(null);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> processPayment(String doctorId, double amount, String ref) async {
    state = const AsyncValue.loading();
    try {
      final database = await db.database;
      await database.transaction((txn) async {
        // 1. Enregistrer le paiement
        final payment = PaymentModel(doctorId: doctorId, amount: amount, paymentDate: DateTime.now().toIso8601String(), reference: ref);
        await txn.insert('payments', payment.toMap());

        // 2. Mettre à jour le Wallet
        final walletMaps = await txn.query('wallets', where: 'doctorId = ?', whereArgs: [doctorId]);
        if (walletMaps.isNotEmpty) {
          final wallet = WalletModel.fromMap(walletMaps.first);
          final updatedWallet = WalletModel(id: wallet.id, doctorId: wallet.doctorId, balance: wallet.balance - amount, totalEarned: wallet.totalEarned, totalWithdrawn: wallet.totalWithdrawn + amount, updatedAt: DateTime.now().millisecondsSinceEpoch);
          await txn.update('wallets', updatedWallet.toMap(), where: 'id = ?', whereArgs: [wallet.id]);
        }

        // 3. Mettre à jour les commissions PENDING -> PAID
        await txn.update('commissions', {'status': 'PAID'}, where: 'doctorId = ? AND status = ?', whereArgs: [doctorId, 'PENDING']);
      });
      state = const AsyncValue.data(null);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }
}

final adminProvider = StateNotifierProvider<AdminController, AsyncValue<void>>((ref) {
  return AdminController(ref.read(databaseProvider));
});
```

## 5. Interface UI Principale (`lib/main.dart`)

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

void main() {
  runApp(const ProviderScope(child: MyApp()));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Prescriptions App',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
      ),
      home: const RoleSelectionScreen(),
    );
  }
}

class RoleSelectionScreen extends StatelessWidget {
  const RoleSelectionScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Sélectionnez votre rôle')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () { /* Navigate to AdminDashboard */ },
              child: const Text('Administrateur'),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () { /* Navigate to DoctorDashboard */ },
              child: const Text('Médecin'),
            ),
          ],
        ),
      ),
    );
  }
}
```

## Pour Continuer

1. Créez un projet Flutter : `flutter create prescriptions_app`
2. Ajoutez les dépendances dans `pubspec.yaml`
3. Copiez ces fichiers dans le répertoire `lib/`
4. Implémentez les écrans UI spécifiques (Dashboard, fl_chart) selon vos préférences en utilisant Material 3.

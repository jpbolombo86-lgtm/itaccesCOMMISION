package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        DoctorEntity::class,
        PatientEntity::class,
        PrescriptionEntity::class,
        CommissionEntity::class,
        WalletEntity::class,
        PaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun commissionDao(): CommissionDao
    abstract fun walletDao(): WalletDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prescription_management_db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(db: AppDatabase) {
            val userDao = db.userDao()
            val doctorDao = db.doctorDao()
            val patientDao = db.patientDao()
            val prescriptionDao = db.prescriptionDao()
            val commissionDao = db.commissionDao()
            val walletDao = db.walletDao()
            val paymentDao = db.paymentDao()

            // 1. Seed Users
            val adminUser = UserEntity("u-admin", "Dr. Elizabeth Vance", "admin@hospital.com", "ADMIN", true)
            val doc1 = UserEntity("u-doc1", "Dr. David Cho", "cho@hospital.com", "DOCTOR", true)
            val doc2 = UserEntity("u-doc2", "Dr. Sarah Jenkins", "jenkins@hospital.com", "DOCTOR", true)
            val doc3 = UserEntity("u-doc3", "Dr. Michael Chang", "chang@hospital.com", "DOCTOR", true)

            userDao.insertUser(adminUser)
            userDao.insertUser(doc1)
            userDao.insertUser(doc2)
            userDao.insertUser(doc3)

            // 2. Seed Doctors
            val d1 = DoctorEntity("d-cho", "u-doc1", "Dr. David Cho", "Cardiology", "MD-98231", "+1 (555) 010-0101", true)
            val d2 = DoctorEntity("d-jenkins", "u-doc2", "Dr. Sarah Jenkins", "Pediatrics", "MD-47291", "+1 (555) 010-0102", true)
            val d3 = DoctorEntity("d-chang", "u-doc3", "Dr. Michael Chang", "Neurology", "MD-63022", "+1 (555) 010-0103", true)

            doctorDao.insertDoctor(d1)
            doctorDao.insertDoctor(d2)
            doctorDao.insertDoctor(d3)

            // 3. Seed Patients
            val p1 = PatientEntity("p-eleanor", "Eleanor Vance", "1982-11-15", "+1 (555) 020-0201")
            val p2 = PatientEntity("p-john", "Johnathan Miller", "1965-04-23", "+1 (555) 020-0202")
            val p3 = PatientEntity("p-clara", "Clara Oswald", "1994-08-30", "+1 (555) 020-0203")
            val p4 = PatientEntity("p-robert", "Robert Baratheon", "1970-12-12", "+1 (555) 020-0204")

            patientDao.insertPatient(p1)
            patientDao.insertPatient(p2)
            patientDao.insertPatient(p3)
            patientDao.insertPatient(p4)

            // 4. Seed Prescriptions (Total amounts: $450, $180, $850, $1200)
            val pr1 = PrescriptionEntity("pr-1", "d-cho", "p-eleanor", "u-admin", "2026-06-25", 450.0, "VALIDATED", "Carvedilol 6.25mg twice daily. Monitor blood pressure.")
            val pr2 = PrescriptionEntity("pr-2", "d-jenkins", "p-john", "u-admin", "2026-06-28", 180.0, "VALIDATED", "Amoxicillin 500mg, three times daily for 10 days.")
            val pr3 = PrescriptionEntity("pr-3", "d-chang", "p-clara", "u-admin", "2026-07-01", 850.0, "PENDING", "Sumatriptan 50mg as needed for migraine onset.")
            val pr4 = PrescriptionEntity("pr-4", "d-cho", "p-robert", "u-admin", "2026-07-03", 1200.0, "VALIDATED", "Atorvastatin 40mg nightly. Routine lipid panel in 6 weeks.")

            prescriptionDao.insertPrescription(pr1)
            prescriptionDao.insertPrescription(pr2)
            prescriptionDao.insertPrescription(pr3)
            prescriptionDao.insertPrescription(pr4)

            // 5. Seed Commissions (5% of base)
            val c1 = CommissionEntity("c-1", "pr-1", "d-cho", 450.0, 0.05, 22.50, "PAID")
            val c2 = CommissionEntity("c-2", "pr-2", "d-jenkins", 180.0, 0.05, 9.00, "PAID")
            val c3 = CommissionEntity("c-3", "pr-3", "d-chang", 850.0, 0.05, 42.50, "PENDING")
            val c4 = CommissionEntity("c-4", "pr-4", "d-cho", 1200.0, 0.05, 60.00, "PENDING")

            commissionDao.insertCommission(c1)
            commissionDao.insertCommission(c2)
            commissionDao.insertCommission(c3)
            commissionDao.insertCommission(c4)

            // 6. Seed Wallets (totalPaid + balance = totalEarned)
            val w1 = WalletEntity("w-cho", "d-cho", 60.0, 82.5, 22.5)
            val w2 = WalletEntity("w-jenkins", "d-jenkins", 0.0, 9.0, 9.0)
            val w3 = WalletEntity("w-chang", "d-chang", 42.5, 42.5, 0.0)

            walletDao.insertWallet(w1)
            walletDao.insertWallet(w2)
            walletDao.insertWallet(w3)

            // 7. Seed Payments
            val py1 = PaymentEntity("py-1", "d-cho", "u-admin", 22.50, "2026-06-30", "Bank Transfer", "TXN-98213", "Processed monthly automatic commission payout.")
            val py2 = PaymentEntity("py-2", "d-jenkins", "u-admin", 9.00, "2026-07-02", "Mobile Money", "TXN-41908", "Paid fully upon request.")

            paymentDao.insertPayment(py1)
            paymentDao.insertPayment(py2)
        }
    }
}

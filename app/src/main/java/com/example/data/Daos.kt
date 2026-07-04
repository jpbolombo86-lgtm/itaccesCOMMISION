package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)
}

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors")
    fun getAllDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: String): DoctorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: DoctorEntity)

    @Query("UPDATE doctors SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)
}

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY createdAt DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: String): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY createdAt DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE doctorId = :doctorId ORDER BY createdAt DESC")
    fun getPrescriptionsByDoctor(doctorId: String): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionById(id: String): PrescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Query("UPDATE prescriptions SET status = :status WHERE id = :id")
    suspend fun updatePrescriptionStatus(id: String, status: String)
}

@Dao
interface CommissionDao {
    @Query("SELECT * FROM commissions ORDER BY createdAt DESC")
    fun getAllCommissions(): Flow<List<CommissionEntity>>

    @Query("SELECT * FROM commissions WHERE doctorId = :doctorId ORDER BY createdAt DESC")
    fun getCommissionsByDoctor(doctorId: String): Flow<List<CommissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommission(commission: CommissionEntity)

    @Query("UPDATE commissions SET status = :status WHERE id = :id")
    suspend fun updateCommissionStatus(id: String, status: String)

    @Query("UPDATE commissions SET status = 'PAID' WHERE doctorId = :doctorId AND status = 'PENDING'")
    suspend fun markDoctorCommissionsAsPaid(doctorId: String)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE doctorId = :doctorId")
    fun getWalletByDoctorFlow(doctorId: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE doctorId = :doctorId")
    suspend fun getWalletByDoctor(doctorId: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE doctorId = :doctorId ORDER BY createdAt DESC")
    fun getPaymentsByDoctor(doctorId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)
}

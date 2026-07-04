package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // "ADMIN" or "DOCTOR"
    val active: Boolean
)

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val specialty: String,
    val licenseNo: String,
    val phone: String,
    val isActive: Boolean
)

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val dateOfBirth: String,
    val phone: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey val id: String,
    val doctorId: String,
    val patientId: String,
    val processedById: String,
    val prescriptionDate: String,
    val totalAmount: Double,
    val status: String, // "PENDING" | "VALIDATED" | "CANCELLED"
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commissions")
data class CommissionEntity(
    @PrimaryKey val id: String,
    val prescriptionId: String,
    val doctorId: String,
    val baseAmount: Double,
    val rate: Double = 0.05, // 5%
    val commissionAmount: Double,
    val status: String, // "PENDING" | "PAID"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val doctorId: String,
    val balance: Double,
    val totalEarned: Double,
    val totalPaid: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val doctorId: String,
    val paidById: String,
    val amount: Double,
    val paymentDate: String,
    val method: String,
    val reference: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)

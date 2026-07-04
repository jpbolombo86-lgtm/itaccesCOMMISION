package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Rich models representing fully resolved database relationships
data class FullPrescription(
    val entity: PrescriptionEntity,
    val doctor: DoctorEntity?,
    val patient: PatientEntity?,
    val commission: CommissionEntity?
)

data class FullCommission(
    val entity: CommissionEntity,
    val doctor: DoctorEntity?,
    val prescription: PrescriptionEntity?,
    val patient: PatientEntity?
)

data class FullPayment(
    val entity: PaymentEntity,
    val doctor: DoctorEntity?
)

data class FullWallet(
    val entity: WalletEntity,
    val doctor: DoctorEntity?
)

class PrescriptionRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val doctorDao = db.doctorDao()
    private val patientDao = db.patientDao()
    private val prescriptionDao = db.prescriptionDao()
    private val commissionDao = db.commissionDao()
    private val walletDao = db.walletDao()
    private val paymentDao = db.paymentDao()

    // 1. Reactive Flows of Full Models
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allDoctors: Flow<List<DoctorEntity>> = doctorDao.getAllDoctors()
    val allPatients: Flow<List<PatientEntity>> = patientDao.getAllPatients()

    val fullPrescriptions: Flow<List<FullPrescription>> = combine(
        prescriptionDao.getAllPrescriptions(),
        doctorDao.getAllDoctors(),
        patientDao.getAllPatients(),
        commissionDao.getAllCommissions()
    ) { prescriptions, doctors, patients, commissions ->
        prescriptions.map { pr ->
            FullPrescription(
                entity = pr,
                doctor = doctors.find { it.id == pr.doctorId },
                patient = patients.find { it.id == pr.patientId },
                commission = commissions.find { it.prescriptionId == pr.id }
            )
        }
    }

    fun getFullPrescriptionsForDoctor(doctorId: String): Flow<List<FullPrescription>> {
        return combine(
            prescriptionDao.getPrescriptionsByDoctor(doctorId),
            doctorDao.getAllDoctors(),
            patientDao.getAllPatients(),
            commissionDao.getAllCommissions()
        ) { prescriptions, doctors, patients, commissions ->
            prescriptions.map { pr ->
                FullPrescription(
                    entity = pr,
                    doctor = doctors.find { it.id == pr.doctorId },
                    patient = patients.find { it.id == pr.patientId },
                    commission = commissions.find { it.prescriptionId == pr.id }
                )
            }
        }
    }

    val fullCommissions: Flow<List<FullCommission>> = combine(
        commissionDao.getAllCommissions(),
        doctorDao.getAllDoctors(),
        prescriptionDao.getAllPrescriptions(),
        patientDao.getAllPatients()
    ) { commissions, doctors, prescriptions, patients ->
        commissions.map { comm ->
            val pr = prescriptions.find { it.id == comm.prescriptionId }
            FullCommission(
                entity = comm,
                doctor = doctors.find { it.id == comm.doctorId },
                prescription = pr,
                patient = if (pr != null) patients.find { it.id == pr.patientId } else null
            )
        }
    }

    fun getFullCommissionsForDoctor(doctorId: String): Flow<List<FullCommission>> {
        return combine(
            commissionDao.getCommissionsByDoctor(doctorId),
            doctorDao.getAllDoctors(),
            prescriptionDao.getAllPrescriptions(),
            patientDao.getAllPatients()
        ) { commissions, doctors, prescriptions, patients ->
            commissions.map { comm ->
                val pr = prescriptions.find { it.id == comm.prescriptionId }
                FullCommission(
                    entity = comm,
                    doctor = doctors.find { it.id == comm.doctorId },
                    prescription = pr,
                    patient = if (pr != null) patients.find { it.id == pr.patientId } else null
                )
            }
        }
    }

    val fullWallets: Flow<List<FullWallet>> = combine(
        walletDao.getAllWallets(),
        doctorDao.getAllDoctors()
    ) { wallets, doctors ->
        wallets.map { wallet ->
            FullWallet(
                entity = wallet,
                doctor = doctors.find { it.id == wallet.doctorId }
            )
        }
    }

    fun getFullWalletForDoctor(doctorId: String): Flow<FullWallet?> {
        return combine(
            walletDao.getWalletByDoctorFlow(doctorId),
            doctorDao.getAllDoctors()
        ) { wallet, doctors ->
            wallet?.let {
                FullWallet(
                    entity = it,
                    doctor = doctors.find { it.id == wallet.doctorId }
                )
            }
        }
    }

    val fullPayments: Flow<List<FullPayment>> = combine(
        paymentDao.getAllPayments(),
        doctorDao.getAllDoctors()
    ) { payments, doctors ->
        payments.map { pm ->
            FullPayment(
                entity = pm,
                doctor = doctors.find { it.id == pm.doctorId }
            )
        }
    }

    // 2. Database Mutators (Suspended operations)

    suspend fun createDoctor(
        fullName: String,
        email: String,
        specialty: String,
        licenseNo: String,
        phone: String
    ) {
        val userId = "u-" + UUID.randomUUID().toString().take(6)
        val doctorId = "d-" + UUID.randomUUID().toString().take(6)
        
        val user = UserEntity(userId, fullName, email, "DOCTOR", true)
        val doctor = DoctorEntity(doctorId, userId, fullName, specialty, licenseNo, phone, true)
        val wallet = WalletEntity("w-" + doctorId, doctorId, 0.0, 0.0, 0.0)

        db.userDao().insertUser(user)
        db.doctorDao().insertDoctor(doctor)
        db.walletDao().insertWallet(wallet)
    }

    suspend fun toggleDoctorActiveStatus(doctorId: String, isActive: Boolean) {
        db.doctorDao().updateActiveStatus(doctorId, isActive)
        // Also toggle matching user status if needed
        val doc = db.doctorDao().getDoctorById(doctorId)
        if (doc != null) {
            val user = db.userDao().getUserById(doc.userId)
            if (user != null) {
                db.userDao().insertUser(user.copy(active = isActive))
            }
        }
    }

    suspend fun createPatient(fullName: String, dateOfBirth: String, phone: String) {
        val patientId = "p-" + UUID.randomUUID().toString().take(6)
        val patient = PatientEntity(patientId, fullName, dateOfBirth, phone)
        db.patientDao().insertPatient(patient)
    }

    suspend fun createPrescription(
        doctorId: String,
        patientId: String,
        processedById: String,
        totalAmount: Double,
        notes: String,
        autoValidate: Boolean = false
    ) {
        val prescriptionId = "pr-" + UUID.randomUUID().toString().take(6)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val initialStatus = if (autoValidate) "VALIDATED" else "PENDING"
        
        val prescription = PrescriptionEntity(
            id = prescriptionId,
            doctorId = doctorId,
            patientId = patientId,
            processedById = processedById,
            prescriptionDate = todayStr,
            totalAmount = totalAmount,
            status = initialStatus,
            notes = notes
        )
        db.prescriptionDao().insertPrescription(prescription)

        // 5% Commission calculation
        val commissionAmount = totalAmount * 0.05
        val commissionStatus = if (autoValidate) "PENDING" else "PENDING" // Starts as pending commission
        
        val commission = CommissionEntity(
            id = "c-" + UUID.randomUUID().toString().take(6),
            prescriptionId = prescriptionId,
            doctorId = doctorId,
            baseAmount = totalAmount,
            rate = 0.05,
            commissionAmount = commissionAmount,
            status = commissionStatus
        )
        db.commissionDao().insertCommission(commission)

        if (autoValidate) {
            // If created validated, update wallet immediately
            updateWalletForNewValidatedCommission(doctorId, commissionAmount)
        }
    }

    suspend fun validatePrescription(prescriptionId: String) {
        val prescription = db.prescriptionDao().getPrescriptionById(prescriptionId) ?: return
        if (prescription.status == "VALIDATED") return // Already validated

        // 1. Update prescription status
        db.prescriptionDao().updatePrescriptionStatus(prescriptionId, "VALIDATED")

        // 2. Fetch commission and make sure it exists
        val commissions = db.commissionDao().getAllCommissions()
        // Find commission for this prescription (we do it in db query or load list)
        // Let's implement a simple transaction to update the wallet
        val commAmount = prescription.totalAmount * 0.05
        updateWalletForNewValidatedCommission(prescription.doctorId, commAmount)
    }

    suspend fun cancelPrescription(prescriptionId: String) {
        db.prescriptionDao().updatePrescriptionStatus(prescriptionId, "CANCELLED")
    }

    private suspend fun updateWalletForNewValidatedCommission(doctorId: String, commissionAmount: Double) {
        val wallet = db.walletDao().getWalletByDoctor(doctorId)
        if (wallet != null) {
            val updatedWallet = wallet.copy(
                balance = wallet.balance + commissionAmount,
                totalEarned = wallet.totalEarned + commissionAmount,
                updatedAt = System.currentTimeMillis()
            )
            db.walletDao().insertWallet(updatedWallet)
        } else {
            // Fallback: Create wallet if it doesn't exist
            val newWallet = WalletEntity(
                id = "w-" + doctorId,
                doctorId = doctorId,
                balance = commissionAmount,
                totalEarned = commissionAmount,
                totalPaid = 0.0
            )
            db.walletDao().insertWallet(newWallet)
        }
    }

    suspend fun processPayment(
        doctorId: String,
        paidById: String,
        method: String,
        reference: String,
        note: String
    ): Boolean {
        val wallet = db.walletDao().getWalletByDoctor(doctorId) ?: return false
        val amountToPay = wallet.balance
        if (amountToPay <= 0) return false

        // 1. Record payment
        val paymentId = "py-" + UUID.randomUUID().toString().take(6)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val payment = PaymentEntity(
            id = paymentId,
            doctorId = doctorId,
            paidById = paidById,
            amount = amountToPay,
            paymentDate = todayStr,
            method = method,
            reference = reference,
            note = note
        )
        db.paymentDao().insertPayment(payment)

        // 2. Update wallet: empty the balance, increment paid
        val updatedWallet = wallet.copy(
            balance = 0.0,
            totalPaid = wallet.totalPaid + amountToPay,
            updatedAt = System.currentTimeMillis()
        )
        db.walletDao().insertWallet(updatedWallet)

        // 3. Mark all PENDING commissions for this doctor as PAID in database
        db.commissionDao().markDoctorCommissionsAsPaid(doctorId)
        
        return true
    }
}

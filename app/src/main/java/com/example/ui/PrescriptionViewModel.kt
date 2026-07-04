package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PrescriptionViewModel(
    application: Application,
    private val repository: PrescriptionRepository
) : AndroidViewModel(application) {

    // 1. Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentDoctor = MutableStateFlow<DoctorEntity?>(null)
    val currentDoctor: StateFlow<DoctorEntity?> = _currentDoctor.asStateFlow()

    // 2. Base Data Streams from Repository
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDoctors: StateFlow<List<DoctorEntity>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPatients: StateFlow<List<PatientEntity>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Dynamic lists based on logged in user's role
    val prescriptions: StateFlow<List<FullPrescription>> = currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else if (user.role == "ADMIN") {
                repository.fullPrescriptions
            } else {
                // Find corresponding doctorId
                val doc = allDoctors.value.find { it.userId == user.id }
                if (doc != null) {
                    repository.getFullPrescriptionsForDoctor(doc.id)
                } else {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commissions: StateFlow<List<FullCommission>> = currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else if (user.role == "ADMIN") {
                repository.fullCommissions
            } else {
                val doc = allDoctors.value.find { it.userId == user.id }
                if (doc != null) {
                    repository.getFullCommissionsForDoctor(doc.id)
                } else {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallets: StateFlow<List<FullWallet>> = repository.fullWallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<FullPayment>> = currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else if (user.role == "ADMIN") {
                repository.fullPayments
            } else {
                val doc = allDoctors.value.find { it.userId == user.id }
                if (doc != null) {
                    repository.fullPayments.map { list -> list.filter { it.entity.doctorId == doc.id } }
                } else {
                    flowOf(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDoctorWallet: StateFlow<FullWallet?> = _currentDoctor
        .flatMapLatest { doc ->
            if (doc != null) {
                repository.getFullWalletForDoctor(doc.id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Let the user authenticate via the login screen on startup
    }

    // 4. Session Operations
    fun switchSession(user: UserEntity) {
        viewModelScope.launch {
            _currentUser.value = user
            if (user.role == "DOCTOR") {
                // Wait/lookup matching doctor
                val matchingDoc = repository.allDoctors.first().find { it.userId == user.id }
                _currentDoctor.value = matchingDoc
            } else {
                _currentDoctor.value = null
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _currentUser.value = null
            _currentDoctor.value = null
        }
    }

    // 5. Database Actions
    fun createDoctor(
        fullName: String,
        email: String,
        specialty: String,
        licenseNo: String,
        phone: String
    ) {
        viewModelScope.launch {
            repository.createDoctor(fullName, email, specialty, licenseNo, phone)
        }
    }

    fun toggleDoctorActiveStatus(doctorId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleDoctorActiveStatus(doctorId, isActive)
        }
    }

    fun createPatient(fullName: String, dateOfBirth: String, phone: String) {
        viewModelScope.launch {
            repository.createPatient(fullName, dateOfBirth, phone)
        }
    }

    fun createPrescription(
        doctorId: String,
        patientId: String,
        totalAmount: Double,
        notes: String,
        autoValidate: Boolean = false
    ) {
        viewModelScope.launch {
            val processedById = currentUser.value?.id ?: "u-admin"
            repository.createPrescription(
                doctorId = doctorId,
                patientId = patientId,
                processedById = processedById,
                totalAmount = totalAmount,
                notes = notes,
                autoValidate = autoValidate
            )
        }
    }

    fun validatePrescription(prescriptionId: String) {
        viewModelScope.launch {
            repository.validatePrescription(prescriptionId)
        }
    }

    fun cancelPrescription(prescriptionId: String) {
        viewModelScope.launch {
            repository.cancelPrescription(prescriptionId)
        }
    }

    fun processPayout(
        doctorId: String,
        method: String,
        reference: String,
        note: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val processedById = currentUser.value?.id ?: "u-admin"
            val success = repository.processPayment(
                doctorId = doctorId,
                paidById = processedById,
                method = method,
                reference = reference,
                note = note
            )
            onComplete(success)
        }
    }
}

class PrescriptionViewModelFactory(
    private val application: Application,
    private val repository: PrescriptionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrescriptionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrescriptionViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

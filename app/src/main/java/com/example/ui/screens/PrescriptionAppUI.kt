package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.ui.PrescriptionViewModel

enum class NavigationDest {
    DASHBOARD,
    DOCTORS,
    PATIENTS,
    PRESCRIPTIONS,
    COMMISSIONS,
    PAYMENTS,
    WALLET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionAppMainScreen(
    viewModel: PrescriptionViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentDoctor by viewModel.currentDoctor.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    
    val allDoctors by viewModel.allDoctors.collectAsStateWithLifecycle()
    val allPatients by viewModel.allPatients.collectAsStateWithLifecycle()
    val prescriptions by viewModel.prescriptions.collectAsStateWithLifecycle()
    val commissions by viewModel.commissions.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val currentDoctorWallet by viewModel.currentDoctorWallet.collectAsStateWithLifecycle()

    var currentDest by remember { mutableStateOf(NavigationDest.DASHBOARD) }
    var showUserSelector by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LoginScreen(
            allUsers = allUsers,
            onLoginSuccess = { user ->
                viewModel.switchSession(user)
            }
        )
        return
    }

    LaunchedEffect(currentUser) {
        if (currentUser?.role == "DOCTOR" && (currentDest == NavigationDest.DOCTORS || currentDest == NavigationDest.PATIENTS || currentDest == NavigationDest.PAYMENTS)) {
            currentDest = NavigationDest.DASHBOARD
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentUser?.role == "ADMIN") "PORTAIL ADMINISTRATEUR" else "PORTAIL MÉDICAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser?.name ?: "Dr. Julian Aris",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    val initials = currentUser?.name?.split(" ")?.let { parts ->
                        val first = parts.firstOrNull()?.take(1) ?: ""
                        val last = parts.lastOrNull()?.take(1) ?: ""
                        "$first$last"
                    }?.uppercase() ?: "JA"
                    
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { showUserSelector = true }
                            .testTag("session_switcher"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = currentDest == NavigationDest.DASHBOARD,
                    onClick = { currentDest = NavigationDest.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Tableau de bord") },
                    label = { Text("Tableau de bord", fontSize = 11.sp) }
                )

                if (currentUser?.role == "ADMIN") {
                    NavigationBarItem(
                        selected = currentDest == NavigationDest.DOCTORS,
                        onClick = { currentDest = NavigationDest.DOCTORS },
                        icon = { Icon(Icons.Default.People, contentDescription = "Médecins") },
                        label = { Text("Médecins", fontSize = 11.sp) }
                    )

                    NavigationBarItem(
                        selected = currentDest == NavigationDest.PATIENTS,
                        onClick = { currentDest = NavigationDest.PATIENTS },
                        icon = { Icon(Icons.Default.GroupAdd, contentDescription = "Patients") },
                        label = { Text("Patients", fontSize = 11.sp) }
                    )
                }

                NavigationBarItem(
                    selected = currentDest == NavigationDest.PRESCRIPTIONS,
                    onClick = { currentDest = NavigationDest.PRESCRIPTIONS },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Ordonnances") },
                    label = { Text(if (currentUser?.role == "ADMIN") "Admin Ordonnances" else "Mes Ordonnances", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentDest == NavigationDest.COMMISSIONS,
                    onClick = { currentDest = NavigationDest.COMMISSIONS },
                    icon = { Icon(Icons.Default.Percent, contentDescription = "Commissions") },
                    label = { Text(if (currentUser?.role == "ADMIN") "Commissions" else "Mes Gains", fontSize = 11.sp) }
                )

                if (currentUser?.role == "ADMIN") {
                    NavigationBarItem(
                        selected = currentDest == NavigationDest.PAYMENTS,
                        onClick = { currentDest = NavigationDest.PAYMENTS },
                        icon = { Icon(Icons.Default.Payment, contentDescription = "Versements") },
                        label = { Text("Versements", fontSize = 11.sp) }
                    )
                } else {
                    NavigationBarItem(
                        selected = currentDest == NavigationDest.WALLET,
                        onClick = { currentDest = NavigationDest.WALLET },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Portefeuille") },
                        label = { Text("Portefeuille", fontSize = 11.sp) }
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentDest) {
                NavigationDest.DASHBOARD -> {
                    if (currentUser?.role == "ADMIN") {
                        AdminDashboardView(
                            allDoctors = allDoctors,
                            allPatients = allPatients,
                            prescriptions = prescriptions,
                            commissions = commissions,
                            payments = payments,
                            onNavigateTo = { currentDest = it }
                        )
                    } else {
                        DoctorDashboardView(
                            doctor = currentDoctor,
                            prescriptions = prescriptions,
                            commissions = commissions,
                            payments = payments,
                            wallet = currentDoctorWallet,
                            onNavigateTo = { currentDest = it }
                        )
                    }
                }
                NavigationDest.DOCTORS -> {
                    if (currentUser?.role == "ADMIN") {
                        DoctorsManagementView(
                            allDoctors = allDoctors,
                            viewModel = viewModel
                        )
                    }
                }
                NavigationDest.PATIENTS -> {
                    if (currentUser?.role == "ADMIN") {
                        PatientsManagementView(
                            allPatients = allPatients,
                            viewModel = viewModel
                        )
                    }
                }
                NavigationDest.PRESCRIPTIONS -> {
                    PrescriptionsListView(
                        prescriptions = prescriptions,
                        allDoctors = allDoctors,
                        allPatients = allPatients,
                        currentUser = currentUser,
                        viewModel = viewModel
                    )
                }
                NavigationDest.COMMISSIONS -> {
                    CommissionsListView(
                        commissions = commissions,
                        allDoctors = allDoctors,
                        currentUser = currentUser
                    )
                }
                NavigationDest.PAYMENTS -> {
                    if (currentUser?.role == "ADMIN") {
                        PaymentsView(
                            payments = payments,
                            allDoctors = allDoctors,
                            viewModel = viewModel
                        )
                    }
                }
                NavigationDest.WALLET -> {
                    if (currentUser?.role == "DOCTOR") {
                        DoctorWalletView(
                            wallet = currentDoctorWallet,
                            payments = payments
                        )
                    }
                }
            }

            if (showUserSelector) {
                UserSessionSelectorDialog(
                    allUsers = allUsers,
                    currentUser = currentUser,
                    onUserSelected = { selectedUser ->
                        viewModel.switchSession(selectedUser)
                        showUserSelector = false
                    },
                    onSignOut = {
                        viewModel.signOut()
                        showUserSelector = false
                    },
                    onDismiss = { showUserSelector = false }
                )
            }
        }
    }
}

@Composable
fun UserSessionSelectorDialog(
    allUsers: List<UserEntity>,
    currentUser: UserEntity?,
    onUserSelected: (UserEntity) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SupervisedUserCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Changer de session",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Simulez facilement les rôles d'administrateur et de médecin pour tester les flux de travail.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                ) {
                    items(allUsers) { user ->
                        val isSelected = user.id == currentUser?.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserSelected(user) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${user.role} • ${user.email}",
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sélectionné",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Déconnexion",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Déconnexion")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardView(
    allDoctors: List<DoctorEntity>,
    allPatients: List<PatientEntity>,
    prescriptions: List<FullPrescription>,
    commissions: List<FullCommission>,
    payments: List<FullPayment>,
    onNavigateTo: (NavigationDest) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dashboard_banner),
                        contentDescription = "Bannière Médicale",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                    startY = 40f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Centre Clinique & Analyses",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Console de prescription, de validation et de commissions.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "Total Rx",
                    value = "${prescriptions.size}",
                    subtitle = "Ordonnances émises",
                    icon = Icons.Default.Description,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavigationDest.PRESCRIPTIONS) }
                )

                val pendingCommissionsTotal = commissions.filter { it.entity.status == "PENDING" }.sumOf { it.entity.commissionAmount }
                DashboardMetricCard(
                    title = "Frais en attente",
                    value = "$${String.format("%.2f", pendingCommissionsTotal)}",
                    subtitle = "Commissions cumulées",
                    icon = Icons.Default.Percent,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavigationDest.COMMISSIONS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val totalPaymentsProcessed = payments.sumOf { it.entity.amount }
                DashboardMetricCard(
                    title = "Versements",
                    value = "$${String.format("%.2f", totalPaymentsProcessed)}",
                    subtitle = "${payments.size} versements traités",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavigationDest.PAYMENTS) }
                )

                DashboardMetricCard(
                    title = "Patients",
                    value = "${allPatients.size}",
                    subtitle = "Noms enregistrés",
                    icon = Icons.Default.Group,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavigationDest.PATIENTS) }
                )
            }
        }

        item {
            MonthlyFinancialBarChart(
                payments = payments,
                commissions = commissions,
                title = "Suivi Financier Global"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Soldes de paiement des médecins",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (allDoctors.isEmpty()) {
                        Text(
                            text = "Aucun médecin enregistré.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    } else {
                        allDoctors.forEach { doc ->
                            val docPendingComms = commissions.filter { it.entity.doctorId == doc.id && it.entity.status == "PENDING" }.sumOf { it.entity.commissionAmount }
                            val docPaidComms = commissions.filter { it.entity.doctorId == doc.id && it.entity.status == "PAID" }.sumOf { it.entity.commissionAmount }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = doc.fullName.split(" ").lastOrNull()?.take(1) ?: "D",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = doc.fullName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = doc.specialty,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%.2f", docPendingComms)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (docPendingComms > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Payé : $${String.format("%.2f", docPaidComms)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DoctorDashboardView(
    doctor: DoctorEntity?,
    prescriptions: List<FullPrescription>,
    commissions: List<FullCommission>,
    payments: List<FullPayment>,
    wallet: FullWallet?,
    onNavigateTo: (NavigationDest) -> Unit
) {
    val doctorRx = prescriptions.filter { it.entity.doctorId == (doctor?.id ?: "") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Welcome Section with Geometric Touch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bienvenue, ${doctor?.fullName ?: "Médecin"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Spécialité : ${doctor?.specialty ?: "N/A"} • Lic. : ${doctor?.licenseNo ?: "N/A"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            // "Geometric Balance" Wallet Card (directly from HTML Design)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp), // rounded-3xl
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solde du portefeuille",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIF",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$${String.format("%.2f", wallet?.entity?.balance ?: 0.0)}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val earned = wallet?.entity?.totalEarned ?: 0.0
                    val paid = wallet?.entity?.totalPaid ?: 0.0
                    val ratio = if (earned > 0) (paid / earned).toFloat().coerceIn(0f, 1f) else 0f
                    val percentage = (ratio * 100).toInt()

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Objectif de gains",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$percentage%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LinearProgressIndicator(
                            progress = ratio,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$${String.format("%.2f", earned)} Total Gagné",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$${String.format("%.2f", paid)} Payé",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "Mes Ordonnances (Rx)",
                    value = "${doctorRx.size}",
                    subtitle = "Ordonnances émises",
                    icon = Icons.Default.Description,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavigationDest.PRESCRIPTIONS) }
                )

                val docCommissionsPaid = prescriptions
                    .filter { it.entity.doctorId == (doctor?.id ?: "") && it.entity.status == "VALIDATED" }
                    .sumOf { it.entity.totalAmount * 0.05 }
                DashboardMetricCard(
                    title = "Honoraires cumulés",
                    value = "$${String.format("%.2f", docCommissionsPaid)}",
                    subtitle = "Commissions gagnées",
                    icon = Icons.Default.Percent,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(NavigationDest.WALLET) }
                )
            }
        }

        item {
            MonthlyFinancialBarChart(
                payments = payments,
                commissions = commissions,
                title = "Mon Suivi Financier"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ordonnances récentes (Rx)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { onNavigateTo(NavigationDest.PRESCRIPTIONS) }) {
                            Text("Voir tout", fontSize = 12.sp)
                        }
                    }

                    if (doctorRx.isEmpty()) {
                        Text(
                            text = "Aucune ordonnance rédigée pour le moment.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        doctorRx.take(3).forEach { pr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = pr.patient?.fullName ?: "Patient inconnu",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${pr.entity.prescriptionDate} • $${String.format("%.2f", pr.entity.totalAmount)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (pr.entity.status) {
                                                "VALIDATED" -> Color(0xFFE8F5E9)
                                                "CANCELLED" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF3E0)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = pr.entity.status,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = when (pr.entity.status) {
                                            "VALIDATED" -> Color(0xFF2E7D32)
                                            "CANCELLED" -> Color(0xFFC62828)
                                            else -> Color(0xFFEF6C00)
                                        }
                                    )
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorsManagementView(
    allDoctors: List<DoctorEntity>,
    viewModel: PrescriptionViewModel
) {
    var showAddDoctorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registre des médecins",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            FloatingActionButton(
                onClick = { showAddDoctorDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un médecin")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (allDoctors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Aucun médecin enregistré",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allDoctors) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Spécialité : ${doc.specialty}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Licence : ${doc.licenseNo} • Tél : ${doc.phone}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Switch(
                                    checked = doc.isActive,
                                    onCheckedChange = { active ->
                                        viewModel.toggleDoctorActiveStatus(doc.id, active)
                                    }
                                )
                                Text(
                                    text = if (doc.isActive) "ACTIF" else "INACTIF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (doc.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDoctorDialog) {
        AddDoctorDialog(
            onDismiss = { showAddDoctorDialog = false },
            onSubmit = { fullName, email, specialty, license, phone ->
                viewModel.createDoctor(fullName, email, specialty, license, phone)
                showAddDoctorDialog = false
            }
        )
    }
}

@Composable
fun AddDoctorDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ajouter un nouveau médecin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nom complet") },
                    placeholder = { Text("Dr. John Watson") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Adresse e-mail") },
                    placeholder = { Text("watson@hospital.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Spécialité médicale") },
                    placeholder = { Text("Médecine générale") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = license,
                    onValueChange = { license = it },
                    label = { Text("Numéro de licence") },
                    placeholder = { Text("MD-99211") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Numéro de téléphone") },
                    placeholder = { Text("+1 (555) 010-3342") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fullName.isEmpty() || email.isEmpty() || specialty.isEmpty() || license.isEmpty() || phone.isEmpty()) {
                                errorMsg = "Tous les champs sont obligatoires."
                            } else {
                                onSubmit(fullName, email, specialty, license, phone)
                            }
                        },
                        modifier = Modifier.testTag("submit_doctor_button")
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

@Composable
fun PatientsManagementView(
    allPatients: List<PatientEntity>,
    viewModel: PrescriptionViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddPatientDialog by remember { mutableStateOf(false) }

    val filteredPatients = allPatients.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dossiers des patients",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            FloatingActionButton(
                onClick = { showAddPatientDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un patient")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher") },
            placeholder = { Text("Rechercher par nom ou téléphone") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (filteredPatients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun dossier trouvé.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredPatients) { pat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), CircleShape)
                                    .padding(6.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = pat.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Né(e) le : ${pat.dateOfBirth} • Tél : ${pat.phone}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPatientDialog) {
        AddPatientDialog(
            onDismiss = { showAddPatientDialog = false },
            onSubmit = { fullName, dob, phone ->
                viewModel.createPatient(fullName, dob, phone)
                showAddPatientDialog = false
            }
        )
    }
}

@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enregistrer un nouveau patient",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nom complet du patient") },
                    placeholder = { Text("Clara Oswald") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date de naissance") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Numéro de téléphone") },
                    placeholder = { Text("+1 (555) 010-0291") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fullName.isEmpty() || dob.isEmpty() || phone.isEmpty()) {
                                errorMsg = "Tous les champs sont obligatoires."
                            } else {
                                onSubmit(fullName, dob, phone)
                            }
                        },
                        modifier = Modifier.testTag("submit_patient_button")
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

@Composable
fun PrescriptionsListView(
    prescriptions: List<FullPrescription>,
    allDoctors: List<DoctorEntity>,
    allPatients: List<PatientEntity>,
    currentUser: UserEntity?,
    viewModel: PrescriptionViewModel
) {
    var showAddPrescriptionWizard by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredList = prescriptions.filter {
        selectedStatusFilter == "ALL" || it.entity.status == selectedStatusFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentUser?.role == "ADMIN") "Toutes les ordonnances" else "Mes Ordonnances",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            FloatingActionButton(
                onClick = { showAddPrescriptionWizard = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une ordonnance")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf("ALL", "PENDING", "VALIDATED", "CANCELLED")
            filterOptions.forEach { filter ->
                val isSelected = selectedStatusFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedStatusFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (filter) {
                            "ALL" -> "TOUT"
                            "PENDING" -> "EN ATTENTE"
                            "VALIDATED" -> "VALIDÉ"
                            "CANCELLED" -> "ANNULÉ"
                            else -> filter
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune ordonnance trouvée.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { pr ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = pr.patient?.fullName ?: "Patient inconnu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Médecin : ${pr.doctor?.fullName ?: "N/A"}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (pr.entity.status) {
                                                "VALIDATED" -> Color(0xFFE8F5E9)
                                                "CANCELLED" -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF3E0)
                                            }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = when (pr.entity.status) {
                                            "VALIDATED" -> "VALIDÉE"
                                            "CANCELLED" -> "ANNULÉE"
                                            "PENDING" -> "EN ATTENTE"
                                            else -> pr.entity.status
                                        },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = when (pr.entity.status) {
                                            "VALIDATED" -> Color(0xFF2E7D32)
                                            "CANCELLED" -> Color(0xFFC62828)
                                            else -> Color(0xFFEF6C00)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Notes : ${pr.entity.notes}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Montant de base : $${String.format("%.2f", pr.entity.totalAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Commission (5 %) : $${String.format("%.2f", pr.entity.totalAmount * 0.05)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                
                                if (currentUser?.role == "ADMIN" && pr.entity.status == "PENDING") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { viewModel.cancelPrescription(pr.entity.id) },
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = "Annuler Rx")
                                        }
                                        Button(
                                            onClick = { viewModel.validatePrescription(pr.entity.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.testTag("validate_prescription_button")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Valider Rx", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Valider", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPrescriptionWizard) {
        CreatePrescriptionWizard(
            allDoctors = allDoctors,
            allPatients = allPatients,
            currentUser = currentUser,
            onDismiss = { showAddPrescriptionWizard = false },
            onSubmit = { doctorId, patientId, amount, notes, autoValidate ->
                viewModel.createPrescription(doctorId, patientId, amount, notes, autoValidate)
                showAddPrescriptionWizard = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePrescriptionWizard(
    allDoctors: List<DoctorEntity>,
    allPatients: List<PatientEntity>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Double, String, Boolean) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    var selectedDoctor by remember { mutableStateOf<DoctorEntity?>(null) }
    var selectedPatient by remember { mutableStateOf<PatientEntity?>(null) }
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var autoValidate by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(currentUser, allDoctors) {
        if (currentUser?.role == "DOCTOR") {
            selectedDoctor = allDoctors.find { it.userId == currentUser.id }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nouvelle ordonnance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Étape $step sur 4",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider()

                when (step) {
                    1 -> {
                        if (currentUser?.role == "ADMIN") {
                            Text("Sélectionner le médecin prescripteur :", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                items(allDoctors) { doc ->
                                    val isSelected = doc.id == selectedDoctor?.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedDoctor = doc },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = doc.fullName + " (${doc.specialty})",
                                            modifier = Modifier.padding(12.dp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            LaunchedEffect(Unit) {
                                step = 2
                            }
                        }
                    }
                    2 -> {
                        Text("Sélectionner le patient :", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        if (allPatients.isEmpty()) {
                            Text("Aucun patient enregistré. Veuillez d'abord enregistrer un patient.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                items(allPatients) { pat ->
                                    val isSelected = pat.id == selectedPatient?.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPatient = pat },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = pat.fullName,
                                            modifier = Modifier.padding(12.dp),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        Text("Détails de la transaction :", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Coût total de l'ordonnance ($)") },
                            placeholder = { Text("300.00") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes cliniques / Médicaments prescrits") },
                            placeholder = { Text("Spécifier le médicament, la posologie et les instructions d'administration") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )

                        if (currentUser?.role == "ADMIN") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = autoValidate, onCheckedChange = { autoValidate = it })
                                Text("Valider automatiquement (Rentre immédiatement dans le portefeuille)", fontSize = 12.sp)
                            }
                        }
                    }
                    4 -> {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        val calculatedCommission = amount * 0.05

                        Text("Résumé de l'ordonnance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Médecin : ${selectedDoctor?.fullName ?: "N/A"}", fontSize = 13.sp)
                            Text("Patient : ${selectedPatient?.fullName ?: "N/A"}", fontSize = 13.sp)
                            Text("Montant total : $${String.format("%.2f", amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Commission calculée (5 %) : $${String.format("%.2f", calculatedCommission)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("Notes : $notes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (step > 1 && !(step == 2 && currentUser?.role == "DOCTOR")) {
                        TextButton(onClick = {
                            errorMsg = ""
                            step--
                        }) {
                            Text("Retour")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Row {
                        TextButton(onClick = onDismiss) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                errorMsg = ""
                                if (step == 1 && selectedDoctor == null) {
                                    errorMsg = "Veuillez sélectionner un médecin."
                                } else if (step == 2 && selectedPatient == null) {
                                    errorMsg = "Veuillez sélectionner un patient."
                                } else if (step == 3) {
                                    val amt = amountStr.toDoubleOrNull()
                                    if (amt == null || amt <= 0) {
                                        errorMsg = "Veuillez entrer un montant valide."
                                    } else if (notes.isEmpty()) {
                                        errorMsg = "Les notes sont requises."
                                    } else {
                                        step++
                                    }
                                } else if (step == 4) {
                                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                                    onSubmit(
                                        selectedDoctor!!.id,
                                        selectedPatient!!.id,
                                        amt,
                                        notes,
                                        autoValidate
                                    )
                                } else {
                                    step++
                                }
                            },
                            modifier = Modifier.testTag("submit_prescription_button")
                        ) {
                            Text(if (step == 4) "Soumettre Rx" else "Suivant")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommissionsListView(
    commissions: List<FullCommission>,
    allDoctors: List<DoctorEntity>,
    currentUser: UserEntity?
) {
    var selectedDoctorFilter by remember { mutableStateOf<DoctorEntity?>(null) }
    var showDoctorFilterDropdown by remember { mutableStateOf(false) }

    val filteredList = commissions.filter {
        selectedDoctorFilter == null || it.entity.doctorId == selectedDoctorFilter?.id
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (currentUser?.role == "ADMIN") "Registre des commissions cumulées" else "Mes Commissions",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (currentUser?.role == "ADMIN") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDoctorFilterDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = selectedDoctorFilter?.fullName ?: "Tous les médecins / Filtrer par médecin")
                }
                DropdownMenu(
                    expanded = showDoctorFilterDropdown,
                    onDismissRequest = { showDoctorFilterDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text("Tous les médecins") },
                        onClick = {
                            selectedDoctorFilter = null
                            showDoctorFilterDropdown = false
                        }
                    )
                    allDoctors.forEach { doc ->
                        DropdownMenuItem(
                            text = { Text(doc.fullName) },
                            onClick = {
                                selectedDoctorFilter = doc
                                showDoctorFilterDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            val pendingSum = filteredList.filter { it.entity.status == "PENDING" }.sumOf { it.entity.commissionAmount }
            val paidSum = filteredList.filter { it.entity.status == "PAID" }.sumOf { it.entity.commissionAmount }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total en attente", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("$${String.format("%.2f", pendingSum)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                }
                VerticalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f), modifier = Modifier.height(40.dp))
                Column {
                    Text("Total versé", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("$${String.format("%.2f", paidSum)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune commission enregistrée.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { comm ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                if (currentUser?.role == "ADMIN") {
                                    Text(
                                        text = comm.doctor?.fullName ?: "Médecin inconnu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Patient : ${comm.patient?.fullName ?: "N/A"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Ordonnance de base : $${String.format("%.2f", comm.entity.baseAmount)} • Taux : 5%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format("%.2f", comm.entity.commissionAmount)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = if (comm.entity.status == "PAID") Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (comm.entity.status == "PAID") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (comm.entity.status) {
                                            "PAID" -> "PAYÉ"
                                            "PENDING" -> "EN ATTENTE"
                                            else -> comm.entity.status
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = if (comm.entity.status == "PAID") Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentsView(
    payments: List<FullPayment>,
    allDoctors: List<DoctorEntity>,
    viewModel: PrescriptionViewModel
) {
    var showPaymentModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Administration des paiements",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = { showPaymentModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("process_payment_button")
            ) {
                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Traiter le paiement", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Historique des décaissements",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun historique de paiement enregistré.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(payments) { pm ->
                    val displayMethod = when (pm.entity.method) {
                        "Bank Transfer" -> "Virement bancaire"
                        "Mobile Money" -> "Mobile Money"
                        "Cheque" -> "Chèque"
                        else -> pm.entity.method
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = pm.doctor?.fullName ?: "Médecin inconnu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Date : ${pm.entity.paymentDate} • Réf : ${pm.entity.reference}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", pm.entity.amount)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Méthode : $displayMethod • Note : ${pm.entity.note}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPaymentModal) {
        ProcessPaymentDialog(
            allDoctors = allDoctors,
            onDismiss = { showPaymentModal = false },
            onSubmit = { doctorId, method, ref, note ->
                viewModel.processPayout(doctorId, method, ref, note)
                showPaymentModal = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessPaymentDialog(
    allDoctors: List<DoctorEntity>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var selectedDoctor by remember { mutableStateOf<DoctorEntity?>(null) }
    var method by remember { mutableStateOf("Bank Transfer") }
    var reference by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Verser le paiement de la commission",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = selectedDoctor?.fullName ?: "Sélectionner le médecin bénéficiaire")
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        allDoctors.forEach { doc ->
                            DropdownMenuItem(
                                text = { Text(doc.fullName) },
                                onClick = {
                                    selectedDoctor = doc
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Sélectionner la méthode :", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val methods = listOf("Bank Transfer", "Mobile Money", "Cheque")
                    methods.forEach { m ->
                        val isSelected = method == m
                        val displayMethodName = when (m) {
                            "Bank Transfer" -> "Virement bancaire"
                            "Mobile Money" -> "Mobile Money"
                            "Cheque" -> "Chèque"
                            else -> m
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { method = m }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayMethodName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Référence / Reçu de transaction") },
                    placeholder = { Text("TXN-1198223") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes de versement") },
                    placeholder = { Text("Paiement intégral du portefeuille en attente traité.") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedDoctor == null) {
                                errorMsg = "Le médecin bénéficiaire est obligatoire."
                            } else if (reference.isEmpty()) {
                                errorMsg = "La référence est obligatoire."
                            } else {
                                onSubmit(selectedDoctor!!.id, method, reference, note)
                            }
                        }
                    ) {
                        Text("Traiter le paiement")
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorWalletView(
    wallet: FullWallet?,
    payments: List<FullPayment>
) {
    var showRequestConfirmation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mon Portefeuille Numérique",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Solde actuel du portefeuille",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", wallet?.entity?.balance ?: 0.0)}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val earned = wallet?.entity?.totalEarned ?: 0.0
                    val paid = wallet?.entity?.totalPaid ?: 0.0
                    val ratio = if (earned > 0) (paid / earned).toFloat() else 0f

                    LinearProgressIndicator(
                        progress = ratio,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total payé : $${String.format("%.2f", paid)}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Gains totaux : $${String.format("%.2f", earned)}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showRequestConfirmation = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Demander un versement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Versements reçus",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (payments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun versement enregistré.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(payments) { pm ->
                val displayMethod = when (pm.entity.method) {
                    "Bank Transfer" -> "Virement bancaire"
                    "Mobile Money" -> "Mobile Money"
                    "Cheque" -> "Chèque"
                    else -> pm.entity.method
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Payé le ${pm.entity.paymentDate}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Réf : ${pm.entity.reference} • $displayMethod",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$${String.format("%.2f", pm.entity.amount)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }

    if (showRequestConfirmation) {
        Dialog(onDismissRequest = { showRequestConfirmation = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    Text("Versement demandé", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Votre demande a été enregistrée. L'équipe d'administration de l'hôpital a été notifiée pour traiter votre solde en attente de $${String.format("%.2f", wallet?.entity?.balance ?: 0.0)}.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { showRequestConfirmation = false }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    allUsers: List<UserEntity>,
    onLoginSuccess: (UserEntity) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_login_hero),
                        contentDescription = "Medical Dashboard Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Portail de Prescription",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Système de gestion et d'administration des commissions de médecins",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Form Inputs
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        errorMsg = ""
                    },
                    label = { Text("Adresse e-mail") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    placeholder = { Text("nom@hospital.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMsg.isNotEmpty()
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    placeholder = { Text("••••••••") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
                
                Button(
                    onClick = {
                        val matchedUser = allUsers.find { it.email.trim().lowercase() == email.trim().lowercase() }
                        if (matchedUser != null) {
                            onLoginSuccess(matchedUser)
                        } else {
                            errorMsg = "Identifiants incorrects ou utilisateur non trouvé."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Se connecter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Demo Accounts
                Text(
                    text = "Connexion rapide (Démonstration)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allUsers.forEach { user ->
                        val roleFrench = if (user.role == "ADMIN") "ADMINISTRATEUR" else "MÉDECIN"
                        val badgeColor = if (user.role == "ADMIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        val badgeTextColor = if (user.role == "ADMIN") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    email = user.email
                                    password = "password123"
                                    onLoginSuccess(user)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = user.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = user.email,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Surface(
                                    color = badgeColor,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = roleFrench,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = badgeTextColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyFinancialBarChart(
    payments: List<FullPayment>,
    commissions: List<FullCommission>,
    title: String,
    modifier: Modifier = Modifier
) {
    val monthsToDisplay = remember {
        val cal = java.util.Calendar.getInstance()
        val sdfKey = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        val sdfLabel = java.text.SimpleDateFormat("MMM yy", java.util.Locale.getDefault())
        val list = mutableListOf<Pair<String, String>>()
        for (i in 0..3) {
            val date = cal.time
            val key = sdfKey.format(date)
            var label = sdfLabel.format(date)
            label = label
                .replace("Jan", "Janv.")
                .replace("Feb", "Févr.")
                .replace("Mar", "Mars")
                .replace("Apr", "Avril")
                .replace("May", "Mai")
                .replace("Jun", "Juin")
                .replace("Jul", "Juil.")
                .replace("Aug", "Août")
                .replace("Sep", "Sept.")
                .replace("Oct", "Oct.")
                .replace("Nov", "Nov.")
                .replace("Dec", "Déc.")
            list.add(0, Pair(key, label))
            cal.add(java.util.Calendar.MONTH, -1)
        }
        list
    }

    val monthlyData = remember(payments, commissions, monthsToDisplay) {
        monthsToDisplay.map { (key, label) ->
            val paidAmount = payments
                .filter { it.entity.paymentDate.startsWith(key) }
                .sumOf { it.entity.amount }

            val pendingAmount = commissions
                .filter { 
                    it.entity.status == "PENDING" && 
                    (it.prescription?.prescriptionDate?.startsWith(key) == true || 
                     (it.prescription == null && java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date(it.entity.createdAt)) == key))
                }
                .sumOf { it.entity.commissionAmount }

            MonthlyStat(key, label, paidAmount, pendingAmount)
        }
    }

    val maxVal = remember(monthlyData) {
        val highest = monthlyData.maxOfOrNull { maxOf(it.paidAmount, it.pendingAmount) } ?: 0.0
        if (highest <= 0.0) 100.0 else highest * 1.15
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("financial_bar_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Versements payés vs commissions en attente",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) { index ->
                        val gridValue = maxVal * (3 - index) / 3
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${String.format("%.0f", gridValue)}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.width(36.dp)
                            )
                            Divider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 40.dp, end = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthlyData.forEach { stat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                val paidHeightPct = (stat.paidAmount / maxVal).toFloat().coerceIn(0f, 1f)
                                val animatedPaidHeightPct by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = paidHeightPct,
                                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 150f),
                                    label = "paid_height"
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (stat.paidAmount > 0) {
                                        Text(
                                            text = "$${String.format("%.0f", stat.paidAmount)}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .fillMaxHeight(animatedPaidHeightPct.coerceAtLeast(0.01f))
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF81C784), Color(0xFF2E7D32))
                                                )
                                            )
                                    )
                                }

                                val pendingHeightPct = (stat.pendingAmount / maxVal).toFloat().coerceIn(0f, 1f)
                                val animatedPendingHeightPct by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = pendingHeightPct,
                                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 150f),
                                    label = "pending_height"
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (stat.pendingAmount > 0) {
                                        Text(
                                            text = "$${String.format("%.0f", stat.pendingAmount)}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .fillMaxHeight(animatedPendingHeightPct.coerceAtLeast(0.01f))
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFFFFB74D), Color(0xFFE65100))
                                                )
                                            )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = stat.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2E7D32))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Versements reçus",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE65100))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Commissions en attente",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

data class MonthlyStat(
    val key: String,
    val label: String,
    val paidAmount: Double,
    val pendingAmount: Double
)

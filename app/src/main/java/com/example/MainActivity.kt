package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.signOut
import com.example.data.HisaabRepository
import com.example.export.StatementGenerator
import com.example.ui.HisaabViewModel
import com.example.ui.UiState
import com.example.ui.components.StatementDownloadSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HisaabHomeScreen
import com.example.ui.screens.UdhaarScreen
import com.example.ui.theme.SalaryHisaabTheme
import com.example.ui.theme.TealPrimary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AppTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String,
) {
    DASHBOARD(R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    HISAAB(R.string.nav_hisaab, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_hisaab"),
    CHARTS(R.string.nav_charts, Icons.Filled.DonutLarge, Icons.Outlined.DonutLarge, "nav_charts"),
    UDHAAR(R.string.nav_udhaar, Icons.Filled.Handshake, Icons.Outlined.Handshake, "nav_udhaar"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalaryHisaabTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { currentUser = auth.currentUser })
    } else {
        val uid = user.uid
        key(uid) {
            SalaryHisaabAppShell(
                currentUserId = uid,
                userName = user.displayName ?: "",
                userEmail = user.email ?: ""
            )
        }
    }
}

@Composable
private fun SalaryHisaabAppShell(
    currentUserId: String,
    userName: String,
    userEmail: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: HisaabViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                HisaabViewModel(HisaabRepository(db), currentUserId)
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showStatementSheet by remember { mutableStateOf(false) }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val data = (uiState as? UiState.Success)?.data
        if (uri != null && data != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            StatementGenerator.writePdfStatement(out, data)
                        } ?: error("File khul nahi paayi")
                    }
                }
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "PDF statement save ho gaya!"
                    else "PDF save nahi ho paaya: ${result.exceptionOrNull()?.localizedMessage ?: "error"}"
                )
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val data = (uiState as? UiState.Success)?.data
        if (uri != null && data != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            StatementGenerator.writeCsvStatement(out, data)
                        } ?: error("File khul nahi paayi")
                    }
                }
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "CSV statement save ho gaya!"
                    else "CSV save nahi ho paaya: ${result.exceptionOrNull()?.localizedMessage ?: "error"}"
                )
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BackHandler(enabled = activeTab != AppTab.DASHBOARD) {
        activeTab = AppTab.DASHBOARD
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 640.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                                )
                            )
                            .heightIn(min = 52.dp)
                            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("💸 Salary ")
                                withStyle(
                                    SpanStyle(
                                        brush = Brush.horizontalGradient(
                                            listOf(TealPrimary, Color(0xFFF59E0B))
                                        )
                                    )
                                ) {
                                    append("Hisaab")
                                }
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showStatementSheet = true },
                                enabled = uiState is UiState.Success,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("download_statement_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = stringResource(R.string.download_statement),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { showSignOutDialog = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = stringResource(R.string.sign_out),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.titleRes)
                                    )
                                },
                                label = { Text(stringResource(tab.titleRes)) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.titleRes)
                                    )
                                },
                                label = { Text(stringResource(tab.titleRes)) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (val state = uiState) {
                        UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is UiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Hisaab load karne mein dikkat aayi",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.selectMonth(viewModel.selectedMonth.value) }) {
                                    Text("Dobara koshish karein")
                                }
                            }
                        }
                        is UiState.Success -> {
                            val data = state.data
                            when (activeTab) {
                                AppTab.DASHBOARD -> DashboardScreen(
                                    data = data,
                                    userName = userName,
                                    onShiftMonth = viewModel::shiftMonth,
                                    onGoToHisaab = { activeTab = AppTab.HISAAB },
                                    onGoToCharts = { activeTab = AppTab.CHARTS },
                                    onGoToUdhaar = { activeTab = AppTab.UDHAAR },
                                    onOpenStatement = { showStatementSheet = true }
                                )
                                AppTab.HISAAB -> HisaabHomeScreen(
                                    data = data,
                                    isSaving = isSaving,
                                    onShiftMonth = viewModel::shiftMonth,
                                    onSaveSalary = { amt -> viewModel.saveMonthlySalary(data.selectedMonth, amt) },
                                    onAddTransaction = viewModel::addTransaction,
                                    onDeleteTransaction = viewModel::deleteTransaction,
                                    onOpenStatement = { showStatementSheet = true }
                                )
                                AppTab.CHARTS -> AnalyticsScreen(
                                    data = data,
                                    onSaveOpeningCash = viewModel::saveOpeningCash
                                )
                                AppTab.UDHAAR -> UdhaarScreen(
                                    data = data,
                                    isSaving = isSaving,
                                    onAddLoan = viewModel::addLoan,
                                    onAddLoanPayment = viewModel::addLoanPayment,
                                    onDeleteLoan = viewModel::deleteLoan
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.sign_out_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (userEmail.isNotBlank()) {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(stringResource(R.string.sign_out_confirm_msg))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        signOut(credentialManager, scope)
                    }
                ) {
                    Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val currentData = (uiState as? UiState.Success)?.data
    if (showStatementSheet && currentData != null) {
        StatementDownloadSheet(
            data = currentData,
            onDismiss = { showStatementSheet = false },
            onDownloadPdf = {
                showStatementSheet = false
                pdfLauncher.launch("Salary_Hisaab_${currentData.selectedMonth}.pdf")
            },
            onDownloadCsv = {
                showStatementSheet = false
                csvLauncher.launch("Salary_Hisaab_${currentData.selectedMonth}.csv")
            },
            onShareText = {
                showStatementSheet = false
                val summary = StatementGenerator.buildShareableSummaryText(currentData)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Salary Hisaab - ${HisaabViewModel.formatMonthLabel(currentData.selectedMonth)}")
                    putExtra(Intent.EXTRA_TEXT, summary)
                }
                context.startActivity(Intent.createChooser(intent, "Statement share karein"))
            }
        )
    }
}

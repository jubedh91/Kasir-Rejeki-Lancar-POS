package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenDestination
import com.example.model.UserRole
import com.example.ui.components.DigitalReceiptDialog
import com.example.ui.components.PinLoginDialog
import com.example.ui.components.PosDrawerContent
import com.example.ui.components.PosNavigationRail
import com.example.ui.components.PosTopAppBar
import com.example.ui.screens.KasbonBookScreen
import com.example.ui.screens.LoginPinScreen
import com.example.ui.screens.MasterProductScreen
import com.example.ui.screens.PosCashierScreen
import com.example.ui.screens.SupplierPurchaseScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PosViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: PosViewModel = viewModel()
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: PosViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val showOwnerPinDialog by viewModel.showOwnerPinRequiredDialog.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val isFavoriteOnly by viewModel.isFavoriteOnly.collectAsState()
    var showUserSwitcherModal by remember { mutableStateOf(false) }

    LaunchedEffect(statusMsg) {
        statusMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Handle Android system back button
    BackHandler(enabled = currentScreen != ScreenDestination.POS || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo(ScreenDestination.POS)
        }
    }

    if (currentUser == null) {
        LoginPinScreen(viewModel = viewModel)
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletExpanded = maxWidth > 840.dp

            if (isTabletExpanded) {
                // Wide Screen / Tablet Layout: Left Navigation Rail + Content Area
                Row(modifier = Modifier.fillMaxSize()) {
                    PosNavigationRail(
                        currentScreen = currentScreen,
                        currentUser = currentUser,
                        onNavigate = { viewModel.navigateTo(it) },
                        modifier = Modifier.fillMaxHeight()
                    )

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        topBar = {
                            PosTopAppBar(
                                currentScreen = currentScreen,
                                currentUser = currentUser,
                                cartCount = cartItemCount,
                                isFavoriteOnly = isFavoriteOnly,
                                onToggleFavorite = { viewModel.toggleFavoriteFilter() },
                                onMenuClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                onUserBadgeClick = { showUserSwitcherModal = true },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            ScreenContent(screen = currentScreen, viewModel = viewModel)
                        }
                    }
                }
            } else {
                // Standard Mobile Layout with Navigation Drawer matching screenshot
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        PosDrawerContent(
                            currentScreen = currentScreen,
                            currentUser = currentUser,
                            onNavigate = { dest ->
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(dest)
                            },
                            onSwitchUser = {
                                scope.launch { drawerState.close() }
                                showUserSwitcherModal = true
                            }
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            PosTopAppBar(
                                currentScreen = currentScreen,
                                currentUser = currentUser,
                                cartCount = cartItemCount,
                                isFavoriteOnly = isFavoriteOnly,
                                onToggleFavorite = { viewModel.toggleFavoriteFilter() },
                                onMenuClick = {
                                    scope.launch { drawerState.open() }
                                },
                                onUserBadgeClick = { showUserSwitcherModal = true },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            ScreenContent(screen = currentScreen, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    // Digital Receipt Modal
    activeReceipt?.let { receipt ->
        DigitalReceiptDialog(
            struk = receipt,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // Owner PIN Protection Dialog (when Kasir attempts to open Owner-restricted features)
    if (showOwnerPinDialog) {
        PinLoginDialog(
            onDismiss = { viewModel.dismissOwnerPinDialog() },
            onPinEntered = { pin -> viewModel.unlockOwnerWithPin(pin) },
            title = "Akses Khusus Owner",
            subtitle = "Fitur ini dikunci. Masukkan PIN Owner (5461) untuk membuka."
        )
    }

    // User Switcher Dialog
    if (showUserSwitcherModal) {
        AlertDialog(
            onDismissRequest = { showUserSwitcherModal = false },
            title = { Text("Kelola Akun Akses Kasir") },
            text = {
                Column {
                    Text(
                        text = "Akun aktif: ${currentUser?.displayName} (${currentUser?.roleBadge})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Pilih akun untuk beralih:",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.switchRole(UserRole.CASHIER)
                            showUserSwitcherModal = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Beralih ke Kasir (PIN: 1234)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.switchRole(UserRole.OWNER)
                            showUserSwitcherModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Beralih ke Owner (PIN: 5461)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            viewModel.logout()
                            showUserSwitcherModal = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout / Kunci Kasir")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showUserSwitcherModal = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun ScreenContent(
    screen: ScreenDestination,
    viewModel: PosViewModel
) {
    when (screen) {
        ScreenDestination.POS -> PosCashierScreen(viewModel = viewModel)
        ScreenDestination.MASTER_PRODUCTS -> MasterProductScreen(viewModel = viewModel)
        ScreenDestination.SUPPLIER_PURCHASE -> SupplierPurchaseScreen(viewModel = viewModel)
        ScreenDestination.KASBON_BOOK -> KasbonBookScreen(viewModel = viewModel)
        ScreenDestination.HISTORY -> TransactionHistoryScreen(viewModel = viewModel)
    }
}

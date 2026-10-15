package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.KasbonEntity
import com.example.data.entity.KasbonPaymentEntity
import com.example.model.FormatUtils
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenPrimaryContainer
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.viewmodel.PosViewModel

@Composable
fun KasbonBookScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allKasbon by viewModel.allKasbon.collectAsState()
    val activeTab by viewModel.kasbonTab.collectAsState()

    val piutangList = remember(allKasbon) { allKasbon.filter { it.type == "PIUTANG" } }
    val hutangList = remember(allKasbon) { allKasbon.filter { it.type == "HUTANG" } }

    val totalActivePiutang = remember(piutangList) {
        piutangList.filter { !it.isSettled }.sumOf { it.remainingAmount }
    }
    val totalActiveHutang = remember(hutangList) {
        hutangList.filter { !it.isSettled }.sumOf { it.remainingAmount }
    }

    var selectedKasbonForPayment by remember { mutableStateOf<KasbonEntity?>(null) }
    var showManualKasbonDialog by remember { mutableStateOf(false) }

    val currentList = if (activeTab == "PIUTANG") piutangList else hutangList

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Summary Cards Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Sisa Piutang Pelanggan", fontSize = 11.sp, color = GreenPrimary)
                        Text(
                            FormatUtils.formatRupiah(totalActivePiutang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                        Text("${piutangList.count { !it.isSettled }} belum lunas", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Sisa Hutang ke Supplier", fontSize = 11.sp, color = GoldTertiary)
                        Text(
                            FormatUtils.formatRupiah(totalActiveHutang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldTertiary
                        )
                        Text("${hutangList.count { !it.isSettled }} belum lunas", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = if (activeTab == "PIUTANG") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == "PIUTANG",
                    onClick = { viewModel.kasbonTab.value = "PIUTANG" },
                    text = {
                        Text(
                            "Piutang Pelanggan (${piutangList.size})",
                            fontWeight = if (activeTab == "PIUTANG") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = activeTab == "HUTANG",
                    onClick = { viewModel.kasbonTab.value = "HUTANG" },
                    text = {
                        Text(
                            "Hutang Supplier (${hutangList.size})",
                            fontWeight = if (activeTab == "HUTANG") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Records
            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (activeTab == "PIUTANG") "Belum ada catatan piutang pelanggan" else "Belum ada catatan hutang supplier",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(currentList, key = { it.id }) { item ->
                        KasbonCard(
                            item = item,
                            onPayClick = { selectedKasbonForPayment = item }
                        )
                    }
                }
            }
        }

        // FAB Add Manual Kasbon
        FloatingActionButton(
            onClick = { showManualKasbonDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_kasbon_fab"),
            containerColor = if (activeTab == "PIUTANG") GreenPrimary else GoldTertiary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Kasbon")
        }
    }

    // Payment Dialog for Installments
    if (selectedKasbonForPayment != null) {
        val kasbon = selectedKasbonForPayment!!
        KasbonPaymentDialog(
            kasbon = kasbon,
            viewModel = viewModel,
            onDismiss = { selectedKasbonForPayment = null },
            onPaymentDone = { amount, notes ->
                viewModel.payKasbonInstallment(kasbon.id, amount, notes)
                selectedKasbonForPayment = null
            }
        )
    }

    // Add Manual Kasbon Dialog
    if (showManualKasbonDialog) {
        ManualKasbonDialog(
            defaultType = activeTab,
            onDismiss = { showManualKasbonDialog = false },
            onSave = { type, party, phone, amount, dueDays, notes ->
                viewModel.addManualKasbon(type, party, phone, amount, dueDays, notes)
                showManualKasbonDialog = false
            }
        )
    }
}

@Composable
fun KasbonCard(
    item: KasbonEntity,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDuePassed = item.dueDate in 1..System.currentTimeMillis() && !item.isSettled

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kasbon_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Party Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.partyName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (item.partyPhone.isNotBlank()) {
                        Text(
                            text = "Telp: ${item.partyPhone}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    color = if (item.isSettled) StatusSuccess.copy(alpha = 0.15f)
                    else if (isDuePassed) StatusDanger.copy(alpha = 0.15f)
                    else StatusWarning.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDuePassed) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = if (item.isSettled) "LUNAS" else if (isDuePassed) "JATUH TEMPO" else "BELUM LUNAS",
                            color = if (item.isSettled) StatusSuccess else if (isDuePassed) StatusDanger else StatusWarning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ref: ${item.referenceNumber} • Tanggal: ${FormatUtils.formatDateOnly(item.createdDate)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )

            if (item.dueDate > 0) {
                Text(
                    text = "Jatuh Tempo: ${FormatUtils.formatDateOnly(item.dueDate)}",
                    fontSize = 11.sp,
                    color = if (isDuePassed) StatusDanger else StatusWarning,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Amount breakdown & Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total: ${FormatUtils.formatRupiah(item.totalAmount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "Sisa: ${FormatUtils.formatRupiah(item.remainingAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isSettled) StatusSuccess else StatusDanger
                        )
                    )
                }

                if (!item.isSettled) {
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bayar Cicilan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lunas Penuh", color = StatusSuccess, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun KasbonPaymentDialog(
    kasbon: KasbonEntity,
    viewModel: PosViewModel,
    onDismiss: () -> Unit,
    onPaymentDone: (Double, String) -> Unit
) {
    var paymentAmountText by remember { mutableStateOf(kasbon.remainingAmount.toLong().toString()) }
    var notes by remember { mutableStateOf("") }

    val paymentsFlow = remember(kasbon.id) { viewModel.repository.getPaymentsForKasbon(kasbon.id) }
    val paymentHistory by paymentsFlow.collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Bayar Cicilan: ${kasbon.partyName}")
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Sisa Tagihan Saat Ini: ${FormatUtils.formatRupiah(kasbon.remainingAmount)}", fontWeight = FontWeight.Bold, color = StatusDanger)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Jumlah Pembayaran (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Shortcut buttons (Bayar Lunas, 50%, etc.)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { paymentAmountText = kasbon.remainingAmount.toLong().toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text("Bayar Lunas", fontSize = 10.sp)
                        }
                        if (kasbon.remainingAmount >= 20000) {
                            val half = (kasbon.remainingAmount / 2).toLong()
                            OutlinedButton(
                                onClick = { paymentAmountText = half.toString() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("50% (${(half / 1000)}k)", fontSize = 10.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Keterangan Pembayaran") },
                        placeholder = { Text("e.g. Dititipkan tunai oleh Pak Budi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (paymentHistory.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Riwayat Cicilan Sebelumnya (${paymentHistory.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    items(paymentHistory) { pay ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(FormatUtils.formatDate(pay.paymentDate), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                if (pay.notes.isNotBlank()) {
                                    Text(pay.notes, fontSize = 10.sp)
                                }
                            }
                            Text(FormatUtils.formatRupiah(pay.amount), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusSuccess)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onPaymentDone(amount, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = (paymentAmountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Simpan Pembayaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ManualKasbonDialog(
    defaultType: String,
    onDismiss: () -> Unit,
    onSave: (type: String, partyName: String, partyPhone: String, amount: Double, dueDays: Int, notes: String) -> Unit
) {
    var type by remember { mutableStateOf(defaultType) }
    var partyName by remember { mutableStateOf("") }
    var partyPhone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDays by remember { mutableStateOf(7) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Catat ${if (type == "PIUTANG") "Piutang Pelanggan" else "Hutang Supplier"} Baru")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "PIUTANG" },
                        colors = if (type == "PIUTANG") ButtonDefaults.buttonColors(containerColor = GreenPrimary) else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Piutang", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { type = "HUTANG" },
                        colors = if (type == "HUTANG") ButtonDefaults.buttonColors(containerColor = GoldTertiary) else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hutang", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (type == "PIUTANG") "Nama Pelanggan" else "Nama Supplier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = partyPhone,
                    onValueChange = { partyPhone = it },
                    label = { Text("No. HP / WA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Nominal Kasbon (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Jatuh Tempo:", fontSize = 11.sp)
                    listOf(7, 14, 30).forEach { d ->
                        OutlinedButton(
                            onClick = { dueDays = d },
                            colors = if (dueDays == d) ButtonDefaults.buttonColors(containerColor = GreenPrimary) else ButtonDefaults.outlinedButtonColors(),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("$d Hari", fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan Kasbon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (partyName.isNotBlank() && amt > 0) {
                        onSave(type, partyName, partyPhone, amt, dueDays, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = partyName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

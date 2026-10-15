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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.example.data.entity.ProductEntity
import com.example.data.entity.SupplierPurchaseEntity
import com.example.model.FormatUtils
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenPrimaryContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.viewmodel.PosViewModel

@Composable
fun SupplierPurchaseScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val allPurchases by viewModel.allPurchases.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Input Stok Masuk, 1: Riwayat Pembelian

    // Form State for Stock In
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var isHutang by remember { mutableStateOf(false) }
    var dueDays by remember { mutableStateOf(14) }
    var notes by remember { mutableStateOf("") }

    // List of items in current purchase: Product -> (qty, buyPrice)
    var purchaseItems by remember { mutableStateOf<List<Pair<ProductEntity, Pair<Int, Double>>>>(emptyList()) }
    var showSelectProductDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tab Selector
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Input Stok Masuk", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Riwayat Pembelian (${allPurchases.size})", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // Form Input Stok Masuk
            val totalPurchase = purchaseItems.sumOf { (_, pair) -> pair.first * pair.second }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Informasi Supplier", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            label = { Text("Nama Supplier / Pabrik") },
                            placeholder = { Text("e.g. PT Maju Plastik") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("supplier_name_input")
                        )
                        OutlinedTextField(
                            value = supplierPhone,
                            onValueChange = { supplierPhone = it },
                            label = { Text("No. Telp / WA") },
                            placeholder = { Text("0812-xxxx") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }

                item {
                    // Payment Method (Tunai vs Hutang)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isHutang = false },
                            colors = if (!isHutang) ButtonDefaults.buttonColors(containerColor = GreenPrimary) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Bayar Tunai")
                        }
                        Button(
                            onClick = { isHutang = true },
                            colors = if (isHutang) ButtonDefaults.buttonColors(containerColor = GoldTertiary) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Hutang Supplier (Tempo)")
                        }
                    }
                }

                if (isHutang) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Jatuh Tempo Faktur:", fontSize = 12.sp, color = Color(0xFF5D4037))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(7, 14, 30, 45).forEach { d ->
                                        OutlinedButton(
                                            onClick = { dueDays = d },
                                            colors = if (dueDays == d) ButtonDefaults.buttonColors(containerColor = GoldTertiary) else ButtonDefaults.outlinedButtonColors(),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("$d Hari", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Barang Masuk (${purchaseItems.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { showSelectProductDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Tambah Barang", fontSize = 12.sp)
                        }
                    }
                }

                if (purchaseItems.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada barang yang dipilih untuk stok masuk",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(purchaseItems) { (product, pair) ->
                        val qty = pair.first
                        val buyPrice = pair.second
                        val subtotal = qty * buyPrice

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("SKU: ${product.sku} • Stok Saat Ini: ${product.stock} ${product.unit}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Tambah: $qty ${product.unit} x ${FormatUtils.formatRupiah(buyPrice)} = ${FormatUtils.formatRupiah(subtotal)}",
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary,
                                        fontSize = 12.sp
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        purchaseItems = purchaseItems.filter { it.first.id != product.id }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Penerimaan Stok / No. Faktur Surat Jalan") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Total & Save Button
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenPrimaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Biaya Pembelian:", fontWeight = FontWeight.SemiBold)
                                Text(
                                    FormatUtils.formatRupiah(totalPurchase),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.recordSupplierPurchase(
                                        supplierName = supplierName,
                                        supplierPhone = supplierPhone,
                                        isHutang = isHutang,
                                        dueDays = dueDays,
                                        notes = notes,
                                        purchaseItems = purchaseItems
                                    )
                                    // Reset form
                                    supplierName = ""
                                    supplierPhone = ""
                                    purchaseItems = emptyList()
                                    notes = ""
                                    activeTab = 1
                                },
                                enabled = supplierName.isNotBlank() && purchaseItems.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_stock_in_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Text("Simpan & Tambah Stok Otomatis", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Riwayat Pembelian Supplier
            if (allPurchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat pembelian supplier", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(allPurchases, key = { it.id }) { po ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = po.purchaseNumber,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = GreenPrimary
                                    )

                                    val isPaid = po.paymentMethod == "TUNAI" || po.paymentStatus == "LUNAS"
                                    Surface(
                                        color = if (isPaid) StatusSuccess.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isPaid) "TUNAI (LUNAS)" else "HUTANG SUPPLIER",
                                            color = if (isPaid) StatusSuccess else StatusWarning,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Supplier: ${po.supplierName} ${if (po.supplierPhone.isNotBlank()) "(${po.supplierPhone})" else ""}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Tanggal Masuk: ${FormatUtils.formatDate(po.timestamp)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                if (po.dueDate > 0) {
                                    Text(
                                        text = "Jatuh Tempo: ${FormatUtils.formatDateOnly(po.dueDate)}",
                                        fontSize = 11.sp,
                                        color = StatusWarning,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Pembelian:", fontSize = 12.sp)
                                    Text(
                                        FormatUtils.formatRupiah(po.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to Select Product to add to purchase
    if (showSelectProductDialog) {
        SelectProductForStockInDialog(
            products = allProducts,
            onDismiss = { showSelectProductDialog = false },
            onAdd = { product, qty, buyPrice ->
                purchaseItems = purchaseItems + Pair(product, Pair(qty, buyPrice))
                showSelectProductDialog = false
            }
        )
    }
}

@Composable
fun SelectProductForStockInDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAdd: (ProductEntity, Int, Double) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
    var qtyText by remember { mutableStateOf("10") }
    var buyPriceText by remember { mutableStateOf(selectedProduct?.buyPrice?.toLong()?.toString() ?: "10000") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pilih Barang Masuk",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            Text("Pilih dari Master Barang:", style = MaterialTheme.typography.labelSmall)
            LazyColumn(modifier = Modifier.height(180.dp)) {
                items(products) { prod ->
                    val isSelected = selectedProduct?.id == prod.id
                    Surface(
                        onClick = {
                            selectedProduct = prod
                            buyPriceText = prod.buyPrice.toLong().toString()
                        },
                        color = if (isSelected) GreenPrimaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(prod.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                Text("SKU: ${prod.sku} • Stok: ${prod.stock} ${prod.unit}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Text(FormatUtils.formatRupiah(prod.buyPrice), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            selectedProduct?.let { prod ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qty Masuk (${prod.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = buyPriceText,
                        onValueChange = { buyPriceText = it },
                        label = { Text("Harga Beli Satuan (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Batal") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val prod = selectedProduct
                        val qty = qtyText.toIntOrNull() ?: 0
                        val price = buyPriceText.toDoubleOrNull() ?: 0.0
                        if (prod != null && qty > 0 && price > 0) {
                            onAdd(prod, qty, price)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = selectedProduct != null && (qtyText.toIntOrNull() ?: 0) > 0
                ) {
                    Text("Tambahkan")
                }
            }
        }
    }
}

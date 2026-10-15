package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.model.CartItem
import com.example.model.FormatUtils
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenPrimaryContainer
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.viewmodel.PosViewModel

enum class ProductSortOrder(val displayName: String) {
    DEFAULT("Urutan Asli"),
    NAME_AZ("Nama A-Z"),
    NAME_ZA("Nama Z-A"),
    STOCK_HIGH("Stok Terbanyak"),
    STOCK_LOW("Stok Sedikit"),
    PRICE_LOW("Harga Termurah"),
    PRICE_HIGH("Harga Termahal")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosCashierScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProductsList by viewModel.allProducts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val isFavoriteOnly by viewModel.isFavoriteOnly.collectAsState()
    val taxPercent by viewModel.taxPercent.collectAsState()

    var showSearchField by remember { mutableStateOf(true) }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(ProductSortOrder.DEFAULT) }

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showTaxDialog by remember { mutableStateOf(false) }
    var showPpobDialog by remember { mutableStateOf(false) }

    var showPaymentModal by remember { mutableStateOf(false) }

    // Distinct categories matching screenshot style
    val categories = remember(allProductsList) {
        listOf("Semua") + allProductsList.map { it.category.lowercase() }.distinct().sorted()
    }

    // Filter and sort products
    val displayProducts = remember(allProductsList, searchQuery, selectedCategory, currentSort, isFavoriteOnly) {
        val filtered = allProductsList.filter { product ->
            val matchQuery = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.sku.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == "Semua" ||
                    product.category.equals(selectedCategory, ignoreCase = true)
            val matchFavorite = !isFavoriteOnly || product.stock > 10 ||
                    product.category.contains("kresek", ignoreCase = true) ||
                    product.category.contains("mika", ignoreCase = true)
            matchQuery && matchCategory && matchFavorite
        }

        when (currentSort) {
            ProductSortOrder.DEFAULT -> filtered
            ProductSortOrder.NAME_AZ -> filtered.sortedBy { it.name.lowercase() }
            ProductSortOrder.NAME_ZA -> filtered.sortedByDescending { it.name.lowercase() }
            ProductSortOrder.STOCK_HIGH -> filtered.sortedByDescending { it.stock }
            ProductSortOrder.STOCK_LOW -> filtered.sortedBy { it.stock }
            ProductSortOrder.PRICE_LOW -> filtered.sortedBy { it.sellPrice }
            ProductSortOrder.PRICE_HIGH -> filtered.sortedByDescending { it.sellPrice }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp) // reserve space for bottom capsule bar
        ) {
            // 1. Action / Utility Row (Matching screenshot icons: Search, +, Barcode frame, Barcode lines, %, TAX, PPOB)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Search toggle icon
                IconButton(
                    onClick = { showSearchField = !showSearchField },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Barang",
                        tint = if (showSearchField || searchQuery.isNotBlank()) GreenPrimary else Color(0xFF4A5568),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Add item icon
                IconButton(
                    onClick = { showQuickAddDialog = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Barang",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Barcode scanner frame icon
                IconButton(
                    onClick = { showBarcodeDialog = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode / SKU",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Barcode lines icon
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Mode input barcode aktif. Ketik atau scan kode SKU barang.", Toast.LENGTH_SHORT).show()
                        showSearchField = true
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewWeek,
                        contentDescription = "Barcode",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Discount icon %
                IconButton(
                    onClick = { showDiscountDialog = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sell,
                        contentDescription = "Diskon",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // TAX button
                Surface(
                    onClick = { showTaxDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    color = if (taxPercent > 0.0) GreenPrimaryContainer else Color.Transparent,
                    border = if (taxPercent > 0.0) androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary) else null,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = if (taxPercent > 0.0) "TAX ${taxPercent.toInt()}%" else "TAX",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (taxPercent > 0.0) GreenPrimary else Color(0xFF4A5568),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                // PPOB text button
                Surface(
                    onClick = { showPpobDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    color = GreenPrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "PPOB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GreenPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // Expandable Search Bar (when user taps magnifying glass or barcode)
            AnimatedVisibility(
                visible = showSearchField || searchQuery.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.posSearchQuery.value = it },
                    placeholder = { Text("Ketik nama barang atau kode SKU...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreenPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.posSearchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // 2. Filter & Sort Row (Sort icon + horizontal scrollable category chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort Icon with Dropdown Menu
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Urutkan",
                            tint = GreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        ProductSortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        order.displayName,
                                        fontWeight = if (currentSort == order) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentSort == order) GreenPrimary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    currentSort = order
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Category chips with clean thin borders matching the screenshot
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(end = 12.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category.equals(selectedCategory, ignoreCase = true)

                        Surface(
                            onClick = { viewModel.selectedCategory.value = category },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GreenPrimaryContainer.copy(alpha = 0.5f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) GreenPrimary else Color(0xFF26A69A).copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) GreenPrimary else Color(0xFF00796B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Clean Vertical Item List (Matching Screenshot exactly!)
            if (displayProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada barang ditemukan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(displayProducts, key = { it.id }) { product ->
                        val inCart = cartItems.find { it.product.id == product.id }
                        SimpleProductRow(
                            product = product,
                            inCartQty = inCart?.qty ?: 0,
                            onClick = { viewModel.addToCart(product) },
                            onIncrease = { viewModel.addToCart(product) },
                            onDecrease = {
                                val current = inCart?.qty ?: 0
                                if (current > 0) viewModel.updateCartQty(product.id, current - 1)
                            }
                        )
                    }
                }
            }
        }

        // 4. Floating Bottom Bar (Matching Signature Kasir Pintar / Modern POS screenshot)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Green Capsule Pill Button: [ (0) Barang          LANJUT > ]
            Surface(
                onClick = {
                    if (cartItemCount > 0) {
                        showPaymentModal = true
                    } else {
                        Toast.makeText(context, "Keranjang masih kosong. Pilih barang terlebih dahulu.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .testTag("floating_cart_pill"),
                color = Color(0xFF008955), // Signature deep vibrant emerald green
                tonalElevation = 6.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left circle with item count + "Barang"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cartItemCount",
                                color = Color(0xFF008955),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Barang",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (cartTotal > 0) {
                                Text(
                                    text = FormatUtils.formatRupiah(cartTotal),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Right: "LANJUT >"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LANJUT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Lanjut",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Customer Selector Button on the right (Icon Person)
            Surface(
                onClick = { showCustomerDialog = true },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF008955), RoundedCornerShape(16.dp)),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = "Pilih Pelanggan",
                        tint = Color(0xFF008955),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }

    // Modal / Dialog: Customer Assignment
    if (showCustomerDialog) {
        CustomerSelectorDialog(
            currentName = customerName,
            onDismiss = { showCustomerDialog = false },
            onSave = { name, phone ->
                viewModel.customerName.value = name
                viewModel.customerPhone.value = phone
                showCustomerDialog = false
                Toast.makeText(context, "Pelanggan diset: $name", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal / Dialog: Barcode & SKU Quick lookup
    if (showBarcodeDialog) {
        BarcodeLookupDialog(
            products = allProductsList,
            onDismiss = { showBarcodeDialog = false },
            onSelect = { prod ->
                viewModel.addToCart(prod)
                showBarcodeDialog = false
                Toast.makeText(context, "${prod.name} ditambahkan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal / Dialog: Global Discount
    if (showDiscountDialog) {
        DiscountDialog(
            cartTotal = cartTotal,
            currentDiscount = viewModel.discountRupiah.collectAsState().value,
            onDismiss = { showDiscountDialog = false },
            onApply = { discount ->
                viewModel.discountRupiah.value = discount
                showDiscountDialog = false
                Toast.makeText(context, "Diskon diterapkan: ${FormatUtils.formatRupiah(discount)}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal / Dialog: Quick Add Custom Product
    if (showQuickAddDialog) {
        QuickAddProductDialog(
            onDismiss = { showQuickAddDialog = false },
            onSave = { name, sku, cat, price, stock ->
                viewModel.saveProduct(
                    sku = sku,
                    name = name,
                    category = cat,
                    unit = "pcs",
                    buyPrice = price * 0.75,
                    sellPrice = price,
                    stock = stock,
                    minStock = 5
                )
                showQuickAddDialog = false
                Toast.makeText(context, "$name berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal / Dialog: TAX selection
    if (showTaxDialog) {
        TaxSelectorDialog(
            currentTax = taxPercent,
            onDismiss = { showTaxDialog = false },
            onApply = { percent ->
                viewModel.setTaxPercent(percent)
                showTaxDialog = false
            }
        )
    }

    // Modal / Dialog: PPOB & Pulsa selection
    if (showPpobDialog) {
        PpobSelectorDialog(
            onDismiss = { showPpobDialog = false },
            onSelectProduct = { name, price ->
                viewModel.addQuickSaleItem(name, price)
                showPpobDialog = false
            }
        )
    }

    // Full Checkout & Payment Calculator Sheet (Triggered by LANJUT >)
    if (showPaymentModal) {
        PaymentCalculatorDialog(
            viewModel = viewModel,
            cartTotal = cartTotal,
            onDismiss = { showPaymentModal = false },
            onPaymentSuccess = {
                showPaymentModal = false
            }
        )
    }
}

/**
 * Clean Single-Row Product Item (Matching Screenshot)
 * Left: Rounded Avatar with 2-letter abbreviation
 * Center: Title + Subtitle ("sisa 199 • Rp 2.500")
 * Right: In-cart quantity or stepper
 */
@Composable
fun SimpleProductRow(
    product: ProductEntity,
    inCartQty: Int,
    onClick: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val abbreviation = remember(product.name) {
        val words = product.name.trim().split(" ")
        if (words.size >= 2) {
            "${words[0].take(1).uppercase()}${words[1].take(1).lowercase()}"
        } else {
            product.name.take(2).capitalize()
        }
    }

    val isOutOfStock = product.stock <= 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock) { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Avatar (Soft pastel background with 2 letters)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8EEF4)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = abbreviation,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center: Title + Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isOutOfStock) Color(0xFF94A3B8) else Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle: "sisa 199 • Rp 2.500"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "sisa ${product.stock}",
                    fontSize = 12.sp,
                    color = if (isOutOfStock) StatusDanger else Color(0xFF64748B),
                    fontWeight = if (isOutOfStock) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = " • ",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = FormatUtils.formatRupiah(product.sellPrice),
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Right side: In-cart Stepper or Indicator
        if (inCartQty > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onDecrease,
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Kurang",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF475569)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GreenPrimary,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "$inCartQty",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    onClick = onIncrease,
                    shape = CircleShape,
                    color = GreenPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            modifier = Modifier.size(14.dp),
                            tint = GreenPrimary
                        )
                    }
                }
            }
        }
    }
}

// Dialog to set Customer Name / Kasbon
@Composable
fun CustomerSelectorDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf("") }

    val quickCustomers = listOf(
        "Umum (Tanpa Nama)",
        "Warung Bu Siti",
        "Kedai Es Teh Melati",
        "Katering Berkah",
        "Bakso Mas Joko"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih / Ubah Pelanggan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pelanggan") },
                    placeholder = { Text("e.g. Warung Bu Siti") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor HP / WhatsApp") },
                    placeholder = { Text("0812-xxxx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("Langganan Cepat:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickCustomers) { cust ->
                        Surface(
                            onClick = { name = if (cust.startsWith("Umum")) "" else cust },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = cust,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// Barcode & SKU lookup dialog
@Composable
fun BarcodeLookupDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onSelect: (ProductEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val matched = products.filter {
        it.sku.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan / Cari SKU Barcode", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Ketik SKU / Kode Barcode") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = GreenPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Hasil Pencarian (${matched.size}):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyColumn(modifier = Modifier.height(180.dp)) {
                    items(matched) { prod ->
                        Surface(
                            onClick = { onSelect(prod) },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(prod.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("SKU: ${prod.sku} • Stok: ${prod.stock}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Text(FormatUtils.formatRupiah(prod.sellPrice), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GreenPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

// Discount dialog
@Composable
fun DiscountDialog(
    cartTotal: Double,
    currentDiscount: Double,
    onDismiss: () -> Unit,
    onApply: (Double) -> Unit
) {
    var discountText by remember { mutableStateOf(if (currentDiscount > 0) currentDiscount.toLong().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Diskon Penjualan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total Tagihan Saat Ini: ${FormatUtils.formatRupiah(cartTotal)}", fontSize = 12.sp)
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Nominal Diskon (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1000.0, 2000.0, 5000.0, 10000.0).forEach { disc ->
                        OutlinedButton(
                            onClick = { discountText = disc.toLong().toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text("${(disc / 1000).toInt()}k", fontSize = 10.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val disc = discountText.toDoubleOrNull() ?: 0.0
                    onApply(disc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// Quick Add product dialog
@Composable
fun QuickAddProductDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, sku: String, category: String, price: Double, stock: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("kantong kresek") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Barang Kilat", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (sku.isBlank()) {
                            sku = it.take(3).uppercase() + "-" + (100..999).random()
                        }
                    },
                    label = { Text("Nama Barang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("Kode SKU") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Harga Jual (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Jumlah Stok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val stock = stockText.toIntOrNull() ?: 10
                    if (name.isNotBlank() && price > 0) {
                        onSave(name, sku, category, price, stock)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = name.isNotBlank() && (priceText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// Dialog for Tax selection
@Composable
fun TaxSelectorDialog(
    currentTax: Double,
    onDismiss: () -> Unit,
    onApply: (Double) -> Unit
) {
    var selectedPercent by remember { mutableStateOf(currentTax) }

    val options = listOf(
        Pair(0.0, "Non-PPN (0%) - Bebas Pajak"),
        Pair(11.0, "PPN 11% (Standar Pajak Resmi)"),
        Pair(5.0, "Pajak Daerah / PB1 (5%)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan Pajak Penjualan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pilih tarif pajak yang berlaku untuk transaksi ini:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                options.forEach { (percent, label) ->
                    val isSelected = selectedPercent == percent
                    Surface(
                        onClick = { selectedPercent = percent },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) GreenPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GreenPrimary else Color.Unspecified
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(selectedPercent) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// Dialog for PPOB Quick Transaction
@Composable
fun PpobSelectorDialog(
    onDismiss: () -> Unit,
    onSelectProduct: (name: String, price: Double) -> Unit
) {
    val ppobItems = listOf(
        Pair("Token PLN 20.000", 22500.0),
        Pair("Token PLN 50.000", 52500.0),
        Pair("Token PLN 100.000", 102500.0),
        Pair("Pulsa Telkomsel 10.000", 12000.0),
        Pair("Pulsa Telkomsel 25.000", 27000.0),
        Pair("Pulsa Indosat 25.000", 27000.0),
        Pair("Pulsa XL 25.000", 27000.0),
        Pair("Paket Data Internet 5GB", 35000.0)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Layanan PPOB & Pulsa", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pilih item PPOB untuk dimasukkan ke kasir secara langsung:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(ppobItems) { (name, price) ->
                        Surface(
                            onClick = { onSelectProduct(name, price) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    FormatUtils.formatRupiah(price),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCalculatorDialog(
    viewModel: PosViewModel,
    cartTotal: Double,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val custName by viewModel.customerName.collectAsState()
    val custPhone by viewModel.customerPhone.collectAsState()
    val discountVal by viewModel.discountRupiah.collectAsState()
    val taxPercentVal by viewModel.taxPercent.collectAsState()
    val cashPaid by viewModel.cashPaidAmount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val notes by viewModel.checkoutNotes.collectAsState()

    val afterDiscount = (cartTotal - discountVal).coerceAtLeast(0.0)
    val taxAmount = afterDiscount * (taxPercentVal / 100.0)
    val finalTotal = afterDiscount + taxAmount
    val changeAmount = if (paymentMethod == "TUNAI") (cashPaid - finalTotal).coerceAtLeast(0.0) else 0.0
    val isEnoughCash = paymentMethod != "TUNAI" || cashPaid >= finalTotal

    var rawInputCash by remember { mutableStateOf(if (cashPaid > 0) cashPaid.toLong().toString() else "") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxHeight(0.92f)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GreenPrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ringkasan & Pembayaran",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${cartItems.sumOf { it.qty }} barang dipilih",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "Tutup")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cart Items breakdown
                item {
                    Text("Daftar Belanja:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(cartItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${FormatUtils.formatRupiah(item.product.sellPrice)} x ${item.qty}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Text(FormatUtils.formatRupiah(item.subtotal), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GreenPrimary)
                    }
                }

                // Payment Method Selector
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Metode Pembayaran:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TUNAI", "QRIS", "KASBON").forEach { method ->
                            val isSelected = paymentMethod == method
                            Button(
                                onClick = {
                                    viewModel.paymentMethod.value = method
                                    if (method == "TUNAI" && cashPaid == 0.0) {
                                        viewModel.cashPaidAmount.value = finalTotal
                                        rawInputCash = finalTotal.toLong().toString()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (isSelected) {
                                    ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                } else {
                                    ButtonDefaults.outlinedButtonColors()
                                }
                            ) {
                                Text(
                                    text = method,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Customer info
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = custName,
                            onValueChange = { viewModel.customerName.value = it },
                            label = { Text(if (paymentMethod == "KASBON") "Nama Pelanggan (Wajib)" else "Pelanggan (Opsional)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = custPhone,
                            onValueChange = { viewModel.customerPhone.value = it },
                            label = { Text("No. HP") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }

                // Summary Total Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenPrimaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Tagihan:", fontSize = 12.sp)
                                Text(FormatUtils.formatRupiah(cartTotal), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            if (discountVal > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Diskon:", fontSize = 12.sp, color = StatusDanger)
                                    Text("- ${FormatUtils.formatRupiah(discountVal)}", color = StatusDanger, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                            if (taxPercentVal > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pajak (${taxPercentVal.toInt()}%):", fontSize = 12.sp)
                                    Text("+ ${FormatUtils.formatRupiah(taxAmount)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    FormatUtils.formatRupiah(finalTotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                }

                // Cash Payment Numpad Section
                if (paymentMethod == "TUNAI") {
                    item {
                        Column {
                            Text("Uang Diterima (Rp):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = if (cashPaid > 0) FormatUtils.formatRupiah(cashPaid) else "Rp 0",
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick cash denomination chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.cashPaidAmount.value = finalTotal
                                        rawInputCash = finalTotal.toLong().toString()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Text("Uang Pas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                listOf(10000.0, 20000.0, 50000.0, 100000.0).forEach { nom ->
                                    if (nom >= finalTotal || nom == 100000.0) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.cashPaidAmount.value = nom
                                                rawInputCash = nom.toLong().toString()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(2.dp)
                                        ) {
                                            Text("${(nom / 1000).toInt()}k", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Compact Numpad
                            val keypad = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("000", "0", "DEL")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                keypad.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        row.forEach { key ->
                                            Surface(
                                                onClick = {
                                                    when (key) {
                                                        "DEL" -> {
                                                            if (rawInputCash.isNotEmpty()) {
                                                                rawInputCash = rawInputCash.dropLast(1)
                                                                viewModel.cashPaidAmount.value = rawInputCash.toDoubleOrNull() ?: 0.0
                                                            }
                                                        }
                                                        else -> {
                                                            if (rawInputCash.length < 10) {
                                                                rawInputCash += key
                                                                viewModel.cashPaidAmount.value = rawInputCash.toDoubleOrNull() ?: 0.0
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(42.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                color = Color(0xFFF1F5F9)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (key == "DEL") {
                                                        Icon(Icons.Default.Backspace, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    } else {
                                                        Text(key, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Change card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (cashPaid >= finalTotal) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (cashPaid >= finalTotal) "Uang Kembalian:" else "Kurang:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (cashPaid >= finalTotal) StatusSuccess else StatusDanger
                                    )
                                    Text(
                                        text = if (cashPaid >= finalTotal) FormatUtils.formatRupiah(changeAmount)
                                        else FormatUtils.formatRupiah(finalTotal - cashPaid),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (cashPaid >= finalTotal) StatusSuccess else StatusDanger
                                    )
                                }
                            }
                        }
                    }
                }

                if (paymentMethod == "KASBON") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Catatan Kasbon Pelanggan", fontWeight = FontWeight.Bold, color = GoldTertiary, fontSize = 12.sp)
                                Text("Transaksi ini otomatis dicatat sebagai piutang atas nama ${custName.ifBlank { "Pelanggan" }}.", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.completeCheckout {
                        onPaymentSuccess()
                    }
                },
                enabled = isEnoughCash && (paymentMethod != "KASBON" || custName.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Selesaikan Transaksi & Buat Struk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

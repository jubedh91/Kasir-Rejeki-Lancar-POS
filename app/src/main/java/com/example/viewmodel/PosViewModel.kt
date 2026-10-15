package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.KasbonEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SupplierPurchaseEntity
import com.example.data.entity.SupplierPurchaseItemEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionItemEntity
import com.example.data.repository.PosRepository
import com.example.model.CartItem
import com.example.model.FormatUtils
import com.example.model.ScreenDestination
import com.example.model.StrukDetail
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = PosRepository(database)

    // Auth & Access Control
    private val _currentUser = MutableStateFlow<UserRole?>(UserRole.OWNER) // Default start with Owner or can switch
    val currentUser: StateFlow<UserRole?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenDestination.POS)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _pinAuthError = MutableStateFlow<String?>(null)
    val pinAuthError: StateFlow<String?> = _pinAuthError.asStateFlow()

    private val _showOwnerPinRequiredDialog = MutableStateFlow(false)
    val showOwnerPinRequiredDialog: StateFlow<Boolean> = _showOwnerPinRequiredDialog.asStateFlow()

    private val _pendingScreenAfterOwnerUnlock = MutableStateFlow<ScreenDestination?>(null)

    // Master Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Catalog & Filter
    val posSearchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("Semua")
    val filterLowStockOnly = MutableStateFlow(false)
    val isFavoriteOnly = MutableStateFlow(false)

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        posSearchQuery,
        selectedCategory,
        filterLowStockOnly
    ) { products, query, cat, lowOnly ->
        products.filter { product ->
            val matchQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.sku.contains(query, ignoreCase = true)
            val matchCategory = cat == "Semua" || product.category.equals(cat, ignoreCase = true)
            val matchStock = !lowOnly || product.stock <= product.minStockAlert
            matchQuery && matchCategory && matchStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartTotal: StateFlow<Double> = _cartItems.combine(_cartItems) { items, _ ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = _cartItems.combine(_cartItems) { items, _ ->
        items.sumOf { it.qty }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Checkout Form State
    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val discountRupiah = MutableStateFlow(0.0)
    val taxPercent = MutableStateFlow(0.0) // 0.0, 11.0, etc.
    val cashPaidAmount = MutableStateFlow(0.0)
    val paymentMethod = MutableStateFlow("TUNAI") // TUNAI, QRIS, KASBON
    val kasbonDueDays = MutableStateFlow(7)
    val checkoutNotes = MutableStateFlow("")

    // Active Digital Receipt Dialog
    private val _activeReceipt = MutableStateFlow<StrukDetail?>(null)
    val activeReceipt: StateFlow<StrukDetail?> = _activeReceipt.asStateFlow()

    // Status Message / Snackbar
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Purchases
    val allPurchases: StateFlow<List<SupplierPurchaseEntity>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Kasbon Book
    val kasbonTab = MutableStateFlow("PIUTANG") // "PIUTANG" or "HUTANG"
    val allKasbon: StateFlow<List<KasbonEntity>> = repository.allKasbon
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transaction History
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun login(pin: String): Boolean {
        val role = UserRole.fromPin(pin)
        return if (role != null) {
            _currentUser.value = role
            _pinAuthError.value = null
            true
        } else {
            _pinAuthError.value = "PIN Salah! (Owner: 5461 | Kasir: 1234)"
            false
        }
    }

    fun switchRole(role: UserRole) {
        _currentUser.value = role
        // If switched to cashier and currently on owner screen, go back to POS
        if (role == UserRole.CASHIER && _currentScreen.value.ownerOnly) {
            _currentScreen.value = ScreenDestination.POS
        }
        _statusMessage.value = "Beralih ke akun ${role.displayName}"
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = ScreenDestination.POS
    }

    fun navigateTo(screen: ScreenDestination) {
        val user = _currentUser.value
        if (screen.ownerOnly && user != UserRole.OWNER) {
            _pendingScreenAfterOwnerUnlock.value = screen
            _showOwnerPinRequiredDialog.value = true
        } else {
            _currentScreen.value = screen
        }
    }

    fun unlockOwnerWithPin(pin: String): Boolean {
        if (pin == UserRole.OWNER.pin) {
            _currentUser.value = UserRole.OWNER
            _showOwnerPinRequiredDialog.value = false
            _pendingScreenAfterOwnerUnlock.value?.let {
                _currentScreen.value = it
                _pendingScreenAfterOwnerUnlock.value = null
            }
            _statusMessage.value = "Akses Owner berhasil dibuka"
            return true
        }
        return false
    }

    fun dismissOwnerPinDialog() {
        _showOwnerPinRequiredDialog.value = false
        _pendingScreenAfterOwnerUnlock.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // --- Cart Actions ---

    fun addToCart(product: ProductEntity) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }

        if (index >= 0) {
            val existing = currentList[index]
            if (existing.qty < product.stock) {
                currentList[index] = existing.copy(qty = existing.qty + 1)
                _cartItems.value = currentList
            } else {
                _statusMessage.value = "Stok ${product.name} tersisa ${product.stock} ${product.unit}"
            }
        } else {
            if (product.stock > 0) {
                currentList.add(CartItem(product = product, qty = 1))
                _cartItems.value = currentList
            } else {
                _statusMessage.value = "Stok ${product.name} habis!"
            }
        }
    }

    fun updateCartQty(productId: Long, newQty: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else if (newQty <= item.product.stock) {
                currentList[index] = item.copy(qty = newQty)
            } else {
                currentList[index] = item.copy(qty = item.product.stock)
                _statusMessage.value = "Maksimum stok ${item.product.name} adalah ${item.product.stock}"
            }
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        resetCheckoutForm()
    }

    private fun resetCheckoutForm() {
        customerName.value = ""
        customerPhone.value = ""
        discountRupiah.value = 0.0
        taxPercent.value = 0.0
        cashPaidAmount.value = 0.0
        paymentMethod.value = "TUNAI"
        checkoutNotes.value = ""
    }

    // --- POS Checkout & Receipt ---

    fun completeCheckout(
        onSuccess: (StrukDetail) -> Unit
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            _statusMessage.value = "Keranjang masih kosong"
            return
        }

        val total = items.sumOf { it.subtotal }
        val discount = discountRupiah.value.coerceAtLeast(0.0)
        val afterDiscount = (total - discount).coerceAtLeast(0.0)
        val taxAmount = afterDiscount * (taxPercent.value / 100.0)
        val finalTotal = afterDiscount + taxAmount
        val method = paymentMethod.value

        val paid = when (method) {
            "TUNAI" -> cashPaidAmount.value
            "QRIS" -> finalTotal
            "KASBON" -> 0.0
            else -> finalTotal
        }

        if (method == "TUNAI" && paid < finalTotal) {
            _statusMessage.value = "Uang tunai kurang dari total tagihan!"
            return
        }

        val change = if (method == "TUNAI") (paid - finalTotal).coerceAtLeast(0.0) else 0.0
        val invoiceNo = FormatUtils.generateInvoiceNumber()
        val cashier = _currentUser.value?.displayName ?: "Kasir"
        val custName = customerName.value.ifBlank {
            if (method == "KASBON") "Pelanggan Kasbon" else "Pelanggan Umum"
        }
        val timestamp = System.currentTimeMillis()

        val txEntity = TransactionEntity(
            invoiceNumber = invoiceNo,
            cashierRole = cashier,
            customerName = custName,
            customerPhone = customerPhone.value,
            totalAmount = total,
            discountAmount = discount,
            finalAmount = finalTotal,
            paidAmount = paid,
            changeAmount = change,
            paymentMethod = method,
            timestamp = timestamp,
            notes = checkoutNotes.value
        )

        val txItems = items.map {
            TransactionItemEntity(
                transactionId = 0,
                productId = it.product.id,
                productName = it.product.name,
                sku = it.product.sku,
                unit = it.product.unit,
                buyPrice = it.product.buyPrice,
                sellPrice = it.product.sellPrice,
                qty = it.qty,
                subtotal = it.subtotal
            )
        }

        val isKasbon = method == "KASBON"
        val dueDate = if (isKasbon) timestamp + (kasbonDueDays.value * 86400000L) else 0L

        viewModelScope.launch {
            try {
                repository.completePosCheckout(
                    transaction = txEntity,
                    items = txItems,
                    isKasbon = isKasbon,
                    kasbonDueDate = dueDate
                )

                val struk = StrukDetail(
                    invoiceNumber = invoiceNo,
                    dateText = FormatUtils.formatDate(timestamp),
                    cashierName = cashier,
                    customerName = custName,
                    items = items,
                    totalAmount = total,
                    discountAmount = discount,
                    finalAmount = finalTotal,
                    paidAmount = paid,
                    changeAmount = change,
                    paymentMethod = method,
                    notes = checkoutNotes.value
                )

                _activeReceipt.value = struk
                clearCart()
                _statusMessage.value = "Transaksi $invoiceNo berhasil disimpan!"
                onSuccess(struk)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal memproses transaksi: ${e.message}"
            }
        }
    }

    fun dismissReceipt() {
        _activeReceipt.value = null
    }

    fun viewReceiptForTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            val items = repository.getItemsForTransactionSync(tx.id)
            val cartList = items.map {
                CartItem(
                    product = ProductEntity(
                        id = it.productId,
                        sku = it.sku,
                        name = it.productName,
                        category = "",
                        unit = it.unit,
                        buyPrice = it.buyPrice,
                        sellPrice = it.sellPrice,
                        stock = 0
                    ),
                    qty = it.qty
                )
            }
            _activeReceipt.value = StrukDetail(
                invoiceNumber = tx.invoiceNumber,
                dateText = FormatUtils.formatDate(tx.timestamp),
                cashierName = tx.cashierRole,
                customerName = tx.customerName,
                items = cartList,
                totalAmount = tx.totalAmount,
                discountAmount = tx.discountAmount,
                finalAmount = tx.finalAmount,
                paidAmount = tx.paidAmount,
                changeAmount = tx.changeAmount,
                paymentMethod = tx.paymentMethod,
                notes = tx.notes
            )
        }
    }

    // --- Master Product CRUD ---

    fun saveProduct(
        id: Long = 0,
        sku: String,
        name: String,
        category: String,
        unit: String,
        buyPrice: Double,
        sellPrice: Double,
        stock: Int,
        minStock: Int
    ) {
        if (name.isBlank() || sku.isBlank()) {
            _statusMessage.value = "Nama dan Kode SKU wajib diisi!"
            return
        }
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                sku = sku.trim(),
                name = name.trim(),
                category = category.trim().ifBlank { "Lainnya" },
                unit = unit.trim().ifBlank { "Pcs" },
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                stock = stock,
                minStockAlert = minStock,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertProduct(product)
                _statusMessage.value = "Barang $name berhasil ditambahkan"
            } else {
                repository.updateProduct(product)
                _statusMessage.value = "Barang $name berhasil diperbarui"
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _statusMessage.value = "Barang ${product.name} dihapus"
        }
    }

    // --- Supplier Purchase & Stock In ---

    fun recordSupplierPurchase(
        supplierName: String,
        supplierPhone: String,
        isHutang: Boolean,
        dueDays: Int,
        notes: String,
        purchaseItems: List<Pair<ProductEntity, Pair<Int, Double>>> // product -> (qty, buyPrice)
    ) {
        if (supplierName.isBlank()) {
            _statusMessage.value = "Nama supplier wajib diisi!"
            return
        }
        if (purchaseItems.isEmpty()) {
            _statusMessage.value = "Pilih minimal 1 barang untuk stok masuk!"
            return
        }

        viewModelScope.launch {
            val total = purchaseItems.sumOf { (_, pair) -> pair.first * pair.second }
            val now = System.currentTimeMillis()
            val poNumber = FormatUtils.generatePurchaseNumber()
            val dueDate = if (isHutang) now + (dueDays * 86400000L) else 0L

            val purchase = SupplierPurchaseEntity(
                purchaseNumber = poNumber,
                supplierName = supplierName.trim(),
                supplierPhone = supplierPhone.trim(),
                totalAmount = total,
                paymentMethod = if (isHutang) "HUTANG" else "TUNAI",
                paymentStatus = if (isHutang) "BELUM_LUNAS" else "LUNAS",
                timestamp = now,
                dueDate = dueDate,
                notes = notes
            )

            val items = purchaseItems.map { (prod, pair) ->
                SupplierPurchaseItemEntity(
                    purchaseId = 0,
                    productId = prod.id,
                    productName = prod.name,
                    qty = pair.first,
                    buyPrice = pair.second,
                    subtotal = pair.first * pair.second
                )
            }

            repository.recordSupplierPurchase(purchase, items)
            _statusMessage.value = "Stok masuk $poNumber berhasil dicatat! Stok inventaris otomatis bertambah."
        }
    }

    // --- Kasbon Payments & Records ---

    fun payKasbonInstallment(
        kasbonId: Long,
        amount: Double,
        notes: String
    ) {
        if (amount <= 0) {
            _statusMessage.value = "Nominal cicilan harus lebih dari 0"
            return
        }
        viewModelScope.launch {
            val paidBy = _currentUser.value?.displayName ?: "Kasir"
            val success = repository.payKasbonInstallment(kasbonId, amount, paidBy, notes)
            if (success) {
                _statusMessage.value = "Pembayaran cicilan berhasil dicatat!"
            } else {
                _statusMessage.value = "Gagal mencatat pembayaran cicilan"
            }
        }
    }

    fun addManualKasbon(
        type: String, // "PIUTANG" or "HUTANG"
        partyName: String,
        partyPhone: String,
        amount: Double,
        dueDays: Int,
        notes: String
    ) {
        if (partyName.isBlank() || amount <= 0) {
            _statusMessage.value = "Nama dan jumlah nominal kasbon wajib diisi!"
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val refNo = if (type == "PIUTANG") "MAN-PTG-${now % 100000}" else "MAN-HTG-${now % 100000}"
            val kasbon = KasbonEntity(
                type = type,
                partyName = partyName.trim(),
                partyPhone = partyPhone.trim(),
                referenceNumber = refNo,
                totalAmount = amount,
                remainingAmount = amount,
                isSettled = false,
                createdDate = now,
                dueDate = if (dueDays > 0) now + (dueDays * 86400000L) else 0L,
                notes = notes
            )
            repository.addKasbon(kasbon)
            _statusMessage.value = "Catatan ${if (type == "PIUTANG") "Piutang" else "Hutang"} baru berhasil disimpan"
        }
    }

    fun toggleFavoriteFilter() {
        isFavoriteOnly.value = !isFavoriteOnly.value
        _statusMessage.value = if (isFavoriteOnly.value) "Filter Produk Favorit Toko Plastik Aktif" else "Menampilkan Semua Produk"
    }

    fun setTaxPercent(percent: Double) {
        taxPercent.value = percent
        _statusMessage.value = if (percent > 0) "Pajak diterapkan: ${percent.toInt()}%" else "Pajak dinonaktifkan (0% Non-PPN)"
    }

    fun addQuickSaleItem(name: String, price: Double, category: String = "ppob") {
        viewModelScope.launch {
            try {
                val all = repository.allProducts.first()
                val existing = all.find { it.name.equals(name, ignoreCase = true) }
                val product = if (existing != null) {
                    existing
                } else {
                    val sku = "PPOB-" + (1000..9999).random()
                    val newProd = ProductEntity(
                        sku = sku,
                        name = name,
                        category = category,
                        unit = "trx",
                        buyPrice = (price * 0.98).coerceAtLeast(0.0),
                        sellPrice = price,
                        stock = 999,
                        minStockAlert = 5
                    )
                    val newId = repository.insertProduct(newProd)
                    newProd.copy(id = newId)
                }
                addToCart(product)
                _statusMessage.value = "$name berhasil ditambahkan ke kasir"
            } catch (e: Exception) {
                _statusMessage.value = "Gagal menambah item: ${e.message}"
            }
        }
    }
}

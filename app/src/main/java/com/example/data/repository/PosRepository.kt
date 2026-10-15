package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.KasbonEntity
import com.example.data.entity.KasbonPaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SupplierPurchaseEntity
import com.example.data.entity.SupplierPurchaseItemEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PosRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val supplierPurchaseDao = database.supplierPurchaseDao()
    private val kasbonDao = database.kasbonDao()

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        if (query.isBlank()) productDao.getAllProducts()
        else productDao.searchProducts(query)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insert(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.update(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.delete(product)

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    // Transactions & POS Checkout
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getItemsForTransaction(txId: Long): Flow<List<TransactionItemEntity>> =
        transactionDao.getItemsForTransaction(txId)

    suspend fun getItemsForTransactionSync(txId: Long): List<TransactionItemEntity> =
        transactionDao.getItemsForTransactionSync(txId)

    suspend fun completePosCheckout(
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>,
        isKasbon: Boolean,
        kasbonDueDate: Long = 0L
    ): Long {
        var txId = 0L
        database.runInTransaction {
            kotlinx.coroutines.runBlocking {
                // 1. Insert transaction
                txId = transactionDao.insertTransaction(transaction)

                // 2. Insert items with transactionId
                val itemsWithId = items.map { it.copy(transactionId = txId) }
                transactionDao.insertItems(itemsWithId)

                // 3. Atomically reduce stock
                for (item in items) {
                    productDao.reduceStock(item.productId, item.qty)
                }

                // 4. If payment method is Kasbon, add to Kasbon records (Piutang)
                if (isKasbon || transaction.paymentMethod == "KASBON") {
                    val kasbon = KasbonEntity(
                        type = "PIUTANG",
                        partyName = transaction.customerName.ifBlank { "Pelanggan Kasbon" },
                        partyPhone = transaction.customerPhone,
                        referenceNumber = transaction.invoiceNumber,
                        totalAmount = transaction.finalAmount,
                        remainingAmount = transaction.finalAmount,
                        isSettled = false,
                        createdDate = transaction.timestamp,
                        dueDate = if (kasbonDueDate > 0) kasbonDueDate else (System.currentTimeMillis() + 86400000L * 7),
                        notes = "Transaksi POS Kasir: ${transaction.invoiceNumber}"
                    )
                    kasbonDao.insertKasbon(kasbon)
                }
            }
        }
        return txId
    }

    // Supplier Purchases & Stock In
    val allPurchases: Flow<List<SupplierPurchaseEntity>> = supplierPurchaseDao.getAllPurchases()

    fun getItemsForPurchase(purchaseId: Long): Flow<List<SupplierPurchaseItemEntity>> =
        supplierPurchaseDao.getItemsForPurchase(purchaseId)

    suspend fun recordSupplierPurchase(
        purchase: SupplierPurchaseEntity,
        items: List<SupplierPurchaseItemEntity>
    ): Long {
        var purchaseId = 0L
        database.runInTransaction {
            kotlinx.coroutines.runBlocking {
                // 1. Insert purchase
                purchaseId = supplierPurchaseDao.insertPurchase(purchase)

                // 2. Insert purchase items
                val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
                supplierPurchaseDao.insertPurchaseItems(itemsWithId)

                // 3. Atomically increase stock & update purchase price for each item
                for (item in items) {
                    productDao.increaseStockWithPrice(item.productId, item.qty, item.buyPrice)
                }

                // 4. If payment method is HUTANG, add to Kasbon records
                if (purchase.paymentMethod == "HUTANG") {
                    val kasbon = KasbonEntity(
                        type = "HUTANG",
                        partyName = purchase.supplierName,
                        partyPhone = purchase.supplierPhone,
                        referenceNumber = purchase.purchaseNumber,
                        totalAmount = purchase.totalAmount,
                        remainingAmount = purchase.totalAmount,
                        isSettled = false,
                        createdDate = purchase.timestamp,
                        dueDate = if (purchase.dueDate > 0) purchase.dueDate else (System.currentTimeMillis() + 86400000L * 14),
                        notes = "Faktur Pembelian Stok Masuk: ${purchase.purchaseNumber}"
                    )
                    kasbonDao.insertKasbon(kasbon)
                }
            }
        }
        return purchaseId
    }

    // Kasbon (Piutang & Hutang)
    val allKasbon: Flow<List<KasbonEntity>> = kasbonDao.getAllKasbon()

    fun getKasbonByType(type: String): Flow<List<KasbonEntity>> = kasbonDao.getKasbonByType(type)

    fun getPaymentsForKasbon(kasbonId: Long): Flow<List<KasbonPaymentEntity>> =
        kasbonDao.getPaymentsForKasbon(kasbonId)

    suspend fun addKasbon(kasbon: KasbonEntity): Long = kasbonDao.insertKasbon(kasbon)

    suspend fun payKasbonInstallment(
        kasbonId: Long,
        paymentAmount: Double,
        paidBy: String,
        notes: String
    ): Boolean {
        var success = false
        database.runInTransaction {
            kotlinx.coroutines.runBlocking {
                val kasbon = kasbonDao.getKasbonById(kasbonId) ?: return@runBlocking
                val newRemaining = (kasbon.remainingAmount - paymentAmount).coerceAtLeast(0.0)
                val isSettled = newRemaining <= 0.0

                kasbonDao.insertPayment(
                    KasbonPaymentEntity(
                        kasbonId = kasbonId,
                        amount = paymentAmount,
                        paidBy = paidBy,
                        notes = notes
                    )
                )

                kasbonDao.updateKasbon(
                    kasbon.copy(
                        remainingAmount = newRemaining,
                        isSettled = isSettled
                    )
                )
                success = true
            }
        }
        return success
    }
}

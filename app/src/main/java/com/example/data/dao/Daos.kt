package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.KasbonEntity
import com.example.data.entity.KasbonPaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SupplierPurchaseEntity
import com.example.data.entity.SupplierPurchaseItemEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= minStockAlert ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET stock = stock - :qty, updatedAt = :time WHERE id = :productId")
    suspend fun reduceStock(productId: Long, qty: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = stock + :qty, buyPrice = :buyPrice, updatedAt = :time WHERE id = :productId")
    suspend fun increaseStockWithPrice(productId: Long, qty: Int, buyPrice: Double, time: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TransactionItemEntity>)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId ORDER BY id ASC")
    fun getItemsForTransaction(txId: Long): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId ORDER BY id ASC")
    suspend fun getItemsForTransactionSync(txId: Long): List<TransactionItemEntity>
}

@Dao
interface SupplierPurchaseDao {
    @Query("SELECT * FROM supplier_purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<SupplierPurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: SupplierPurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<SupplierPurchaseItemEntity>)

    @Query("SELECT * FROM supplier_purchase_items WHERE purchaseId = :purchaseId ORDER BY id ASC")
    fun getItemsForPurchase(purchaseId: Long): Flow<List<SupplierPurchaseItemEntity>>
}

@Dao
interface KasbonDao {
    @Query("SELECT * FROM kasbon_records ORDER BY isSettled ASC, createdDate DESC")
    fun getAllKasbon(): Flow<List<KasbonEntity>>

    @Query("SELECT * FROM kasbon_records WHERE type = :type ORDER BY isSettled ASC, createdDate DESC")
    fun getKasbonByType(type: String): Flow<List<KasbonEntity>>

    @Query("SELECT * FROM kasbon_records WHERE id = :id LIMIT 1")
    suspend fun getKasbonById(id: Long): KasbonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKasbon(kasbon: KasbonEntity): Long

    @Update
    suspend fun updateKasbon(kasbon: KasbonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: KasbonPaymentEntity): Long

    @Query("SELECT * FROM kasbon_payments WHERE kasbonId = :kasbonId ORDER BY paymentDate DESC")
    fun getPaymentsForKasbon(kasbonId: Long): Flow<List<KasbonPaymentEntity>>
}

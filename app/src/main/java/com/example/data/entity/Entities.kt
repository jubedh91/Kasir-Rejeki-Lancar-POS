package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val name: String,
    val category: String,
    val unit: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stock: Int,
    val minStockAlert: Int = 10,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val cashierRole: String, // "Owner" or "Kasir"
    val customerName: String = "Pelanggan Umum",
    val customerPhone: String = "",
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val finalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val paymentMethod: String, // "TUNAI", "QRIS", "KASBON"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "transaction_items")
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val sku: String,
    val unit: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val qty: Int,
    val subtotal: Double
)

@Entity(tableName = "supplier_purchases")
data class SupplierPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseNumber: String,
    val supplierName: String,
    val supplierPhone: String = "",
    val totalAmount: Double,
    val paymentMethod: String, // "TUNAI" or "HUTANG"
    val paymentStatus: String, // "LUNAS" or "BELUM_LUNAS"
    val timestamp: Long = System.currentTimeMillis(),
    val dueDate: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "supplier_purchase_items")
data class SupplierPurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val qty: Int,
    val buyPrice: Double,
    val subtotal: Double
)

@Entity(tableName = "kasbon_records")
data class KasbonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "PIUTANG" (Pelanggan) or "HUTANG" (Supplier)
    val partyName: String,
    val partyPhone: String = "",
    val referenceNumber: String, // Invoice TRX atau Faktur Supplier
    val totalAmount: Double,
    val remainingAmount: Double,
    val isSettled: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val dueDate: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "kasbon_payments")
data class KasbonPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kasbonId: Long,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paidBy: String = "",
    val notes: String = ""
)

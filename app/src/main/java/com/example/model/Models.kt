package com.example.model

import com.example.data.entity.ProductEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class UserRole(val pin: String, val displayName: String, val roleBadge: String) {
    OWNER("5461", "Owner (Pemilik)", "Owner - Akses Penuh"),
    CASHIER("1234", "Kasir Toko", "Kasir - Akses Terbatas");

    companion object {
        fun fromPin(pin: String): UserRole? {
            return when (pin) {
                OWNER.pin -> OWNER
                CASHIER.pin -> CASHIER
                else -> null
            }
        }
    }
}

enum class ScreenDestination(val title: String, val ownerOnly: Boolean) {
    POS("Kasir POS", false),
    MASTER_PRODUCTS("Master Barang", true),
    SUPPLIER_PURCHASE("Stok & Supplier", true),
    KASBON_BOOK("Buku Kasbon", false),
    HISTORY("Riwayat & Struk", false)
}

data class CartItem(
    val product: ProductEntity,
    val qty: Int
) {
    val subtotal: Double
        get() = product.sellPrice * qty
}

data class StrukDetail(
    val invoiceNumber: String,
    val dateText: String,
    val cashierName: String,
    val customerName: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val discountAmount: Double,
    val finalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val paymentMethod: String,
    val notes: String
)

object FormatUtils {
    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

    fun formatRupiah(amount: Double): String {
        return rupiahFormat.format(amount).replace("Rp", "Rp ")
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }

    fun generateInvoiceNumber(): String {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        return "TRX-${sdf.format(Date())}"
    }

    fun generatePurchaseNumber(): String {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        return "PO-${sdf.format(Date())}"
    }

    fun buildDigitalReceiptText(struk: StrukDetail): String {
        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine("      TOKO PLASTIK REJEKI LANCAR")
        sb.appendLine("   Pusat Grosir & Eceran Plastik")
        sb.appendLine(" Jl. Pasar Baru No. 88 - Telp: 0812-3456-7890")
        sb.appendLine("================================")
        sb.appendLine("No. Nota : ${struk.invoiceNumber}")
        sb.appendLine("Waktu    : ${struk.dateText}")
        sb.appendLine("Kasir    : ${struk.cashierName}")
        sb.appendLine("Pelanggan: ${struk.customerName}")
        sb.appendLine("--------------------------------")
        for (item in struk.items) {
            sb.appendLine(item.product.name)
            val lineQtyPrice = "  ${item.qty} ${item.product.unit} x ${formatRupiah(item.product.sellPrice)}"
            val lineSub = formatRupiah(item.subtotal)
            val padding = 32 - lineQtyPrice.length - lineSub.length
            val spaces = if (padding > 0) " ".repeat(padding) else " "
            sb.appendLine("$lineQtyPrice$spaces$lineSub")
        }
        sb.appendLine("--------------------------------")
        val printRow = { label: String, value: String ->
            val pad = 32 - label.length - value.length
            val sp = if (pad > 0) " ".repeat(pad) else " "
            sb.appendLine("$label$sp$value")
        }
        printRow("Total Belanja", formatRupiah(struk.totalAmount))
        if (struk.discountAmount > 0) {
            printRow("Diskon", "-${formatRupiah(struk.discountAmount)}")
        }
        printRow("Grand Total", formatRupiah(struk.finalAmount))
        printRow("Metode Bayar", struk.paymentMethod)
        printRow("Bayar Tunai", formatRupiah(struk.paidAmount))
        printRow("Kembalian", formatRupiah(struk.changeAmount))
        if (struk.notes.isNotBlank()) {
            sb.appendLine("Catatan: ${struk.notes}")
        }
        sb.appendLine("================================")
        sb.appendLine("Barang yang sudah dibeli tidak")
        sb.appendLine("dapat ditukar atau dikembalikan.")
        sb.appendLine("      Terima Kasih Banyak       ")
        sb.appendLine("================================")
        return sb.toString()
    }
}

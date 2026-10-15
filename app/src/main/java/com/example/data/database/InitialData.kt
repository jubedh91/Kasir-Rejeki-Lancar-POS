package com.example.data.database

import com.example.data.entity.KasbonEntity
import com.example.data.entity.KasbonPaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SupplierPurchaseEntity
import com.example.data.entity.SupplierPurchaseItemEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionItemEntity

object InitialData {
    val sampleProducts = listOf(
        ProductEntity(
            sku = "TW-650",
            name = "TW WRP Rect 650ml pcs",
            category = "mika",
            unit = "pcs",
            buyPrice = 1800.0,
            sellPrice = 2500.0,
            stock = 199,
            minStockAlert = 20
        ),
        ProductEntity(
            sku = "AL-45500",
            name = "ALTA Wrap Jumbo 45x500 roll",
            category = "wrapping",
            unit = "roll",
            buyPrice = 135000.0,
            sellPrice = 160000.0,
            stock = 4,
            minStockAlert = 5
        ),
        ProductEntity(
            sku = "ST-5840",
            name = "Struk Thermal 58x40 Woya roll",
            category = "atk",
            unit = "roll",
            buyPrice = 2800.0,
            sellPrice = 4000.0,
            stock = 17,
            minStockAlert = 10
        ),
        ProductEntity(
            sku = "LO-35KG",
            name = "Los Ori 35 Woke Mrh kg",
            category = "kantong kresek",
            unit = "kg",
            buyPrice = 41000.0,
            sellPrice = 47000.0,
            stock = 190,
            minStockAlert = 25
        ),
        ProductEntity(
            sku = "PP-2545",
            name = "PP WAYANG 25x45 pak",
            category = "kantong kresek",
            unit = "pak",
            buyPrice = 9500.0,
            sellPrice = 12000.0,
            stock = 63,
            minStockAlert = 15
        ),
        ProductEntity(
            sku = "WR-6085",
            name = "Waring Hijau Muly 60x85 pcs",
            category = "bahan makanan",
            unit = "pcs",
            buyPrice = 1400.0,
            sellPrice = 2000.0,
            stock = 118,
            minStockAlert = 20
        ),
        ProductEntity(
            sku = "WK-LOTUS",
            name = "Wakul LOTUS CKB Eriko pcs",
            category = "baskom",
            unit = "pcs",
            buyPrice = 5200.0,
            sellPrice = 7000.0,
            stock = 10,
            minStockAlert = 5
        ),
        ProductEntity(
            sku = "WK-ERIKO",
            name = "Wakul Kotak ERIKO Mono 200 pcs",
            category = "baskom",
            unit = "pcs",
            buyPrice = 7000.0,
            sellPrice = 9000.0,
            stock = 2,
            minStockAlert = 5
        ),
        ProductEntity(
            sku = "WD-KRAFT",
            name = "Wadah Kraft Bowl Saja KAMI",
            category = "cup gelas",
            unit = "pcs",
            buyPrice = 1100.0,
            sellPrice = 1500.0,
            stock = 271,
            minStockAlert = 30
        ),
        ProductEntity(
            sku = "WD-PUTIH",
            name = "Wadah Putih Bowl Saja KAMI",
            category = "cup gelas",
            unit = "pcs",
            buyPrice = 1300.0,
            sellPrice = 1800.0,
            stock = 80,
            minStockAlert = 20
        ),
        ProductEntity(
            sku = "AL-PUNCH",
            name = "Alat PUNCH Plong VTRO pc",
            category = "atk",
            unit = "pc",
            buyPrice = 22000.0,
            sellPrice = 28000.0,
            stock = 15,
            minStockAlert = 5
        ),
        ProductEntity(
            sku = "KB-1530",
            name = "Kresek Bening 15x30 (Pak 100)",
            category = "kantong kresek",
            unit = "pak",
            buyPrice = 6500.0,
            sellPrice = 8500.0,
            stock = 85,
            minStockAlert = 15
        ),
        ProductEntity(
            sku = "MK-1818",
            name = "Mika Nasi Kotak 18x18 (Isi 50)",
            category = "mika",
            unit = "pak",
            buyPrice = 21000.0,
            sellPrice = 26000.0,
            stock = 32,
            minStockAlert = 8
        ),
        ProductEntity(
            sku = "CP-16OV",
            name = "Cup Minuman Oval 16oz (50 pcs)",
            category = "cup gelas",
            unit = "pak",
            buyPrice = 14000.0,
            sellPrice = 18500.0,
            stock = 75,
            minStockAlert = 20
        ),
        ProductEntity(
            sku = "SD-BB12",
            name = "Sedotan Bubble Steril 12mm",
            category = "cup gelas",
            unit = "bungkus",
            buyPrice = 8000.0,
            sellPrice = 11000.0,
            stock = 50,
            minStockAlert = 10
        ),
        ProductEntity(
            sku = "PK-1015",
            name = "Plastik Klip 10x15 cm (Isi 100)",
            category = "kantong kresek",
            unit = "pak",
            buyPrice = 7000.0,
            sellPrice = 9500.0,
            stock = 62,
            minStockAlert = 15
        )
    )

    suspend fun populateDatabase(database: AppDatabase) {
        val productDao = database.productDao()
        if (productDao.countProducts() == 0) {
            productDao.insertAll(sampleProducts)

            // Initial Sample Transaction for testing receipt and history
            val txDao = database.transactionDao()
            val sampleTx = TransactionEntity(
                invoiceNumber = "TRX-20261015-001",
                cashierRole = "Kasir",
                customerName = "Warung Bu Siti",
                customerPhone = "0812-3456-7890",
                totalAmount = 51500.0,
                discountAmount = 1500.0,
                finalAmount = 50000.0,
                paidAmount = 50000.0,
                changeAmount = 0.0,
                paymentMethod = "TUNAI",
                timestamp = System.currentTimeMillis() - 3600000L * 2,
                notes = "Langganan katering pasar"
            )
            val txId = txDao.insertTransaction(sampleTx)
            txDao.insertItems(
                listOf(
                    TransactionItemEntity(
                        transactionId = txId,
                        productId = 1,
                        productName = "Kresek Bening 15x30 (Pak 100)",
                        sku = "KB-1530",
                        unit = "Pak",
                        buyPrice = 6500.0,
                        sellPrice = 8500.0,
                        qty = 2,
                        subtotal = 17000.0
                    ),
                    TransactionItemEntity(
                        transactionId = txId,
                        productId = 4,
                        productName = "Mika Nasi Kotak 18x18 (Isi 50)",
                        sku = "MK-1818",
                        unit = "Pak",
                        buyPrice = 21000.0,
                        sellPrice = 26000.0,
                        qty = 1,
                        subtotal = 26000.0
                    ),
                    TransactionItemEntity(
                        transactionId = txId,
                        productId = 12,
                        productName = "Plastik Klip 6x10 cm (Isi 100)",
                        sku = "PK-0610",
                        unit = "Pak",
                        buyPrice = 4000.0,
                        sellPrice = 6000.0,
                        qty = 1,
                        subtotal = 6000.0
                    )
                )
            )

            // Sample Supplier Purchase
            val purchaseDao = database.supplierPurchaseDao()
            val samplePurchase = SupplierPurchaseEntity(
                purchaseNumber = "PO-202610-001",
                supplierName = "PT Sumber Plastik Nusantara",
                supplierPhone = "0813-8899-7711",
                totalAmount = 450000.0,
                paymentMethod = "HUTANG",
                paymentStatus = "BELUM_LUNAS",
                timestamp = System.currentTimeMillis() - 86400000L * 3,
                dueDate = System.currentTimeMillis() + 86400000L * 11,
                notes = "Pengiriman stok mika & cup awal bulan"
            )
            val purchaseId = purchaseDao.insertPurchase(samplePurchase)
            purchaseDao.insertPurchaseItems(
                listOf(
                    SupplierPurchaseItemEntity(
                        purchaseId = purchaseId,
                        productId = 4,
                        productName = "Mika Nasi Kotak 18x18 (Isi 50)",
                        qty = 15,
                        buyPrice = 21000.0,
                        subtotal = 315000.0
                    ),
                    SupplierPurchaseItemEntity(
                        purchaseId = purchaseId,
                        productId = 6,
                        productName = "Cup Minuman Oval 16oz (50 pcs)",
                        qty = 10,
                        buyPrice = 13500.0,
                        subtotal = 135000.0
                    )
                )
            )

            // Sample Kasbon records (1 Piutang Pelanggan & 1 Hutang Supplier)
            val kasbonDao = database.kasbonDao()
            // 1. Piutang Pelanggan
            val piutangId = kasbonDao.insertKasbon(
                KasbonEntity(
                    type = "PIUTANG",
                    partyName = "Kedai Es Teh Melati",
                    partyPhone = "0857-9912-3344",
                    referenceNumber = "TRX-20261014-008",
                    totalAmount = 148000.0,
                    remainingAmount = 68000.0,
                    isSettled = false,
                    createdDate = System.currentTimeMillis() - 86400000L * 2,
                    dueDate = System.currentTimeMillis() + 86400000L * 5,
                    notes = "Ambil cup 16oz & sedotan 4 pak"
                )
            )
            kasbonDao.insertPayment(
                KasbonPaymentEntity(
                    kasbonId = piutangId,
                    amount = 80000.0,
                    paymentDate = System.currentTimeMillis() - 86400000L,
                    paidBy = "Kasir",
                    notes = "Cicilan pertama dibayar tunai"
                )
            )

            // 2. Hutang Supplier
            kasbonDao.insertKasbon(
                KasbonEntity(
                    type = "HUTANG",
                    partyName = "PT Sumber Plastik Nusantara",
                    partyPhone = "0813-8899-7711",
                    referenceNumber = "PO-202610-001",
                    totalAmount = 450000.0,
                    remainingAmount = 450000.0,
                    isSettled = false,
                    createdDate = System.currentTimeMillis() - 86400000L * 3,
                    dueDate = System.currentTimeMillis() + 86400000L * 11,
                    notes = "Faktur PO-202610-001 tempo 14 hari"
                )
            )
        }
    }
}

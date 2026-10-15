package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.KasbonDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SupplierPurchaseDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.KasbonEntity
import com.example.data.entity.KasbonPaymentEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SupplierPurchaseEntity
import com.example.data.entity.SupplierPurchaseItemEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.TransactionItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        SupplierPurchaseEntity::class,
        SupplierPurchaseItemEntity::class,
        KasbonEntity::class,
        KasbonPaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun supplierPurchaseDao(): SupplierPurchaseDao
    abstract fun kasbonDao(): KasbonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rejeki_lancar_pos.db"
                ).addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        InitialData.populateDatabase(database)
                    }
                }
            }
        }
    }
}

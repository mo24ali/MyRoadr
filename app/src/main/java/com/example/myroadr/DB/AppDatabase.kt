//package com.example.myroadr.DB
//
//import android.content.Context
//import androidx.room.*
//import androidx.room.migration.Migration
//import androidx.sqlite.db.SupportSQLiteDatabase
//import com.example.myroadr.DAO.CyclingEventDao
//import com.example.myroadr.DAO.UserProfileDao
//import com.example.myroadr.DAO.EventDao
//import com.example.myroadr.Entity.UserProfile
//import com.example.myroadr.Entity.CyclingEventEntity
//import kotlinx.coroutines.InternalCoroutinesApi
//import kotlinx.coroutines.internal.synchronized
//
////@Database(entities = [UserProfile::class, CyclingEventEntity::class], version = 1)
//@Database(entities = [UserProfile::class, CyclingEventEntity::class], version = 2) //
//
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun userProfileDao(): UserProfileDao
//    abstract fun eventDao(): CyclingEventDao
//
//    companion object {
//        @Volatile private var INSTANCE: AppDatabase? = null
////        val MIGRATION_1_2 = object : Migration(1, 2) {
////            override fun migrate(database: SupportSQLiteDatabase) {
////                database.execSQL("""
////            CREATE TABLE IF NOT EXISTS `cycling_events` (
////                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
////                `title` TEXT NOT NULL,
////                `description` TEXT NOT NULL,
////                `date` TEXT NOT NULL,
////                `locationName` TEXT NOT NULL
////            )
////        """.trimIndent())
////            }
////        }
//
//        @OptIn(InternalCoroutinesApi::class)
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "myroadr_db"
//                )
//                    .fallbackToDestructiveMigration()
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//
//    annotation class Volatile
//}
package com.example.myroadr.DB

import android.content.Context
import androidx.room.*
import androidx.room.Room.databaseBuilder
import com.example.myroadr.DAO.CyclingEventDao
import com.example.myroadr.DAO.UserProfileDao
import com.example.myroadr.Entity.UserProfile
import com.example.myroadr.Entity.CyclingEventEntity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    entities = [UserProfile::class, CyclingEventEntity::class],
    version = 3, // AUGMENTÉ si tu modifies la structure
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun eventDao(): CyclingEventDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myroadr_db"
                )
                    .fallbackToDestructiveMigration() // IMPORTANT
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    annotation class Volatile
}

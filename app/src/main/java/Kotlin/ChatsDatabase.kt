package Kotlin

import androidx.room.RoomDatabase
import Kotlin.ChatDao
import Kotlin.ChatsDatabase
import android.content.Context
import androidx.room.Database
import androidx.room.Room

@Database(entities = [Chat::class], version = 8)
abstract class ChatsDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao?

    companion object {
        var INSTANCE: ChatsDatabase? = null
        fun getDatabase(context: Context): ChatsDatabase? {
            if (INSTANCE == null) {
                synchronized(ChatsDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext, ChatsDatabase::class.java, "chat-database").fallbackToDestructiveMigration().build()
                    }
                }
            }
            return INSTANCE
        }
    }
}
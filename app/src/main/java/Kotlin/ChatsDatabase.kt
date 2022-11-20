package Kotlin

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Chat::class), version = 1)
abstract class ChatsDatabase : RoomDatabase() {
    abstract fun chatDao() : ChatDao
}
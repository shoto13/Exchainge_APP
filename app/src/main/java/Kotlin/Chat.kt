package Kotlin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat (
        @PrimaryKey(autoGenerate = true)
        val isSeen :  Boolean,
        val message : String,
        val messageTimestamp : String,
        val receiver : String,
        val sender : String,
)
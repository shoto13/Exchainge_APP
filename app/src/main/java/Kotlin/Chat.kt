package Kotlin

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.lang.reflect.Constructor


@Entity(tableName = "chats_table")
class Chat(var message: String, var messageTimestamp: String, var receiver: String, var sender: String) {
        @PrimaryKey(autoGenerate = true)
        var id = 0
}
//@Entity(tableName = "chats_table")
//data class Chat (
//        @PrimaryKey(autoGenerate = true)
//        val id : Int,
//
//        val message : String,
//        val messageTimestamp : String,
//        val receiver : String,
//        val sender : String,
//
//)
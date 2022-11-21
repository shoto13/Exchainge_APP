package Kotlin

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.lang.reflect.Constructor

@Entity(tableName = "chats")
data class Chat (
        @PrimaryKey()
        val id : Int,
        val isSeen :  Boolean,
        val message : String,
        val messageTimestamp : String,
        val receiver : String,
        val sender : String,


//@Ignore
//public Chat(Boolean isSeen, String message, String messageTimestamp, String receiver, String sender) {
//        this.isSeen = isSeen;
//        this.message = message;
//        this.messageTimestamp = messageTimestamp;
//        this.receiver = receiver;
//        this.sender = sender;
//}
)
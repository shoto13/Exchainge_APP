package Kotlin

import androidx.room.*
import java.lang.reflect.Constructor


@Entity(tableName = "chats_table", indices = [Index(value = ["UMID"], unique = true)])
class Chat(var message: String,
           var messageTimestamp: String,
           var receiver: String,
           var sender: String,
           @ColumnInfo(name = "UMID")
           var UMID : String
           ) {
        @PrimaryKey(autoGenerate = true)
        var id = 0

        //@ColumnInfo(name = "UMID")
       // var UMID = java.util.UUID.randomUUID().toString()
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
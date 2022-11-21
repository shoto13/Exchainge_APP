package Kotlin

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {
    //Insert new chat
    @Insert
    fun insert(chat: Chat)

    //Delete chat item
    @Delete
    fun delete(chat: Chat)

    //Search for chats based on message receiver
    @Query("SELECT * FROM chats " + " WHERE receiver LIKE :receiver")
    fun findChatsByUserReceiver(receiver: String): List<Chat>

    @Query("SELECT * FROM chats")
    fun getAll(): List<Chat?>?

    @Query("SELECT COUNT(message) FROM chats")
    fun getRowCount(): Int



}
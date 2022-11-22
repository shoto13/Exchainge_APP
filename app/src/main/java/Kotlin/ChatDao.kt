package Kotlin

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatDao {
    //Insert new chat
    @Insert
    fun insert(chat: Chat)

    @Insert
    fun insertAll(chat: Chat);

    //Delete chat item
    @Delete
    fun delete(chat: Chat)

    //Search for chats based on message receiver
    @Query("SELECT * FROM chats_table " + " WHERE receiver LIKE :receiver")
    fun findChatsByUserReceiver(receiver: String): List<Chat>

    @Query("SELECT * FROM chats_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Chat>>

    @Query("SELECT * FROM chats_table")
    fun getAllChats(): LiveData<List<Chat>>

    @Query("SELECT COUNT(message) FROM chats_table")
    fun getRowCount(): Int



}
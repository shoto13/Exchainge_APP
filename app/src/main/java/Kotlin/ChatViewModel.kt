package Kotlin

import androidx.lifecycle.AndroidViewModel
import Kotlin.ChatRepository
import android.app.Application
import androidx.lifecycle.LiveData

class ChatViewModel(application: Application?) : AndroidViewModel(application!!) {
    var repository: ChatRepository
    var allChats: LiveData<List<Chat>>

    init {
        repository = ChatRepository(application)
        allChats = repository.chat
    }

    fun insertChat(chat: Chat?) {
        repository.insert(chat)
    }

}
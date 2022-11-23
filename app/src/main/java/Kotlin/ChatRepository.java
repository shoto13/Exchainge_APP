package Kotlin;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ChatRepository {

    ChatDao chatDao;

    ChatRepository(Application application){
        ChatsDatabase db = ChatsDatabase.Companion.getDatabase(application);
        chatDao = db.chatDao();
    }

    LiveData<List<Chat>> getChat() {
        return chatDao.getAllChats();
    }

    void insert(Chat chat) {
        new insertAsyncTask(chatDao).execute(chat);
    }


    private static class insertAsyncTask extends AsyncTask<Chat, Void, Void>{
        private ChatDao taskDao;

        insertAsyncTask(ChatDao chatDao) {
            taskDao = chatDao;
        }

        @Override
        protected  Void doInBackground(Chat... chats) {
            taskDao.insertAll(chats[0]);
            return null;
        }
    }

}

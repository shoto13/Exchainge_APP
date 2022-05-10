package com.journey13.exchainge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class blockedUsers extends AppCompatActivity {

    ListView blockedUsersList;

    //BLOCKED USERS LIST TO BE POPULATED
    String[] blockedUsers = {"user1", "user2", "user3", "user4", "user5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        blockedUsersList = (ListView)findViewById(R.id.listBlocked);
        ArrayAdapter<String> blockedAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout_blocked_user, R.id.listItemText, blockedUsers);
        blockedUsersList.setAdapter(blockedAdapter);
    }
}
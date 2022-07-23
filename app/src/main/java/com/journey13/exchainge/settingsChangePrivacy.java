package com.journey13.exchainge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class settingsChangePrivacy extends AppCompatActivity {

    ListView privacyList;
    ListView securityList;
    ListView contactsSettingsList;

    //Connect to database
    private FirebaseUser fuser;
    private DatabaseReference reference;
    
    //SETTINGS HEADERS
    //SECURITY
    String[] securityItemHeads = {"Passcode Lock", "Two-Step Verification"};
    //PRIVACY
    String[] privacyItemHeads = {"Search For Me", "Searchable by username", "Searchable by email"};
    //BLOCKED USERS
    String[] contactsHead = {"Blocked users"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_privacy);

        securityList = (ListView)findViewById(R.id.listSecurity);
        ArrayAdapter<String> securityArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout_toggle, R.id.listItemText, securityItemHeads);
        securityList.setAdapter(securityArrayAdapter);

        privacyList = (ListView)findViewById(R.id.listPrivacy);
        ArrayAdapter<String> privacyArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout_toggle, R.id.listItemText, privacyItemHeads);
        privacyList.setAdapter(privacyArrayAdapter);

        contactsSettingsList = (ListView)findViewById(R.id.listContacts);
        ArrayAdapter<String> contactsArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout, R.id.listItemText, contactsHead);
        contactsSettingsList.setAdapter(contactsArrayAdapter);

        contactsSettingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    Intent intent = new Intent(getApplicationContext(), blockedUsers.class);
                    startActivity(intent);
                }
            }
        });



    }
}
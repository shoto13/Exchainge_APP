package com.journey13.exchainge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class settingsChangePrivacy extends AppCompatActivity {

    ListView privacySettingsList;
    ListView securitySettingsList;
    ListView contactsSettingsList;

    //Connect to database
    private FirebaseUser fuser;
    private DatabaseReference reference;

    Switch setSwitch;

    //TODO change switches to programmed in so that they are not dynamically loaded
    //SETTINGS HEADERS
    //SECURITY
    String[] securityItemHeads = {"Passcode Lock", "Two-Step Verification"};
    //PRIVACY
    String[] privacyItemHeads = {"Search for Me", "Searchable by Username", "Searchable by Email"};
    //BLOCKED USERS
    String[] contactsHead = {"Blocked Users"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_privacy);

        securitySettingsList = (ListView)findViewById(R.id.listSecurity);
        ArrayAdapter<String> securityArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout_toggle, R.id.listItemText, securityItemHeads);
        securitySettingsList.setAdapter(securityArrayAdapter);

        privacySettingsList = (ListView)findViewById(R.id.listPrivacy);
        ArrayAdapter<String> privacyArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout_toggle, R.id.listItemText, privacyItemHeads);
        privacySettingsList.setAdapter(privacyArrayAdapter);


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

        //TODO make onclicklisteners for both privacy settings and security settings as above.
    }
}
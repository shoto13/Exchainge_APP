package com.journey13.exchainge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class settingsChangePrivacy extends AppCompatActivity {

    ListView privacyList;
    ListView securityList;

    //SETTINGS HEADERS
    //SECURITY
    String[] securityItemHeads = {"Passcode Lock", "Two-Step Verification"};
    //PRIVACY
    String[] privacyItemHeads = {"Search For Me", "Blocked Users"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_privacy);

        securityList = (ListView)findViewById(R.id.listSecurity);
        ArrayAdapter<String> securityArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout, R.id.listItemText, securityItemHeads);
        securityList.setAdapter(securityArrayAdapter);

        privacyList = (ListView)findViewById(R.id.listPrivacy);
        ArrayAdapter<String> privacyArrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_layout, R.id.listItemText, privacyItemHeads);
        privacyList.setAdapter(privacyArrayAdapter);



    }
}
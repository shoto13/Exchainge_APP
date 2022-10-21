package com.journey13.exchainge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class settingsChangePrivacy extends AppCompatActivity {

    ListView contactsSettingsList;
    SwitchCompat passcodeLockSwitch, twostepVerificationSwitch, searchForMeSwitch, searchableUsernameSwitch, searchableEmailSwitch;

    //Connect to database
    private FirebaseUser fuser;
    private DatabaseReference reference;

    //SETTINGS HEADERS
    //BLOCKED USERS
    String[] contactsHead = {"Blocked Users"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_privacy);

        passcodeLockSwitch = findViewById(R.id.passcode_lock_switch);
        twostepVerificationSwitch = findViewById(R.id.twostep_verification_switch);
        searchForMeSwitch = findViewById(R.id.search_for_me_switch);
        searchableUsernameSwitch = findViewById(R.id.searchable_username_switch);
        searchableEmailSwitch = findViewById(R.id.searchable_email_switch);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(fuser.getUid());

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

        passcodeLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(settingsChangePrivacy.this, "Passcode lock toggled", Toast.LENGTH_LONG).show();
            }
        });

        twostepVerificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(settingsChangePrivacy.this, "Two-step verification toggled", Toast.LENGTH_LONG).show();
            }
        });

        searchForMeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    reference.child("searchable").setValue(true);
                } else {
                    reference.child("searchableByUsername").setValue(false);
                    reference.child("searchableByEmail").setValue(false);
                    reference.child("searchable").setValue(false);
                    searchableUsernameSwitch.setChecked(false);
                    searchableEmailSwitch.setChecked(false);
                }
            }
        });

        searchableUsernameSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    reference.child("searchableByUsername").setValue(true);
                } else {
                    reference.child("searchableByUsername").setValue(false);
                }
            }
        });

        searchableEmailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    reference.child("searchableByEmail").setValue(true);
                } else {
                    reference.child("searchableByEmail").setValue(false);
                }
            }
        });
    }
}
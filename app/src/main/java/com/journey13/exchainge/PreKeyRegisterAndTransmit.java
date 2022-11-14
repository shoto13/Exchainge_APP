package com.journey13.exchainge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class PreKeyRegisterAndTransmit extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseUser fuser;
    private RegistrationKeyModel keyGen;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_key_register_and_transmit);

        //SET UP USER AND GET DB REFERENCE
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        //GENERATE KEYS
        try {
            keyGen = GlobalMethods.generateKeys();
            postKeys(fuser.getUid(), keyGen.getIdentityKeyPairString(), keyGen.getPreKeyIds(), keyGen.getRegistrationId(), keyGen.getSignedPreKeyRecordString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("There was an exception while attempting to generate keys");
        }
    }

    private void postKeys(String userid, String identityKeyPairString, String PreKeyIds, int RegistrationId, String SignedPreKeyRecordString) {

        //TODO figure out how to get this working
        sharedPreferences = getSharedPreferences("STORED_KEY_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();

        editor.putString("IdentityKeyPairString", identityKeyPairString);
        editor.putString("PreKeyIds", PreKeyIds);
        editor.putInt("RegistrationId", RegistrationId);
        editor.putString("SignedPreKeyRecordString", SignedPreKeyRecordString);
        editor.apply();

        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Keys").child(fuser.getUid());

        HashMap<String,Object> hashMap = new HashMap<String ,Object>();

        hashMap.put("IdentityKeyPairString", identityKeyPairString);
        hashMap.put("PreKeyIds", PreKeyIds);
        hashMap.put("RegistrationId", RegistrationId);
        hashMap.put("SignedPreKeyRecordString", SignedPreKeyRecordString);

        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
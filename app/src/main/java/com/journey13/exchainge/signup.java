package com.journey13.exchainge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.journey13.exchainge.Notifications.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class signup extends AppCompatActivity {

    EditText username, firstNameEditText, secondNameEditText, emailEditText, passwordEditText;
    Button registerButton;

    FirebaseAuth auth;
    DatabaseReference reference, contactsReference, walletReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.usernameEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        secondNameEditText = findViewById(R.id.secondNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        //CREATE FIREBASE AUTH INSTANCE
        auth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = username.getText().toString();
                String txt_email = emailEditText.getText().toString();
                String txt_password = passwordEditText.getText().toString();
                String txt_firstName = firstNameEditText.getText().toString();
                String txt_secondName = secondNameEditText.getText().toString();
                String txt_tagline = "I'm now on Exchainge!";
                Float walletBalance = 0.001f;
                // TEST VALS BELOW - REMOVE THESE AFTER TEST
                //TODO:: THIS NEEDS TO BE A LIST INSTEAD OF AN ARRAY STRING IN ORDER TO FUNCTION SO FIX THIS FIRST
                String[] contacts = {"user1TEST", "user2TEST", "user3TEST"};
                List<String> contactsList = Arrays.asList(contacts);


                //CHECK IF FIELDS ARE EMPTY (ADD ADDITIONAL FIELDS)
                if(TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(signup.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6) {
                    Toast.makeText(signup.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    register(txt_username, txt_email, txt_password, txt_firstName, txt_secondName, txt_tagline, walletBalance, contactsList);
                }
            }
        });

    }

    //REGISTER A NEW USER USING FIREBASE
    private void register(String username, String email, String password, String firstName, String secondName, String tagline, Float walletBal, List<String> contacts) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(userid);
                            contactsReference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(userid);
                            walletReference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Wallets").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("tagline", tagline);
                            hashMap.put("imageURL", "default");
                            hashMap.put("firstName", firstName);
                            hashMap.put("secondName", secondName);
                            hashMap.put("status", "offline");
                            hashMap.put("search", username.toLowerCase());

                            HashMap<String, Float> walHashMap = new HashMap<>();
                            walHashMap.put("wBalance", walletBal);

                            HashMap<String, List<String>> contactsHashMap = new HashMap<>();
                            contactsHashMap.put("contacts", contacts);


                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        //successful user information storage
                                    }
                                }
                            });

                            contactsReference.setValue(contactsHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //successful user contacts storage
                                    }
                                }
                            });

                            walletReference.setValue(walHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(signup.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });




                        } else {
                            Toast.makeText(signup.this, "You cannot register with this email", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }


    public void go2Home(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
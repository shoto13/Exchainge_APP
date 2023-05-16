package com.journey13.exchainge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class init_landing extends AppCompatActivity {

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Check if user is already logged in
        if (firebaseUser != null) {
            Intent intent = new Intent(init_landing.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_landing);

    }

    //Go to login page
    public void gotToLogin(View view) {
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
    }

    //Go to signup page
    public void goToSignUp(View view) {
        Intent intent = new Intent(this, signup.class);
        startActivity(intent);
    }

}
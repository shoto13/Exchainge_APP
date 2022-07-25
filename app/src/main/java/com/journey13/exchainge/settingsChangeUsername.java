package com.journey13.exchainge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class settingsChangeUsername extends AppCompatActivity {

    private Button changeUNameButton;
    private EditText changeUNameEditText;

    private DatabaseReference reference;
    private FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_username);

        changeUNameButton = findViewById(R.id.confirmChangesButton);
        changeUNameEditText = findViewById(R.id.changeUNameEditText);
        fuser = FirebaseAuth.getInstance().getCurrentUser();


        changeUNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(changeUNameEditText.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "You must enter a new username in the field above", Toast.LENGTH_SHORT).show();
                } else {
                    String newUName = changeUNameEditText.getText().toString();
                    setNewUName(newUName);
                }

            }
        });
    }

    public void setNewUName(String uname) {

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        //UPDATE USERNAME ON DB
        HashMap<String, Object> map = new HashMap<>();
        map.put("username", uname);
        reference.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Username updated", Toast.LENGTH_SHORT).show();
                changeUNameEditText.setText("");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Username failed to update. Please try again later", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
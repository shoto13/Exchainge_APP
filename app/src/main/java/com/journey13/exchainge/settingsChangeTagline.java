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

public class settingsChangeTagline extends AppCompatActivity {

    private Button changeTaglineButton;
    private EditText changeTaglineEditText;

    private DatabaseReference reference;
    private FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_tagline);

        changeTaglineButton = findViewById(R.id.confirmChangesButton);
        changeTaglineEditText = findViewById(R.id.changeTaglineEditText);
        fuser = FirebaseAuth.getInstance().getCurrentUser();


        changeTaglineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(changeTaglineEditText.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "You must enter your new tag line in the field above", Toast.LENGTH_SHORT).show();
                } else {
                    String newTag = changeTaglineEditText.getText().toString();
                    setNewTagline(newTag);
                }
            }
        });


    }

    public void setNewTagline(String tagline) {

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        //UPDATE TAGLINE ON DB
        HashMap<String, Object> map = new HashMap<>();
        map.put("tagline", tagline);
        reference.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Tagline updated", Toast.LENGTH_SHORT).show();
                changeTaglineEditText.setText("");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Tagline failed to update. Please try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
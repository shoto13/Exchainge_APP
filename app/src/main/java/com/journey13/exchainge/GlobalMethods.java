package com.journey13.exchainge;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journey13.exchainge.Fragments.UsersFragment;

import java.util.ArrayList;
import java.util.List;

public class GlobalMethods {

    public static interface MyCallback<T> {
        void callback(T data);
    }

    public static void getUserContacts(@NonNull MyCallback<ArrayList<String>> ids) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(fuser.getUid());
        DatabaseReference userRef = reference.child("contacts");

        List<String> new_contacts = new ArrayList<String>();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    new_contacts.add(snapshot.getValue().toString());
                }
                ids.callback((ArrayList<String>) new_contacts);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void getBlockedIds(@NonNull MyCallback<ArrayList<String>> ids) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Blocked").child(fuser.getUid());
        DatabaseReference blockedRef = reference.child("contacts");

        List<String> blocked_users = new ArrayList<String>();

        blockedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    blocked_users.add(snapshot.getValue().toString());
                }

                ids.callback((ArrayList<String>) blocked_users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}

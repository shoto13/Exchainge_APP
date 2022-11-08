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

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.libsignal.util.Medium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            public void onCancelled(@NonNull DatabaseError error) {}
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

    //METHOD WHICH TAKES TWO IDS (THE CURRENT USER AND THE SECONDARY PARTICIPANT IN THE CONVERSATION)
    // IT COMPARES THESE VALUES TO DETERMINE WHICH VALUE IS GREATER IT THEN CREATES THE REFERENCE
    // STRING TO THE DATABASE BY CONCATENATING THE TWO VALUES WITH THE HIGHEST ONE FIRST
    // THIS CREATES A SIMPLE REPRODUCIBLE REFERENCING SCHEME SO THAT WE DO NOT NEED TO SEARCH EVERY
    // MESSAGE IN THE DB FOR THE CURRENT CONVERSATION
    public static String compareIdsToCreateReference(String currentUser, String secondaryUser) {
        Integer x = currentUser.compareTo(secondaryUser);
        String chat_db_ref;

        if (x > 0) {
            chat_db_ref = currentUser + secondaryUser;
        } else {
            chat_db_ref = secondaryUser + currentUser;
        }
        return chat_db_ref;
    }

    public static RegistrationKeyModel generateKeys() throws InvalidKeyException, IOException {
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        int registrationId = KeyHelper.generateRegistrationId(false);
        SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, new Random().nextInt(Medium.MAX_VALUE - 1));
        List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(new Random().nextInt(Medium.MAX_VALUE - 101), 100);
        return new RegistrationKeyModel(
                identityKeyPair,
                registrationId,
                preKeys,
                signedPreKey
        );
    }



}

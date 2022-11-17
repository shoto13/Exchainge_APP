package com.journey13.exchainge;

import android.content.Context;
import android.content.SharedPreferences;

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
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.libsignal.util.Medium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
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

    // FUNCTION WHICH TAKES THE SHARED PREFS, REMOTEUSERID AND LOCAL USER ID AND ATTEMPTS TO BUILD
    //AN ENCRYPTED LOCAL USER AND AN ENCRYPTED REMOTE USER. THE LOCAL USER IS CREATED FIRST,
    // THE REMOTE USER IS THEN BUILT FROM THE PREKEYS AND PUBLIC KEYS RETRIEVED. IF BOTH BUILDS
    // COMPLETE SUCCESSFULLY, THE CREATELOCALANDREMOTEUSER CLASS RETURNS WITH BOTH USERS STORED WITHIN
    // THIS ALLOWS THE CALLBACK FUNCTION TO TAKE IN BOTH USERS.
    public static void getRemoteAndLocalEncryptedUser(@NonNull MyCallback<CreateLocalAndRemoteUser> localRemoteUsers, FirebaseUser fuser, String remoteUserId, SharedPreferences sharedPreferences) {
        EncryptedLocalUser encryptedLocalUser;
        EncryptedRemoteUser encryptedRemoteUser;

        CreateLocalAndRemoteUser localAndRemoteUser = new CreateLocalAndRemoteUser();

        // ATTEMPT TO BUILD THE ENCRYPTED LOCAL USER
        try {
            byte[] decodedIdentityKeyPair = {};
            byte[] decodedSignedPreKeyRecord = {};
            String[] preKeysArray = {};

            Integer registrationId = sharedPreferences.getInt("RegistrationId", 0);
            String identityKeyPairString = sharedPreferences.getString("IdentityKeyPairString", "");
            String preKeyIdsString = sharedPreferences.getString("PreKeyIds", "");
            String signedPreKeyRecordString = sharedPreferences.getString("SignedPreKeyRecordString", "");

            decodedIdentityKeyPair = Base64.getDecoder().decode(identityKeyPairString);
            decodedSignedPreKeyRecord = Base64.getDecoder().decode(signedPreKeyRecordString);
            String bracketsRemoved = preKeyIdsString.substring(1, preKeyIdsString.length() - 1);
            preKeysArray = bracketsRemoved.split(", ");

            try {
                RegistrationKeyModel localRegistrationKeyModel = new RegistrationKeyModel(decodedIdentityKeyPair, registrationId, preKeysArray, decodedSignedPreKeyRecord);
                try {
                    encryptedLocalUser = new EncryptedLocalUser(decodedIdentityKeyPair, registrationId, fuser.getUid(), 2, localRegistrationKeyModel.getPreKeysAsByteArrays(), decodedSignedPreKeyRecord);
                    localAndRemoteUser.setEncryptedLocalUser(encryptedLocalUser);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("THERE WAS AN ERROR WHILE BUILDING THE ENCRYPTED LOCAL USER");
            e.printStackTrace();
        }

        // ATTEMPT TO BUILD THE ENCRYPTED REMOTE USER AND IF SUCCESSFUL RETURN WITH THE CALLBACK FUNCTION
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Keys").child(remoteUserId);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String identityKeyPair = "";
                    int registrationId = 0;
                    String[] preKeysArray = {"", ""};
                    String signedPreKeyRecord = "";
                    byte[] decodedIdentityKeyPair = {};
                    byte[] decodedSignedPreKeyRecord = {};
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals("IdentityKeyPairString")) {
                            identityKeyPair = snapshot.getValue().toString();
                            decodedIdentityKeyPair = Base64.getDecoder().decode(identityKeyPair);
                        } else if (snapshot.getKey().equals("PreKeyIds")) {
                            System.out.println("We got the prekeyids " + snapshot.getValue());
                            String bracketsRemoved = snapshot.getValue().toString().substring(1, snapshot.getValue().toString().length() - 1);
                            preKeysArray = bracketsRemoved.split(", ");
                        } else if (snapshot.getKey().equals("RegistrationId")) {
                            registrationId = Integer.parseInt(snapshot.getValue().toString());
                            System.out.println("The registration id is  " + registrationId);
                        } else if (snapshot.getKey().equals("SignedPreKeyRecordString")) {
                            signedPreKeyRecord = snapshot.getValue().toString();
                            decodedSignedPreKeyRecord = Base64.getDecoder().decode(signedPreKeyRecord);
                        }
                    }
                    try {
                        RegistrationKeyModel remoteUserKeyModel = new RegistrationKeyModel(decodedIdentityKeyPair, registrationId, preKeysArray, decodedSignedPreKeyRecord);
                        PreKeyRecord rec = remoteUserKeyModel.getPreKey();
                        int prekeyid = rec.getId();
                        ECKeyPair preKeyPub = rec.getKeyPair();
                        ECPublicKey prekeypublickey = preKeyPub.getPublicKey();
                        byte[] prekeyPublicKeyArray = prekeypublickey.serialize();
                        try {
                            EncryptedRemoteUser encryptedRemoteUser = new EncryptedRemoteUser(
                                    remoteUserKeyModel.getRegistrationId(),
                                    remoteUserId,
                                    2,
                                    prekeyid,
                                    prekeyPublicKeyArray,
                                    remoteUserKeyModel.getSignedPreKeyId(),
                                    remoteUserKeyModel.getSignedPreKeyPublicKeyByteArray(),
                                    remoteUserKeyModel.getSignedPreKeySignatureByteArray(),
                                    remoteUserKeyModel.getPublicIdentityKey()
                            );
                            localAndRemoteUser.setEncryptedRemoteUser(encryptedRemoteUser);
                            localRemoteUsers.callback((CreateLocalAndRemoteUser) localAndRemoteUser);

                        } catch (Exception e) {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("THERE WAS AN ERROR WHILE BUILDING THE ENCRYPTED REMOTE USER");
        }

    }


}

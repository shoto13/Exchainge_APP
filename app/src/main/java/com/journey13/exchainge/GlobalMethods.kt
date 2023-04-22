package com.journey13.exchainge

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlin.Throws
import org.whispersystems.libsignal.util.KeyHelper
import org.whispersystems.libsignal.util.Medium
import android.content.SharedPreferences
import com.journey13.exchainge.EncryptedLocalUser
import com.journey13.exchainge.EncryptedRemoteUser
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.ecc.ECKeyPair
import java.io.IOException
import java.lang.Exception
import java.util.*

object GlobalMethods {
    @JvmStatic
    fun getUserContacts(ids: MyCallback<ArrayList<String>?>) {
        val fuser = FirebaseAuth.getInstance().currentUser
        val reference =
            FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Contacts").child(
                fuser!!.uid
            )
        val userRef = reference.child("contacts")
        val new_contacts: MutableList<String> = ArrayList()
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    new_contacts.add(snapshot.value.toString())
                }
                ids.callback(new_contacts as ArrayList<String>)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @JvmStatic
    fun getBlockedIds(ids: MyCallback<ArrayList<String>?>) {
        val fuser = FirebaseAuth.getInstance().currentUser
        val reference =
            FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Blocked").child(
                fuser!!.uid
            )
        val blockedRef = reference.child("contacts")
        val blocked_users: MutableList<String> = ArrayList()
        blockedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    blocked_users.add(snapshot.value.toString())
                }
                ids.callback(blocked_users as ArrayList<String>)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //METHOD WHICH TAKES TWO IDS (THE CURRENT USER AND THE SECONDARY PARTICIPANT IN THE CONVERSATION)
    // IT COMPARES THESE VALUES TO DETERMINE WHICH VALUE IS GREATER IT THEN CREATES THE REFERENCE
    // STRING TO THE DATABASE BY CONCATENATING THE TWO VALUES WITH THE HIGHEST ONE FIRST
    // THIS CREATES A SIMPLE REPRODUCIBLE REFERENCING SCHEME SO THAT WE DO NOT NEED TO SEARCH EVERY
    // MESSAGE IN THE DB FOR THE CURRENT CONVERSATION
    @JvmStatic
    fun compareIdsToCreateReference(currentUser: String, secondaryUser: String): String {
        val x = currentUser.compareTo(secondaryUser)
        val chat_db_ref: String
        chat_db_ref = if (x > 0) {
            currentUser + secondaryUser
        } else {
            secondaryUser + currentUser
        }
        return chat_db_ref
    }

    @JvmStatic
    @Throws(InvalidKeyException::class, IOException::class)
    fun generateKeys(): RegistrationKeyModel {
        val identityKeyPair = KeyHelper.generateIdentityKeyPair()
        val registrationId = KeyHelper.generateRegistrationId(false)
        val signedPreKey =
            KeyHelper.generateSignedPreKey(identityKeyPair, Random().nextInt(Medium.MAX_VALUE - 1))
        val preKeys = KeyHelper.generatePreKeys(Random().nextInt(Medium.MAX_VALUE - 101), 100)
        return RegistrationKeyModel(
            identityKeyPair,
            registrationId,
            preKeys,
            signedPreKey
        )
    }

    // FUNCTION WHICH TAKES THE SHARED PREFS, REMOTEUSERID AND LOCAL USER ID AND ATTEMPTS TO BUILD
    //AN ENCRYPTED LOCAL USER AND AN ENCRYPTED REMOTE USER. THE LOCAL USER IS CREATED FIRST,
    // THE REMOTE USER IS THEN BUILT FROM THE PREKEYS AND PUBLIC KEYS RETRIEVED. IF BOTH BUILDS
    // COMPLETE SUCCESSFULLY, THE CREATELOCALANDREMOTEUSER CLASS RETURNS WITH BOTH USERS STORED WITHIN
    // THIS ALLOWS THE CALLBACK FUNCTION TO TAKE IN BOTH USERS.
    @JvmStatic
    fun getRemoteAndLocalEncryptedUser(
        localRemoteUsers: (Any) -> Any,
        fuser: FirebaseUser,
        remoteUserId: String?,
        sharedPreferences: SharedPreferences
    ) {
        val encryptedLocalUser: EncryptedLocalUser
        var encryptedRemoteUser: EncryptedRemoteUser
        val localAndRemoteUser = LocalAndRemoteUserModel()

        // ATTEMPT TO BUILD THE ENCRYPTED LOCAL USER
        try {
            var decodedIdentityKeyPair: ByteArray? = byteArrayOf()
            var decodedSignedPreKeyRecord: ByteArray? = byteArrayOf()
            var preKeysArray = arrayOf<String?>()
            val registrationId = sharedPreferences.getInt("RegistrationId", 0)
            val identityKeyPairString = sharedPreferences.getString("IdentityKeyPairString", "")
            val preKeyIdsString = sharedPreferences.getString("PreKeyIds", "")
            val signedPreKeyRecordString =
                sharedPreferences.getString("SignedPreKeyRecordString", "")
            decodedIdentityKeyPair = Base64.getDecoder().decode(identityKeyPairString)
            decodedSignedPreKeyRecord = Base64.getDecoder().decode(signedPreKeyRecordString)
            val bracketsRemoved = preKeyIdsString!!.substring(1, preKeyIdsString.length - 1)
            preKeysArray = bracketsRemoved.split(", ").toTypedArray()
            try {
                val localRegistrationKeyModel = RegistrationKeyModel(
                    decodedIdentityKeyPair,
                    registrationId,
                    preKeysArray,
                    decodedSignedPreKeyRecord
                )
                try {
                    encryptedLocalUser = EncryptedLocalUser(
                        decodedIdentityKeyPair,
                        registrationId,
                        fuser.uid,
                        2,
                        localRegistrationKeyModel.preKeysAsByteArrays,
                        decodedSignedPreKeyRecord
                    )
                    localAndRemoteUser.setEncryptedLocalUser(encryptedLocalUser)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            println("THERE WAS AN ERROR WHILE BUILDING THE ENCRYPTED LOCAL USER")
            e.printStackTrace()
        }

        // ATTEMPT TO BUILD THE ENCRYPTED REMOTE USER AND IF SUCCESSFUL RETURN WITH THE CALLBACK FUNCTION
        try {
            val reference =
                FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("Keys").child(
                    remoteUserId!!
                )
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var identityKeyPair = ""
                    var registrationId = 0
                    var preKeysArray = arrayOf("", "")
                    var signedPreKeyRecord = ""
                    var decodedIdentityKeyPair: ByteArray? = byteArrayOf()
                    var decodedSignedPreKeyRecord: ByteArray? = byteArrayOf()
                    for (snapshot in dataSnapshot.children) {
                        if (snapshot.key == "IdentityKeyPairString") {
                            identityKeyPair = snapshot.value.toString()
                            decodedIdentityKeyPair = Base64.getDecoder().decode(identityKeyPair)
                        } else if (snapshot.key == "PreKeyIds") {
                            val bracketsRemoved = snapshot.value.toString()
                                .substring(1, snapshot.value.toString().length - 1)
                            preKeysArray = bracketsRemoved.split(", ").toTypedArray()
                        } else if (snapshot.key == "RegistrationId") {
                            registrationId = snapshot.value.toString().toInt()
                        } else if (snapshot.key == "SignedPreKeyRecordString") {
                            signedPreKeyRecord = snapshot.value.toString()
                            decodedSignedPreKeyRecord =
                                Base64.getDecoder().decode(signedPreKeyRecord)
                        }
                    }
                    try {
                        val remoteUserKeyModel = RegistrationKeyModel(
                            decodedIdentityKeyPair,
                            registrationId,
                            preKeysArray,
                            decodedSignedPreKeyRecord
                        )
                        val rec = remoteUserKeyModel.preKey
                        val prekeyid = rec.id
                        val preKeyPub = rec.keyPair
                        val prekeypublickey = preKeyPub.publicKey
                        val prekeyPublicKeyArray = prekeypublickey.serialize()
                        try {
                            val encryptedRemoteUser = EncryptedRemoteUser(
                                remoteUserKeyModel.getRegistrationId(),
                                remoteUserId,
                                2,
                                prekeyid,
                                prekeyPublicKeyArray,
                                remoteUserKeyModel.signedPreKeyId,
                                remoteUserKeyModel.signedPreKeyPublicKeyByteArray,
                                remoteUserKeyModel.signedPreKeySignatureByteArray,
                                remoteUserKeyModel.publicIdentityKey
                            )
                            localAndRemoteUser.setEncryptedRemoteUser(encryptedRemoteUser)
                            localRemoteUsers.callback(localAndRemoteUser)
                        } catch (e: Exception) {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
            println("THERE WAS AN ERROR WHILE BUILDING THE ENCRYPTED REMOTE USER")
        }
    }

    interface MyCallback<T> {
        fun callback(data: T)
    }
}
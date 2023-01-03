package com.journey13.exchainge.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;


public class FirebaseIDService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String theToken) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        super.onNewToken(theToken);

        if (firebaseUser != null) {
            updateToken(theToken);
        }
    }


    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tokens");
        Token token = new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(token);
    }

}

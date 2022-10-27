package com.journey13.exchainge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journey13.exchainge.Adapter.MessageAdapter;
import com.journey13.exchainge.Adapter.UserAdapter;
import com.journey13.exchainge.Model.User;

import java.util.ArrayList;
import java.util.List;

public class blockedUsers extends AppCompatActivity {

    private ListView blockedUsersList;
    private FirebaseUser fuser;
    private DatabaseReference reference, blockedContactsReference;
    private List<User> mUsers, mblockedUsers;
    private UserAdapter userAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        mUsers = new ArrayList<>();
        mblockedUsers = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        getBlockedIds();
    }

    // GET the ids of the users on the blocked list
    //TODO FIGURE OUT WHY THIS CODE WILL NOT UPDATE THE RECYCLER VIEW IN THE BLOCKED SECTION
    private void getBlockedIds() {
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(fuser.getUid());

        blockedContactsReference = reference.child("blocked");
        List<String> blocked_ids = new ArrayList<String>();

        blockedContactsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String contact_id = snapshot.getValue().toString();
                    blocked_ids.add(contact_id);

                    System.out.println("HERE IS THE ID OF A BLOCKED USER "+ contact_id);
                }
                System.out.println("WE are here, having completed the for loop before the readusers");
                readUsers(blocked_ids);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //TOO FIGURE OUT HOW TO GET THIS TO DISPLAY IN OUR BLOCKED CONTACTS LIST, MUCH IS COPIED FROM USERSFRAGMENT SO IT SHOULD WORK THE SAME BUT DOESNT
    private void readUsers(List<String> blockedList) {

        System.out.println("We are now inside the readusers method./..");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    mblockedUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        System.out.println("We are here man " + user.getUsername());

                        assert user != null;
                        assert firebaseUser != null;
                        if (blockedList.contains(user.getId())) {
                            mblockedUsers.add(user);
                        } else {
                            System.out.println("The user was not in the contacts list");
                        }

                    }
                    userAdapter = new UserAdapter(getApplicationContext(), mblockedUsers, false, false, true);
                    recyclerView.setAdapter(userAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

}
package com.journey13.exchainge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.journey13.exchainge.Adapter.UserAdapter;
import com.journey13.exchainge.Fragments.UsersFragment;
import com.journey13.exchainge.Model.User;

import java.util.ArrayList;
import java.util.List;

public class newContactsSearch extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private EditText search_users;
    private static final String TAG = "MyActivity";
    private Button addUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_search);

        //USERS DISPLAY RECYCLER
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUsers = new ArrayList<>();

        // USE THE CLALLBACK LISTENER AND THE USERID LOOKUP FUNCTION FROM GLOBAL METHODS
        // TO GET THE USERS CONTACTS SO THAT WE CAN REMOVE THEM FROM THE NEW CONTACT SEARCH
        GlobalMethods.getUserContacts(new GlobalMethods.MyCallback<ArrayList<String>>() {
            @Override
            public void callback(ArrayList<String> data) {
                System.out.println("got some contacts if that okay with you");
                //SEARCH BAR CONFIG
                search_users = findViewById(R.id.contacts_seachbar);
                search_users.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //DO NOTHING BEFORE
                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        userLookup(charSequence.toString().toLowerCase(), data);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        //DO NOTHING AFTER
                    }
                });
            }
        });
    }

    private void userLookup(String s, ArrayList<String> data) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert fuser != null;
                    // ONLY ADD USER TO LIST IF IT IS NOT THE CURRENT USER, THE USERS LIST IS NOT ALREADY
                    // 20 USERS LONG, THE SEARCH BAR IS NOT EMPTY AND THE USER IS NOT ALREADY IN THE CONTACTS LIST
                    if(!user.getId().equals(fuser.getUid()) && mUsers.size() < 20 && !s.equals("") && !data.contains(user.getId())) {
                        if (user.getSearchable()) {
                            mUsers.add(user);
                        } else {
                            System.out.println("The user was not searchable");
                        }
                    }
                }
                userAdapter = new UserAdapter(getApplicationContext(), mUsers, false, false, false);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
package com.journey13.exchainge.Fragments;

import android.content.Intent;
import android.media.MediaRouter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.journey13.exchainge.Model.User;
import com.journey13.exchainge.R;
import com.journey13.exchainge.newContactsSearch;
import com.journey13.exchainge.settingsChangePrivacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView, contactsRecyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers, mContacts;
    private List<String> userLookup;
    private Button newContactsButton;
    private EditText search_users;
    private FirebaseUser fuser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        newContactsButton = view.findViewById(R.id.find_contacts_button);
        newContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Go to contacts search
                //TODO figure out how to pass a list of contacts to this intent so that we can not show already added users in the search
                Intent intent = new Intent(getActivity(), newContactsSearch.class);
                startActivity(intent);
            }
        });

        //SET UP THE CONTACTS DISPLAY RECYCLER
        contactsRecyclerView = view.findViewById(R.id.recycler_view_contacts);
        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mContacts = new ArrayList<>();
        mUsers = new ArrayList<>();
        userLookup = new ArrayList<String>();

        search_users = view.findViewById(R.id.search_users);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toLowerCase());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // USE THE CLALLBACK LISTENER TO GET THE USERS CONTACTS
        getUserContacts(new MyCallback<ArrayList<String>>() {
            @Override
            public void callback(ArrayList<String> data) {
                readContacts(data);
            }
        });

        return view;
    }

    // TODO: FIX THIS SO WE EFFICIENTLY SEARCH FOR THE USERS IN OUR CONTACTS LIST
    private void searchUsers(String s) {

//        fuser = FirebaseAuth.getInstance().getCurrentUser();
////        Query query =  FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").orderByChild("search")
////                .startAt(s)
////                .endAt(s+"\uf8ff");
//
//        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
//
//        DatabaseReference searchRef = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(fuser.getUid());
//        Query query = searchRef.child("contacts").startAt(s).endAt(s+"\uf8ff");
//
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                mUsers.clear();
//                userLookup.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    String snapshotString = snapshot.getValue().toString();
//                    userLookup.add(snapshotString);
//
//                    System.out.println("Here is an id from the lookup" + snapshotString);
////                    User user = snapshot.getValue(User.class);
////
////                    if (!user.getId().equals(fuser.getUid())) {
////                        mUsers.add(user);
////                    }
//                }
//                userAdapter = new UserAdapter(getContext(), mUsers, false, true, false);
//                contactsRecyclerView.setAdapter(userAdapter);
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
    }

    // Function which uses callback to retrieve a list of ids for the users contacts
    private void getUserContacts(@NonNull MyCallback<ArrayList<String>> ids) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
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

    public interface MyCallback<T> {
        void callback(T data);
    }

    // TAKE CONTACTS LIST AND RETRIEVE RELEVANT USERS FOR DISPLAY
    private void readContacts(List<String> contactsList) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (search_users.getText().toString().equals("")) {

                    mContacts.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            if (contactsList.contains(user.getId())) {
                                System.out.println("Success we found a contact!");
                                mContacts.add(user);
                            }
                        }
                    }
                    userAdapter = new UserAdapter(getContext(), mContacts, false, true, false);
                    contactsRecyclerView.setAdapter(userAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    //OLD CONTACT READ FUNC IS HERE
    // RETRIEVE AND FORMAT A LIST OF CONTACTS RELATED TO THE CURRENT USER
//    private void getContacts() {
//
//        fuser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(fuser.getUid());
//
//        List<String> new_contacts = new ArrayList<String>();
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//
//                    String contact_ids = snapshot.getValue().toString();
//                    contact_ids = contact_ids.substring(1, contact_ids.length() - 1);
//                    List<String> contact_ids_list = Arrays.asList(contact_ids.split(", "));
//                    List<String> contact_ids_updated = new ArrayList<String>();
//                    List<String> new_contacts = new ArrayList<String>();
//
//                    String item = snapshot.getValue().toString();
//                    new_contacts.add(item);
//
//                    for (int i = 0; i < contact_ids_list.size(); i++) {
//                        String contact_item = contact_ids_list.get(i);
//                        contact_item = contact_item.split("=")[0];
//                        contact_ids_updated.add(contact_item);
//                    }
//                    System.out.println("Just CHECKING TO SEE IF THIS SHIT WORKS BECAUSE THIS IS FUCKING ANNOYING " + contact_ids_updated);
//                    readContacts(contact_ids_updated);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {}
//        });
//    }

}

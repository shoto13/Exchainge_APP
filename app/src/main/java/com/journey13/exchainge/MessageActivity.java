package com.journey13.exchainge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.journey13.exchainge.Adapter.MessageAdapter;
import com.journey13.exchainge.Adapter.UserAdapter;
import com.journey13.exchainge.Fragments.APIService;
import com.journey13.exchainge.Model.Chat;
import com.journey13.exchainge.Model.User;
import com.journey13.exchainge.Notifications.Client;
import com.journey13.exchainge.Notifications.Data;
import com.journey13.exchainge.Notifications.Response;
import com.journey13.exchainge.Notifications.Sender;
import com.journey13.exchainge.Notifications.Token;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.IdentityKeyStore;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;
import org.whispersystems.libsignal.state.SessionStore;
import org.whispersystems.libsignal.state.SignedPreKeyStore;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import Kotlin.ChatDao;
import Kotlin.ChatViewModel;
import Kotlin.ChatsDatabase;
import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.jvm.internal.Intrinsics;
import retrofit2.Call;
import retrofit2.Callback;
import java.sql.Timestamp;
import java.util.concurrent.ThreadLocalRandom;

public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView username, tagline, addContactText;
    private FirebaseUser fuser;
    private List<RegistrationKeyModel> mUsers;
    private DatabaseReference reference;
    private ImageButton btn_send, decline_contact_button;
    private EditText text_send;
    private MessageAdapter messageAdapter;
    private List<Chat> mChat;
    private RecyclerView recyclerView;
    private Toolbar addContactToolbar;
    private String userid;
    private ChatViewModel viewModel;
    private User user;

    //REMOTE USER ENCRYPTED VARIABLES
    String identityKeyPair;
    int registrationId;
    String[] preKeys;
    String signedPreKeyRecord;
    EncryptedRemoteUser encryptedRemoteUser;
    EncryptedLocalUser encryptedLocalUser;
    EncryptedSession encryptedSession;
    private List<Kotlin.Chat> localChatsForReceiver;
    List<Chat> localChatList;

    Intent intent;
    ValueEventListener seenListener;
    APIService apiService;
    boolean notify = false;
    private ChatsDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //TOOLBAR SETUP
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        // INIT VALUES
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        tagline = findViewById(R.id.taglineText);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        addContactToolbar = findViewById(R.id.add_contact_toolbar);
        addContactText = findViewById(R.id.add_contact_text);
        decline_contact_button = findViewById(R.id.decline_contact_button);
        mUsers = new ArrayList<>();
        mChat = new ArrayList<>();
        intent = getIntent();
        localChatList = new ArrayList<>();
        // // // // // // // // //


        //Build the remote user item from the intent extras
        User remoteUser = getUserFromExtras();
        String userid = remoteUser.getId();

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(remoteUser.getId());

        //SET UP THE SHARED PREFERENCES STRING AND REFERENCE, TO BE SENT TO OUR METHOD
        String storageString = fuser.getUid() + "_STORED_KEY_PREFS";
        SharedPreferences sharedPreferences = getSharedPreferences(storageString, Context.MODE_PRIVATE);

        Chat tempChat = new Chat("test", "test", "message", false, "today");
        mChat.add(tempChat);

        //CALLBACK FOR THE REMOTE/LOCAL ENCRYPTED USER
        GlobalMethods.getRemoteAndLocalEncryptedUser(new GlobalMethods.MyCallback<CreateLocalAndRemoteUser>()  {
            @Override
            public void callback(CreateLocalAndRemoteUser data) {
                System.out.println(data);
                try {
                    encryptedSession = new EncryptedSession(data.getEncryptedLocalUser(), data.getEncryptedRemoteUser());
                    System.out.println("the encrypted session was built! :):):):):)");
                } catch (Exception e) {
                    System.out.println("The Encrypted session could not be build, not too sure why just yet!");
                }
            }
        }, fuser, remoteUser.getId(), sharedPreferences);

        //INITIALISE THE RECYCLERVIEW
        initRecycler(mChat, remoteUser.getImageURL());

        //SET UP VIEWMODEL AND GET LOCAL MESSAGES STORED IN THE ROOM DATABASE
        viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        Context mContext = getApplicationContext();
        getLocalMessages(remoteUser);

        //SET THE CLICK LISTENER FOR THE SEND MESSAGE
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy");
                String ts = sdf.format(calendar.getTime());

                if (!msg.equals("")) {
                    storeMessageOnDb(fuser.getUid(), remoteUser.getId(), msg, ts);
                    storeLocalMessage(fuser.getUid(), remoteUser.getId(), msg, ts, viewModel);
                } else {
                    Toast.makeText(MessageActivity.this, "You cannot send empty messages", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        seenMessage(remoteUser);
        isContact(remoteUser);

        localChatsForReceiver = new ArrayList<>();

        //TODO : LOCAL MESSAGES NOW DISPLAYING CORRECTLY FROM THE LOCAL DB, NOW WE NEED SIMPLY TO
        //TODO : UPDATE THE SAME LIST WHEN A NEW MESSAGE IS DETECTED ON THE SERVER SO WE HAVE
        //TODO : BOTH REMOTE AND LOCAL MESSAGES DISPLAYING, AND NEW MESSAGES ARE BEING STORED ON THE LOCAL DEVICE
    }

    private User getUserFromExtras() {

        Boolean searchable = Boolean.parseBoolean(intent.getStringExtra("searchable"));
        Boolean searchable_by_email = Boolean.parseBoolean(intent.getStringExtra("searchable_by_email"));
        Boolean searchable_by_username = Boolean.parseBoolean(intent.getStringExtra("searchable_by_username"));

        User remoteUser = new User(
                intent.getStringExtra("user_id"),
                intent.getStringExtra("username"),
                intent.getStringExtra("tagline"),
                intent.getStringExtra("imageURL"),
                intent.getStringExtra("status"),
                intent.getStringExtra("search"),
                intent.getStringExtra("first_name"),
                intent.getStringExtra("second_name"),
                searchable,
                searchable_by_email,
                searchable_by_username);

        username.setText(remoteUser.getUsername());
        tagline.setText(remoteUser.getTagline());
        if (remoteUser.getImageURL().equals("default")) {
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(getApplicationContext()).load(remoteUser.getImageURL()).into(profile_image);
        }

        return remoteUser;
    }

    private void getLocalMessages(User remoteUser) {
        viewModel.getAllChats().observe(this, chatsList -> {
            localChatList.clear();
            for (Kotlin.Chat item : chatsList) {
                //Log.d("Chat", item.getMessage() + " sent by: " + item.getSender() + " Sent to: " + item.getReceiver());
                if (item.getReceiver().equals(remoteUser.getId()) && item.getSender().equals(fuser.getUid())) {
                    localChatsForReceiver.add(item);
                    Log.d("Chat, why no work?", item.getMessage() + " sent by " + item.getSender() + " send to: " +item.getReceiver());
                    Chat itemj = new Chat(item.getSender(), item.getReceiver(), item.getMessage(), false, item.getMessageTimestamp());
                    localChatList.add(itemj);
                }
            }
            messageAdapter.updateList(localChatList);
        });
    }

    private void seenMessage(User remoteUser) {

        String idRef = GlobalMethods.compareIdsToCreateReference(fuser.getUid(), remoteUser.getId());

        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats").child(idRef);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(remoteUser.getId())) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void storeMessageOnDb(String sender, String receiver, String message, String timestampString){

        String chat_db_ref = GlobalMethods.compareIdsToCreateReference(sender, receiver);

        //TODO SWAP BACK TO THIS WHEN READY TO ENCRYPT MESSAGES AGAIN
        //String encryptedMessage = encryptedSession.encrypt(message);
        String encryptedMessage = message;

        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats");
        final String userid = receiver;

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", encryptedMessage);
        hashMap.put("isSeen", false);
        hashMap.put("messageTimestamp", timestampString);

        reference.child(chat_db_ref).push().setValue(hashMap);

        // ADD message to sender (current user) chat list
        DatabaseReference chatRef = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        // SETUP new chat instance if one does not exist for this sender
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // ADD message to receiver chat list
        DatabaseReference chatRef2 = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());

        // SETUP new chat instance if one does not exist for this receiver
        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef2.child("id").setValue(fuser.getUid());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        final String msg = encryptedMessage;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void storeLocalMessage(String senderid, String receiverid, String message, String timestampString, ChatViewModel viewModel) {
        // SAVE THE MESSAGES LOCALLY
        Kotlin.Chat chat = new Kotlin.Chat(message, timestampString, receiverid, senderid);

        viewModel.insertChat(chat);
    }

    private void sendNotification(String receiver, String username, String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(),
                            R.mipmap.ic_launcher,
                            username+": "+message,
                            "New Message",
                            receiver
                    );

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {}
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void readMessages(final String myid, final User remoteUser, final String imageurl) {

        String userid = remoteUser.getId();
        //  READ MESSAGES FROM THE REMOTE DATABASE
        String idRef = GlobalMethods.compareIdsToCreateReference(myid, remoteUser.getId());
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats").child(idRef);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
//                    if (chat.getReceiver() != null  || chat.getSender() != null) {
                    //chat.getReceiver() != null && chat.getSender() != null && chat.getReceiver().equals(userid) && chat.getSender().equals(myid)
                    assert chat != null;
                    if (chat.getReceiver() != null && chat.getSender() != null && chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) {
                            String encryptedMessage = chat.getMessage();

                            // TODO REPLACE THIS LINE WHEN WE ARE READY TO ENCRYPT/DECRYPT AGAIN
                            //String decryptedMessage = encryptedSession.decrypt(encryptedMessage);
                            String decryptedMessage = encryptedMessage;
                            chat.setMessage(decryptedMessage);

                        }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    private void initRecycler(List<Chat> mChat, String imageurl) {
        messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
        recyclerView.setAdapter(messageAdapter);
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    private void isContact(User remoteUser) {
        DatabaseReference contactsReference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(fuser.getUid());
        DatabaseReference userIdContactsReference = contactsReference.child("contacts").child(remoteUser.getId());

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    addContactToolbar.setVisibility(View.VISIBLE);

                    addContactText.setOnClickListener((View view) -> {
                        // Initializing the popup menu and giving the reference as current context
                        PopupMenu popupMenu = new PopupMenu(MessageActivity.this, addContactToolbar);

                        // Inflating popup menu from popup_menu.xml file
                        popupMenu.getMenuInflater().inflate(R.menu.add_contact_menu, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                // Toast message on menu item clicked
                                Toast.makeText(MessageActivity.this, "User added to contacts", Toast.LENGTH_LONG).show();
                                contactsReference.child("contacts").child(remoteUser.getId()).setValue(remoteUser.getId());
                                addContactToolbar.setVisibility(View.GONE);
                                return true;
                            }
                        });
                        // Showing the popup menu
                        popupMenu.show();
                    });

                } else {
                    decline_contact_button.setOnClickListener((View view) -> {
                        // Press the cross button in  the toolbar to hide the add contact banner
                        addContactToolbar.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database error", databaseError.getMessage());
            }
        };
        userIdContactsReference.addListenerForSingleValueEvent(eventListener);
    }

    public interface MyCallback<T> {
        void callback(T data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("Online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("Offline");
        currentUser("none");

    }

}
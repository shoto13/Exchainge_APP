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
import com.google.gson.Gson;
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
import java.util.ServiceConfigurationError;
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
    private List<Chat> mChat, chatty;
    private RecyclerView recyclerView;
    private Toolbar addContactToolbar;
    private String userid;
    private ChatViewModel viewModel;
    private User user;
    private List<String> serverDeletionMessageList;

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
    ValueEventListener remoteMessageListener;
    APIService apiService;
    boolean notify = true;
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
        chatty = new ArrayList<>();
        intent = getIntent();
        localChatList = new ArrayList<>();
        serverDeletionMessageList = new ArrayList<>();
        // // // // // // // // //

        //Build the remote user item from the intent extras
        User remoteUser = getUserFromExtras();
        String userid = remoteUser.getId();

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(remoteUser.getId());

        //SET UP THE SHARED PREFERENCES STRING AND REFERENCE, TO BE SENT TO OUR METHOD
        String storageString = fuser.getUid() + "_STORED_KEY_PREFS";
        SharedPreferences sharedPreferences = getSharedPreferences(storageString, Context.MODE_PRIVATE);

        Chat tempChat = new Chat("test", "test", "", false, "today");
        chatty.add(tempChat);

        //CALLBACK FOR THE REMOTE/LOCAL ENCRYPTED USER
            GlobalMethods.getRemoteAndLocalEncryptedUser(new GlobalMethods.MyCallback<LocalAndRemoteUserModel>() {
                @Override
                public void callback(LocalAndRemoteUserModel data) {
                    System.out.println(data);
                    try {
                            Log.d("VARIABLE_SESSION_LISTENER", "the session was equal to null, i.e. the session did not exist");
                            encryptedSession = new EncryptedSession(data.getEncryptedLocalUser(), data.getEncryptedRemoteUser());

                            Log.d("Encrypted_session_notifier", "the encrypted session was built! :):):):):)");

                            getRemoteMessages(new MyCallback<ArrayList<Chat>>() {
                                @Override
                                public void callback(ArrayList<Chat> data) {
                                    //storeLocalMessagesAsList(data);
                                    //seenMessage(remoteUser);
                                    deleteServerMessages(fuser, remoteUser, serverDeletionMessageList);
                                    getLocalMessages(remoteUser, data);
                                    for (Chat item : data) {
                                        Log.d("info_Return", item.getMessage());
                                    }
                                }
                            }, remoteUser, fuser);


                    } catch (Exception e) {
                        Log.d("Encrypted_session_notifier", "The encrypted session could not be built");
                    }
                }
            }, fuser, remoteUser.getId(), sharedPreferences);

        //INITIALISE THE RECYCLERVIEW
        initRecycler(chatty, remoteUser.getImageURL());

        //SET UP VIEWMODEL AND GET LOCAL MESSAGES STORED IN THE ROOM DATABASE
        viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        Context mContext = getApplicationContext();

        //SET THE CLICK LISTENER FOR THE SEND MESSAGE
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy");
                String ts = sdf.format(calendar.getTime());

                Chat chat = new Chat(fuser.getUid(), remoteUser.getId(), msg, false, ts);

                if (!chat.getMessage().equals("")) {
                    storeMessageOnDb(chat);
                    storeLocalMessage(chat, viewModel);
                } else {
                    Toast.makeText(MessageActivity.this, "You cannot send empty messages", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        //seenMessage(remoteUser);
        isContact(remoteUser);

        localChatsForReceiver = new ArrayList<>();
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

    private void getLocalMessages(User remoteUser, List<Chat> remoteMessages) {
        viewModel.getAllChats().observe(this, chatsList -> {
            localChatList.clear();
            for (Kotlin.Chat item : chatsList) {
                if (item.getReceiver().equals(remoteUser.getId()) && item.getSender().equals(fuser.getUid()) ||
                    item.getReceiver().equals(fuser.getUid()) && item.getSender().equals(remoteUser.getId())) {
                    //localChatsForReceiver.add(item);
                    Log.d("Chat_messager_listing", item.getMessage() + " sent by " + item.getSender() + " send to: " +item.getReceiver() + "here is the UMID " + item.getUMID());
                    Chat itemj = new Chat(item.getSender(), item.getReceiver(), item.getMessage(), false, item.getMessageTimestamp(), item.getUMID());
                    if (!localChatList.contains(itemj)) {
                        localChatList.add(itemj);
                    }
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
                    if (dataSnapshot.getChildren() != null) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(remoteUser.getId())) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isSeen", true);
                            snapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void storeMessageOnDb(Chat chat_to_store){

        //hat chat_to_add = new Chat(sender, receiver, message, false, timestampString);
        String chat_db_ref = GlobalMethods.compareIdsToCreateReference(chat_to_store.getSender(), chat_to_store.getReceiver());

        String encryptedMessage = encryptedSession.encrypt(chat_to_store.getMessage());

        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats");
        final String userid = chat_to_store.getReceiver();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", chat_to_store.getSender());
        hashMap.put("receiver", chat_to_store.getReceiver());
        hashMap.put("message", encryptedMessage);
        hashMap.put("isSeen", false);
        hashMap.put("messageTimestamp", chat_to_store.getMessageTimestamp());
        hashMap.put("UMID", chat_to_store.getUMID());

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
        Log.d("Notification_notifier_1", "So we are in the store message on db function, before the notifier begins");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Notification_notifier_2", "The data has changed so we are looking at the snapshot");

                User user = snapshot.getValue(User.class);
                if (notify) {
                    Log.d("Notification_notifier_3", "We are inside the if statement for the notifier inside the message activity");
                    sendNotification(chat_to_store.getReceiver(), user.getUsername(), "test notification");
                }
                //TODO notification switch should be switched off maybe? CHECK?
                notify = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void storeLocalMessage(Chat chat_to_store, ChatViewModel viewModel) {
        // SAVE THE MESSAGES LOCALLY
        Kotlin.Chat chat = new Kotlin.Chat(chat_to_store.getMessage(), chat_to_store.getMessageTimestamp(), chat_to_store.getReceiver(), chat_to_store.getSender(), chat_to_store.getUMID());
        viewModel.insertChat(chat);

    }

    private void storeLocalMessagesFromList(List<Chat> chat_list) {

        for (Chat item : chat_list) {
            Kotlin.Chat chat = new Kotlin.Chat(item.getMessage(), item.getMessageTimestamp(), item.getReceiver(), item.getSender(), item.getUMID());

            viewModel.insertChat(chat);
        }
    }

    private void sendNotification(String receiver, String username, String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        Log.d("Notification_notifier_4", "We are now inside the send notification function in messageactivity");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("Notification_notifier_5", "Inside the ondatachange for the tokens in the sendnotification function");
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
                                    Log.d("Notification_notifier_6", "We are inside the apiservice send notification service");
                                    if (response.code() == 200) {
                                        Log.d("Notification_notifier_7", "inside the response service here is the response code " + response.code());
                                        if (response.body().success == 1) {
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        } else if (response.body().success == 0) {
                                            Toast.makeText(MessageActivity.this, "Successfully sent noti", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.d("Notification_notifier_7", "inside the response service here is the response code " + response.code());
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

    private void getRemoteMessages(@NonNull MyCallback<ArrayList<Chat>> remoteChats, User remoteUser, FirebaseUser localUser) {
        String userid = remoteUser.getId();
        //  READ MESSAGES FROM THE REMOTE DATABASE
        String idRef = GlobalMethods.compareIdsToCreateReference(localUser.getUid(), remoteUser.getId());
        List<Chat> remChats = new ArrayList<>();
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats").child(idRef);
        remoteMessageListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                remChats.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        String messageid = snapshot.getKey();
//                    if (chat.getReceiver() != null  || chat.getSender() != null) {
                        //chat.getReceiver() != null && chat.getSender() != null && chat.getReceiver().equals(userid) && chat.getSender().equals(myid)localUser.getUid()
                        assert chat != null;
                        if (chat.getReceiver() != null && chat.getSender() != null && chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(remoteUser.getId())) {
                            String encryptedMessage = chat.getMessage();
                            serverDeletionMessageList.add(messageid);
                            if (encryptedMessage != null) {
                                Log.d("encrypted_message", "Here is the encrypted message item " + encryptedMessage);
                                Log.d("encrypted_message", "Here is the encrypted message directly from the chat item " + chat.getMessage());
                                String decryptedMessage = encryptedSession.decrypt(encryptedMessage);
                                chat.setMessage(decryptedMessage);
                                remChats.add(chat);
                            }
                            //String decryptedMessage = encryptedMessage;
                            //storeLocalMessageFromJavaChat(chat);
                        }
                    }
                } else {
                    Log.d("encrypted_message_notifier", "There was no relevant messages on the server to retrieve");
                }

                //decryptListOfMessagesFromServer(remChats)
                storeLocalMessagesFromList(remChats);
                remoteChats.callback((ArrayList<Chat>) remChats);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    private void deleteServerMessages(FirebaseUser localUser, User remoteUser, List<String> messagesToDelete) {

        String idRef = GlobalMethods.compareIdsToCreateReference(localUser.getUid(), remoteUser.getId());
        reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats").child(idRef);

        for (String id : messagesToDelete) {
            reference.child(id).removeValue();
        }
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
        reference.removeEventListener(remoteMessageListener);
        //reference.removeEventListener(seenListener);
        status("Offline");
        currentUser("none");

    }

}
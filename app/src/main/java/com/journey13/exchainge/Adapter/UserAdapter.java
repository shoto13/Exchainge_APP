package com.journey13.exchainge.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journey13.exchainge.MessageActivity;
import com.journey13.exchainge.Model.Chat;
import com.journey13.exchainge.Model.User;
import com.journey13.exchainge.R;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;

    private List<User> mUsers;
    private boolean ischat;
    private boolean isContact;
    private String lastMessage;
    private String lastMessageTime;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat, boolean isContact) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
        this.isContact = isContact;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        //  GET USER PROFILE PIC OR USE DEFAULT
        if (user.getImageURL().equals("deafault")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        // CHECK IF THE ADAPTER IS BEING IMPLEMENTED AS A CHAT OR NOT
        if (ischat) {
            lastMessage(user.getId(), holder.last_message, holder.message_timestamp);
        } else {
            holder.last_message.setVisibility(View.GONE);
            holder.message_timestamp.setVisibility(View.GONE);
        }

        // GET ONLINE/OFFLINE STATUS OF USER
        if (ischat){
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        // CHECK IF USER IS A CONTACT OR NOT AND ACT ACCORDINGLY
        if (!isContact){
            holder.add_contact_button.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addContact(user.getId());
                }
            });

        // IF USER IS ALREADY A CONTACT, REMOVE FUNCTIONALITY TO ADD THEM, CREATE CHAT ACTIVITY IF USER IS CLICKED
        } else {
            holder.add_contact_button.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", user.getId());
                    mContext.startActivity(intent);
                }
            });

            // IF USER IS A CONTACT ALREADY ADD CONTACT MENU FUNCTIONALITY
            holder.tripledot_user_menu.setVisibility(View.VISIBLE);

            holder.tripledot_user_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(mContext, holder.tripledot_user_menu);
                    popup.inflate(R.menu.user_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            System.out.println("WE ARE HERE INSIDE THE ONCLICK LISTENER");
                            switch (menuItem.getItemId()) {
                                case R.id.delete_conversation:
                                    Toast.makeText(mContext, "You clicked to delete this conversation", Toast.LENGTH_SHORT).show();

                                    // Get reference to our Chat lists
                                    DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist");
                                    // Get reference to the specific conversation
                                    DatabaseReference requestingUserReference = reference.child(fUser.getUid()).child(user.getId());
                                    //Remove this user's version of the chatlist
                                    requestingUserReference.removeValue();

                                    //Find out if the other version of the chat still exists on the other participant's device.
                                    DatabaseReference alternativeReference = reference.child(user.getId()).child(fUser.getUid());

                                    ValueEventListener eventListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()) {
                                                Toast.makeText(mContext, "The conversation still exists with the other participant", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(mContext, "The conversation does not exist with the other participant", Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d("Database error", databaseError.getMessage());
                                        }
                                    };
                                    alternativeReference.addListenerForSingleValueEvent(eventListener);

                                    //If the other version does exist then return and do nothing

                                    //Todo If the other version does not exist then delete all the messages which were related to this conversation

                                    break;
                                case R.id.block_contact:
                                    Toast.makeText(mContext, "You clicked to block this user", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private ImageView img_off;
        private ImageView img_on;
        private TextView last_message;
        private TextView message_timestamp;
        private TextView tripledot_user_menu;
        private Button add_contact_button;
        private Button user_options_menu_button;


        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_online);
            img_off = itemView.findViewById(R.id.img_offline);
            last_message = itemView.findViewById(R.id.last_message);
            message_timestamp = itemView.findViewById(R.id.message_time);
            add_contact_button = itemView.findViewById(R.id.add_contact_button);
            user_options_menu_button = itemView.findViewById(R.id.user_menu_button);
            tripledot_user_menu = itemView.findViewById(R.id.tripledot_user_menu);
        }
    }

    //Get most recent message
    private void lastMessage(String userid, TextView last_message, TextView message_timestamp) {
        lastMessage = "default";
        lastMessageTime = "hh:mm dd/mm/yy";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)
                            || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        lastMessage = chat.getMessage();
                        lastMessageTime = chat.getMessageTimestamp();
                    }
                }

                switch (lastMessage) {
                    case "default":
                        last_message.setText("~~~");
                        break;

                    default:
                        last_message.setText(lastMessage);
                        break;
                }
                lastMessage = "default";

                switch (lastMessageTime) {
                    case "default":
                        message_timestamp.setText("~~~");
                        break;

                    default:
                        message_timestamp.setText(lastMessageTime);
                        break;
                }
//                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addContact(String userid) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(firebaseUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<String ,Object>();
        hashMap.put(userid, userid);

        reference.child("contacts").child(userid).setValue(userid);


    }

    // CREATE FUNCTION TO ADD USER WHEN BUTTON PRESSED
    private void addUser () {

    }
}

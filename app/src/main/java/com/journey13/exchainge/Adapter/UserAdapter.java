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
import com.journey13.exchainge.GlobalMethods;
import com.journey13.exchainge.MessageActivity;
import com.journey13.exchainge.Model.Chat;
import com.journey13.exchainge.Model.User;
import com.journey13.exchainge.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;
    private boolean isContact;
    private boolean isBlocked;
    private String lastMessage;
    private String lastMessageTime;

    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat, boolean isContact, boolean isBlocked) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
        this.isContact = isContact;
        this.isBlocked = isBlocked;
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
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addContact(user);
                }
            });
        // IF USER IS ALREADY A CONTACT, REMOVE FUNCTIONALITY TO ADD THEM, CREATE CHAT ACTIVITY IF USER IS CLICKED
        } else {
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
                            switch (menuItem.getItemId()) {
                                case R.id.delete_conversation:
                                    Toast.makeText(mContext, "You clicked to delete this conversation", Toast.LENGTH_SHORT).show();
                                    // Get reference to our Chat lists
                                    DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chatlist");
                                    // Get reference to the specific conversation and remove
                                    DatabaseReference requestingUserReference = reference.child(fUser.getUid()).child(user.getId());
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
                                    //RUN THE BLOCK/UNBLOCK FUNCTION
                                    block_or_unblock_user(false, fUser, user);
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }

        if (isBlocked) {
            holder.unblock_user_button.setVisibility(View.VISIBLE);
            holder.tripledot_user_menu.setVisibility(View.GONE);
            holder.unblock_user_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "The unblock button was pressed", Toast.LENGTH_SHORT).show();
                    block_or_unblock_user(true, fUser, user);
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
        private Button unblock_user_button;


        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_online);
            img_off = itemView.findViewById(R.id.img_offline);
            last_message = itemView.findViewById(R.id.last_message);
            message_timestamp = itemView.findViewById(R.id.message_time);
            tripledot_user_menu = itemView.findViewById(R.id.tripledot_user_menu);
            unblock_user_button = itemView.findViewById(R.id.unblock_user_button);
        }
    }

    //FUNCTION TO GET THE MOST RECENT MESSAGE IN A CONVERSATION AND ALSO GRAB THE TIMESTAMP AND FORMAT
    // THE TIMESTAMP SO THAT IT CAN EFFECTIVELY DISPLAY WHEN THE CHAT TOOK PLACE
    private void lastMessage(String userid, TextView last_message, TextView message_timestamp) {

        lastMessage = "default";
        lastMessageTime = "";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String idRef = GlobalMethods.compareIdsToCreateReference(firebaseUser.getUid(), userid);

        System.out.println("the idref in this case is " + idRef);

        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats").child(idRef);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)
                            || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        lastMessage = chat.getMessage();
                        lastMessageTime = chat.getMessageTimestamp();}
                }

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy");
                String ts = sdf.format(calendar.getTime());

                //SPLIT OUT THE DATE STRINGS SO WE CAN COMPARE THEM
                //VAl 1 is month (MM)
                //Val 2 is year (YY)
                String[] current_date_split = ts.split("/");
                String[] message_date_split = lastMessageTime.split("/");

                //VAL 0 is hh:mm
                //VAL 1 is Day (DD)
                String[] time_day_split_current = current_date_split[0].split(" ");
                String[] time_day_split_message = message_date_split[0].split(" ");

                // TIME VALS ONLY hh:mm
                String current_time_only = time_day_split_current[0];
                String message_time_only = time_day_split_message[0];

                System.out.println("The current time in line 262 is: " + current_time_only);
                System.out.println("The message time in line 263 is : " + message_time_only);

                //SPLIT time strings into minutes and hours
                String[] minute_time_current = current_time_only.split(":");
                String[] minute_time_message = message_time_only.split(":");

                System.out.println("The minute time of the message is as follows: " + minute_time_message);
                System.out.println("The minute time currently is as follows: " + minute_time_message);

                //CONVERT MINUTES AND HOURS INTO INTEGERS
                int current_mins = Integer.parseInt(minute_time_current[1]);
                int current_hours = Integer.parseInt(minute_time_current[0]);

                System.out.println("Current Minutes from the current mins int " + current_mins);
                System.out.println("Current hours from the current hours int " + current_hours);

                int message_mins = Integer.parseInt(minute_time_message[1]);
                int message_hours = Integer.parseInt(minute_time_message[0]);

                int current_mins_total = current_hours * 60 + current_mins;
                int message_mins_total = message_hours * 60 + message_mins;

                // GET THE DIFFERENCE BETWEEN THE TWO TIMES IN MINUTES
                int message_current_time_difference = current_mins_total - message_mins_total;

                // IF STATEMENTS BELOW DETERMINE HOW LONG AGO MESSAGES WERE SEND AND THEN FORMATS THE OUTPUT TO THE USER
                if (ts.equals(lastMessageTime)) {
                    message_timestamp.setText("Now");
                } else if (message_current_time_difference < 60
                            && message_current_time_difference > 1
                            && current_date_split[1].equals(message_date_split[1])
                            && current_date_split[2].equals(message_date_split[2])
                            && time_day_split_current[1].equals(time_day_split_message[1])) {

                    String displayString = message_current_time_difference + " minutes ago";
                    message_timestamp.setText(displayString);

                } else if (current_date_split[1].equals(message_date_split[1])
                        && current_date_split[2].equals(message_date_split[2])
                        && time_day_split_current[1].equals(time_day_split_message[1])) {
                    if (message_current_time_difference >=60 && message_current_time_difference < 120) {
                        message_timestamp.setText("an hour ago");
                    } else if (message_current_time_difference >= 120 && message_current_time_difference < 180) {
                        message_timestamp.setText("2 hours ago");
                    } else if (message_current_time_difference >= 180 && message_current_time_difference < 240) {
                        message_timestamp.setText("3 hours ago");
                    } else {
                        message_timestamp.setText(message_time_only);
                    }
                } else if (current_date_split[2].equals(message_date_split[2])) {
                    String day_month = time_day_split_message[1] + "/" + message_date_split[1];
                    message_timestamp.setText(day_month);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //FUNCTION TO ADD A NEW USER
    private void addContact(User user) {

        String userId = user.getId();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(firebaseUser.getUid());
        reference.child("contacts").child(userId).setValue(userId);
        Toast.makeText(mContext, "The user has been added to your contacts list", Toast.LENGTH_SHORT).show();
    }

    // FUNCTION TO EITHER BLOCK OR UNBLOCK A USER
    private void block_or_unblock_user (Boolean isBlocked, FirebaseUser fUser, User user) {

        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Contacts").child(fUser.getUid());
        DatabaseReference reference2 = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Blocked")
                .child(fUser.getUid());

        // IF ISBLOCKED IS FALSE THEN THE CURRENT USER IS TRYING TO BLOCK THIS USER
        if (!isBlocked) {
            //STEP 1 CHECK IF THE USER IS IN CONTACTS & REMOVE IF SO
            DatabaseReference userIdContactsReference = reference.child("contacts").child(user.getId());
            ValueEventListener blockedContactEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userIdContactsReference.removeValue();
                    } else {
                        Toast.makeText(mContext, "The user was not a contact", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Database error", databaseError.getMessage());
                }
            };
            userIdContactsReference.addListenerForSingleValueEvent(blockedContactEventListener);

            //STEP 2 PLACE THE USER IN THE BLOCKED SECTION IN SETTINGS
            reference2.child("contacts").child(user.getId()).setValue(user.getId());

            //STEP 3 MAKE IT IMPOSSIBLE FOR USERS TO CONTACT EACHOTHER

            //STEP 4 ADD THE BLOCKED USER TO THE BLOCKED USER LIST

            //STEP 5 MAKE IT POSSIBLE TO REMOVE BLOCKED USER AND RE-ENABLE CONTACT WHEN REMOVED
            //

        } else {
            DatabaseReference blockedUserReference = reference2.child("contacts").child(user.getId());
            ValueEventListener unblockedContactEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        blockedUserReference.removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("Database error", databaseError.getMessage());
                }
            };
            blockedUserReference.addListenerForSingleValueEvent(unblockedContactEventListener);
            //TODO : FIGURE OUT WHY ITEMS ARE NOT CORRECTLY BEING REMOVED FROM THE RECYCLER

        }
    }

    // FUNCTION ATTEMPTING TO REMOVE DATA FROM THE RECYCLER LIST
    public void removeAt(int position) {
        System.out.println("We have reached the removal function now");
        mUsers.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mUsers.size());
    }
}

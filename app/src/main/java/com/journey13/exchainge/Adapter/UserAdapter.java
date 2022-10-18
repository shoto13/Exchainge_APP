package com.journey13.exchainge.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        if (user.getImageURL().equals("deafault")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat) {
            lastMessage(user.getId(), holder.last_message);
        } else {
            holder.last_message.setVisibility(View.GONE);
        }

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

        if (!isContact){
            holder.add_contact_button.setVisibility(View.VISIBLE);
        } else {
            holder.add_contact_button.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });
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
        private Button add_contact_button;


        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_online);
            img_off = itemView.findViewById(R.id.img_offline);
            last_message = itemView.findViewById(R.id.last_message);
            add_contact_button = itemView.findViewById(R.id.add_contact_button);
        }
    }

    //Get most recent message
    private void lastMessage(String userid, TextView last_message) {
        lastMessage = "default";
        lastMessageTime = "hh:mm dd/mm/yy";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://exchainge-db047-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    System.out.println(chat.getMessage());
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)
                            || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                        lastMessage = chat.getMessage();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // CREATE FUNCTION TO ADD USER WHEN BUTTON PRESSED
    private void addUser () {

    }
}

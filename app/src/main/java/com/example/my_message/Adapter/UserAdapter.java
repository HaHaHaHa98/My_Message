package com.example.my_message.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.my_message.MessageActivity;
import com.example.my_message.Model.Chat;
import com.example.my_message.Model.User;
import com.example.my_message.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<User> mUser;
    private boolean isChat;
    private String theLastMessage;

    public UserAdapter(Context mContext, ArrayList<User> mUser, boolean isChat) {
        this.mContext = mContext;
        this.mUser = mUser;
        this.isChat = isChat;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUser.get(position);
        holder.tvUsername.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.imgProfile);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });

        if(isChat){
            getLassMessage(user.getId(),holder.tvLassMess);
        }else {
            holder.tvLassMess.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            } else {
                holder.imgOn.setVisibility(View.GONE);
                holder.imgOff.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imgOn.setVisibility(View.GONE);
            holder.imgOff.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvUsername  ,tvLassMess;
        public ImageView imgProfile, imgOn, imgOff;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvUsername = itemView.findViewById(R.id.tvUsername);
            this.imgProfile = itemView.findViewById(R.id.imgProfile);
            this.imgOn = itemView.findViewById(R.id.imgOn);
            this.imgOff = itemView.findViewById(R.id.imgOff);
            this.tvLassMess = itemView.findViewById(R.id.tvLassMess);
        }

    }

    private void getLassMessage(String userId, TextView tvLassMess) {
        theLastMessage = "default";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                        theLastMessage = chat.getMessage();

                    }
                    switch (theLastMessage) {
                        case "default":
                            tvLassMess.setText("No message");
                            break;
                        default:
                            tvLassMess.setText(theLastMessage);
                            break;
                    }
                    theLastMessage="default";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}

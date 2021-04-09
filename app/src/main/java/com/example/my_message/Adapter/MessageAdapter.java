package com.example.my_message.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.my_message.Model.Chat;
import com.example.my_message.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TITLE_LEFT = 0;
    public static final int MSG_TITLE_RIGHT = 1;
    private FirebaseUser firebaseUser;
    private String imgURL;
    private ArrayList<Chat> mChat;
    private Context mContext;

    public MessageAdapter(Context mContext, ArrayList<Chat> mChat, String imgURL) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imgURL = imgURL;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TITLE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.tvMessage.setText(chat.getMessage());
        if (imgURL.equals("default")) {
            holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imgURL).into(holder.imgProfile);
        }
        if (position == mChat.size() - 1) {
            if (chat.getIsSeen()) {
                holder.tvSeen.setText("Seen");
            } else {
                holder.tvSeen.setText("Delivery");
            }
        } else {
            holder.tvSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }


    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TITLE_RIGHT;
        } else {
            return MSG_TITLE_LEFT;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgProfile;

        public TextView tvMessage, tvSeen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            imgProfile = (ImageView) itemView.findViewById(R.id.imgProfile);
            tvSeen = itemView.findViewById(R.id.tv_seen);
        }
    }
}
/*
public static final int MSG_TITLE_LEFT = 0;
    public static final int MSG_TITLE_RIGHT = 1;
    private FirebaseUser firebaseUser;
    private String imgURL;
    private ArrayList<Chat> mChat;
    private Context mContext;

    public MessageAdapter(Context mContext, ArrayList<Chat> mChat, String imgURL) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imgURL = imgURL;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.tvMessage.setText(chat.getMessage());
        if (imgURL.equals("default")) {
            holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imgURL).into(holder.imgProfile);
        }
        if (position == mChat.size() - 1) {
            if (chat.isSeen()) {
                holder.tvSeen.setText("Seen");
            } else {
                holder.tvSeen.setText("Delivery");
            }
        } else {
            holder.tvSeen.setVisibility(View.GONE);
        }
    }

    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return 1;
        }
        return 0;
    }

    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgProfile;

        public TextView tvMessage, tvSeen;

        public ViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            imgProfile = (ImageView) itemView.findViewById(R.id.imgProfile);
            tvSeen = itemView.findViewById(R.id.tv_seen);
        }
    }
 */
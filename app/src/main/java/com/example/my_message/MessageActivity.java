package com.example.my_message;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.my_message.Adapter.MessageAdapter;
import com.example.my_message.Model.Chat;
import com.example.my_message.Model.User;
import com.example.my_message.Notification.ApiService;
import com.example.my_message.Notification.Client;
import com.example.my_message.Notification.Data;
import com.example.my_message.Notification.MyResponse;
import com.example.my_message.Notification.Sender;
import com.example.my_message.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private ImageButton btnSend;
    public EditText etSend;
    public FirebaseUser firebaseUser;
    public CircleImageView imgProfile;
    public ArrayList<Chat> mChat;
    public MessageAdapter messageAdapter;
    private DatabaseReference myRef;
    public RecyclerView rvMessage;
    private Toolbar toolbar;
    public TextView tvUsername;
    private Intent intent;
    private ValueEventListener seenListener;
    private ApiService apiService;
    private String userId;
    private boolean notify = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_message);
        imgProfile = (CircleImageView) findViewById(R.id.imgProfile);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.etSend);
        rvMessage = (RecyclerView) findViewById(R.id.rvMessage);
        rvMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        rvMessage.setLayoutManager(linearLayoutManager);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);

        intent = getIntent();
        userId = intent.getStringExtra("userId");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        myRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                User user = (User) snapshot.getValue(User.class);
                tvUsername.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    imgProfile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into((ImageView) imgProfile);
                }

                readMessage(firebaseUser.getUid(), userId, user.getImageURL());
            }

            public void onCancelled(DatabaseError error) {
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                notify = true;
                String msg = etSend.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(), userId, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message!", Toast.LENGTH_SHORT).show();
                }
                etSend.setText("");
            }
        });

        seenMessage(userId);
    }

    private void seenMessage(final String userId) {
        myRef = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) {
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


    public void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen", false);
        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(userId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(notify){
                    sendNotification(receiver, user.getUsername(), msg);
                    notify=false;
                }
            }

            private void sendNotification(String receiver, String username, String message) {
                DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
                Query query = tokens.orderByKey().equalTo(receiver);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Token token = snapshot.getValue(Token.class);
                            Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, username + ":" + message, "New message", userId);
                            Sender sender = new Sender(data, token.getToken());
                            apiService.sendNotification(sender)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.code() == 200) {
                                                if (response.body().success != 1) {
                                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void readMessage(final String myId, final String userId, final String imageURL) {
        mChat = new ArrayList<>();
        myRef = FirebaseDatabase.getInstance().getReference("Chats");
        myRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = (Chat) snapshot.getValue(Chat.class);
                    if ((chat.getReceiver().equals(myId) && chat.getSender().equals(userId)) || (chat.getReceiver().equals(userId) && chat.getSender().equals(myId))) {
                        mChat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(getBaseContext(), mChat, imageURL);
                    rvMessage.setAdapter(messageAdapter);
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void setStatus(String status) {
        myRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        myRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        myRef.removeEventListener(seenListener);
        setStatus("offline");
    }
}
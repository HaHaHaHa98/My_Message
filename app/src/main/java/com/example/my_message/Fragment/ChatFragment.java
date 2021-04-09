package com.example.my_message.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_message.Adapter.UserAdapter;
import com.example.my_message.Model.Chat;
import com.example.my_message.Model.ChatList;
import com.example.my_message.Model.User;
import com.example.my_message.Notification.Token;
import com.example.my_message.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;



public class ChatFragment extends Fragment {


    public FirebaseUser firebaseUser;

    public ArrayList<User> mUser;
    private DatabaseReference myRef;

    public RecyclerView rvChat;

    public UserAdapter userAdapter;

    public ArrayList<ChatList> userList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvChat);
        rvChat = recyclerView;
        rvChat.setHasFixedSize(true);
        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        userList = new ArrayList<>();
        myRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatList=snapshot.getValue(ChatList.class);
                    userList.add(chatList);
                }
                getChatList();
            }

            public void onCancelled(DatabaseError error) {
            }
        });
        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }

    private void getChatList(){
        mUser=new ArrayList<>();
        myRef=FirebaseDatabase.getInstance().getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User user=snapshot.getValue(User.class);
                    for(ChatList chatList:userList){
                        if(user.getId().equals(chatList.getId())){
                            mUser.add(user);
                        }
                    }
                    userAdapter=new UserAdapter(getContext(),mUser,true);
                    rvChat.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void updateToken(String token){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1=new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);

    }

}
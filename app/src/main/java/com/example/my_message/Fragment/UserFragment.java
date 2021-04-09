package com.example.my_message.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.my_message.Adapter.UserAdapter;
import com.example.my_message.Model.User;
import com.example.my_message.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class UserFragment extends Fragment {

    public ArrayList<User> mUsers;

    public RecyclerView rvUser;

    public UserAdapter userAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        rvUser = (RecyclerView) view.findViewById(R.id.rvUser);
        rvUser.setHasFixedSize(true);
        rvUser.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        readUser();
        return view;
    }

    private void readUser() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = (User) snapshot.getValue(User.class);
                    if (firebaseUser == null) {
                        throw new AssertionError();
                    } else if (user == null) {
                        throw new AssertionError();
                    } else if (!user.getId().equals(firebaseUser.getUid())) {
                        mUsers.add(user);
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, false);
                rvUser.setAdapter(userAdapter);

            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }
}
package com.example.my_message;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.my_message.Fragment.ChatFragment;
import com.example.my_message.Fragment.ProfileFragment;
import com.example.my_message.Fragment.UserFragment;
import com.example.my_message.Model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser firebaseUser;

    public CircleImageView imgProfile;
    private DatabaseReference myRef;
    private TabLayout tabLayout;
    private Toolbar toolbar;

    public TextView tvUsername;
    private ViewPager viewPager;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        imgProfile = (CircleImageView) findViewById(R.id.imgProfile);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatFragment(), "Chat");
        viewPagerAdapter.addFragment(new UserFragment(), "User");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                User user = (User) snapshot.getValue(User.class);
                tvUsername.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    imgProfile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into((ImageView) imgProfile);
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.logout) {
            return false;
        }
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
        return true;
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private ArrayList<String> titles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        public int getCount() {
            return fragments.size();
        }

        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }
    }

    private void setStatus(String status) {
        myRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(NotificationCompat.CATEGORY_STATUS, status);
        myRef.updateChildren(hashMap);
    }


    public void onResume() {
        super.onResume();
        setStatus("online");
    }


    public void onPause() {
        super.onPause();
        setStatus("offline");
    }
}

package com.example.my_message.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.my_message.Model.User;
import com.example.my_message.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class ProfileFragment extends Fragment {
    private static final int IMAGE_REQUEST = 1;
    
    public FirebaseUser firebaseUser;
    private Uri imageUri;
    
    public ImageView imgProfile;
    
    public DatabaseReference myRef;
    private StorageReference storageRef;
    
    public TextView tvUsername;
    private StorageTask uploadTask;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        imgProfile = (ImageView) view.findViewById(R.id.imgProfile);
        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference child = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        myRef = child;
        child.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                User user = (User) snapshot.getValue(User.class);
                if (user != null) {
                    tvUsername.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        imgProfile.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(getContext()).load(user.getImageURL()).into(imgProfile);
                    }
                }
            }

            public void onCancelled(DatabaseError error) {
            }
        });
        imgProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openImage();
            }
        });
        return view;
    }

    
    public void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(intent, 1);
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContext().getContentResolver().getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading");
        progressDialog.show();
        if (imageUri != null) {
            StorageReference storageReference = storageRef;
            final StorageReference filReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            UploadTask putFile = filReference.putFile(imageUri);
            uploadTask = putFile;
            putFile.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                public Task<Uri> then(Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful()) {
                        return filReference.getDownloadUrl();
                    }
                    throw task.getException();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                public void onComplete(Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String mUri = task.getResult().toString();
                        DatabaseReference unused = myRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        Map<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        myRef.updateChildren(map);
                        progressDialog.dismiss();
                        return;
                    }
                    Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
            return;
        }
        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            StorageTask storageTask = uploadTask;
            if (storageTask == null || !storageTask.isInProgress()) {
                uploadImage();
            } else {
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
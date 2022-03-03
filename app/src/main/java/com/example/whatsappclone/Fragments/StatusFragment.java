package com.example.whatsappclone.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsappclone.Adapter.StatusAdapter;
import com.example.whatsappclone.Models.Status;
import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.FragmentChatBinding;
import com.example.whatsappclone.databinding.FragmentStatusBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class StatusFragment extends Fragment {

    FragmentStatusBinding binding;
    ArrayList<UserStatus> userStatuses = new ArrayList<>();
    FirebaseDatabase database;
    StatusAdapter statusAdapter;
    ProgressDialog dialog;
    Users user;

    public StatusFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatusBinding.inflate(inflater, container, false);


        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(Users.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference().child("Stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName(dataSnapshot.child("name").getValue(String.class));
                        status.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(dataSnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.child("statuses").getChildren()){
                            Status sampleStatus = dataSnapshot1.getValue(Status.class);
                            statuses.add(sampleStatus);

                        }

                        status.setStatuses(statuses);

                        if(dataSnapshot.child("name").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())){
                            userStatuses.add(0,status);
                        }else {
                            userStatuses.add(status);
                        }




                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        statusAdapter = new StatusAdapter(getContext(),userStatuses);

        binding.statusRecyclerView.setAdapter(statusAdapter);
        binding.statusRecyclerView.setLayoutManager(linearLayoutManager);

        binding.btnAddStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,101);
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!= null){
            if(data.getData() != null ){
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime()+"");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                UserStatus userStatus = new UserStatus();
                                userStatus.setName(user.getUserName());
                                userStatus.setProfileImage(user.getProfilePic());
                                userStatus.setLastUpdated(date.getTime());

                                HashMap<String,Object> obj = new HashMap<>();
                                obj.put("name",userStatus.getName());
                                obj.put("profileImage",userStatus.getProfileImage());
                                obj.put("lastUpdated",userStatus.getLastUpdated());


                                String imageUrl = uri.toString();
                                Status status = new Status(imageUrl, userStatus.getLastUpdated());

                                database.getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                        .updateChildren(obj);

                                database.getReference().child("Stories").child(FirebaseAuth.getInstance().getUid())
                                        .child("statuses")
                                        .push()
                                        .setValue(status);

                                dialog.dismiss();

                            }
                        });
                    }
                });
            }
        }
    }
}
package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.whatsappclone.Adapter.ChatAdapter;
import com.example.whatsappclone.Models.MessagesModel;
import com.example.whatsappclone.databinding.ActivityChatDetailsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatDetailsActivity extends AppCompatActivity {

    ActivityChatDetailsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    String senderNode,recieverNode,senderId, recieverId ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();


        senderId = auth.getUid();
        recieverId =  getIntent().getStringExtra("userId");
//        Log.d("userid2",recieverId);
        String username = getIntent().getStringExtra("username");
        String profilePic = getIntent().getStringExtra("profilePic");
        String token = getIntent().getStringExtra("token");
//        Toast.makeText(this,token, Toast.LENGTH_SHORT).show();

        binding.UserNameText.setText(username);
        Picasso.get().load(profilePic).placeholder(R.drawable.user).into(binding.profileImage);

        binding.backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailsActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        final ArrayList<MessagesModel> messagesModels = new ArrayList<>();

        final ChatAdapter chatAdapter = new ChatAdapter(messagesModels,this, recieverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        senderNode = senderId+recieverId;
        recieverNode = recieverId+senderId;

        database.getReference().child("chats").child(senderNode).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messagesModels.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MessagesModel model = dataSnapshot.getValue(MessagesModel.class);

                    model.setMessageId(dataSnapshot.getKey());
                    messagesModels.add(model);
                }

                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               String message =  binding.messageEt.getText().toString();
               if(message.equals("")){

               }else{
                   final MessagesModel model = new MessagesModel(senderId,message);

                   model.setTimeStamp(new Date().getTime());



                   binding.messageEt.setText("");


                   database.getReference().child("chats").child(senderNode).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void unused) {
                           database.getReference().child("chats").child(recieverNode).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void unused) {
                                   sendNotification(username,model.getMessage(),token);
                               }
                           });
                       }
                   });
               }



            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,112);
            }
        });


        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    void sendNotification(String name, String message, String token){

        try{
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title",name);
            data.put("body",message);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {



                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    HashMap<String,String> map =  new HashMap<>();
                    String key = "Key=AAAAu0iHatA:APA91bFmdWBSz0avY4p70n1Ae9IJ0Pg1Y7DEA9W5wYe9c5FBDRklyCAz6Tj5WDCwpbYyGTmHOfwrXMH1Q5ACirz_Pxw4xzIAZbZMRLlo3xhps_T_Gf9p4FtgE9dhqkqxT6OAZhIurHiF";

                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");

                    return map;
                }
            };

            queue.add(request);

        }catch (Exception e){

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            if(data.getData() != null){

                Uri sFile = data.getData();

                Calendar calendar = Calendar.getInstance();

                final StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis()+"");


                reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profilePic").setValue(uri.toString());


                                String message =  binding.messageEt.getText().toString();
                                final MessagesModel model = new MessagesModel(senderId,message);

                                model.setTimeStamp(new Date().getTime());
                                model.setMessage("photo");
                                model.setImageURL(uri.toString());
                                binding.messageEt.setText("");


                                database.getReference().child("chats").child(senderNode).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        database.getReference().child("chats").child(recieverNode).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }




    }
}
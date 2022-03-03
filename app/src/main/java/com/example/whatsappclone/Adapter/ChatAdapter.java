package com.example.whatsappclone.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.Models.MessagesModel;
import com.example.whatsappclone.R;
import com.example.whatsappclone.databinding.SampleReceiverBinding;
import com.example.whatsappclone.databinding.SampleSenderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatAdapter extends RecyclerView.Adapter{

    ArrayList<MessagesModel> messagesModel;
    Context context;
    String recId;

    public ChatAdapter(ArrayList<MessagesModel> messagesModel, Context context, String recId) {
        this.messagesModel = messagesModel;
        this.context = context;
        this.recId = recId;
    }

    int SENDER_VIEW_TYPE  = 1;
    int RECIEVER_VIEW_TYPE = 2;

    public ChatAdapter(ArrayList<MessagesModel> messagesModel, Context context) {
        this.messagesModel = messagesModel;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == SENDER_VIEW_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,false);
            return new SenderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver,parent,false);
            return new RecieverViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {

        if(messagesModel.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }else {
            return RECIEVER_VIEW_TYPE;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

       MessagesModel model = messagesModel.get(position);

       holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View v) {

               new AlertDialog.Builder(context)
                       .setTitle("Delete")
                       .setMessage("Are you Sure")
                       .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {

                               FirebaseDatabase database = FirebaseDatabase.getInstance();
                               String senderNode = FirebaseAuth.getInstance().getUid()+recId;
                               database.getReference().child("chats").child(senderNode).child(model .getMessageId()).setValue(null);

                           }
                       })
                       .setNegativeButton("no", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                           }
                       }).show();

               return false;
           }
       });

       if(holder.getClass() == SenderViewHolder.class){

           if(model.getMessage().equals("photo")){
               ((SenderViewHolder) holder).senderBinding.image.setVisibility(View.VISIBLE);
               ((SenderViewHolder) holder).senderBinding.senderText.setVisibility(View.GONE);
               Picasso.get().load(model.getImageURL()).into(((SenderViewHolder) holder).senderBinding.image);
           }

           String time[]  = new Date(model.getTimeStamp()).toString().split(" ");
           String split[] = time[3].split(":");

           ((SenderViewHolder)holder).senderMsg.setText(model.getMessage());
           ((SenderViewHolder)holder).senderTime.setText(split[0]+":"+split[1]);
       }else{

           if(model.getMessage().equals("photo")){
               ((RecieverViewHolder) holder).receiverBinding.image.setVisibility(View.VISIBLE);
               ((RecieverViewHolder) holder).receiverBinding.recieverText.setVisibility(View.GONE);
               Picasso.get().load(model.getImageURL()).into(((RecieverViewHolder) holder).receiverBinding.image);
           }

           String time[]  = new Date(model.getTimeStamp()).toString().split(" ");
           String split[] = time[3].split(":");
           ((RecieverViewHolder)holder).recieverMsg.setText(model.getMessage());
           ((RecieverViewHolder)holder).reciverTime.setText(split[0]+":"+split[1]);
       }

    }

    @Override
    public int getItemCount() {
        return messagesModel.size();
    }

    public  class RecieverViewHolder extends RecyclerView.ViewHolder {

        TextView recieverMsg, reciverTime;
        SampleReceiverBinding receiverBinding;



        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            recieverMsg = itemView.findViewById(R.id.recieverText);
            reciverTime = itemView.findViewById(R.id.recieverTime);
            receiverBinding = SampleReceiverBinding.bind(itemView);
        }
    }

    public  class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime;
        SampleSenderBinding senderBinding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderBinding = SampleSenderBinding.bind(itemView);
        }
    }

}

package com.example.hprestaurant.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Models.MessageModel;
import com.example.hprestaurant.R;
import com.example.hprestaurant.Utils.Util;

import java.util.List;



public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int TYPE_SEND_MESSAGE = 0;
    private static final int TYPE_RECEIVED_MESSAGE = 1;
    private static final int TYPE_TIMESTAMP = 2;

    private List<MessageModel> messageModelList;
    private Customer customer;

    public MessageAdapter(Customer customer) {
        this.customer = customer;
    }


    public void setData(List<MessageModel> messageModelList){
        this.messageModelList = messageModelList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(TYPE_SEND_MESSAGE == viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_send_message,parent,false);
            return new SendMessageViewHolder(view);
        }else if(TYPE_RECEIVED_MESSAGE == viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_received_message,parent,false);
            return new ReceivedMessageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messageModelList.get(position);
        if (message == null )return;

        if(TYPE_SEND_MESSAGE == holder.getItemViewType()){
            SendMessageViewHolder sendMessageViewHolder = (SendMessageViewHolder) holder;
            sendMessageViewHolder.sendTv.setText(message.getContent());
            String sTime = Util.dateTimeInMessageFormatting(message.getTimestamp());
            sendMessageViewHolder.timeTv.setText(sTime);
        }else if(TYPE_RECEIVED_MESSAGE == holder.getItemViewType()){
            ReceivedMessageViewHolder receivedMessageViewHolder = (ReceivedMessageViewHolder) holder;
            receivedMessageViewHolder.receivedTv.setText(message.getContent());
            String sTime = Util.dateTimeInMessageFormatting(message.getTimestamp());
            receivedMessageViewHolder.timeTv.setText(sTime);
        }

    }

    @Override
    public int getItemCount() {
        if(messageModelList != null) return messageModelList.size();
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageModelList.get(position);
        if(customer.getCustomer_id() == message.getSender_id()){
            return TYPE_SEND_MESSAGE;
        }
        if(customer.getCustomer_id() == message.getReceiver_id()){
            return TYPE_RECEIVED_MESSAGE;
        }
        return TYPE_RECEIVED_MESSAGE;
    }

    public static class SendMessageViewHolder extends RecyclerView.ViewHolder{
        private TextView sendTv,timeTv;
        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sendTv = itemView.findViewById(R.id.sendTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private TextView receivedTv,timeTv;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedTv = itemView.findViewById(R.id.receivedTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}

package com.example.hprestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.hprestaurant.Adapters.MessageAdapter;
import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Models.MessageModel;
import com.example.hprestaurant.Models.Request.ServerRequest;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityMessageBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private ActivityMessageBinding binding;
    private List<MessageModel> messageModelList;
    private MessageAdapter messageAdapter;
    private MyApplication mApp;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mApp = (MyApplication) getApplication();


        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.outSideMessageEtRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
        messageModelList = new ArrayList<>();

        messageAdapter = new MessageAdapter(mApp.getCustomer());
        binding.conversationRv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        binding.conversationRv.setAdapter(messageAdapter);
        getConversation();


        binding.conversationRv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });



        binding.sendMessageBtn.setVisibility(View.GONE);
        binding.selectPictureBtn.setVisibility(View.GONE);
        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send message
                sendMessage();
            }
        });

        binding.messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewSendBtn(!s.toString().trim().isEmpty());
            }
        });



    }

    private void getConversation() {
        Customer customer = mApp.getCustomer();
        ApiService.apiService.getMessages(new ServerRequest(new Date(),customer.getCustomer_id())).enqueue(new Callback<ArrayList<MessageModel>>() {
            @Override
            public void onResponse(Call<ArrayList<MessageModel>> call, Response<ArrayList<MessageModel>> response) {
                if(response.isSuccessful()){
                    messageModelList.addAll(response.body());
                    messageAdapter.setData(messageModelList);
                    binding.conversationRv.scrollToPosition(messageModelList.size()-1);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MessageModel>> call, Throwable t) {

            }
        });
    }

    private void sendMessage() {
        String content = binding.messageEt.getText().toString().trim();
        Customer customer = mApp.getCustomer();

        JSONObject object = new JSONObject();
        try {
            object.put("isCustomer",true);
            object.put("customer",customer.toJson());
            object.put("from", Auth.userUid);
            object.put("content",content);
            JSONObject message = new JSONObject();
            message.put("status","đã gửi tin nhắn") ;
            object.put("message",message);
            object.put("action","CONVERSATION");
            mApp.getWebsocketClient().sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MessageModel messageModel = new MessageModel(customer.getCustomer_id(),content);
        messageModelList.add(messageModel);
        messageAdapter.notifyDataSetChanged();
        binding.conversationRv.scrollToPosition(messageModelList.size()-1);
        binding.messageEt.setText("");

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.slide_out_right);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.messageRl.getWindowToken(), 0);
            binding.messageEt.clearFocus();
        }
    }
    private void viewSendBtn(boolean isView){
        if(isView){
            binding.sendMessageBtn.setVisibility(View.VISIBLE);
            //binding.selectPictureBtn.setVisibility(View.GONE);
            return;
        }
        //binding.selectPictureBtn.setVisibility(View.VISIBLE);
        binding.sendMessageBtn.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {

        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageModel messageModel) {
        messageModelList.add(messageModel);
        int lastP = messageModelList.indexOf(messageModel);
        messageAdapter.notifyItemInserted(lastP);
        binding.conversationRv.scrollToPosition(lastP);

    }
}
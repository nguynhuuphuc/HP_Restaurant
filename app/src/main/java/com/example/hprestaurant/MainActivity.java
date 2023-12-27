package com.example.hprestaurant;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.hprestaurant.Adapters.ViewPagerAdapter;
import com.example.hprestaurant.Interfaces.MyWebSocketListener;
import com.example.hprestaurant.Models.MessageEvent;
import com.example.hprestaurant.Models.ReservationModel;
import com.example.hprestaurant.Models.Response.WebsocketResponse;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.MyWebsocketClient;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okio.ByteString;

public class MainActivity extends AppCompatActivity implements MyWebSocketListener {
    private ActivityMainBinding binding;
    private ViewPagerAdapter viewPagerAdapter;
    private String titleToolBar = "Trang Chủ";
    private ActivityResultLauncher<Intent> launcher;
    private MyApplication mApp;
    private MyWebsocketClient myWebsocketClient;
    private List<ReservationModel> changeReservations;
    private MessageEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setContentView(binding.getRoot());
        Util.sharedReferencesSaving(this);
        setUpViewPager();
        setBottomNavigation();
        registerLauncher();
        mApp = (MyApplication) getApplication();
        myWebsocketClient = mApp.getWebsocketClient();
        changeReservations = new ArrayList<>();

        event = new MessageEvent();
        event.setReservationModels(new ArrayList<>());

        if(myWebsocketClient == null){
            myWebsocketClient = new MyWebsocketClient();
            mApp.setWebsocketClient(myWebsocketClient);
        }
        myWebsocketClient.setMyWebSocketListener(this);




        binding.bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BookingOptionsActivity.class  );
                launcher.launch(intent);
            }
        });

    }

    private void registerLauncher() {
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    int action = data.getIntExtra("action",-1);
                    if(action == Action.RE_SIGNUP || action == Action.LOGOUT) {
                        signOut();
                    }
                }
            }
        });
    }

    private void signOut() {
        binding.signOutProgress.setVisibility(View.VISIBLE);
        binding.viewPager2.setCurrentItem(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Util.sharedReferencesRemoving(MainActivity.this);
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }

    private void setBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                titleToolBar = item.getTitle().toString();
                switch (item.getItemId()){
                    case R.id.menu_home:
                        binding.viewPager2.setCurrentItem(0);
                        break;
                    case R.id.menu_history:
                        binding.viewPager2.setCurrentItem(1);
                        break;
                    case R.id.menu_voucher:
                        binding.viewPager2.setCurrentItem(2);
                        break;
                    case R.id.menu_user:
                        binding.viewPager2.setCurrentItem(3);
                        break;
                }
                return true;
            }
        });
    }

    private void setUpViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager2.setAdapter(viewPagerAdapter);
        binding.viewPager2.setUserInputEnabled(false);
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        binding.bottomNavigation.setSelectedItemId(R.id.menu_home);
                        binding.toolBarTitle.setText(titleToolBar);
                        break;
                    case 1:
                        binding.bottomNavigation.setSelectedItemId(R.id.menu_history);
                        binding.toolBarTitle.setText(titleToolBar);
                        break;
                    case 2:
                        binding.bottomNavigation.setSelectedItemId(R.id.menu_voucher);
                        binding.toolBarTitle.setText(titleToolBar);
                        break;
                    case 3:
                        binding.bottomNavigation.setSelectedItemId(R.id.menu_user);
                        binding.toolBarTitle.setText(titleToolBar);
                        break;
                }
                super.onPageSelected(position);
            }
        });
    }

    public ActivityResultLauncher<Intent> getLauncher() {
        return launcher;
    }

    @Override
    public void onMessage(String text) {
        WebsocketResponse response = new Gson().fromJson(text,WebsocketResponse.class);
        if(response.getReservation() != null)
            changeReservations.add(response.getReservation());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(response.getActivity() != null && response.getActivity().equals("Message")){
                    if(response.getConversationMessage().getConversation_id() == mApp.getCustomer().getCustomer_id()
                        && response.getConversationMessage().getSender_id() != mApp.getCustomer().getCustomer_id()){
                        EventBus.getDefault().post(response.getConversationMessage());
                    }

                }
                if(response.getFrom() != null && !response.getFrom().equals(Auth.getUserUid())){
                    EventBus.getDefault().post(new MessageEvent(response.getTableId(),response.isTableInProgress(),response.getDateBooking()));
                }

                if(response.getToCustomer()==mApp.getCustomer().getCustomer_id()){
                    Util.changeReservationAdding(event.getReservationModels(),changeReservations);
                    changeReservations.clear();
                    EventBus.getDefault().postSticky(event);
                }
            }
        });
    }

    @Override
    public void onMessage(ByteString bytes) {

    }

    @Override
    public void onOpen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mApp, "open", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClosed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mApp, "close", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void popUpNotification(CharSequence contentText) {
        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/"+ R.raw.sound_notification);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.hp_res_logo)
                .setSound(sound)
                .setContentTitle("Thông báo")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null){

        }
    }
}
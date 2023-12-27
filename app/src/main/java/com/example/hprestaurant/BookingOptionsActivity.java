package com.example.hprestaurant;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.databinding.ActivityBookingOptionsBinding;

public class BookingOptionsActivity extends AppCompatActivity {
    private ActivityBookingOptionsBinding binding;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        registerLauncher();

        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.tableCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingOptionsActivity.this,BookingActivity.class);
                activityLauncher(intent);
            }
        });

        binding.dishesCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingOptionsActivity.this,DishesSelectionActivity.class);
                intent.putExtra("action", Action.ONLY_DISHES);
                activityLauncher(intent);
            }
        });

        binding.tableDishesCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookingOptionsActivity.this,DishesSelectionActivity.class);
                intent.putExtra("action", Action.DISHES_AND_TABLE);
                activityLauncher(intent);
            }
        });
    }

    private void activityLauncher(Intent intent){
        launcher.launch(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
    }

    private void registerLauncher() {
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    assert data != null;
                    int action = data.getIntExtra("action",-1);
                    if(action == Action.BOOKING_SUCCESS){
                        finish();
                    }
                }
            }
        });
    }
}
package com.example.hprestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;

import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Models.Request.ServerRequest;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.MySharedPreferences;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivitySplashBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private MyApplication mApp;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mApp = (MyApplication) getApplication();
        customer = mApp.getCustomer();

        if(MySharedPreferences.isExistUserUid(this)){
            //User Uid đã lưu
            Auth.setUserUid(MySharedPreferences.getUserUid(this));
            Auth.setPhoneNumber(MySharedPreferences.getPhone(this));
            if(customer == null){
                getInfoCustomer();
            }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Thực hiện hành động sau độ trễ ở đây
                    Toast.makeText(mApp, "Mừng bạn quay lại", Toast.LENGTH_SHORT).show();
                    openMain();
                }
            }, 2000);}

        }else{
            //User Uid chưa lưu
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Thực hiện hành động sau độ trễ ở đây
                    openLogin();
                }
            }, 2000);
        }

    }

    private void openMain() {
        startActivity(new Intent(this,MainActivity.class));

        finish();
    }

    private void openLogin() {
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
    private void getInfoCustomer() {
        ApiService.apiService.getInfoCustomer(new ServerRequest(Auth.getUserUid())).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if(response.isSuccessful()){
                    customer = response.body();
                    mApp.setCustomer(customer);
                    Toast.makeText(mApp, "Mừng bạn quay lại", Toast.LENGTH_SHORT).show();
                    openMain();
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                Toast.makeText(SplashActivity.this, "Server err", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
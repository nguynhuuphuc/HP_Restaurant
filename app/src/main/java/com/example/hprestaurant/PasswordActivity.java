package com.example.hprestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.hprestaurant.Models.Account;
import com.example.hprestaurant.Models.Request.PasswordRequest;
import com.example.hprestaurant.Models.Response.ServerResponse;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.PasswordUtil;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordActivity extends AppCompatActivity {
    private ActivityPasswordBinding binding;
    private FirebaseAuth mAuth;
    private int action;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        action = getIntent().getIntExtra("action",-1);
        setViewWithAction();

        binding.passwordEditText.setOnTouchListener(Util.ShowOrHidePass(binding.passwordEditText));
        binding.rePasswordEditText.setOnTouchListener(Util.ShowOrHidePass(binding.rePasswordEditText));
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(Util.onKeyBoardListener(binding.getRoot(), new Util.IKeyBoardListener() {
            @Override
            public void onHidden() {
                binding.rePasswordEditText.clearFocus();
                binding.passwordEditText.clearFocus();
            }

            @Override
            public void onVisible() {

            }
        }));

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = binding.passwordEditText.getText().toString().trim();
                String rePassword = binding.rePasswordEditText.getText().toString().trim();
                if(!password.equals(rePassword)){
                    binding.rePasswordEditText.setError("Mật khẩu nhập lại không đúng");
                    return;
                }
                String hashPassword = PasswordUtil.hashPassword(rePassword);
                if(action == Action.SIGNUP){
                    createNewAccount(hashPassword);
                }else if(action == Action.PASSWORD_REMADE){
                    PasswordRequest request = new PasswordRequest(mAuth.getUid(),hashPassword);
                    postCreatePassword(request);
                }
            }
        });




    }

    private void setViewWithAction() {
        switch (action){
            case Action.PASSWORD_REMADE:
                binding.actionPasswordLabel.setText("Tạo lại mật khẩu mới");
                return;
            default:
                binding.actionPasswordLabel.setText("Tạo mật khẩu");
                return;
        }
    }

    private void createNewAccount(String hashPassword) {
        String phoneNumber = mAuth.getCurrentUser().getPhoneNumber();
        String uid = mAuth.getCurrentUser().getUid();
        Account account = new Account(phoneNumber,hashPassword,uid,"KH",true);
        ApiService.apiService.addNewAccount(account).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    ServerResponse res = response.body();
                    Toast.makeText(PasswordActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intentResult = new Intent();
                    intentResult.putExtra("action",Action.PASSWORD_CREATED);
                    setResult(RESULT_OK,intentResult);
                    finish();
                }else {
                    Toast.makeText(PasswordActivity.this, "Thêm tài khoản thất bại", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(PasswordActivity.this, "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postCreatePassword(PasswordRequest request) {
        ApiService.apiService.createPassword(request).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    ServerResponse res = response.body();
                    Toast.makeText(PasswordActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intentResult = new Intent();
                    intentResult.putExtra("action",Action.ACCOUNT_CREATED);
                    setResult(RESULT_OK,intentResult);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }
}
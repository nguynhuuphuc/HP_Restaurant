package com.example.hprestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hprestaurant.Models.Request.PasswordRequest;
import com.example.hprestaurant.Models.Response.ServerResponse;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.PasswordUtil;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityChangePasswordBinding;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private String currPassword,newPassword,rePassword;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        setOnFocusChanged();

        binding.currentPasswordEditText.setOnTouchListener(Util.ShowOrHidePass(binding.currentPasswordEditText));
        binding.newPasswordEditText.setOnTouchListener(Util.ShowOrHidePass(binding.newPasswordEditText));
        binding.rePasswordEditText.setOnTouchListener(Util.ShowOrHidePass(binding.rePasswordEditText));



        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currPassword = binding.currentPasswordEditText.getText().toString().trim();
                if(currPassword.isEmpty()){
                    viewEditTextError(binding.errCurrPassword,R.string.password_empty);
                    return;
                }
                if((newPassword = binding.newPasswordEditText.getText().toString().trim()).isEmpty()){
                    viewEditTextError(binding.errNewPassword,R.string.password_empty);
                    return;
                }
                if(!(rePassword = binding.rePasswordEditText.getText().toString().trim()).equals(newPassword)){
                    viewEditTextError(binding.errRePassword,R.string.re_password_incorrect);
                    return;
                }
                checkingAndChanging();
            }
        });
    }

    private void viewEditTextError(TextView textView, int err) {
        textView.setText(err);
        textView.setVisibility(View.VISIBLE);
    }
    private void hideEditTextError(TextView textView){
        textView.setVisibility(View.INVISIBLE);
    }

    private void checkingAndChanging() {
        if(currPassword.isEmpty() || newPassword.isEmpty() || rePassword.isEmpty()){
            return;
        }
        inProgress(true);
        PasswordRequest request = new PasswordRequest(Auth.userUid,currPassword);
        ApiService.apiService.passwordChecking(request).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    changePassword(request);
                    return;
                }
                if(response.code() == 401){
                    viewEditTextError(binding.errCurrPassword,R.string.incorrect_password);
                    inProgress(false);
                }

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Server err", Toast.LENGTH_SHORT).show();
                inProgress(false);
            }
        });

    }

    private void changePassword(PasswordRequest request) {
        String password = PasswordUtil.hashPassword(rePassword);
        request.setPassword(password);
        ApiService.apiService.createPassword(request).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    Intent intentResult = new Intent();
                    intentResult.putExtra("action", Action.PASSWORD_REMADE);
                    setResult(RESULT_OK,intentResult);
                    finish();
                }
                if(response.code() == 500){
                    Toast.makeText(ChangePasswordActivity.this, "Đã có lỗi xãy ra!!!", Toast.LENGTH_SHORT).show();
                    inProgress(false);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Server err", Toast.LENGTH_SHORT).show();
                inProgress(false);
            }
        });

    }

    private void setOnFocusChanged() {
        binding.currentPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    hideEditTextError(binding.errCurrPassword);
                    binding.newPasswordEditText.clearFocus();
                    binding.rePasswordEditText.clearFocus();
                }
            }
        });
        binding.newPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    hideEditTextError(binding.errNewPassword);
                    binding.currentPasswordEditText.clearFocus();
                    binding.rePasswordEditText.clearFocus();
                }
            }
        });
        binding.rePasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    hideEditTextError(binding.errRePassword);
                    binding.newPasswordEditText.clearFocus();
                    binding.currentPasswordEditText.clearFocus();
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.slide_out_right);
    }

    void inProgress(boolean isIn){
        if(isIn){
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.submitButton.setEnabled(false);
            binding.submitButton.setVisibility(View.INVISIBLE);
            return;
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.submitButton.setEnabled(true);
        binding.submitButton.setVisibility(View.VISIBLE);
    }
}
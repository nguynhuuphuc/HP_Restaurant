package com.example.hprestaurant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.databinding.ActivityVerifyBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {
    private ActivityVerifyBinding binding;
    private FirebaseAuth auth;
    private Long timeoutSeconds = 60L;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private String verificationCode;
    private int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        action = getIntent().getIntExtra("action",-1);

        auth = FirebaseAuth.getInstance();
        auth.getFirebaseAuthSettings().forceRecaptchaFlowForTesting(false);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneNumberTextView.setText(phoneNumber);


        sendOtp(phoneNumber,false);
        binding.verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.otpEditText.getText().toString().isEmpty()){
                    binding.otpEditText.setError("Chưa nhập OTP");
                    return;
                }
                String enterOtp = binding.otpEditText.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode,enterOtp);
                signIn(credential);
                setInProgress(true);
            }
        });

        binding.resendOtpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp(phoneNumber,true);
            }
        });

    }


    void sendOtp(String phoneNumber,boolean isResend){
        startResendTimer();
        setInProgress(true);
        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signIn(phoneAuthCredential);
                                setInProgress(false);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(VerifyActivity.this, "Gửi OTP không thành công", Toast.LENGTH_SHORT).show();
                                setInProgress(false);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                verificationCode = s;
                                resendingToken = forceResendingToken;
                                Toast.makeText(VerifyActivity.this, "Gửi OTP thành công", Toast.LENGTH_SHORT).show();
                                setInProgress(false);
                            }
                        });
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }else {

            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }
    void startResendTimer(){
        binding.resendOtpTv.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.resendOtpTv.setText("Gửi lại OTP trong "+timeoutSeconds+" giây");
                    }
                });
                if(timeoutSeconds <= 0){
                    timeoutSeconds = 60L;
                    timer.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.resendOtpTv.setText(R.string.resend_otp);
                            binding.resendOtpTv.setEnabled(true);
                        }
                    });
                }
            }
        },0,1000);

    }
    void signIn(PhoneAuthCredential phoneAuthCredential){
        //login and next
        setInProgress(true);
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intentResult = new Intent();
                    if(action == Action.FORGOT_PASSWORD){
                        intentResult.putExtra("action",Action.PASSWORD_REMADE);
                    }else {
                        intentResult.putExtra("action", Action.VERIFIED);
                    }
                    setResult(RESULT_OK,intentResult);
                    finish();
                }else {
                    Toast.makeText(VerifyActivity.this, "OTP không đúng", Toast.LENGTH_SHORT).show();
                    setInProgress(false);
                }
            }
        });
    }

    void setInProgress(boolean isIn){
        if(isIn){
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.verifyButton.setVisibility(View.GONE);
            return;
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.verifyButton.setVisibility(View.VISIBLE);
    }

}
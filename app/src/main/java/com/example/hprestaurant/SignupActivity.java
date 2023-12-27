package com.example.hprestaurant;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private ActivityResultLauncher<Intent> launcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    int action = o.getData().getIntExtra("action",-1);
                    if(action == Action.VERIFIED){
                        Intent intentResult = new Intent();
                        intentResult.putExtra("action",Action.SIGNUP);
                        setResult(RESULT_OK,intentResult);
                        finish();
                    }

                }
            }
        });

        inProgress(false);
        setContentView(binding.getRoot());
        binding.ccp.registerCarrierNumberEditText(binding.phoneNumberEditText);

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                binding.getRoot().getWindowVisibleDisplayFrame(r);
                int screenHeight =  binding.getRoot().getHeight();

                // Calculate the height of the keyboard
                int keypadHeight = screenHeight - r.bottom;

                // If the height of the keyboard is less than a certain threshold, it's considered hidden
                boolean isKeyboardVisible = keypadHeight > screenHeight * 0.15; // Adjust this threshold as needed

                if (isKeyboardVisible) {
                    // The keyboard is currently visible
                    // Handle the event here

                } else {
                    // The keyboard is currently hidden
                    // Handle the event here
                    binding.phoneNumberEditText.clearFocus();
                }
            }
        });

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        if(phoneNumber != null){
            binding.phoneNumberEditText.setText(phoneNumber.replace("+84",""));
        }

        binding.sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.ccp.isValidFullNumber()){
                    binding.phoneNumberEditText.setError("Số điện thoại không đúng");
                    return;
                }
                openVerify();
            }
        });
    }

    private void openVerify() {
        Intent intent = new Intent(this,VerifyActivity.class);
        intent.putExtra("phoneNumber",binding.ccp.getFullNumberWithPlus());
        launcher.launch(intent);

    }

    void inProgress(boolean isIn) {
        if (isIn) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.sendOtpButton.setVisibility(View.GONE);
            return;
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.sendOtpButton.setVisibility(View.VISIBLE);
    }
}
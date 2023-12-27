package com.example.hprestaurant;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Models.Request.LoginRequest;
import com.example.hprestaurant.Models.Request.ServerRequest;
import com.example.hprestaurant.Models.Response.LoginResponse;
import com.example.hprestaurant.Models.Response.ServerResponse;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.MySharedPreferences;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

import eightbitlab.com.blurview.RenderScriptBlur;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private FirebaseAuth auth;
    private Dialog dialog;
    private MyApplication mApp;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        inProgress(false);
        setBlurView();
        mApp = (MyApplication) getApplication();
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    assert data != null;
                    int action = data.getIntExtra("action",-1);
                    solveAction(action,data);
                }
            }
        });
        String phoneNumber;
        if((phoneNumber = MySharedPreferences.getPhone(this)) != null){
            if(!phoneNumber.isEmpty()){
                binding.phoneNumberEditText.setText(phoneNumber.replace("+84",""));
            }
        }

        binding.ccp.registerCarrierNumberEditText(binding.phoneNumberEditText);



        binding.passwordEditText.setOnTouchListener(Util.ShowOrHidePass(binding.passwordEditText));
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(Util.onKeyBoardListener(binding.getRoot(), new Util.IKeyBoardListener() {
            @Override
            public void onHidden() {
                binding.passwordEditText.clearFocus();
                binding.phoneNumberEditText.clearFocus();
            }

            @Override
            public void onVisible() {

            }
        }));

        binding.signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignup();
            }
        });
        binding.forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog();
            }
        });
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.ccp.isValidFullNumber()){
                    binding.phoneNumberEditText.setError("Số điện thoại chưa đúng");
                    return; 
                }
                String phoneNumber = binding.ccp.getFullNumberWithPlus();
                String password = binding.passwordEditText.getText().toString().trim();
                if(password.isEmpty()){
                    binding.passwordEditText.setError("Mật khẩu trống");
                    return;
                }
                postToLogin(phoneNumber,password);
                
            }
        });


    }

    private void forgotPasswordDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_phone_number_input);

        EditText phoneNumberEt = dialog.findViewById(R.id.phone_number_edit_text);
        CardView sendOTPBtn = dialog.findViewById(R.id.send_otp_button_cv);
        TextView closeTv = dialog.findViewById(R.id.close_text_view);
        CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttribute = window.getAttributes();
        windowAttribute.gravity = Gravity.CENTER;
        window.setAttributes(windowAttribute);
        dialog.setCancelable(true);
        ccp.registerCarrierNumberEditText(phoneNumberEt);
        if(binding.ccp.isValidFullNumber()) {
            phoneNumberEt.setText(binding.ccp.getFullNumberWithPlus().replace("+84",""));
        }
        closeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        inProgressForgotDialog(progressBar,sendOTPBtn,false);

        sendOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ccp.isValidFullNumber()){
                    phoneNumberEt.setError("Số điện thoại chưa đúng");
                    return;
                }
                inProgressForgotDialog(progressBar,sendOTPBtn,true);
                openVerify(ccp,Action.FORGOT_PASSWORD,progressBar,sendOTPBtn);
            }

        });


        dialog.show();
    }

    private void inProgressForgotDialog(ProgressBar progressBar, CardView sendOTPBtn, boolean b) {
        if(b){
            progressBar.setVisibility(View.VISIBLE);
            sendOTPBtn.setVisibility(View.GONE);
            return;
        }
        progressBar.setVisibility(View.GONE);
        sendOTPBtn.setVisibility(View.VISIBLE);

    }


    private void postToLogin(String phoneNumber, String password) {
        inProgress(true);
        LoginRequest request = new LoginRequest(phoneNumber,password);
        ApiService.apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    // Request was successful, process the response
                    LoginResponse r = response.body();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    assert r != null;
                    Auth.setUserUid(r.getUser_uid());
                    Auth.setPhoneNumber(binding.ccp.getFullNumberWithPlus());
                    putInFoCustomerAndStart(intent);

                } else {
                    // Request failed, handle the error
                    int statusCode = response.code();
                    // Check the status code for specific error handling
                    switch (statusCode){
                        case 500:
                            Toast.makeText(LoginActivity.this, "An error occurred during login.", Toast.LENGTH_SHORT).show();
                            break;
                        case 401:
                            Toast.makeText(LoginActivity.this, "Số điện thoại hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                            break;
                        case 402:
                            Toast.makeText(LoginActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    inProgress(false);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

            }
        });
    }

    private void putInFoCustomerAndStart(Intent intent) {
        ApiService.apiService.getInfoCustomer(new ServerRequest(Auth.getUserUid())).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if(response.isSuccessful()){
                    Customer customer = response.body();
                    mApp.setCustomer(customer);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Server err", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void solveAction(int action,Intent data) {
        Intent intent;
        switch (action){
            case Action.VERIFIED:

                return;
            case Action.LOGIN:
                return;
            case Action.SIGNUP:

                postAddNewCustomer();
                return;
            case Action.PASSWORD_REMADE:
                intent = new Intent(this,PasswordActivity.class);
                intent.putExtra("action",Action.PASSWORD_REMADE);
                launcher.launch(intent);
                return;
            case Action.PASSWORD_CREATED:

                intent = new Intent(this,InsertInfoActivity.class);
                intent.putExtra("phoneNumber",auth.getCurrentUser().getPhoneNumber());
                intent.putExtra("user_uid",auth.getCurrentUser().getUid());
                launcher.launch(intent);
                return;
            case Action.ACCOUNT_CREATED:
                Auth.setPhoneNumber(auth.getCurrentUser().getPhoneNumber());
                Auth.setUserUid(auth.getCurrentUser().getUid());
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();

        }
    }

    private void postAddNewCustomer() {
        inProgress2(true);
        Customer customer = new Customer();
        customer.setUser_uid(auth.getCurrentUser().getUid());
        customer.setPhone_number(auth.getCurrentUser().getPhoneNumber());
        ApiService.apiService.addNewCustomer(customer).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    ServerResponse res = response.body();
                    Toast.makeText(LoginActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,PasswordActivity.class);
                    intent.putExtra("action",Action.SIGNUP);
                    launcher.launch(intent);
                }else{
                    // Request failed, handle the error
                    int statusCode = response.code();
                    // Check the status code for specific error handling
                    switch (statusCode){
                        case 500:
                            Toast.makeText(LoginActivity.this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
                            break;
                        case 401:
                            break;
                    }

                }
                inProgress(false);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Server err", Toast.LENGTH_SHORT).show();
                inProgress(false);
            }
        });
    }

    private void openSignup() {
        Intent intent = new Intent(this,SignupActivity.class);
        if(binding.ccp.isValidFullNumber()){
            intent.putExtra("phoneNumber",binding.ccp.getFullNumberWithPlus());
        }
        launcher.launch(intent);
    }
    void inProgress(boolean isIn){
        if(isIn){
            binding.loginButton.setEnabled(false);
            binding.loginButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            return;
        }
        binding.loginButton.setEnabled(true);
        binding.loginButton.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void openVerify(CountryCodePicker ccp, int action, ProgressBar progressBar, CardView sendOTPBtn) {
        ApiService.apiService.checkPhoneNumberRequest(new LoginRequest(ccp.getFullNumberWithPlus(),null)).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this,VerifyActivity.class);
                    intent.putExtra("action",action);
                    intent.putExtra("phoneNumber",ccp.getFullNumberWithPlus());
                    launcher.launch(intent);
                    dialog.dismiss();
                }
                if (response.code() == 401){
                    Toast.makeText(LoginActivity.this, "Số điện thoại không tồn tại!", Toast.LENGTH_SHORT).show();
                }
                inProgressForgotDialog(progressBar,sendOTPBtn,false);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Server err", Toast.LENGTH_SHORT).show();
                inProgressForgotDialog(progressBar,sendOTPBtn,false);
            }
        });

       
    }
    void setBlurView(){
        float radius = 20f;// Adjust the blur radius as needed
        View decorView = getWindow().getDecorView();
        Drawable windowBackground = decorView.getBackground();
        binding.blurView.setupWith(binding.getRoot(),new RenderScriptBlur(this))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);
        binding.blurView.setBlurEnabled(false);
    }
    void inProgress2(boolean isIn){
        Toast.makeText(this, String.valueOf(isIn), Toast.LENGTH_SHORT).show();
        if(isIn){
            binding.blurView.setBlurEnabled(true);
            binding.loginButton.setVisibility(View.GONE);
            binding.progressBar2.setVisibility(View.VISIBLE);
            return;
        }
        binding.blurView.setBlurEnabled(false);
        binding.loginButton.setVisibility(View.VISIBLE);
        binding.progressBar2.setVisibility(View.GONE);


    }

}
package com.example.hprestaurant;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Models.Request.ServerRequest;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityMemberBinding;
import com.hbb20.CountryCodePicker;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberActivity extends AppCompatActivity{
    private ActivityMemberBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    private Customer customer;
    private AnimatorSet front_anim;
    private AnimatorSet back_anim;
    private boolean isFront = true;
    private float scale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        registerLauncher();
        getInfoCustomer();

        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        binding.cardFront.setCameraDistance(8000*scale);
        binding.cardBack.setCameraDistance(8000*scale);

        front_anim = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.front_animator);
        back_anim = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.back_animator);

        binding.flipCardRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDelayEnableRl();
                doFlipSwitch2Card();
            }
        });

        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemberActivity.this,ChangePasswordActivity.class);
                launcher.launch(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
            }
        });

        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildSignOutDialog();
            }
        });
        binding.updateInfoCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemberActivity.this,UpdateInfoActivity.class);
                intent.putExtra("customer",customer);
                launcher.launch(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
            }
        });



    }

    private void doFlipSwitch2Card() {
        if(isFront) {
            front_anim.setTarget(binding.cardFront);
            back_anim.setTarget(binding.cardBack);
            front_anim.start();
            back_anim.start();
            isFront = false;
        }else {
            front_anim.setTarget(binding.cardBack);
            back_anim.setTarget(binding.cardFront);
            back_anim.start();
            front_anim.start();
            isFront = true;
        }
    }

    private void setDelayEnableRl() {
        binding.flipCardRl.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.flipCardRl.setEnabled(true);
            }
        },1000);
    }

    private void registerLauncher() {
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    int action = data.getIntExtra("action",-1);
                    if(action == Action.PASSWORD_REMADE){
                        buildReSignUpDialog();
                        return;
                    }
                    if(action == Action.UPDATE_INFO_CUSTOMER){
                        customer = (Customer) data.getSerializableExtra("customer");
                        setViewInfo();
                        return;
                    }
                }
            }
        });
    }
    private void getInfoCustomer() {
        inProgress(true);
        ApiService.apiService.getInfoCustomer(new ServerRequest(Auth.getUserUid())).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if(response.isSuccessful()){
                    customer = response.body();
                    setViewInfo();
                    inProgress(false);

                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                Toast.makeText(MemberActivity.this, "Server err", Toast.LENGTH_SHORT).show();
                inProgress(false);
            }
        });

    }

    private void setViewInfo() {
        if(customer.getAvatar() !=null && !customer.getAvatar().isEmpty()){
            Glide.with(this).load(customer.getAvatar()).into(binding.avatar);
        }
        String text = "Xin chào "+ customer.getFull_name()+"!";
        binding.nameTv.setText(text);
        String created_date = Util.DayFormatting(customer.getCreated_date());
        binding.createdDateTv.setText(created_date);

        if(customer.getEmail() != null && !customer.getEmail().isEmpty()){
            binding.emailTextView.setText(customer.getEmail());
        }

        binding.customerId.setText(String.valueOf(customer.getCustomer_id()));
        binding.phoneNumberTextView.setText(customer.getPhone_number());
        if(customer.getDate_of_birth() != null){
            binding.dateOfBirthTextView.setText(Util.DayFormatting(customer.getDate_of_birth()));
        }
    }

    private void inProgress(boolean b) {
        if(b){
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.contentLL.setVisibility(View.GONE);
            return;
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.contentLL.setVisibility(View.VISIBLE);
    }


    private void buildSignOutDialog(){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom_form);

        TextView title = dialog.findViewById(R.id.titleDialog);
        TextView closeTv = dialog.findViewById(R.id.close_text_view);
        CardView negativeCv = dialog.findViewById(R.id.negative_button_cv);
        CardView positiveCv = dialog.findViewById(R.id.positive_button_cv);
        TextView content = dialog.findViewById(R.id.content_text_view);
        TextView positiveTv = dialog.findViewById(R.id.positive_text_view);

        title.setText(R.string.logout);
        positiveTv.setText(R.string.logout);

        content.setText("Bạn có chắc chắn muốn đăng xuất?");

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

        closeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        negativeCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        positiveCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentResult = new Intent();
                intentResult.putExtra("action",Action.LOGOUT);
                setResult(RESULT_OK,intentResult);
                finish();
            }
        });
        dialog.show();

    }



    private void buildReSignUpDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_re_sign_up);

        TextView closeTv = dialog.findViewById(R.id.close_text_view);
        CardView cancelCv = dialog.findViewById(R.id.cancel_button_cv);
        CardView reSignInCv = dialog.findViewById(R.id.re_sign_in_button_cv);


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

        closeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        cancelCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        reSignInCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentResult = new Intent();
                intentResult.putExtra("action",Action.RE_SIGNUP);
                setResult(RESULT_OK,intentResult);
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.slide_out_right);
    }
}
package com.example.hprestaurant;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;

import android.content.ContentResolver;
import android.content.Intent;


import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hprestaurant.Models.Customer;
import com.example.hprestaurant.Models.Request.ServerRequest;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityUpdateInfoBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("ClickableViewAccessibility")
public class UpdateInfoActivity extends AppCompatActivity {
    private ActivityUpdateInfoBinding binding;
    private DatePickerDialog datePickerDialog;
    private String[] genders = {"Nam","Nữ","Khác"};
    private ArrayAdapter<String> genderAdapter;
    private Customer customer;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri imageUri;
    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setOnTouchEditText();
        genderAdapter = new ArrayAdapter<>(this,R.layout.item_in_auto_com_text_view,genders);
        binding.genderAutoCompleteTextView.setAdapter(genderAdapter);


        customer = (Customer) getIntent().getSerializableExtra("customer");
        setViewInfo();
        buildDialogDatePicker();
        binding.dateOfBirthEditText.setInputType(InputType.TYPE_NULL);

        storageRef = FirebaseStorage.getInstance().getReference();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            imageUri = data.getData();
                            binding.avatar.setImageURI(imageUri);
                        }
                        else {
                            Toast.makeText(UpdateInfoActivity.this, "Chưa chọn ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        binding.changeAvatarTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(!isValidInput()){
                   return;
               }
               updateInfoCustomer();
            }
        });


        binding.phoneNumberEditText.setText(Auth.phoneNumber);


    }

    private void updateInfoCustomer() {
        Customer customer = new Customer();
        customer.setUser_uid(Auth.userUid);
        customer.setFull_name(binding.nameEditText.getText().toString().trim());
        customer.setEmail(binding.emailEditText.getText().toString().trim());
        customer.setGender(binding.genderAutoCompleteTextView.getText().toString().trim());
        String dateString = binding.dateOfBirthEditText.getText().toString().trim();
        // Define a DateTimeFormatter for the specified format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            // Parse the date string into a LocalDate
            LocalDate parsedDate = LocalDate.parse(dateString, formatter);
            Instant instant = parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            customer.setDate_of_birth(Date.from(instant));
            // Now you can work with the parsed LocalDate object
            System.out.println("Parsed Date: " + parsedDate);

            // You can also format the LocalDate object back to a string
            String formattedDate = parsedDate.format(formatter);
            System.out.println("Formatted Date: " + formattedDate);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error parsing date string.");
        }

        inProgress(true);
        if (imageUri != null) {
            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
            StorageReference imgReference = storageRef.child("user/customers/" + Auth.userUid + "." + getFileExtension(imageUri));
            imgReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imgReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            customer.setAvatar(uri.toString());
                            saveInfoToServer(customer);
                        }

                    });
                }
            });
        }
        else
        {
            //Không có ảnh
            saveInfoToServer(customer);
        }
    }

    private void saveInfoToServer(Customer customer) {
        ApiService.apiService.updateCustomer(customer).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if(response.isSuccessful()){
                    Toast.makeText(UpdateInfoActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    Intent intentResult = new Intent();
                    intentResult.putExtra("action",Action.UPDATE_INFO_CUSTOMER);
                    intentResult.putExtra("customer",response.body());
                    setResult(RESULT_OK,intentResult);
                    finish();
                }else{
                    Toast.makeText(UpdateInfoActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                Toast.makeText(UpdateInfoActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
                inProgress(false);
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void selectPhoto() {
        Intent photoPicker = new Intent();
        photoPicker.setAction(Intent.ACTION_GET_CONTENT);
        photoPicker.setType("image/*");
        activityResultLauncher.launch(photoPicker);
    }
    private void getInfoCustomer() {
        inProgress(true);

        ApiService.apiService.getInfoCustomer(new ServerRequest(Auth.getUserUid())).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if(response.isSuccessful()){
                    customer = response.body();
                    setViewInfo();
                    buildDialogDatePicker();
                    inProgress(false);

                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                Toast.makeText(UpdateInfoActivity.this, "Server err", Toast.LENGTH_SHORT).show();
                inProgress(false);
            }
        });

    }

    private void setViewInfo() {
        binding.nameEditText.setText(customer.getFull_name());
        if(customer.getDate_of_birth()!=null){
            binding.dateOfBirthEditText.setText(Util.DayFormatting(customer.getDate_of_birth()));
        }
        if(customer.getGender()!=null ){
            binding.genderAutoCompleteTextView.setText(customer.getGender());
        }
        if(customer.getEmail() != null){
            binding.emailEditText.setText(customer.getEmail());
        }
        if(customer.getAvatar() != null && !customer.getAvatar().isEmpty()){
            Glide.with(this).load(customer.getAvatar()).into(binding.avatar);
        }

    }

    private void setOnTouchEditText() {
        binding.nameEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.nameTextInputLayout.setError(null);
                return false;
            }
        });
        binding.genderAutoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.genderTextInputLayout.setError(null);
                return false;
            }
        });

        binding.emailEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.emailTextInputLayout.setError(null);
                return false;
            }
        });


        binding.dateOfBirthEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.datePickerTextInputLayout.setError(null);
                datePickerDialog.show();
                return false;
            }
        });

    }

    private boolean isValidInput() {
        if(binding.nameEditText.getText().toString().trim().isEmpty()){
            binding.nameTextInputLayout.setError("Chưa nhập tên!");
            return false;
        }else {
            binding.nameTextInputLayout.setError(null);

        }
        if(binding.genderAutoCompleteTextView.getText().toString().trim().isEmpty()){
            binding.genderTextInputLayout.setError("Chưa nhập giới tính!");
            return false;
        }else{
            binding.genderTextInputLayout.setError(null);
        }
        if(binding.dateOfBirthEditText.getText().toString().trim().isEmpty()){
            binding.datePickerTextInputLayout.setError("Chưa nhập ngày sinh!");
            return false;
        }else {
            binding.datePickerTextInputLayout.setError(null);
        }
        if(binding.emailEditText.getText().toString().trim().isEmpty()){
            binding.emailTextInputLayout.setError("Chưa nhập email!");
            return false;
        }else {
            binding.emailTextInputLayout.setError(null);
        }
        String email = binding.emailEditText.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Email không hợp lệ
            binding.emailTextInputLayout.setError("Email chưa đúng định dạng!");
            return  false;
        }else{
            binding.emailTextInputLayout.setError(null);
        }
        return true;
    }


    void buildDialogDatePicker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);
                binding.dateOfBirthEditText.setText(date);
                binding.dateOfBirthEditText.setSelection(date.length());
            }
        };

        Calendar calendar = Calendar.getInstance();
        if(!binding.dateOfBirthEditText.getText().toString().isEmpty()){
            Date dateOB = Util.DateParse(binding.dateOfBirthEditText.getText().toString());
            assert dateOB != null;
            calendar.setTime(dateOB);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT ,dateSetListener,year,month,day);
            datePickerDialog.getDatePicker().setBackgroundColor(getColor(R.color.lightest_yellow_orange));
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            return;
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT ,dateSetListener,year,month,day);
        datePickerDialog.getDatePicker().setBackgroundColor(getColor(R.color.lightest_yellow_orange));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());


    }
    private String makeDateString(int day, int month, int year){
        if(day < 10) return "0"+day+"/"+month+"/"+year;
        return day+"/"+month+"/"+year;
    }

    void inProgress(boolean isIn){
        if(isIn){
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.contentSV.setVisibility(View.GONE);
            return;
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.contentSV.setVisibility(View.VISIBLE);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.slide_out_right);
    }
}
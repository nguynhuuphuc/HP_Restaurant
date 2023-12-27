package com.example.hprestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.hprestaurant.Adapters.LocalDateAdapter;
import com.example.hprestaurant.Adapters.TimeAdapter;
import com.example.hprestaurant.Interfaces.IOnClickDateItemListener;
import com.example.hprestaurant.Models.Request.BookTableRequest;
import com.example.hprestaurant.Models.Request.OrderRequest;
import com.example.hprestaurant.Models.Response.ServerResponse;
import com.example.hprestaurant.Models.TableModel;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.ScreenUtils;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityDateTimeSelectedBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DateTimeSelectedActivity extends AppCompatActivity {
    private ActivityDateTimeSelectedBinding binding;
    private MyApplication mApp;
    private DateTimeFormatter formatter;
    private List<LocalDate> localDates;
    private LocalDateAdapter localDateAdapter;
    private int dateItemSelected = 0;
    private OrderRequest orderRequest;
    private String[] quantity = {"1","2","4","6","8"};
    private ArrayAdapter<String> quantityPeopleAdapter;
    private TimeAdapter timeAdapter;
    private List<LocalTime> timeList;
    private LocalDate dateBooking = LocalDate.now();
    private LocalTime timeSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDateTimeSelectedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mApp = (MyApplication) getApplication();

        orderRequest = (OrderRequest) getIntent().getSerializableExtra("orderRequest");

        formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy", new Locale("vi"));
        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        quantityPeopleAdapter = new ArrayAdapter<>(this,R.layout.item_in_auto_com_text_view,quantity);
        binding.numberPersonAutoCompleteTextView.setAdapter(quantityPeopleAdapter);

        localDates = getDates();
        localDateAdapter = new LocalDateAdapter(this, localDates, new IOnClickDateItemListener() {
            @Override
            public void onClick(int position) {
                // Sử dụng formatter để chuyển LocalDate thành chuỗi
                dateItemSelected = position;
                LocalDate date = localDates.get(position);
                dateBooking = date;
                String formattedDate = date.format(formatter);
                binding.dateTextView.setText(formattedDate);
                timeList.clear();
                timeList = generateTimeList("10:30", "22:00", 30);
                timeAdapter.setData(timeList);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this,7);
        binding.daysRecyclerView.setLayoutManager(layoutManager);
        binding.daysRecyclerView.setAdapter(localDateAdapter);

        timeList = generateTimeList("10:30", "22:00", 30);
        timeAdapter = new TimeAdapter(this,timeList);
        timeAdapter.setOnClickItemListener(new TimeAdapter.onClickItemListener() {
            @Override
            public void onClick(LocalTime time) {
                timeSelected = time;
            }
        });
        int screenWidth = ScreenUtils.getScreenWidth(getApplicationContext());
        float dpWidth = ScreenUtils.pxToDp(getApplicationContext(), screenWidth);
        int spanCount = (int) (dpWidth / 80);

        GridLayoutManager timeGridLayoutManager = new GridLayoutManager(this,spanCount);
        binding.timesRv.setLayoutManager(timeGridLayoutManager);
        binding.timesRv.setAdapter(timeAdapter);

        binding.bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timeSelected == null){
                    Toast.makeText(DateTimeSelectedActivity.this, "Bạn chưa chọn thời gian đặt!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String  quantity_peopleS = binding.numberPersonAutoCompleteTextView.getText().toString();
                if(quantity_peopleS.isEmpty()){
                    Toast.makeText(DateTimeSelectedActivity.this, "Bạn chưa chọn số lượng người!", Toast.LENGTH_SHORT).show();
                    return;
                }
                postBooking();

            }
        });
    }

    private void postBooking() {
        int quantity = Integer.parseInt(binding.numberPersonAutoCompleteTextView.getText().toString().trim());
        BookTableRequest bookTableRequest = new BookTableRequest(dateBooking,timeSelected);
        orderRequest.setDate(bookTableRequest.getDate());
        orderRequest.setQuantity_people(quantity);
        ApiService.apiService.bookingDishesOnly(orderRequest).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if(response.isSuccessful()){
                    Toast.makeText(mApp, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intentResult = new Intent();
                    intentResult.putExtra("action", Action.BOOKING_SUCCESS);
                    setResult(RESULT_OK,intentResult);
                    notifyBookingSuccess(response.body().getReservation_id());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }
    private void notifyBookingSuccess(int reservation_id) {
        JSONObject object = new JSONObject();
        try {
            object.put("isCustomer",true);
            object.put("from", Auth.userUid);
            JSONObject message = new JSONObject();
            message.put("reservation_id",reservation_id);
            message.put("status","đã đặt bàn");
            object.put("message",message);
            object.put("action","BOOKING_TABLE");
            mApp.getWebsocketClient().sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LocalTime> generateTimeList(String sStartTime, String endTime, int intervalMinutes) {
        List<LocalTime> timeList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate currentDate = LocalDate.now();
        LocalTime endTimeValue = LocalTime.parse(endTime,formatter);
        if(dateBooking.equals(currentDate)){
            LocalTime currentTime = LocalTime.now();
            LocalTime startTime = LocalTime.parse(sStartTime, formatter);

            if(startTime.isAfter(currentTime)){
                currentTime = startTime;
            }
            int minute = currentTime.getMinute();
            // Làm tròn lên đến 30 phút kế tiếp
            if (minute < 30) {
                currentTime =  currentTime.withMinute(30).withSecond(0).withNano(0);
            } else {
                currentTime = currentTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            }
            while (currentTime.isBefore(endTimeValue) || currentTime.equals(endTimeValue)) {
                timeList.add(currentTime);
                currentTime = currentTime.plusMinutes(intervalMinutes);
            }

            return timeList;
        }
        LocalTime currentTime = LocalTime.parse(sStartTime, formatter);
        while (currentTime.isBefore(endTimeValue) || currentTime.equals(endTimeValue)) {
            timeList.add(currentTime);
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }

        return timeList;
    }

    private List<LocalDate> getDates() {
        List<LocalDate> localDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(formatter);
        binding.dateTextView.setText(formattedDate);
        localDates.add(today);

        // Thêm 6 ngày tiếp theo vào List
        for (int i = 1; i <= 6; i++) {
            localDates.add(today.plusDays(i));
        }
        return localDates;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.slide_out_right);
    }
}
package com.example.hprestaurant;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.hprestaurant.Adapters.TimeAdapter;
import com.example.hprestaurant.Interfaces.MyWebSocketListener;
import com.example.hprestaurant.Models.Request.BookTableRequest;
import com.example.hprestaurant.Models.Request.OrderRequest;
import com.example.hprestaurant.Models.Request.ReservationRequest;
import com.example.hprestaurant.Models.Request.ServerRequest;
import com.example.hprestaurant.Models.ReservationModel;
import com.example.hprestaurant.Models.Response.ServerResponse;
import com.example.hprestaurant.Models.TableModel;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.Utils.MyWebsocketClient;
import com.example.hprestaurant.Utils.ScreenUtils;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityTableBookingBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableBookingActivity extends AppCompatActivity {
    private ActivityTableBookingBinding binding;
    private TableModel tableModel;
    private TimeAdapter timeAdapter;
    private List<LocalTime> timeList;
    private DateTimeFormatter formatter;
    private LocalDate dateBooking;
    private List<ReservationModel> reservationList;
    private Handler handler;
    private Runnable runnable;
    private LocalTime currentTime;
    private OrderRequest orderRequest;
    private LocalTime timeSelected = null;
    private MyApplication mApp;
    private int reservation_id;
    private OnBackPressedCallback onBackPressedCallback;
    private Dialog progressDialog;
    private String[] quantity = {"1","2","4","6","8"};
    private ArrayAdapter<String> quantityPeopleAdapter;
    private boolean isBookingSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTableBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mApp = (MyApplication) getApplication();
        buildProgressDialog();

        mApp.getWebsocketClient().setOnConnectionListener(new MyWebsocketClient.OnConnectionListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {
                //reconnect
                mApp.getWebsocketClient().reconnectWebSocket();
            }
        });

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                reservationInProgressRelease();
            }
        };

        quantityPeopleAdapter = new ArrayAdapter<>(this,R.layout.item_in_auto_com_text_view,quantity);
        binding.numberPersonAutoCompleteTextView.setAdapter(quantityPeopleAdapter);

        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

        orderRequest = (OrderRequest) getIntent().getSerializableExtra("orderRequest");
        tableModel = (TableModel) getIntent().getSerializableExtra("tableModel");
        dateBooking = (LocalDate) getIntent().getSerializableExtra("bookingDate");
        reservation_id = getIntent().getIntExtra("reservation_id",-1);
        formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy", new Locale("vi"));
        String formattedDate = dateBooking.format(formatter);
        binding.dateTextView.setText(formattedDate);
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        reservationList = new ArrayList<>();
        getReservation();
        currentTime = LocalTime.now();
        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reservationInProgressRelease();
                
            }
        });

        binding.toolBarTitle.setText(tableModel.getTable_name());


        timeList = generateTimeList("10:30", "22:00", 30);

        timeAdapter = new TimeAdapter(this,timeList);
        timeAdapter.setOnClickItemListener(new TimeAdapter.onClickItemListener() {
            @Override
            public void onClick(LocalTime time) {
                timeSelected = time;
            }
        });


        if(dateBooking.equals(LocalDate.now())){
            // Khởi tạo Handler và Runnable
            handler = new Handler();
            updateCurrentTime();
            // Cập nhật thời gian mỗi giây
            handler.postDelayed(runnable, 1000);
        }




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
                    Toast.makeText(TableBookingActivity.this, "Bạn chưa chọn thời gian đặt!", Toast.LENGTH_SHORT).show(); 
                    return;
                }
                String  quantity_peopleS = binding.numberPersonAutoCompleteTextView.getText().toString();
                if(quantity_peopleS.isEmpty()){
                    Toast.makeText(TableBookingActivity.this, "Bạn chưa chọn số lượng người!", Toast.LENGTH_SHORT).show();
                    return;
                }
                postBooking();
            }
        });

    }

    private void buildProgressDialog() {
        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.dialog_progress_bar_with_content);

        Window window = progressDialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttribute = window.getAttributes();
        windowAttribute.gravity = Gravity.CENTER;
        window.setAttributes(windowAttribute);
    }

    private void reservationInProgressRelease() {
        inProgress(true);
        ApiService.apiService.reservationInProgressRelease(new ReservationRequest(reservation_id))
                .enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        if(response.isSuccessful()){
                            if(!isBookingSuccess){
                                notifyTableInProgress();
                            }
                            finish();

                        }
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        Toast.makeText(mApp, "server err", Toast.LENGTH_SHORT).show();
                        inProgress(false);
                    }
                });
    }
    private void notifyTableInProgress() {
        JSONObject object = new JSONObject();
        try {
            object.put("tableInProgress",false);
            object.put("from", Auth.userUid);
            object.put("tableId",tableModel.getTable_id());
            object.put("dateBooking",dateBooking);
            mApp.getWebsocketClient().sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void postBooking() {
        int quantity = Integer.parseInt(binding.numberPersonAutoCompleteTextView.getText().toString().trim());

        BookTableRequest request = new BookTableRequest(tableModel.getTable_id(),
                mApp.getCustomer().getCustomer_id(),
                dateBooking,timeSelected,
                quantity);
        request.setReservation_id(reservation_id);
        if(orderRequest == null){
            request.setCreated_time(new Date());
            ApiService.apiService.booingTable(request).enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    if(response.isSuccessful()){
                        Intent intentResult = new Intent();
                        intentResult.putExtra("action", Action.BOOKING_SUCCESS);
                        setResult(RESULT_OK,intentResult);
                        Toast.makeText(mApp, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        notifyBookingSuccess(tableModel,response.body().getReservation_id());
                        isBookingSuccess = true;
                        reservationInProgressRelease();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {

                }
            });
            return;
        }
        orderRequest.setDate(request.getDate());
        orderRequest.setTable_id(request.getTable_id());
        orderRequest.setReservation_id(reservation_id);
        orderRequest.setQuantity_people(quantity);
        orderRequest.setCreated_time(new Date());
        ApiService.apiService.bookingDishesAndTable(orderRequest).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Toast.makeText(mApp, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                Intent intentResult = new Intent();
                intentResult.putExtra("action", Action.BOOKING_SUCCESS);
                setResult(RESULT_OK,intentResult);
                notifyBookingSuccess(tableModel,reservation_id);
                isBookingSuccess = true;
                reservationInProgressRelease();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }


    private void updateCurrentTime() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // Lấy thời gian hiện tại
                LocalTime currentTime = LocalTime.now();

                int hour = currentTime.getHour();
                // Lấy giờ và phút từ currentTime

                // Kiểm tra xem có phần tử nào trong danh sách có cùng giờ và phút không
                boolean matchFound = timeList.stream()
                        .anyMatch(time -> time.getHour() == hour);

                if (matchFound) {
                    // Xử lý hành động khi giờ và phút của currentTime trùng với một phần tử trong list
                    timeAdapter.getFilter().filter("update");
                }
                // Cập nhật giá trị thời gian
                // Ở đây bạn có thể làm gì đó với giá trị thời gian mới, chẳng hạn như hiển thị nó trên giao diện người dùng
                // Ví dụ: textView.setText(currentTime);

                // Tiếp tục cập nhật sau 1 giây
                handler.postDelayed(this, 1000);
            }
        };
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng việc cập nhật khi activity bị hủy
        if(dateBooking.equals(LocalDate.now()))
            handler.removeCallbacks(runnable);
    }

    private void notifyBookingSuccess(TableModel tableModel,int reservation_id) {
        JSONObject object = new JSONObject();
        try {
            object.put("isCustomer",true);
            object.put("from", Auth.userUid);
            JSONObject message = new JSONObject();
            message.put("reservation_id",reservation_id);
            message.put("status","đã đặt " + tableModel.getTable_name()) ;
            message.put("booking_table_id",tableModel.getTable_id());
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
    private void getReservation() {
        ApiService.apiService.getReservationByDay(new ServerRequest(tableModel.getTable_id(),dateBooking.toString())).enqueue(new Callback<ArrayList<ReservationModel>>() {
            @Override
            public void onResponse(Call<ArrayList<ReservationModel>> call, Response<ArrayList<ReservationModel>> response) {
                if(response.isSuccessful()){
                    reservationList.addAll(response.body());
                    if(reservationList.isEmpty()) return;
                    LocalTime max = LocalTime.MIN;
                    for (ReservationModel model : reservationList){
                        LocalTime time = LocalTime.parse(model.getAfter_available());
                        if(time.isAfter(max)){
                            max = time;
                        }
                    }
                    timeAdapter.getFilter().filter(max. toString());

                }
            }
            @Override
            public void onFailure(Call<ArrayList<ReservationModel>> call, Throwable t) {

            }
        });
    }
    private void inProgress(boolean isIn){
        if(isIn){
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.bookingButton.setEnabled(false);
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.bookingButton.setEnabled(true);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.slide_out_right);
    }

}
package com.example.hprestaurant;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.hprestaurant.Adapters.CategoryTableAdapter;
import com.example.hprestaurant.Adapters.LocalDateAdapter;
import com.example.hprestaurant.Adapters.TableAdapter;
import com.example.hprestaurant.Interfaces.IClickItemCategoryTableListener;
import com.example.hprestaurant.Interfaces.IClickItemTableListener;
import com.example.hprestaurant.Interfaces.IOnClickDateItemListener;
import com.example.hprestaurant.ItemDecorations.GridSpacingItemDecoration;
import com.example.hprestaurant.Models.LocationModel;
import com.example.hprestaurant.Models.MessageEvent;
import com.example.hprestaurant.Models.Request.BookTableRequest;
import com.example.hprestaurant.Models.Request.DateTimeRequest;
import com.example.hprestaurant.Models.Request.OrderRequest;
import com.example.hprestaurant.Models.ReservationModel;
import com.example.hprestaurant.Models.TableModel;
import com.example.hprestaurant.Utils.Action;
import com.example.hprestaurant.Utils.Auth;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.ActivityBookingBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {
    private ActivityBookingBinding binding;
    private List<LocalDate> localDates;
    private LocalDateAdapter localDateAdapter;
    private DateTimeFormatter formatter;
    private List<LocationModel> locationList;
    private CategoryTableAdapter categoryTableAdapter;
    private List<TableModel> tableModelList;
    private TableAdapter tableAdapter;
    private String location = "1";
    private MyApplication mApp;
    private OrderRequest orderRequest;
    private Dialog progressDialog;
    private int dateItemSelected = 0;
    private int timeItemSelected = 0;
    private ActivityResultLauncher<Intent> launcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        registerLauncher();
        mApp = (MyApplication) getApplication();
        buildProgressDialog();
        int action = getIntent().getIntExtra("action",-1);
        if(action == Action.DISHES_SELECTED){
            orderRequest = (OrderRequest) getIntent().getSerializableExtra("orderRequest");
        }

        formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy", new Locale("vi"));
        binding.toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        localDates = getDates();
        localDateAdapter = new LocalDateAdapter(this, localDates, new IOnClickDateItemListener() {
            @Override
            public void onClick(int position) {
                // Sử dụng formatter để chuyển LocalDate thành chuỗi
                dateItemSelected = position;
                LocalDate date = localDates.get(position);
                String formattedDate = date.format(formatter);
                binding.dateTextView.setText(formattedDate);
                getAllTables();
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this,7);
        binding.daysRecyclerView.setLayoutManager(layoutManager);
        binding.daysRecyclerView.setAdapter(localDateAdapter);



        locationList = new ArrayList<>();
        categoryTableAdapter = new CategoryTableAdapter(this,locationList, new IClickItemCategoryTableListener() {
            @Override
            public void onClickItemCategoryTableListener(LocationModel locationModel) {
                location = String.valueOf(locationModel.getLocation_id());
                tableAdapter.getFilter().filter(location);
            }

        });
        viewTableCategories();
        binding.categoryTablesRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryTablesRv.setAdapter(categoryTableAdapter);



        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        binding.tablesRv.setLayoutManager(gridLayoutManager);

        // Đặt ItemDecoration để tạo khoảng trống ở trên và dưới các hàng của lưới
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing); // Đặt giá trị tùy ý
        tableModelList = new ArrayList<>();

        binding.tablesRv.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true, 0));
        tableAdapter = new TableAdapter(this,tableModelList, new IClickItemTableListener() {
            @Override
            public void onClickItemTableListener(TableModel tableModel) {
                getReservationInProgress(tableModel);

            }
        });
        getAllTables();
        binding.tablesRv.setAdapter(tableAdapter);
    }

    private void getReservationInProgress(TableModel tableModel) {
        setContentProgressDialog(tableModel.getTable_name());
        progressDialog.show();
        ApiService.apiService.reservationInProgress(new BookTableRequest(tableModel.getTable_id(),
                        mApp.getCustomer().getCustomer_id(),
                        localDates.get(dateItemSelected).toString()))
                .enqueue(new Callback<ReservationModel>() {
                    @Override
                    public void onResponse(Call<ReservationModel> call, Response<ReservationModel> response) {
                        if(response.isSuccessful()){
                            Intent intent = new Intent(BookingActivity.this,TableBookingActivity.class);
                            intent.putExtra("orderRequest",orderRequest);
                            intent.putExtra("reservation_id", response.body().getId());
                            intent.putExtra("bookingDate",localDates.get(dateItemSelected));
                            intent.putExtra("tableModel",tableModel);
                            notifyTableInProgress(tableModel);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    activityLauncher(intent);
                                    progressDialog.dismiss();
                                }
                            },2000);

                        }

                    }

                    @Override
                    public void onFailure(Call<ReservationModel> call, Throwable t) {
                        Toast.makeText(mApp, "server err", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void notifyTableInProgress(TableModel tableModel) {
        JSONObject object = new JSONObject();
        try {
            object.put("tableInProgress",true);
            object.put("from", Auth.userUid);
            object.put("tableId",tableModel.getTable_id());
            object.put("dateBooking",localDates.get(dateItemSelected));
            mApp.getWebsocketClient().sendMessage(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setContentProgressDialog(String table_name) {
        TextView content = progressDialog.findViewById(R.id.content_text_view);
        String text = table_name+"\n"+getString(R.string.in_progress_booking_table_content);
        content.setText(text);
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


    private void activityLauncher(Intent intent){
        launcher.launch(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
    }

    private void getAllTables() {
        inProgress(true);
        if(!tableModelList.isEmpty()){
            tableModelList.clear();
        }
        ApiService.apiService.getAllTables(new DateTimeRequest(localDates.get(dateItemSelected)))
                .enqueue(new Callback<ArrayList<TableModel>>() {
                    @Override
                    public void onResponse(Call<ArrayList<TableModel>> call, Response<ArrayList<TableModel>> response) {
                        if(response.isSuccessful()){
                            assert response.body() !=null  ;
                            tableModelList.addAll(response.body());
                            tableAdapter.getFilter().filter(location);
                            
                        }
                        inProgress(false);
                    }
                    @Override
                    public void onFailure(Call<ArrayList<TableModel>> call, Throwable t) {
                        Toast.makeText(mApp, "Server err", Toast.LENGTH_SHORT).show();
                        inProgress(false);
                    }
                });
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

    private void viewTableCategories() {
        if(!locationList.isEmpty()){
            locationList.clear();
        }
        ApiService.apiService.getAllLocations()
                .enqueue(new Callback<ArrayList<LocationModel>>() {
                    @Override
                    public void onResponse(Call<ArrayList<LocationModel>> call, Response<ArrayList<LocationModel>> response) {
                        if(response.isSuccessful()){
                            assert response.body() != null;
                            locationList.addAll(response.body());
                            categoryTableAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<LocationModel>> call, Throwable t) {

                    }
                });
    }
    private void registerLauncher() {
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent data = o.getData();
                    assert data != null;
                    int action = data.getIntExtra("action",-1);
                    if(action == Action.BOOKING_SUCCESS){
                        setResult(-1,o.getData());
                        finish();
                    }
                }
            }
        });
    }

    private void inProgress(boolean isIn){
        if(isIn){
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.nestedScrollView.setEnabled(false);
            return;
        }
        binding.progressBar.setVisibility(View.GONE);
        binding.nestedScrollView.setEnabled(true);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        LocalDate date = localDates.get(dateItemSelected);
        LocalDate dateUpdate = event.getDateUpdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if(dateUpdate.equals(date)){
            tableAdapter.updatingTableInProgress(event);
        }
    }
}
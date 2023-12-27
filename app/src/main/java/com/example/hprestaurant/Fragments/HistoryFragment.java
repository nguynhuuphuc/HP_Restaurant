package com.example.hprestaurant.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hprestaurant.Adapters.ReservationAdapter;
import com.example.hprestaurant.Models.MessageEvent;
import com.example.hprestaurant.Models.Request.CustomerRequest;
import com.example.hprestaurant.Models.ReservationModel;
import com.example.hprestaurant.MyApplication;
import com.example.hprestaurant.R;
import com.example.hprestaurant.api.ApiService;
import com.example.hprestaurant.databinding.FragmentHistoryBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private List<ReservationModel> reservationModelList;
    private ReservationAdapter reservationAdapter;
    private MyApplication mApp;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        mApp = (MyApplication) requireActivity().getApplication();
        reservationModelList = new ArrayList<>();
        Toast.makeText(mApp, String.valueOf(mApp.getCustomer().getCustomer_id()), Toast.LENGTH_SHORT).show();
        getReservationByCustomer();

        reservationAdapter = new ReservationAdapter(getContext(),reservationModelList);
        binding.reservationRv.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        binding.reservationRv.setAdapter(reservationAdapter);
        reservationAdapter.setOnClickItemListener(new ReservationAdapter.OnClickItemListener() {
            @Override
            public void onClick(int position) {

            }
        });
        

        return binding.getRoot();
    }

    private void getReservationByCustomer() {
        ApiService.apiService.getReservationByCustomerId(new CustomerRequest(mApp.getCustomer().getCustomer_id()))
                .enqueue(new Callback<ArrayList<ReservationModel>>() {
                    @Override
                    public void onResponse(Call<ArrayList<ReservationModel>> call, Response<ArrayList<ReservationModel>> response) {
                        if(response.isSuccessful()){
                            reservationModelList.addAll(response.body());
                            reservationAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<ReservationModel>> call, Throwable t) {

                    }
                });
    }



    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if(event.getReservationModels() != null || !event.getReservationModels().isEmpty()){
            for (ReservationModel model : event.getReservationModels()){
                reservationAdapter.updateItem(model);
            }
        }
        if(EventBus.getDefault().removeStickyEvent(event)){
            event.setReservationModels(new ArrayList<>());
        }

    }
}

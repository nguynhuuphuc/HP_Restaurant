package com.example.hprestaurant.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hprestaurant.R;
import com.example.hprestaurant.databinding.FragmentVoucherBinding;

public class VoucherFragment extends Fragment {
    private FragmentVoucherBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVoucherBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }
}

package com.example.hprestaurant.Fragments;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import com.example.hprestaurant.MainActivity;


import com.example.hprestaurant.MemberActivity;
import com.example.hprestaurant.MessageActivity;
import com.example.hprestaurant.R;
import com.example.hprestaurant.Utils.Util;
import com.example.hprestaurant.databinding.FragmentUserBinding;

import java.util.Objects;

public class UserFragment extends Fragment {
    private FragmentUserBinding binding;
    private ActivityResultLauncher<Intent> launcher;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(getLayoutInflater());
        MainActivity activity = (MainActivity) getActivity();
        launcher = activity.getLauncher();

        binding.memberCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcher.launch(new Intent(getContext(), MemberActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
            }
        });

        binding.contactPhoneFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = "tel:" + Util.RESTAURANT_PHONE;
                Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber));

                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE},23);
                }
                else
                {
                    startActivity(dialIntent);
                }

            }
        });
        binding.contactMessageFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcher.launch(new Intent(getContext(), MessageActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.no_animation);
            }
        });

        return binding.getRoot();
    }
}

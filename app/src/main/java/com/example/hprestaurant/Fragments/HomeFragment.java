package com.example.hprestaurant.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hprestaurant.Adapters.ImageSliderAdapter;
import com.example.hprestaurant.R;
import com.example.hprestaurant.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private List<Integer> imageList = new ArrayList<>();
    private Handler handler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(binding.viewPager.getCurrentItem() == imageList.size() -1){
                binding.viewPager.setCurrentItem(0);
            }else{
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem()+1);
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        // Thêm các tấm hình vào danh sách
        imageList.add(R.drawable.home_img1);
        imageList.add(R.drawable.home_img2);

        // Tạo adapter và thiết lập cho ViewPager2
        ImageSliderAdapter adapter = new ImageSliderAdapter(imageList);
        binding.viewPager.setAdapter(adapter);
        binding.circleIndicator3.setViewPager(binding.viewPager);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                handler.removeCallbacks(mRunnable);
                handler.postDelayed(mRunnable,5000);
            }
        });



        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(mRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(mRunnable,5000);
    }
}

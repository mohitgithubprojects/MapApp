package com.example.mapboxapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.mapboxapp.databinding.ActivityProgressBinding;

public class ProgressActivity extends AppCompatActivity {

    private ActivityProgressBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.lottieAnimation.animate().translationX(1500).translationY(-1500).setDuration(2000).setStartDelay(6000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(ProgressActivity.this, MapsActivity.class));
            }
        },8000);
    }
}
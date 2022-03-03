package com.example.whatsappclone.AuthenticationActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.whatsappclone.databinding.ActivityPhoneNumberSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneNumberSignUp extends AppCompatActivity {

    ActivityPhoneNumberSignUpBinding binding;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberSignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getSupportActionBar().hide();



        binding.btnGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneNumberSignUp.this,otpActivity.class);
                intent.putExtra("phoneNumber",binding.etPhone.getText().toString());
                intent.putExtra("userName",binding.etUsername.getText().toString());
                intent.putExtra("password",binding.etPassword.getText().toString());
                startActivity(intent);
            }
        });

    }
}
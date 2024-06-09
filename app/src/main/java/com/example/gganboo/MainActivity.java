package com.example.gganboo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gganboo.signin.SigninActivity;
import com.example.gganboo.signup.SignupActivity;
import com.example.gganboo.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityMainBinding binding; // View binding을 위한 변수
    private FirebaseAuth mAuth; // FirebaseAuth 인스턴스를 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Edge-to-edge 디스플레이 설정
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // View binding을 통해 레이아웃 설정

        binding.btnSignUp.setOnClickListener(this); // 회원가입 버튼 클릭 리스너 설정
        binding.btnSignIn.setOnClickListener(this); // 로그인 버튼 클릭 리스너 설정

        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth 인스턴스 초기화
        FirebaseUser currentUser = mAuth.getCurrentUser(); // 현재 로그인된 사용자 정보 가져오기

        if (currentUser != null) {
            // 로그인이 되어 있으면 바로 GganbooActivity로 이동
            Intent intent = new Intent(MainActivity.this, GganBooActivity.class);
            startActivity(intent);
            finish(); // MainActivity 종료
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        if(v == binding.btnSignUp){
            // 회원가입 버튼 클릭 시 SignupActivity로 이동
            Intent signIntent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(signIntent);
        } else if(v == binding.btnSignIn){
            // 로그인 버튼 클릭 시 SigninActivity로 이동
            Intent signIntent = new Intent(MainActivity.this, SigninActivity.class);
            startActivity(signIntent);
        }
    }
}
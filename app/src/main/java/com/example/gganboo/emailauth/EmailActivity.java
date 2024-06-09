package com.example.gganboo.emailauth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gganboo.R;
import com.example.gganboo.databinding.ActivityEmailBinding;
import com.example.gganboo.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailActivity extends AppCompatActivity implements View.OnClickListener {

    private @NonNull ActivityEmailBinding binding; // ViewBinding 객체
    private FirebaseAuth mAuth; // FirebaseAuth 객체
    private String userEmail; // 사용자 이메일
    private String userPassword; // 사용자 비밀번호

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 전체화면을 위한 설정
        binding = ActivityEmailBinding.inflate(getLayoutInflater()); // ViewBinding 설정
        setContentView(binding.getRoot());

        // FirebaseAuth 초기화
        mAuth = FirebaseAuth.getInstance();

        // Intent로부터 이메일과 비밀번호 값을 받음
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");
        userPassword = intent.getStringExtra("userPassword");

        // 이메일 확인 메시지 설정
        binding.txtEmailCon.setText("이메일 '" + userEmail + "'" + binding.txtEmailCon.getText());

        // 이메일 인증 버튼 클릭 리스너 설정
        binding.btnEmailAuth.setOnClickListener(this);

        // 시스템 바 여백 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnEmailAuth) {
            checkEmailVerification(); // 이메일 인증 확인 메서드 호출
        }
    }

    // 이메일 인증 확인 메서드
    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    showToast("이메일 인증이 완료되었습니다.");
                    // 프로필 설정 페이지로 이동
                    Intent intent = new Intent(EmailActivity.this, ProfileActivity.class);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("userPassword", userPassword);
                    startActivity(intent);
                    finish(); // 현재 액티비티 종료
                } else {
                    showToast("이메일 인증을 확인해주세요.");
                }
            });
        }
    }

    // 토스트 메시지 표시 메서드
    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}

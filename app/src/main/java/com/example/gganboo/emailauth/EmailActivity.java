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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailActivity extends AppCompatActivity implements View.OnClickListener {

    private @NonNull ActivityEmailBinding binding;
    private FirebaseAuth mAuth;
    private String userEmail;
    private String userPassword;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // FirebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // Intent로부터 이메일과 비밀번호 값을 받음
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");
        userPassword = intent.getStringExtra("userPassword");

        if (userEmail != null) {
            binding.txtEmailCon.setText("이메일이 '" + userEmail + "' " + binding.txtEmailCon.getText());
        }

        binding.btnEmailAuth.setOnClickListener(this);

        sendEmailVerification();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnEmailAuth) {
            verifyEmail();
        }
    }

    // 이메일 인증 이메일 보내기
    private void sendEmailVerification() {
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(this, verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            showToast("이메일 인증 메일을 보냈습니다. 메일을 확인해주세요.");
                                        } else {
                                            showToast("이메일 인증 메일 전송에 실패했습니다.");
                                        }
                                    });
                        }
                    } else {
                        showToast("회원가입에 실패했습니다: " + task.getException().getMessage());
                    }
                });
    }

    // 이메일 인증 확인 및 회원 추가
    private void verifyEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        addUserToFirebase();
                    } else {
                        showToast("이메일 인증이 아직 완료되지 않았습니다. 이메일을 확인해주세요.");
                    }
                } else {
                    showToast("이메일 인증 상태를 확인하는 데 실패했습니다.");
                }
            });
        } else {
            showToast("유효한 사용자를 찾을 수 없습니다.");
        }
    }

    // Firebase에 사용자 추가
    private void addUserToFirebase() {
        // Firebase Authentication에 사용자가 이미 추가되어 있으므로 별도의 추가 작업은 필요 없습니다.
        // 필요한 경우 추가 작업 수행
        showToast("회원가입이 완료되었습니다.");
        finish(); // EmailActivity 종료
    }

    // Toast 출력 메서드
    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}

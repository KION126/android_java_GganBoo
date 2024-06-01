package com.example.gganboo.signup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gganboo.R;
import com.example.gganboo.databinding.ActivitySignupBinding;
import com.example.gganboo.emailauth.EmailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySignupBinding binding;

    // 유효성 검사 변수
    private boolean isIDValid = false;
    private boolean isPWValid = false;
    private boolean isAssentChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeUI(); // UI 초기화
    }

    // UI 초기화
    private void initializeUI() {
        // 회원가입 확인 버튼
        binding.btnSignupDo.setOnClickListener(this);

        // 동의 체크 박스
        binding.cbAssent.setOnClickListener(this);

        // 아이디, 비밀번호 EditText
        binding.etEmail.addTextChangedListener(textWatcher);
        binding.etPW.addTextChangedListener(textWatcher);

        // EditText 띄어쓰기 필터 추가
        InputFilter noSpaceFilter = (source, start, end, dest, dstart, dend) -> {
            if (source != null && source.toString().contains(" ")) {
                return source.toString().replace(" ", "");
            }
            return null;
        };

        // 띄어쓰기 필터 적용
        binding.etEmail.setFilters(new InputFilter[]{noSpaceFilter});
        binding.etPW.setFilters(new InputFilter[]{noSpaceFilter});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // EditText 입력값 변화 이벤트
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkSignupButton();
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkSignupButton();
        }
    };

    // 클릭 이벤트
    @Override
    public void onClick(View v) {
        if (v == binding.cbAssent) {
            checkSignupButton();
        } else if (v == binding.btnSignupDo) {
            handleSignup();
        }
    }

    // 회원가입버튼 클릭 시
    private void handleSignup() {
        // 유효성 확인
        if (!isIDValid) {
            showToast("이메일의 형식을 확인해주세요.");
        } else if (!isPWValid) {
            showToast("비밀번호는 8자이상,\n 영문+숫자+특수문자를 필수조합해주세요.");
        } else if (!isAssentChecked) {
            showToast("이용약관과 개인정보 정책에 동의해주세요.");
        } else {
            // 이메일 인증 화면으로 이동
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPW.getText().toString().trim();
            Intent intent = new Intent(SignupActivity.this, EmailActivity.class);
            intent.putExtra("userEmail", email);
            intent.putExtra("userPassword", password);
            startActivity(intent);
        }
    }

    // Toast 출력 메서드
    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // EditText(Email, PW) 유효성 검사
    private void checkSignupButton() {
        String id = binding.etEmail.getText().toString();
        String pw = binding.etPW.getText().toString();
        isIDValid = Patterns.EMAIL_ADDRESS.matcher(id).matches();
        isPWValid = pw.length() >= 8 && pw.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$") && !pw.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
        isAssentChecked = binding.cbAssent.isChecked();

        if (isIDValid && isPWValid && isAssentChecked) {
            binding.btnSignupDo.setEnabled(true);
            binding.btnSignupDo.setTextColor(Color.BLACK);
        } else {
            binding.btnSignupDo.setEnabled(false);
            binding.btnSignupDo.setTextColor(Color.rgb(189, 189, 189));
        }
    }
}

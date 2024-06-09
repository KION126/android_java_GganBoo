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
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySignupBinding binding; // View binding을 위한 변수

    // 유효성 검사 변수
    private boolean isIDValid = false;
    private boolean isPWValid = false;
    private boolean isAssentChecked = false;

    // Firebase
    private FirebaseAuth mAuth; // FirebaseAuth 인스턴스를 위한 변수
    private DatabaseReference myDB_Reference; // Firebase Database 참조 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Edge-to-edge 디스플레이 설정
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // View binding을 통해 레이아웃 설정

        initializeFirebase();
        initializeUI();
    }

    // Firebase 초기화
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        myDB_Reference = FirebaseDatabase.getInstance().getReference("Users");
    }

    // UI 초기화
    private void initializeUI() {
        // 회원가입 확인 버튼 클릭 리스너 설정
        binding.btnSignupDo.setOnClickListener(this);

        // 동의 체크 박스 클릭 리스너 설정
        binding.cbAssent.setOnClickListener(this);

        // 아이디, 비밀번호 EditText 입력값 변화 리스너 설정
        binding.etEmail.addTextChangedListener(textWatcher);
        binding.etPW.addTextChangedListener(textWatcher);

        // 띄어쓰기 필터 추가 및 적용
        InputFilter noSpaceFilter = (source, start, end, dest, dstart, dend) -> {
            if (source != null && source.toString().contains(" ")) {
                return source.toString().replace(" ", "");
            }
            return null;
        };
        binding.etEmail.setFilters(new InputFilter[]{noSpaceFilter});
        binding.etPW.setFilters(new InputFilter[]{noSpaceFilter});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // EditText 입력값 변화 이벤트 처리
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkSignupButton(); // 회원가입 버튼 활성화 여부 체크
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkSignupButton(); // 회원가입 버튼 활성화 여부 체크
        }
    };

    // 클릭 이벤트 처리
    @Override
    public void onClick(View v) {
        if (v == binding.cbAssent) {
            checkSignupButton(); // 이용약관 동의 체크 상태 확인
        } else if (v == binding.btnSignupDo) {
            handleSignup(); // 회원가입 처리
        }
    }

    // 회원가입 버튼 클릭 시 처리
    private void handleSignup() {
        // 유효성 확인
        if (!isIDValid) {
            showToast("이메일의 형식을 확인해주세요.");
        } else if (!isPWValid) {
            showToast("비밀번호는 8자 이상, 영문+숫자+특수문자를 포함해야 합니다.");
        } else if (!isAssentChecked) {
            showToast("이용약관과 개인정보 정책에 동의해주세요.");
        } else {
            // Firebase 회원가입 및 이메일 인증
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPW.getText().toString().trim();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                sendEmailVerification(currentUser, email, password);
                            }
                        } else {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            handleSignUpError(errorCode);
                        }
                    });
        }
    }

    // 이메일 인증 메일 보내기
    private void sendEmailVerification(FirebaseUser user, String email, String password) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 이메일 인증 화면으로 이동
                        Intent intent = new Intent(SignupActivity.this, EmailActivity.class);
                        intent.putExtra("userEmail", email);
                        intent.putExtra("userPassword", password);
                        startActivity(intent);
                        finish();
                    } else {
                        showToast("이메일 인증 메일 전송에 실패했습니다.");
                    }
                });
    }

    // Toast 메시지 출력 메서드
    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // 회원가입 오류코드에 따른 오류 메시지 처리
    private void handleSignUpError(String errorCode) {
        switch (errorCode) {
            case "ERROR_EMAIL_ALREADY_IN_USE":
                showToast("이미 가입된 이메일입니다.");
                break;
            default:
                showToast("회원가입 실패: " + errorCode);
                break;
        }
    }

    // 회원가입 버튼 활성화 여부 체크
    private void checkSignupButton() {
        String id = binding.etEmail.getText().toString();
        String pw = binding.etPW.getText().toString();
        isIDValid = Patterns.EMAIL_ADDRESS.matcher(id).matches();
        isPWValid = pw.length() >= 8 && pw.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
                && !pw.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
        isAssentChecked = binding.cbAssent.isChecked();

        if (isIDValid && isPWValid && isAssentChecked) {
            binding.btnSignupDo.setTextColor(Color.BLACK); // 모든 조건이 충족되면 버튼 활성화
        } else {
            binding.btnSignupDo.setTextColor(Color.rgb(189, 189, 189)); // 조건이 충족되지 않으면 버튼 비활성화
        }
    }
}

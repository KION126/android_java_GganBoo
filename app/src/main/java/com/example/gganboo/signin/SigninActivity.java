package com.example.gganboo.signin;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gganboo.GganBooActivity;
import com.example.gganboo.MainActivity;
import com.example.gganboo.R;
import com.example.gganboo.databinding.ActivitySigninBinding;
import com.example.gganboo.emailauth.EmailActivity;
import com.example.gganboo.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SigninActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivitySigninBinding binding;

    private FirebaseAuth mAuth;

    private static final String TAG = "SigninActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 로그인 확인 버튼
        binding.btnSigninDo.setOnClickListener(this);
        // 비밀번호 찾기
        binding.txtFindPW.setOnClickListener(this);

        binding.btnTest.setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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

    // 사용자가 입력한 email과 password가 Firebase에 등록되어 있는 정상적인 유저인지 확인하는 메서드
    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // 등록되어 있는 회원인지 확인
                        if (user != null) {
                            // 이메일 인증 확인
                            if (user.isEmailVerified()) {
                                fetchUserName(user.getUid());
                            } else {
                                showToast("이메일이 인증되지 않았습니다. 메일을 확인해 주세요.");
                                mAuth.signOut();    // 로그아웃 처리
                            }
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthException) {
                            String errorCode = ((FirebaseAuthException) e).getErrorCode();
                            handleSignInError(errorCode);
                        } else {
                            showToast("로그인 중 오류가 발생했습니다. 다시 시도해주세요.");
                        }
                    }
                });
    }

    // Realtime Database에서 사용자 이름 가져오기
    private void fetchUserName(String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    showToast(name + "님 반갑습니다.");
                    startActivity(new Intent(SigninActivity.this, GganBooActivity.class));
                    finish();
                } else {
                    Intent intent = new Intent(SigninActivity.this, ProfileActivity.class);
                    showToast("프로필 설정을 해주세요.");
                    String userEmail = binding.etEmail.getText().toString();
                    String userPassword = binding.etPW.getText().toString();
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("userPassword", userPassword);
                    finish();
                }
            } else {
                showToast("사용자 정보를 불러오는 중 오류가 발생했습니다.");
                Log.e(TAG, "사용자 정보 불러오기 실패", task.getException());
            }
        });
    }

    // 로그인 실패코드에 따른 실패원인 Toast 출력
    private void handleSignInError(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                showToast("잘못된 이메일 형식입니다.");
                break;
            case "ERROR_USER_NOT_FOUND":
                showToast("존재하지 않는 이메일입니다.");
                break;
            case "ERROR_WRONG_PASSWORD":
                showToast("잘못된 비밀번호입니다.");
                break;
            case "ERROR_USER_DISABLED":
                showToast("사용자 계정이 비활성화되었습니다.");
                break;
            case "ERROR_TOO_MANY_REQUESTS":
                showToast("요청이 너무 많습니다. 나중에 다시 시도해주세요.");
                break;
            case "ERROR_OPERATION_NOT_ALLOWED":
                showToast("이메일 및 비밀번호 로그인 방식이 비활성화되었습니다.");
                break;
            default:
                showToast("로그인 실패: " + errorCode);
                break;
        }
    }


    // 비밀번호 재설정 이메일 전송
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showToast("비밀번호 재설정 이메일을 보냈습니다. 이메일을 확인해 주세요.");
                    } else {
                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                        handleSignInError(errorCode);
                    }
                });
    }

    // Toast출력 메서드
    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // 클릭 이벤트
    @Override
    public void onClick(View v) {
        // 로그인 확인 버튼 클릭 이벤트
        if (v == binding.btnSigninDo) {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPW.getText().toString().trim();
            signInWithEmailAndPassword(email, password);
        }
        // 비밀번호 찾기 클릭 이벤트
        else if (v == binding.txtFindPW) {
            handlePasswordReset();
        }

        else if(v == binding.btnTest){
            signInWithEmailAndPassword("kg2001216@naver.com", "admin1!");
        }
    }

    // 비밀번호 재설정 처리
    private void handlePasswordReset() {
        String email = binding.etEmail.getText().toString().trim();
        
        // 이메일 형식이 아닐경우
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("이메일 형식이 아닙니다.\n 등록된 이메일을 입력해주세요.");
            return;
        }

        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "fetchSignInMethodsForEmail: 성공");
                        Log.d(TAG, "Sign-in methods: " + task.getResult().getSignInMethods());

                        // 해당 이메일주소가 회원목록에 없는 경우
                        if (task.getResult() != null && task.getResult().getSignInMethods() != null && task.getResult().getSignInMethods().isEmpty()) {
                            showToast("등록되지 않은 이메일입니다.\n 이메일을 확인해주세요.");
                        }
                        // 회원목록에 있으면 sendPasswordResetEmail메서드로 이동
                        else {
                            sendPasswordResetEmail(email);
                        }
                    } else {
                        Log.e(TAG, "fetchSignInMethodsForEmail: 실패", task.getException());
                        showToast("이메일 확인 중 오류가 발생했습니다.\n 다시 시도해주세요.");
                    }
                });
    }
}
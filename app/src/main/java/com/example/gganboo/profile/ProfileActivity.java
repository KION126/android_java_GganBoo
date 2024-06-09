package com.example.gganboo.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gganboo.GganBooActivity;
import com.example.gganboo.R;
import com.example.gganboo.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE_REQUEST = 1; // 이미지 선택 요청 코드

    private @NonNull ActivityProfileBinding binding; // View binding을 위한 변수
    private FirebaseAuth mAuth; // FirebaseAuth 인스턴스를 위한 변수
    private DatabaseReference myDB_Reference; // Firebase Database 참조 변수
    private StorageReference storageReference; // Firebase Storage 참조 변수
    private Uri imageUri; // 선택된 이미지의 URI

    private String userEmail; // 사용자 이메일
    private String userPassword; // 사용자 비밀번호

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Edge-to-edge 디스플레이 설정
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // View binding을 통해 레이아웃 설정

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        myDB_Reference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Intent로부터 이메일과 비밀번호 값을 받음
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");
        userPassword = intent.getStringExtra("userPassword");

        binding.btnProfileSubmit.setOnClickListener(this); // 프로필 제출 버튼 클릭 리스너 설정
        binding.ivProfileImage.setOnClickListener(this); // 프로필 이미지 클릭 리스너 설정

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnProfileSubmit) {
            handleProfileSubmit(); // 프로필 제출 처리
        } else if (v == binding.ivProfileImage) {
            openImagePicker(); // 이미지 선택기 열기
        }
    }

    // 이미지 선택기 열기
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // 선택된 이미지의 URI 가져오기
            binding.ivProfileImage.setImageURI(imageUri); // 프로필 이미지 뷰에 설정
        }
    }

    // 프로필 정보 제출 및 Firebase에 사용자 추가
    private void handleProfileSubmit() {
        String userName = binding.etName.getText().toString().trim();
        String userDescription = binding.etInt.getText().toString().trim();

        if (userName.isEmpty()) {
            showToast("이름을 입력해주세요.");
            return;
        }

        if (imageUri != null) {
            uploadProfileImage(userName, userDescription, imageUri); // 프로필 이미지 업로드
        } else {
            // 기본 이미지 사용
            String defaultImageUrl = "https://ibb.co/rtsnJnT";
            saveUserToDatabase(userName, userDescription, defaultImageUrl); // 기본 이미지 URL 사용
        }
    }

    // 프로필 이미지 업로드
    private void uploadProfileImage(String userName, String userDescription, Uri imageUri) {
        StorageReference fileReference = storageReference.child(UUID.randomUUID().toString());
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveUserToDatabase(userName, userDescription, imageUrl); // 업로드된 이미지 URL 사용
                }))
                .addOnFailureListener(e -> {
                    showToast("프로필 사진 업로드에 실패했습니다: " + e.getMessage());
                });
    }

    // Firebase Database에 사용자 정보 저장
    private void saveUserToDatabase(String userName, String userDescription, String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            UserProfile userProfile = new UserProfile(userId, userEmail, userName, userDescription, imageUrl);

            myDB_Reference.child(userId).setValue(userProfile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("회원가입이 완료되었습니다.");
                            Intent intent = new Intent(ProfileActivity.this, GganBooActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            showToast("회원가입에 실패했습니다: " + task.getException().getMessage());
                        }
                    });
        } else {
            showToast("유효한 사용자를 찾을 수 없습니다.");
        }
    }

    // Toast 출력 메서드
    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}

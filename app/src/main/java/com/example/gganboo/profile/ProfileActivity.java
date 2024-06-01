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

    private static final int PICK_IMAGE_REQUEST = 1;

    private @NonNull ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference myDB_Reference;
    private StorageReference storageReference;
    private Uri imageUri;

    private String userEmail;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        myDB_Reference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Intent로부터 이메일과 비밀번호 값을 받음
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("userEmail");
        userPassword = intent.getStringExtra("userPassword");

        binding.btnProfileSubmit.setOnClickListener(this);
        binding.ivProfileImage.setOnClickListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnProfileSubmit) {
            handleProfileSubmit();
        } else if (v == binding.ivProfileImage) {
            openImagePicker();
        }
    }

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
            imageUri = data.getData();
            binding.ivProfileImage.setImageURI(imageUri);
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
            uploadProfileImage(userName, userDescription, imageUri);
        } else {
            // 기본 이미지 사용
            String defaultImageUrl = "https://ibb.co/rtsnJnT";
            saveUserToDatabase(userName, userDescription, defaultImageUrl);
        }
    }

    private void uploadProfileImage(String userName, String userDescription, Uri imageUri) {
        StorageReference fileReference = storageReference.child(UUID.randomUUID().toString());
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveUserToDatabase(userName, userDescription, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    showToast("프로필 사진 업로드에 실패했습니다: " + e.getMessage());
                });
    }

    private void saveUserToDatabase(String userName, String userDescription, String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            UserProfile userProfile = new UserProfile(userId, userEmail, userName, userDescription, imageUrl);
            myDB_Reference.child(userId).setValue(userProfile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("회원가입이 완료되었습니다.");
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

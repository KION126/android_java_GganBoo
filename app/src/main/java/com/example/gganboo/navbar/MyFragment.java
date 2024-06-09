package com.example.gganboo.navbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.gganboo.MainActivity;
import com.example.gganboo.R;
import com.example.gganboo.databinding.FragmentMyBinding;
import com.example.gganboo.follow.FollowActivity;
import com.example.gganboo.profile.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1"; // 첫 번째 파라미터 이름
    private static final String ARG_PARAM2 = "param2"; // 두 번째 파라미터 이름
    private String mParam1; // 첫 번째 파라미터 값
    private String mParam2; // 두 번째 파라미터 값
    private @NonNull FragmentMyBinding binding; // View binding을 위한 변수

    public MyFragment() {
        // Required empty public constructor
    }

    /**
     * 프래그먼트 초기화 메서드
     *
     * @param param1 첫 번째 파라미터
     * @param param2 두 번째 파라미터
     * @return MyFragment의 새 인스턴스
     */
    public static MyFragment newInstance(String param1, String param2) {
        MyFragment fragment = new MyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1); // ARG_PARAM1 값을 가져옴
            mParam2 = getArguments().getString(ARG_PARAM2); // ARG_PARAM2 값을 가져옴
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyBinding.inflate(inflater, container, false); // View binding 초기화
        View view = binding.getRoot();

        // 클릭 리스너 설정
        binding.txtFollower.setOnClickListener(this);
        binding.txtFollowing.setOnClickListener(this);
        binding.btnLogout.setOnClickListener(this);

        loadData(); // 사용자 데이터 로드

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();  // 프래그먼트가 다시 활성화될 때 데이터를 로드
    }

    // 사용자 데이터 로드 메서드
    private void loadData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference("Users").child(userId);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            updateUI(userProfile); // UI 업데이트
                        } else {
                            Log.d("firebase", "UserProfile is null");
                        }
                    } else {
                        Log.d("firebase", "No data found");
                    }
                } else {
                    Log.d("firebase", "Error getting data", task.getException());
                }
            }
        });
    }

    // UI 업데이트 메서드
    private void updateUI(UserProfile userProfile) {
        binding.txtName.setText(userProfile.getName());
        binding.txtEmail.setText(userProfile.getEmail());

        // 팔로잉 및 팔로워 수에서 "null" 값을 제외하고 계산
        int followingCount = userProfile.getFollowing().contains("null") ? userProfile.getFollowing().size() - 1 : userProfile.getFollowing().size();
        int followersCount = userProfile.getFollowers().contains("null") ? userProfile.getFollowers().size() - 1 : userProfile.getFollowers().size();

        binding.txtFollowing.setText("팔로잉 " + followingCount);
        binding.txtFollower.setText("팔로워 " + followersCount);

        loadProfileImage(userProfile.getImageUrl()); // 프로필 이미지 로드
    }

    // 프로필 이미지 로드 메서드
    private void loadProfileImage(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_person_24)  // 로딩 중에 표시할 이미지
                .error(R.drawable.ic_person_24)        // 로드 실패 시 표시할 이미지
                .into(binding.imgProfileImage);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        if (v == binding.txtFollowing) {
            // 팔로잉 목록 클릭 시
            intent = new Intent(getActivity(), FollowActivity.class);
            intent.putExtra("tab", "following");
        } else if (v == binding.txtFollower) {
            // 팔로워 목록 클릭 시
            intent = new Intent(getActivity(), FollowActivity.class);
            intent.putExtra("tab", "followers");
        } else if (v == binding.btnLogout) {
            // 로그아웃 버튼 클릭 시
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(getActivity(), MainActivity.class);
            getActivity().finish(); // 현재 액티비티 종료
        }
        startActivity(intent); // 해당 액티비티로 이동
    }
}

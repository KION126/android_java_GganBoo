package com.example.gganboo.navbar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.gganboo.databinding.FragmentSearchBinding;
import com.example.gganboo.profile.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment"; // 로그 태그
    private FragmentSearchBinding binding; // View binding을 위한 변수
    private UserAdapter userAdapter; // 사용자 어댑터
    private List<UserProfile> userList; // 사용자 리스트
    private FirebaseAuth mAuth; // FirebaseAuth 인스턴스를 위한 변수
    private DatabaseReference userRef; // Firebase Database 참조 변수
    private List<String> followingList; // 팔로잉 리스트

    public SearchFragment() {
        // 기본 생성자 필요
    }

    // 새 인스턴스를 생성하는 메서드
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth 인스턴스 초기화
        userRef = FirebaseDatabase.getInstance().getReference("Users"); // Firebase Database 참조 초기화
        followingList = new ArrayList<>(); // 팔로잉 리스트 초기화
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false); // View binding 초기화
        View view = binding.getRoot();

        // RecyclerView 설정
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        String currentUserId = mAuth.getCurrentUser().getUid();
        userAdapter = new UserAdapter(userList, new UserAdapter.FollowUserCallback() {
            @Override
            public void followUser(UserProfile userToFollow) {
                handleFollowUser(userToFollow); // 팔로우 처리
            }

            @Override
            public void unfollowUser(UserProfile userToUnfollow) {
                handleUnfollowUser(userToUnfollow); // 언팔로우 처리
            }

            @Override
            public void removeFollower(UserProfile userToRemove) {
                // SearchFragment에서는 이 메서드를 사용하지 않으므로 구현하지 않습니다.
            }
        }, currentUserId, followingList, false);
        binding.recyclerView.setAdapter(userAdapter); // 어댑터 설정

        // 검색 입력 필드의 텍스트 변경 리스너
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    userList.clear(); // 검색어가 없으면 사용자 리스트 초기화
                    userAdapter.notifyDataSetChanged(); // 어댑터에 변경 알림
                } else {
                    searchUsers(s.toString()); // 사용자 검색
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchFollowingUsers(); // 팔로잉 사용자 가져오기
        return view;
    }

    // 팔로잉 사용자를 가져오는 메서드
    private void fetchFollowingUsers() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference currentUserRef = userRef.child(currentUserId).child("following");
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getValue(String.class);
                    followingList.add(userId);
                }
                userAdapter.notifyDataSetChanged(); // 팔로잉 목록을 받은 후 어댑터에 알림
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch following users", databaseError.toException());
            }
        });
    }

    // 사용자 검색 메서드
    private void searchUsers(String query) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null && userProfile.getName().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(userProfile); // 검색어와 일치하는 사용자를 리스트에 추가
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user profile", e);
                    }
                }
                userAdapter.notifyDataSetChanged(); // 어댑터에 변경 알림
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error", databaseError.toException());
            }
        });
    }

    // 팔로우 처리 메서드
    private void handleFollowUser(UserProfile userToFollow) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference currentUserRef = userRef.child(currentUserId);
        DatabaseReference userToFollowRef = userRef.child(userToFollow.getUserId());

        // 현재 사용자의 팔로잉 목록 업데이트
        currentUserRef.child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> followingList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        followingList.add(snapshot.getValue(String.class));
                    }
                }
                if (!followingList.contains(userToFollow.getUserId())) {
                    followingList.add(userToFollow.getUserId());
                    currentUserRef.child("following").setValue(followingList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // 팔로우 대상 사용자의 팔로워 목록 업데이트
        userToFollowRef.child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> followersList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        followersList.add(snapshot.getValue(String.class));
                    }
                }
                if (!followersList.contains(currentUserId)) {
                    followersList.add(currentUserId);
                    userToFollowRef.child("followers").setValue(followersList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        followingList.add(userToFollow.getUserId()); // 팔로잉 리스트에 추가
        userAdapter.notifyDataSetChanged(); // 어댑터에 변경 알림
    }

    // 언팔로우 처리 메서드
    private void handleUnfollowUser(UserProfile userToUnfollow) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference currentUserRef = userRef.child(currentUserId);
        DatabaseReference userToUnfollowRef = userRef.child(userToUnfollow.getUserId());

        // 현재 사용자의 팔로잉 목록 업데이트
        currentUserRef.child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> followingList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        followingList.add(snapshot.getValue(String.class));
                    }
                }
                if (followingList.contains(userToUnfollow.getUserId())) {
                    followingList.remove(userToUnfollow.getUserId());
                    currentUserRef.child("following").setValue(followingList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // 언팔로우 대상 사용자의 팔로워 목록 업데이트
        userToUnfollowRef.child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> followersList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        followersList.add(snapshot.getValue(String.class));
                    }
                }
                if (followersList.contains(currentUserId)) {
                    followersList.remove(currentUserId);
                    userToUnfollowRef.child("followers").setValue(followersList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        followingList.remove(userToUnfollow.getUserId()); // 팔로잉 리스트에서 제거
        userAdapter.notifyDataSetChanged(); // 어댑터에 변경 알림
    }
}

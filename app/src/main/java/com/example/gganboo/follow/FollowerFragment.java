package com.example.gganboo.follow;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.gganboo.R;
import com.example.gganboo.navbar.UserAdapter;
import com.example.gganboo.profile.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class FollowerFragment extends Fragment implements UserAdapter.FollowUserCallback {

    private RecyclerView recyclerView; // 팔로워 목록을 표시할 RecyclerView
    private UserAdapter userAdapter; // 사용자 어댑터
    private List<UserProfile> followerList; // 팔로워 목록
    private List<String> followingList; // 팔로잉 목록
    private String currentUserId; // 현재 사용자 ID
    private DatabaseReference userRef; // Firebase 사용자 참조

    public FollowerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView); // RecyclerView 초기화
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // 레이아웃 매니저 설정
        followerList = new ArrayList<>(); // 팔로워 목록 초기화
        followingList = new ArrayList<>(); // 팔로잉 목록 초기화
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID 가져오기
        userRef = FirebaseDatabase.getInstance().getReference("Users"); // Firebase 사용자 참조 초기화
        userAdapter = new UserAdapter(followerList, this, currentUserId, followingList, true); // 사용자 어댑터 초기화
        recyclerView.setAdapter(userAdapter); // RecyclerView에 어댑터 설정
        loadFollowers(); // 팔로워 목록 불러오기
        loadFollowing(); // 팔로잉 목록 불러오기
        return view;
    }

    // 팔로워 목록을 불러오는 메서드
    private void loadFollowers() {
        DatabaseReference followersRef = userRef.child(currentUserId).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followerList.clear(); // 기존 팔로워 목록을 비움
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getValue(String.class);
                    loadUserProfile(userId); // 각 팔로워의 프로필 불러오기
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // 팔로잉 목록을 불러오는 메서드
    private void loadFollowing() {
        DatabaseReference followingRef = userRef.child(currentUserId).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear(); // 기존 팔로잉 목록을 비움
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getValue(String.class);
                    followingList.add(userId); // 팔로잉 목록에 추가
                }
                userAdapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알림
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // 특정 사용자의 프로필을 불러오는 메서드
    private void loadUserProfile(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                if (userProfile != null) {
                    followerList.add(userProfile); // 팔로워 목록에 추가
                    userAdapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알림
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // 팔로우 기능 (사용되지 않음)
    @Override
    public void followUser(UserProfile userToFollow) {
        // 팔로우 기능 구현
    }

    // 언팔로우 기능 (사용되지 않음)
    @Override
    public void unfollowUser(UserProfile userToUnfollow) {
        // 언팔로우 기능 구현
    }

    // 팔로워 제거 기능
    @Override
    public void removeFollower(UserProfile userToRemove) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference currentUserRef = userRef.child(currentUserId);
        DatabaseReference followerRef = userRef.child(userToRemove.getUserId()).child("following");

        // 현재 사용자의 팔로워 목록에서 제거
        currentUserRef.child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> followersList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        followersList.add(snapshot.getValue(String.class));
                    }
                }
                if (followersList.contains(userToRemove.getUserId())) {
                    followersList.remove(userToRemove.getUserId()); // 팔로워 목록에서 제거
                    currentUserRef.child("followers").setValue(followersList); // Firebase에 업데이트
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // 팔로워의 팔로잉 목록에서 제거
        followerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> followingList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        followingList.add(snapshot.getValue(String.class));
                    }
                }
                if (followingList.contains(currentUserId)) {
                    followingList.remove(currentUserId); // 팔로잉 목록에서 제거
                    followerRef.setValue(followingList); // Firebase에 업데이트
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        followerList.remove(userToRemove); // 팔로워 목록에서 제거
        userAdapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알림
    }
}

package com.example.gganboo.follow;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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

public class FollowingFragment extends Fragment implements UserAdapter.FollowUserCallback {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserProfile> followingList;
    private List<String> followingUserIds;
    private String currentUserId;
    private DatabaseReference userRef;

    public FollowingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        followingList = new ArrayList<>();
        followingUserIds = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        userAdapter = new UserAdapter(followingList, this, currentUserId, followingUserIds, false);
        recyclerView.setAdapter(userAdapter);
        loadFollowing();
        return view;
    }

    private void loadFollowing() {
        DatabaseReference followingRef = userRef.child(currentUserId).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingUserIds.clear();
                followingList.clear(); // 기존 데이터를 비웁니다.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getValue(String.class);
                    followingUserIds.add(userId);
                }
                loadUserProfiles();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadUserProfiles() {
        followingList.clear(); // 중복을 방지하기 위해 데이터를 비웁니다.
        for (String userId : followingUserIds) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        followingList.add(userProfile);
                        userAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

    @Override
    public void followUser(UserProfile userToFollow) {
        // 팔로우 기능 구현
    }

    @Override
    public void unfollowUser(UserProfile userToUnfollow) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                refreshFragment(); // 언팔로우 후 프래그먼트를 새로고침합니다.
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
    }

    @Override
    public void removeFollower(UserProfile userToRemove) {
        // FollowingFragment에서는 이 메서드를 사용하지 않으므로 구현하지 않습니다.
    }

    private void refreshFragment() {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commitAllowingStateLoss();
    }
}

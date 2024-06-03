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

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserProfile> followerList;
    private List<String> followingList;
    private String currentUserId;
    private DatabaseReference userRef;

    public FollowerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        followerList = new ArrayList<>();
        followingList = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        userAdapter = new UserAdapter(followerList, this, currentUserId, followingList, true);
        recyclerView.setAdapter(userAdapter);
        loadFollowers();
        loadFollowing();
        return view;
    }

    private void loadFollowers() {
        DatabaseReference followersRef = userRef.child(currentUserId).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getValue(String.class);
                    loadUserProfile(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadFollowing() {
        DatabaseReference followingRef = userRef.child(currentUserId).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getValue(String.class);
                    followingList.add(userId);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadUserProfile(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                if (userProfile != null) {
                    followerList.add(userProfile);
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public void followUser(UserProfile userToFollow) {
        // 팔로우 기능 구현
    }

    @Override
    public void unfollowUser(UserProfile userToUnfollow) {
        // 언팔로우 기능 구현
    }

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
                    followersList.remove(userToRemove.getUserId());
                    currentUserRef.child("followers").setValue(followersList);
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
                    followingList.remove(currentUserId);
                    followerRef.setValue(followingList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        followerList.remove(userToRemove);
        userAdapter.notifyDataSetChanged();
    }
}

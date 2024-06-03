package com.example.gganboo.navbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gganboo.R;
import com.example.gganboo.profile.UserProfile;
import com.squareup.picasso.Picasso;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<UserProfile> userList;
    private FollowUserCallback followUserCallback;
    private String currentUserId;
    private List<String> followingList;
    private boolean isFollowerList;

    public UserAdapter(List<UserProfile> userList, FollowUserCallback followUserCallback, String currentUserId, List<String> followingList, boolean isFollowerList) {
        this.userList = userList;
        this.followUserCallback = followUserCallback;
        this.currentUserId = currentUserId;
        this.followingList = followingList;
        this.isFollowerList = isFollowerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = userList.get(position);

        // 현재 사용자와 일치하지 않는 경우에만 뷰를 설정합니다.
        if (!user.getUserId().equals(currentUserId)) {
            // 사용자 이름 및 설명을 설정합니다.
            holder.nameTextView.setText(user.getName());
            holder.descriptionTextView.setText(user.getDescription());

            // 프로필 이미지를 설정합니다.
            Picasso.get().load(user.getImageUrl()).into(holder.imageView);

            // 팔로워 목록인 경우
            if (isFollowerList) {
                // 팔로우 버튼을 숨기고 삭제 버튼을 표시합니다.
                holder.followButton.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setText("삭제");
                holder.deleteButton.setBackgroundResource(R.drawable.follow_delete_button);

                // 삭제 버튼 클릭 시 팔로워를 제거하는 콜백을 호출합니다.
                holder.deleteButton.setOnClickListener(v -> followUserCallback.removeFollower(user));
            }
            // 팔로잉 목록인 경우
            else {
                // 삭제 버튼을 숨기고 팔로우 버튼을 표시합니다.
                holder.deleteButton.setVisibility(View.GONE);
                holder.followButton.setVisibility(View.VISIBLE);

                // 팔로우 버튼의 상태를 설정합니다.
                if (followingList.contains(user.getUserId())) {
                    // 팔로잉 상태인 경우
                    holder.followButton.setEnabled(true);
                    holder.followButton.setText("팔로잉");
                    holder.followButton.setBackgroundResource(R.drawable.following_button);

                    // 팔로우 버튼 클릭 시 언팔로우 콜백을 호출합니다.
                    holder.followButton.setOnClickListener(v -> followUserCallback.unfollowUser(user));
                } else {
                    // 팔로우 상태가 아닌 경우
                    holder.followButton.setEnabled(true);
                    holder.followButton.setText("팔로우");
                    holder.followButton.setBackgroundResource(R.drawable.follow_button);

                    // 팔로우 버튼 클릭 시 팔로우 콜백을 호출합니다.
                    holder.followButton.setOnClickListener(v -> followUserCallback.followUser(user));
                }
            }
        } else {
            // 현재 사용자의 항목인 경우 뷰를 숨깁니다.
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView nameTextView;
        public TextView descriptionTextView;
        public Button followButton;
        public Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgProfileImage);
            nameTextView = itemView.findViewById(R.id.txtName);
            descriptionTextView = itemView.findViewById(R.id.txtDescription);
            followButton = itemView.findViewById(R.id.btnFollow);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface FollowUserCallback {
        void followUser(UserProfile userToFollow);
        void unfollowUser(UserProfile userToUnfollow);
        void removeFollower(UserProfile userToRemove);
    }
}

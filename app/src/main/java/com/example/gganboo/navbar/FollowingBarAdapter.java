package com.example.gganboo.navbar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.gganboo.R;
import com.example.gganboo.profile.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class FollowingBarAdapter extends RecyclerView.Adapter<FollowingBarAdapter.FollwingBarViewHolder> {

    // 아이템 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(String userId); // 다른 유저 클릭 시 호출
        void onCurrentUserClick(); // 현재 유저 클릭 시 호출
    }

    private List<Following> FollowingList; // 팔로잉 리스트
    private UserProfile user; // 사용자 프로필
    private Context context; // 컨텍스트
    private OnItemClickListener listener; // 아이템 클릭 리스너
    private String selectedUserId; // 선택된 유저 ID

    // 생성자
    public FollowingBarAdapter(List<Following> FollowingList, Context context, OnItemClickListener listener, String selectedUserId) {
        this.FollowingList = FollowingList;
        this.context = context;
        this.listener = listener;
        this.selectedUserId = selectedUserId;
    }

    @NonNull
    @Override
    public FollwingBarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_following, parent, false); // 뷰 생성
        return new FollwingBarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollwingBarViewHolder holder, int position) {
        Following f = FollowingList.get(position);
        findUser(f.getUid(), holder); // 유저 정보 로드

        // 배경 색상 설정
        if (f.getUid().equals(selectedUserId)) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.selected_item_background)); // 선택된 유저 배경 색상
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.default_item_background)); // 기본 배경 색상
        }

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (f.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    listener.onCurrentUserClick(); // 현재 유저 클릭 시
                } else {
                    listener.onItemClick(f.getUid()); // 다른 유저 클릭 시
                    selectedUserId = f.getUid(); // 선택된 유저 ID 업데이트
                    notifyDataSetChanged(); // UI 업데이트
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return FollowingList.size(); // 아이템 개수 반환
    }

    static class FollwingBarViewHolder extends RecyclerView.ViewHolder {
        TextView userName; // 유저 이름
        ImageView userImage; // 유저 이미지
        CardView cardView; // 카드뷰

        FollwingBarViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.FCardView);
            userName = itemView.findViewById(R.id.FTextView);
            userImage = itemView.findViewById(R.id.FIamgeView);
        }
    }

    // 유저 정보 로드 메서드
    public void findUser(String uid, FollwingBarViewHolder holder) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserProfile.class); // 유저 프로필 객체로 변환
                holder.userName.setText(user.getName()); // 유저 이름 설정
                Glide.with(context)
                        .load(user.getImageUrl())
                        .placeholder(R.drawable.ic_person_24)  // 로딩 중에 표시할 이미지
                        .error(R.drawable.ic_person_24)        // 로드 실패 시 표시할 이미지
                        .into(holder.userImage); // 유저 이미지 설정
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 처리
            }
        });
    }
}

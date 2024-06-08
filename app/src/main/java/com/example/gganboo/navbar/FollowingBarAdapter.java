package com.example.gganboo.navbar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gganboo.R;
import com.example.gganboo.profile.UserProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FollowingBarAdapter extends RecyclerView.Adapter<FollowingBarAdapter.FollwingBarViewHolder>{

    private List<Following> FollowingList;
    private UserProfile user;
    private Context context;

    public FollowingBarAdapter(List<Following> FollowingList, Context context) {
        this.FollowingList = FollowingList;
        this.context = context;
    }

    @NonNull
    @Override
    public FollwingBarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_following, parent, false);
        return new FollwingBarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollwingBarViewHolder holder, int position) {
        Following f = FollowingList.get(position);
        findUser(f.getUid(), holder);
        //holder.userName.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        return FollowingList.size();
    }

    static class FollwingBarViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView userImage;
        CardView cardView;
        FollwingBarViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.FCardView);
            userName = itemView.findViewById(R.id.FTextView);
            userImage = itemView.findViewById(R.id.FIamgeView);
        }
    }


    public void findUser(String uid, FollwingBarViewHolder holder){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(UserProfile.class);
                holder.userName.setText(user.getName());
                Glide.with(context)
                        .load(user.getImageUrl())
                        .placeholder(R.drawable.ic_person_24)  // 로딩 중에 표시할 이미지
                        .error(R.drawable.ic_person_24)        // 로드 실패 시 표시할 이미지
                        .into(holder.userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

}

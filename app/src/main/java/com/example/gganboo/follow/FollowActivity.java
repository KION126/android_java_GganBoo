package com.example.gganboo.follow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gganboo.R;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;

public class FollowActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        // 뒤로 가기 버튼 설정
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // TabLayout과 ViewPager 설정
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new FollowListAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("팔로잉");
            } else {
                tab.setText("팔로워");
            }
        }).attach();

        // 인텐트를 통해 전달받은 정보를 기반으로 적절한 탭을 선택하도록 설정
        Intent intent = getIntent();
        String selectedTab = intent.getStringExtra("tab");
        if (selectedTab != null && selectedTab.equals("followers")) {
            viewPager.setCurrentItem(1); // 팔로워 탭을 선택
        } else {
            viewPager.setCurrentItem(0); // 팔로잉 탭을 선택
        }
    }

    private static class FollowListAdapter extends FragmentStateAdapter {
        public FollowListAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new FollowingFragment();
            } else {
                return new FollowerFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}

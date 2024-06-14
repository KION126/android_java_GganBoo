package com.example.gganboo.navbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.example.gganboo.databinding.FragmentCalendarBinding;
import com.example.gganboo.profile.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CalendarFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1"; // 첫 번째 파라미터 이름
    private static final String ARG_PARAM2 = "param2"; // 두 번째 파라미터 이름

    private String mParam1; // 첫 번째 파라미터 값
    private String mParam2; // 두 번째 파라미터 값

    List<Todo> listTask = new ArrayList<>(); // 할 일 리스트
    List<Following> listFollowing = new ArrayList<>(); // 팔로잉 리스트
    private TodoAdapter toDoAdapter; // 할 일 어댑터
    private FollowingBarAdapter followingBarAdapter; // 팔로잉 바 어댑터
    private FragmentCalendarBinding binding; // View binding을 위한 변수
    private CalendarDay selectedDate; // 선택된 날짜
    private DatabaseReference mDatabase; // Firebase 데이터베이스 참조
    private String selectedUserId; // 선택된 유저의 ID 저장 변수

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * 프래그먼트 초기화 메서드
     *
     * @param param1 첫 번째 파라미터
     * @param param2 두 번째 파라미터
     * @return CalendarFragment의 새 인스턴스
     */
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false); // View binding 초기화
        binding.calendarView.addDecorator(new TodayDecorator(requireContext())); // 오늘 날짜 데코레이터 추가
        binding.calendarView.setSelectedDate(CalendarDay.today()); // 오늘 날짜 선택
        selectedDate = CalendarDay.today(); // 오늘 날짜로 초기화
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Firebase 데이터베이스 참조 초기화

        // 현재 사용자 ID를 선택된 사용자로 설정
        selectedUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchFollowings(); // 팔로잉 사용자 목록 불러오기
        binding.calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                selectedDate = date; // 선택된 날짜 업데이트
                if (selectedUserId != null) {
                    fetchTasks(selectedUserId, selectedDate); // 선택된 유저의 할 일 불러오기
                } else {
                    fetchTasks(FirebaseAuth.getInstance().getCurrentUser().getUid(), selectedDate); // 현재 사용자 ID 사용
                }
            }
        });

        followingBarAdapter = new FollowingBarAdapter(listFollowing, getContext(), new FollowingBarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String userId) {
                if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    // 자신의 프로필이 클릭된 경우 자신의 캘린더와 할 일을 다시 보여줌
                    selectedUserId = null;
                    updateUIForSelectedUser(true);
                    fetchTasks(FirebaseAuth.getInstance().getCurrentUser().getUid(), selectedDate);
                } else {
                    selectedUserId = userId; // 선택된 유저 ID 저장
                    loadUserCalendarAndTasks(userId);
                    updateUIForSelectedUser(false); // 다른 유저 프로필 선택 시 UI 업데이트
                }
            }

            @Override
            public void onCurrentUserClick() {
                // 자신의 프로필이 클릭된 경우 UI 초기화
                selectedUserId = null;
                updateUIForSelectedUser(true);
                fetchTasks(FirebaseAuth.getInstance().getCurrentUser().getUid(), selectedDate);
            }
        }, selectedUserId);

        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false)); // 팔로잉 바 레이아웃 설정
        binding.recyclerView2.setAdapter(followingBarAdapter); // 팔로잉 바 어댑터 설정

        toDoAdapter = new TodoAdapter(listTask, selectedDate, true); // 초기에는 수정 가능하도록 설정
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // 할 일 리스트 레이아웃 설정
        binding.recyclerView.setAdapter(toDoAdapter); // 할 일 리스트 어댑터 설정
        binding.addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToDoDialog(selectedDate); // 할 일 추가 다이얼로그 표시
            }
        });

        return binding.getRoot();
    }

    // 선택된 사용자에 따라 UI 업데이트 메서드
    private void updateUIForSelectedUser(boolean isCurrentUser) {
        if (isCurrentUser) {
            binding.addTaskButton.setVisibility(View.VISIBLE); // 할 일 추가 버튼 표시
            toDoAdapter.setEditable(true); // 할 일 리스트 수정 가능
        } else {
            binding.addTaskButton.setVisibility(View.GONE); // 할 일 추가 버튼 숨기기
            toDoAdapter.setEditable(false); // 할 일 리스트 수정 불가능
        }
        toDoAdapter.notifyDataSetChanged(); // 어댑터에 변경 사항 알림
    }

    // 유저의 캘린더와 할 일을 불러오는 메서드
    private void loadUserCalendarAndTasks(String userId) {
        fetchTasks(userId, selectedDate);
    }

    // 유저의 할 일을 불러오는 메서드
    private void fetchTasks(String userId, CalendarDay date) {
        mDatabase.child("Users").child(userId).child("tasks")
                .child(date.getYear() + "" + date.getMonth() + date.getDay()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listTask.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        String strTask = d.getKey();
                        String check = d.getValue(String.class);
                        Todo t = new Todo();
                        t.setTask(strTask);
                        t.setCheck(check);
                        listTask.add(t);
                    }
                    toDoAdapter.notifyDataSetChanged();
                } else {
                    toDoAdapter.notifyDataSetChanged();
                }
                if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    // 자신의 프로필이 클릭된 경우 자신의 캘린더와 할 일을 다시 보여줌
                    toDoAdapter = new TodoAdapter(listTask, selectedDate, true);
                } else {
                    toDoAdapter = new TodoAdapter(listTask, selectedDate, false);
                }

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.recyclerView.setAdapter(toDoAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 오류 처리
            }
        });
    }

    // 할 일 추가 다이얼로그 표시 메서드
    private View.OnClickListener showAddToDoDialog(CalendarDay date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일 할 일 추가");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String task = input.getText().toString().trim();
                if (!task.isEmpty()) {
                    String sDate = selectedDate.getYear() + "" + selectedDate.getMonth() + selectedDate.getDay();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference tasksRef = database.getReference("Users").child(userId).child("tasks");
                    tasksRef.child(sDate).child(task).setValue("false");
                } else {
                    Toast.makeText(requireContext(), "할 일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        return null;
    }

    // 팔로잉하는 사용자 목록을 불러오는 메서드
    private void fetchFollowings() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference currentUserRef = mDatabase.child("Users").child(currentUserId);

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listFollowing.clear();

                // 현재 사용자 프로필 추가
                UserProfile currentUserProfile = dataSnapshot.getValue(UserProfile.class);
                Following currentUserFollowing = new Following();
                currentUserFollowing.setUid(currentUserId);
                listFollowing.add(currentUserFollowing);

                // 팔로잉하는 사용자 목록 추가
                currentUserRef.child("following").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot followingSnapshot) {
                        if (followingSnapshot.exists()) {
                            for (DataSnapshot d : followingSnapshot.getChildren()) {
                                String strF = d.getValue(String.class);
                                if (strF.equals("null")) continue;
                                Following f = new Following();
                                f.setUid(strF);
                                listFollowing.add(f);
                            }
                        }
                        followingBarAdapter.notifyDataSetChanged();

                        // 자신의 프로필을 선택된 상태로 설정
                        updateUIForSelectedUser(true);
                        fetchTasks(FirebaseAuth.getInstance().getCurrentUser().getUid(), selectedDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 오류 처리
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 오류 처리
            }
        });
    }
}

package com.example.gganboo.navbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gganboo.databinding.FragmentCalendarBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    List<Todo> listTask = new ArrayList<>();
    List<Following> listFollowing = new ArrayList<>();
    private TodoAdapter toDoAdapter;
    private FollowingBarAdapter followingBarAdapter;
    private FragmentCalendarBinding binding;
    private CalendarDay selectedDate;
    private DatabaseReference mDatabase;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        binding.calendarView.addDecorator(new TodayDecorator(requireContext()));
        binding.calendarView.setSelectedDate(CalendarDay.today());
        selectedDate = CalendarDay.today();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        fetchFollowings();
        fetchTasks(selectedDate);
        binding.calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                selectedDate = date;
                fetchTasks(selectedDate);
            }
        });

        followingBarAdapter = new FollowingBarAdapter(listFollowing, getContext());
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));
        binding.recyclerView2.setAdapter(followingBarAdapter);

        toDoAdapter = new TodoAdapter(listTask, selectedDate);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(toDoAdapter);
        binding.addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddToDoDialog(selectedDate);
            }
        });


        return binding.getRoot();
    }

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
                    String sDate = selectedDate.getYear() +""+ selectedDate.getMonth() + selectedDate.getDay();
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

    private void fetchTasks(CalendarDay date) {
        mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tasks").child(date.getYear() + "" + date.getMonth() + date.getDay()).addValueEventListener(new ValueEventListener() {
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
                toDoAdapter = new TodoAdapter(listTask, selectedDate);
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.recyclerView.setAdapter(toDoAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

        private void fetchFollowings() {
            mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    listFollowing.clear();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            String strF = d.getValue(String.class);
                            if(strF.equals("null")) continue;
                            Following f = new Following();
                            f.setUid(strF);
                            listFollowing.add(f);
                        }
                        followingBarAdapter.notifyDataSetChanged();
                    } else {
                        followingBarAdapter.notifyDataSetChanged();
                    }
                    followingBarAdapter = new FollowingBarAdapter(listFollowing, getContext());
                    binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));
                    binding.recyclerView2.setAdapter(followingBarAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

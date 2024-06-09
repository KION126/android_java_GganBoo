package com.example.gganboo.navbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gganboo.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ToDoViewHolder> implements View.OnClickListener {

    private List<Todo> toDoList; // 할 일 리스트
    CalendarDay date; // 선택된 날짜
    private boolean isEditable; // 수정 가능 여부

    public TodoAdapter(List<Todo> toDoList, CalendarDay selectedDate, boolean isEditable) {
        this.toDoList = toDoList;
        this.date = selectedDate;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new ToDoViewHolder(view); // 뷰 홀더 생성
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        Todo task = toDoList.get(position);
        holder.taskTextView.setText(task.getTask());
        holder.taskButton.setOnClickListener(this);
        holder.taskCheckBox.setOnClickListener(this);
        holder.taskCheckBox.setEnabled(isEditable); // 수정 가능 여부에 따라 체크박스 활성화/비활성화 설정
        holder.taskCheckBox.setChecked(task.getCheck().equals("true")); // 체크박스 상태 설정
    }

    @Override
    public int getItemCount() {
        return toDoList.size(); // 할 일 리스트의 크기 반환
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable; // 수정 가능 여부 설정
    }

    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        TextView taskTextView; // 할 일 텍스트뷰
        CheckBox taskCheckBox; // 할 일 체크박스
        Button taskButton; // 할 일 버튼

        ToDoViewHolder(View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskButton = itemView.findViewById(R.id.taskButton);
        }
    }

    @Override
    public void onClick(View v) {
        int position = getItemCount() - 1;
        Todo todo = toDoList.get(position);

        // 할 일 삭제 버튼 클릭 시
        if (v.getId() == R.id.taskButton) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tasks")
                    .child(date.getYear() + "" + date.getMonth() + date.getDay()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                if (d.getKey().equals(todo.getTask())) {
                                    d.getRef().removeValue(); // 할 일 삭제
                                }
                            }
                        }
                    });
        }
        // 할 일 체크박스 클릭 시
        else if (v.getId() == R.id.taskCheckBox) {
            CheckBox c = v.findViewById(R.id.taskCheckBox);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tasks")
                    .child(date.getYear() + "" + date.getMonth() + date.getDay()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                if (d.getKey().equals(todo.getTask())) {
                                    d.getRef().setValue(c.isChecked() ? "true" : "false"); // 할 일 상태 업데이트
                                }
                            }
                        }
                    });
        }
    }
}

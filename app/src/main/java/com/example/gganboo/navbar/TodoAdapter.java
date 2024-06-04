package com.example.gganboo.navbar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gganboo.R;
import com.example.gganboo.profile.UserProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ToDoViewHolder> implements View.OnClickListener{

    private List<Todo> toDoList;
    CalendarDay date;

    public TodoAdapter(List<Todo> toDoList, CalendarDay selectedDate) {
        this.toDoList = toDoList;
        this.date = selectedDate;
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new ToDoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        Todo task = toDoList.get(position);
        holder.taskTextView.setText(task.getTask());
        holder.taskButton.setOnClickListener(this);
        holder.taskCheckBox.setOnClickListener(this);
        if(task.getCheck().equals("true"))
            holder.taskCheckBox.setChecked(true);
        else
            holder.taskCheckBox.setChecked(false);
    }


    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        TextView taskTextView;
        CheckBox taskCheckBox;
        Button taskButton;
        ToDoViewHolder(View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskButton = itemView.findViewById(R.id.taskButton);
        }
    }

    @Override
    public void onClick(View v) {
        int position = getItemCount();
        Todo todo = toDoList.get(position-1);
        if(v.getId() == R.id.taskButton) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tasks")
                    .child(date.getYear() + "" + date.getMonth() + date.getDay()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                if (d.getKey().equals(todo.getTask())) {
                                    d.getRef().removeValue();
                                }
                            }
                        }
                    });
        } else if(v.getId() == R.id.taskCheckBox) {
            CheckBox c = v.findViewById(R.id.taskCheckBox);
            if(c.isChecked()) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tasks")
                        .child(date.getYear() + "" + date.getMonth() + date.getDay()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    if (d.getKey().equals(todo.getTask())) {
                                        d.getRef().setValue("true");
                                    }
                                }
                            }
                        });
            } else {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("tasks")
                        .child(date.getYear() + "" + date.getMonth() + date.getDay()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    if (d.getKey().equals(todo.getTask())) {
                                        d.getRef().setValue("false");
                                    }
                                }
                            }
                        });
            }
        }
    }
}
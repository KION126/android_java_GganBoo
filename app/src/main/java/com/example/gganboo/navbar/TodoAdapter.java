package com.example.gganboo.navbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gganboo.R;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ToDoViewHolder> {

    private List<String> toDoList;

    public TodoAdapter(List<String> toDoList) {
        this.toDoList = toDoList;
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new ToDoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        TextView taskTextView;

        ToDoViewHolder(View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(com.example.gganboo.R.id.taskTextView);
        }
    }
}
package com.example.gganboo.navbar;

public class Todo {
    private String key;
    private String task;

    public Todo(String key, String task) {
        this.key = key;
        this.task = task;
    }

    public String getDate() {
        return key;
    }

    public String getTask() {
        return task;
    }
}

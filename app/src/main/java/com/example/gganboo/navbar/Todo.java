package com.example.gganboo.navbar;

// 할 일(Todo) 클래스
public class Todo {
    String task; // 할 일 내용
    String check; // 할 일 완료 여부 (true/false)

    // 할 일 완료 여부를 반환
    public String getCheck() {
        return check;
    }

    // 할 일 완료 여부를 설정
    public void setCheck(String check) {
        this.check = check;
    }

    // 할 일 내용을 반환
    public String getTask() {
        return task;
    }

    // 할 일 내용을 설정
    public void setTask(String task) {
        this.task = task;
    }
}

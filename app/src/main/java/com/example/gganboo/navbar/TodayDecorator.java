package com.example.gganboo.navbar;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.gganboo.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class TodayDecorator implements DayViewDecorator {

    private CalendarDay today; // 오늘 날짜를 저장하는 변수
    private final Drawable highlightDrawable; // 오늘 날짜를 강조하는 데 사용할 Drawable

    // 생성자
    public TodayDecorator(Context context) {
        today = CalendarDay.today(); // 오늘 날짜를 가져옴
        highlightDrawable = ContextCompat.getDrawable(context, R.drawable.today_circle); // 오늘 날짜를 강조하는 Drawable 설정
    }

    // 이 데코레이터가 특정 날짜를 장식할지 여부를 결정
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return today.equals(day); // 오늘 날짜와 동일한지 확인
    }

    // DayViewFacade를 사용하여 날짜를 장식
    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable); // 배경 Drawable 설정
    }
}

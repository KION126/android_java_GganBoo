package com.example.gganboo.navbar;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.example.gganboo.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class TodayDecorator implements DayViewDecorator {

    private CalendarDay today;
    private final Drawable highlightDrawable;

    public TodayDecorator(Context context) {
        today = CalendarDay.today();
        highlightDrawable = ContextCompat.getDrawable(context, R.drawable.today_circle);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return today.equals(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
    }
}
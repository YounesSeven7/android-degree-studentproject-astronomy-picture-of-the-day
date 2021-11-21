package com.barmej.apod.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.barmej.apod.R;
import com.barmej.apod.databinding.FragmentDatePickerBinding;

public class DatePickerFragment extends DialogFragment {
    FragmentDatePickerBinding binding;
    OnSelectDay onSelectDay;
    String selectDay;
    public DatePickerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onSelectDay = (OnSelectDay) context;

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_picker, container, false);
        binding = FragmentDatePickerBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                month++;
                selectDay = year + "-" + month + "-" + dayOfMonth;
                onSelectDay.onSelectDay(selectDay);
            }
        });
    }
    public interface  OnSelectDay {
        void onSelectDay(String date);
    }
}
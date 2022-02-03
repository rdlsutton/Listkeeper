package com.radlsuttonedmn.listkeeper;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

/**
 * Implements a time selection dialog for fields defined as time type
 */

class TimeListener {

    private final Context mContext;
    private final OnTouchEditText mEditTextField;
    private int mHour, mMinute;

    // Constructor with the data value edit text as a parameter
    TimeListener(OnTouchEditText editTextField, Context context) {

        mEditTextField = editTextField;
        mContext = context;

        // If the field is empty then send the current time to the time selector dialog
        if (mEditTextField.getText() == null || mEditTextField.getText().toString().trim().equals("")) {
            Calendar calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            setTime(mEditTextField.getText().toString());
        }
        setListener();
    }

    // Place a time listener on the time field
    private void setListener() {

        mEditTextField.setFocusable(false);
        mEditTextField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    TimePickerDialog.OnTimeSetListener timePickerListener
                                        = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hour, int minute) {
                            String AmPm;
                            if (hour == 0) {
                                hour = 12;
                                AmPm = "AM";
                            } else {
                                if (hour < 12) {
                                    AmPm = "AM";
                                } else {
                                    if (hour != 12) {
                                        hour -= 12;
                                    }
                                    AmPm = "PM";
                                }
                            }
                            String inTime = String.format(Locale.US, "%02d", hour) + ":" +
                                    String.format(Locale.US,"%02d", minute) + " " + AmPm;
                            mEditTextField.setText(inTime);
                            setTime(inTime);
                        }
                    };
                    TimePickerDialog timePicker = new TimePickerDialog(mContext, R.style.PickerStyle,
                            timePickerListener, mHour, mMinute, false);
                    timePicker.setTitle(mContext.getString(R.string.select_time));

                    timePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            mEditTextField.setText("");
                        }
                    });
                    timePicker.show();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
            return false;
            }
        });
    }

    // Convert a string time value to hours and minutes
    private void setTime(String time) {

        String[] timeTokens = time.trim().split(":");
        mHour = Integer.parseInt(timeTokens[0]);
        if (timeTokens[1].trim().endsWith("AM")) {
            if (mHour == 12) {
                mHour = 0;
            }
        }
        if (timeTokens[1].trim().endsWith("PM")) {
            if (mHour != 12) {
                mHour += 12;
            }
        }
        mMinute = Integer.parseInt(timeTokens[1].substring(0, 2));
    }
}

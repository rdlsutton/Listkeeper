package com.radlsuttonedmn.listkeeper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Locale;

/**
 * Implements a date selection dialog for fields defined as date type
 */

class DateListener {

    private final Context mContext;
    private final OnTouchEditText mEditTextField;
    private int mYear, mMonth, mDay;

    // Constructor with the date field as a parameter
    DateListener(OnTouchEditText editTextField, Context context) {

        mEditTextField = editTextField;
        mContext = context;

        // If the field is empty then send today's date to the date selector dialog
        if (mEditTextField.getText() == null || mEditTextField.getText().toString().trim().equals("")) {
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            setDate(mEditTextField.getText().toString());
        }
        setListener();
    }

    // Place a date listener on the date field
    private void setListener() {

        mEditTextField.setFocusable(false);
        mEditTextField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event){
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            String mm = String.format(Locale.US, "%02d", month + 1);
                            String dd = String.format(Locale.US, "%02d", day);
                            String yyyy = String.format(Locale.US, "%04d", year);
                            String date = mm + "/" + dd + "/" + yyyy;
                            mEditTextField.setText(date);
                            setDate(date);
                        }

                    };
                    DatePickerDialog datePicker = new DatePickerDialog(mContext, R.style.PickerStyle,
                            datePickerListener, mYear, mMonth, mDay);
                    datePicker.setTitle(mContext.getString(R.string.select_date));

                    datePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            mEditTextField.setText("");
                        }
                    });
                    datePicker.show();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return false;
            }

        });
    }

    // Convert a string date value to day, month and year
    private void setDate(String dateString) {

        String[] dateTokens = dateString.trim().split("/");
        mYear = Integer.parseInt(dateTokens[2]);
        mMonth = Integer.parseInt(dateTokens[0]) - 1;
        mDay = Integer.parseInt(dateTokens[1]);
    }
}

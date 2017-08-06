package com.android.mis.controllers.Attendance;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.mis.R;
import com.android.mis.models.Attendance.DefaulterStudentAttendanceItem;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.util.Log.VERBOSE;

/**
 * Created by Zeus on 25-07-2017.
 */

public class DefaulterStudentAttendanceItemAdapter extends ArrayAdapter<com.android.mis.models.Attendance.DefaulterStudentAttendanceItem> {

    public DefaulterStudentAttendanceItemAdapter(Activity context, ArrayList<com.android.mis.models.Attendance.DefaulterStudentAttendanceItem> defStuList) {

        super(context, 0, defStuList);  //(context for adapter, resource id for list view item, data list)

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        View listItemView = convertView; // View given for recycling

        if(listItemView == null){
            // Given view is empty. Need to inflate one on our own.
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.defaulter_student_list_item, parent, false);
        }

        // ---------------------------------------------------------
        TextView serialNumberTextView = (TextView) listItemView.findViewById(R.id.serial_number_text_view);

        TextView studentNameTextView = (TextView) listItemView.findViewById(R.id.student_name_text_view);
        TextView admissionNumberTextView = (TextView) listItemView.findViewById(R.id.admission_number_text_view);
        TextView totalAttendanceTextView = (TextView) listItemView.findViewById(R.id.total_attendance_text_view);

        TextView attendacePercentageTextView = (TextView) listItemView.findViewById(R.id.attendance_percentage_text_view);

        // ----------------------------------------------------------
        com.android.mis.models.Attendance.DefaulterStudentAttendanceItem currDefStu = getItem(position);

        // ----------------------------------------------------------
        serialNumberTextView.setText(Integer.toString(currDefStu.getSerialNumber()));
        Log.v("Serial", "Serial - " + currDefStu.getSerialNumber());

        studentNameTextView.setText(currDefStu.getStudentName());
        admissionNumberTextView.setText(currDefStu.getAdmissionNumber());
        totalAttendanceTextView.setText("Total Present: " + currDefStu.getTotalPresent());

        //DecimalFormat class can be used to set floating point numbers to a particular format, in the
        //form of a String
        DecimalFormat percentageFormat = new DecimalFormat("#.00");
        percentageFormat.setRoundingMode(RoundingMode.DOWN);
        attendacePercentageTextView.setText(percentageFormat.format( currDefStu.getPercentagePresent() ) + "%");

        // ----------------------------------------------------------
        return listItemView;
    }
}

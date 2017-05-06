package com.android.mis.javac.Attendance.GenerateAttendanceSheet;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.mis.R;

public class GenerateAttendanceSheetForCommFragment extends Fragment {

    public GenerateAttendanceSheetForCommFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_generate_attendance_sheet_for_comm, container, false);
        return rootView;
    }
}

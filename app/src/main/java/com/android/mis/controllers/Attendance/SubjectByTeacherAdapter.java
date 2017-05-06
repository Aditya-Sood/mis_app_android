package com.android.mis.controllers.Attendance;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.javac.Attendance.GenerateAttendanceSheet.ViewAttendanceSheet;
import com.android.mis.models.Attendance.SubjectByTeacher;
import com.android.mis.utils.Util;

import java.util.List;

/**
 * Created by rajat on 5/5/17.
 */

public class SubjectByTeacherAdapter extends RecyclerView.Adapter<SubjectByTeacherAdapter.MyViewHolder>{
    private List<SubjectByTeacher> subjectsList;
    private Context context;
    private Activity activity;
    private Bundle extras;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView course,session,branch;
        public TextView semester,subjectName;
        public Button show_attendance;
        public Layout layout;

        public MyViewHolder(View view) {
            super(view);
            session = (TextView)view.findViewById(R.id.session);
            branch = (TextView)view.findViewById(R.id.branch);
            course = (TextView) view.findViewById(R.id.course);
            semester = (TextView) view.findViewById(R.id.semester);
            subjectName = (TextView)view.findViewById(R.id.subject_name);
            show_attendance = (Button)view.findViewById(R.id.show_attendance);
        }
    }


    public SubjectByTeacherAdapter(List<SubjectByTeacher> subjectsList,Context context,Activity activity,Bundle extras) {
        this.subjectsList = subjectsList;
        this.activity = activity;
        this.context = context;
        this.extras = extras;
    }

    @Override
    public SubjectByTeacherAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subjects_by_teacher_item, parent, false);
        return new SubjectByTeacherAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SubjectByTeacherAdapter.MyViewHolder holder, int position) {
        final SubjectByTeacher member = subjectsList.get(position);
        holder.course.setText(member.getCourseName());
        holder.branch.setText(member.getBranchName());
        holder.subjectName.setText(member.getSubName());
        holder.semester.setText("Semester "+member.getSemester());
        String aggrId = member.getAggrId();

        holder.session.setText("["+aggrId.split("_")[2]+"-"+aggrId.split("_")[3]+"]");

        holder.show_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extra_info = new Bundle();
                extra_info.putParcelable("subject",member);
                extra_info.putString("session",extras.getString("session"));
                extra_info.putString("session_year",extras.getString("session_year"));
                Util.moveToActivity(activity, ViewAttendanceSheet.class,extra_info,false);
                //Toast.makeText(context,"Clicked",Toast.LENGTH_SHORT).show();
            }
        });

        if(member.getTag()%2 == 0)
            holder.itemView.setBackgroundResource(R.color.details_background1);
        else
            holder.itemView.setBackgroundResource(R.color.details_background2);
    }

    @Override
    public int getItemCount() {
        return subjectsList.size();
    }
}

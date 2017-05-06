package com.android.mis.javac.Attendance.GenerateAttendanceSheet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.controllers.Attendance.AttendanceSheetAdapter;
import com.android.mis.models.Attendance.Student;
import com.android.mis.models.Attendance.SubjectByTeacher;
import com.android.mis.utils.Callback;
import com.android.mis.utils.NetworkRequest;
import com.android.mis.utils.SessionManagement;
import com.android.mis.utils.Urls;
import com.android.mis.utils.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ViewAttendanceSheet extends AppCompatActivity implements Callback {
    private RecyclerView recyclerView;
    private ArrayList<Student> studentList;
    private View mProgressView;
    private View mErrorView;
    private Button refreshOnError;
    private Bundle extras,extra_info;
    private HashMap<String,Integer> absents;
    private HashMap<String,String> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance_sheet);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        extra_info = new Bundle();
        extras = getIntent().getExtras();
        absents = new HashMap<>();
        params = new HashMap<>();
        studentList = new ArrayList<>();

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mProgressView = findViewById(R.id.loader);
        mErrorView = findViewById(R.id.err);

        fetchDetails();
        refreshOnError = (Button)mErrorView.findViewById(R.id.refresh_button);
        refreshOnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDetails();
            }
        });
    }

    private void fetchDetails()
    {
        SessionManagement session = new SessionManagement(getApplicationContext());
        if(session.isLoggedIn())
        {
            params = session.getTokenDetails();
        }
        if(extras!=null)
        {
            SubjectByTeacher subject = extras.getParcelable("subject");
            params.put("session",extras.getString("session"));
            params.put("session_year",extras.getString("session_year"));
            params.put("course_name",subject.getCourseName());
            params.put("course_id",subject.getCourseId());
            params.put("branch_name",subject.getBranchName());
            params.put("branch_id",subject.getBranchId());
            params.put("sub_id",subject.getSubId());
            params.put("sub_name",subject.getSubName());
        }

        NetworkRequest nr = new NetworkRequest(ViewAttendanceSheet.this,mProgressView,mErrorView,this,"get", Urls.server_protocol,Urls.view_attendance_sheet_url,params,false,true,0);
        nr.setSnackbar_message(Urls.error_connection_message);
        nr.initiateRequest();
    }

    @Override
    public void performAction(String result, int tag) {
        try {
            Toast.makeText(getApplicationContext(),"aajaaaaaaaaaaaaaaa",Toast.LENGTH_LONG).show();
            JSONObject json = new JSONObject(result);
            if (json.getBoolean("success") == true) {
                JSONObject att_sheet = json.getJSONObject("att_sheet");
                JSONArray stu_array = att_sheet.getJSONArray("stu_name");
                JSONObject stu_absents = att_sheet.getJSONObject("admission");
                Iterator keysToCopyIterator = stu_absents.keys();
                while(keysToCopyIterator.hasNext()) {
                    String key = (String) keysToCopyIterator.next();
                    JSONObject temp = stu_absents.getJSONObject(key);
                    absents.put(temp.getString("admn_no"),temp.getInt("absents"));
                }
                extra_info.putInt("total_classes",att_sheet.getInt("total_number_of_classes"));
                extra_info.putString("session",att_sheet.getString("session"));
                extra_info.putString("session_year",att_sheet.getString("session_year"));
                extra_info.putString("map_id",att_sheet.getString("map_id"));
                extra_info.putString("sub_id",att_sheet.getString("sub_id"));
                extra_info.putString("session_id",att_sheet.getString("session_id"));
                extra_info.putInt("semester",att_sheet.getInt("semester"));
                for(int i=0;i<stu_array.length();i++){
                    JSONObject student = stu_array.getJSONObject(i);
                    studentList.add(new Student(student.getString("id"),student.getString("first_name"),student.getString("middle_name"),student.getString("last_name"),absents.get(student.getString("id")),i));
                }
                AttendanceSheetAdapter mAdapter = new AttendanceSheetAdapter(studentList,getApplicationContext(),ViewAttendanceSheet.this,extra_info);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mAdapter);
            } else {
                Util.viewSnackbar(findViewById(android.R.id.content), json.getString("err_msg"));
            }
        } catch (Exception e) {
            Log.d("Result",result);
            Log.e("Exception", e.toString());
            //     Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
            Util.viewSnackbar(findViewById(android.R.id.content), Urls.parsing_error_message);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, GenerateAttendanceSheetPreDetails.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}

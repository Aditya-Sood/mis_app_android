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

import com.android.mis.R;
import com.android.mis.controllers.Attendance.SubjectByTeacherAdapter;
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

public class SubjectsByTeacherActivity extends AppCompatActivity implements Callback{

    private RecyclerView recyclerView;
    private ArrayList<SubjectByTeacher> subjectsList;
    private View mProgressView;
    private View mErrorView;
    private Button refreshOnError;
    private Bundle extras;
    private HashMap<String,String> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects_by_teacher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        extras = getIntent().getExtras();
        params = new HashMap<>();
        subjectsList = new ArrayList<>();

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
            params.put("session",extras.getString("session"));
            params.put("sessionyear",extras.getString("session_year"));
        }

        NetworkRequest nr = new NetworkRequest(SubjectsByTeacherActivity.this,mProgressView,mErrorView,this,"get", Urls.server_protocol,Urls.subjects_mapped_url,params,false,true,0);
        nr.setSnackbar_message(Urls.error_connection_message);
        nr.initiateRequest();
    }

    @Override
    public void performAction(String result, int tag) {
        try {
            JSONObject json = new JSONObject(result);
            if (json.getBoolean("success") == true) {
                JSONArray subject_array = json.getJSONArray("subjects");
                for(int i=0;i<subject_array.length();i++)
                {
                    JSONObject subject = subject_array.getJSONObject(i);
                    subjectsList.add(new SubjectByTeacher(subject.getString("s_id"),subject.getString("sub_name"),subject.getString("sub_id"),subject.getInt("semester"),subject.getInt("group"),subject.getString("branch_name"),subject.getString("course_name"),subject.getString("branch_id"),subject.getString("course_id"),subject.getString("aggr_id"),i));
                }

                SubjectByTeacherAdapter mAdapter = new SubjectByTeacherAdapter(subjectsList,getApplicationContext(),SubjectsByTeacherActivity.this,extras);
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

package com.android.mis.javac.Attendance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.controllers.Attendance.DefaulterStudentAttendanceItemAdapter;
import com.android.mis.models.Attendance.DefaulterStudentAttendanceItem;
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
import java.util.Set;

/**
 * LOGS for possible future modifications:
 * 1) Complete database, with NO defaulters.
 */

public class ViewDefaulterStudentAttendance extends AppCompatActivity implements Callback {

    private Bundle extras;
    private HashMap<String, String> urlParamameters;
    private ArrayList<DefaulterStudentAttendanceItem> defStuArrayList;

    //private RecyclerView recyclerView;
    private View mProgressView;
    private View mErrorView;

    private Button refreshOnError;

    private String subjectName;
    private int totalClasses;
    private String dateLastClass;

    private ListView defStuListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defaulter_student_list);

        // ----------------- Basic Set Up -----------------
        mProgressView = findViewById(R.id.loader);
        mErrorView = findViewById(R.id.err);

        refreshOnError = (Button) mErrorView.findViewById(R.id.refresh_button);
        refreshOnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchDefaulterStudentDetails();
            }
        });

        defStuListView = (ListView) findViewById(R.id.defaulter_student_list_view);

        extras = getIntent().getExtras();
        urlParamameters = new HashMap<>();
        defStuArrayList = new ArrayList<DefaulterStudentAttendanceItem>();

        // ----------------- Fetch Data For Display -----------------
        fetchDefaulterStudentDetails();
    }

    private void fetchDefaulterStudentDetails() {

        SessionManagement currentSession = new SessionManagement(getApplicationContext());

        if(currentSession.isLoggedIn()) {

            urlParamameters = currentSession.getSessionDetails();
        }

        if(extras != null) {

            urlParamameters.put("session_year", extras.getString("session_year"));
            urlParamameters.put("session", extras.getString("session"));

            urlParamameters.put("course_id", extras.getString("course_id"));
            urlParamameters.put("course_name", extras.getString("course_name"));

            urlParamameters.put("branch_id", extras.getString("branch_id"));
            urlParamameters.put("branch_name", extras.getString("branch_name"));

            urlParamameters.put("sub_id", extras.getString("sub_id"));
            urlParamameters.put("sub_name", extras.getString("sub_name"));

            urlParamameters.put("semester", extras.getString("semester"));
        }

        NetworkRequest networkRequest1 = new NetworkRequest(ViewDefaulterStudentAttendance.this, mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.view_defaulter_student_list, urlParamameters, false, true, 0);
        networkRequest1.setSnackbar_message(Urls.error_connection_message);
        networkRequest1.initiateRequest();
    }

    @Override
    public void performAction(String result, int tag) {
        try {
            JSONObject responseJSONObject = new JSONObject(result);

            if(responseJSONObject.getBoolean("success") == true) {
                JSONObject defaulter_listJSONObject = responseJSONObject.getJSONObject("defaulter_list");

                totalClasses = Integer.parseInt(defaulter_listJSONObject.getString("total_number_of_classes"));

                JSONArray date_of_classJSONArray = defaulter_listJSONObject.getJSONArray("date_of_class");
                JSONObject lastClassJSONObject = date_of_classJSONArray.getJSONObject(totalClasses - 1);
                dateLastClass = lastClassJSONObject.getString("date"); // Had to pickup the object explicitly because of identical names.

                // -------------
                subjectName = extras.getString("sub_name");
                TextView subjectNameTextView = (TextView) findViewById(R.id.subject_name_text_view);
                subjectNameTextView.setText(subjectName);

                TextView totalClassesTextView = (TextView) findViewById(R.id.total_classes_text_view);
                totalClassesTextView.setText("Total Classes - " + totalClasses);

                TextView lastClassDateTextView = (TextView) findViewById(R.id.last_class_date_text_view);
                lastClassDateTextView.setText("Last Class - " + dateLastClass);

                // --------------
                JSONObject defaulterAdmissionJSONObject = defaulter_listJSONObject.getJSONObject("admission");
                JSONArray stu_nameJSONArray = defaulter_listJSONObject.getJSONArray("stu_name");

                Iterator<String> defSerialNumIterator = defaulterAdmissionJSONObject.keys();
                // All the objects had student serial numbers as key values.
                // That's why the {@ JSONObject.keys()} method is employed.

                // Get details of defaulter students, put 'em in arraylist, populate listview.
                while(defSerialNumIterator.hasNext()) {
                    int serialNumber = Integer.parseInt(defSerialNumIterator.next());

                    int totalAbsent = Integer.parseInt( ( defaulterAdmissionJSONObject.getJSONObject("" + serialNumber) ).getString("absents") );
                    String defStuAdmNo = ( defaulterAdmissionJSONObject.getJSONObject("" + serialNumber) ).getString("admn_no");

                    //Getting student name from JSONObject stu_name
                    JSONObject defStuJSONObject = stu_nameJSONArray.getJSONObject(serialNumber);
                    String defStuName = (defStuJSONObject.getString("first_name") + " " + defStuJSONObject.getString("last_name"));


                    defStuArrayList.add(new DefaulterStudentAttendanceItem(serialNumber, defStuName, defStuAdmNo, totalClasses - totalAbsent, totalClasses));
                }

                // Create and attach adapter to the list view
                DefaulterStudentAttendanceItemAdapter defStuAdapter = new DefaulterStudentAttendanceItemAdapter(ViewDefaulterStudentAttendance.this, defStuArrayList);
                defStuListView.setAdapter(defStuAdapter);

            }


        } catch (Exception e) {
            Log.e("Exception", e.toString());
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            Util.viewSnackbar(findViewById(android.R.id.content), Urls.parsing_error_message);
        }

    }
}
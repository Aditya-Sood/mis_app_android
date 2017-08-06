
package com.android.mis.javac.Attendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.utils.Callback;
import com.android.mis.utils.NetworkRequest;
import com.android.mis.utils.SessionManagement;
import com.android.mis.utils.Urls;
import com.android.mis.utils.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * LOGS for possible future modifications:
 * 1) Complete database - Fix semester & year fetch.
 */

public class AttendanceDefaulterPreDetails extends AppCompatActivity implements AdapterView.OnItemSelectedListener, Callback {
    private Spinner session_spinner, session_year_spinner, course_spinner, branch_spinner, subject_spinner, semester_spinner;
    private Button submit_button, reset_button;
    private ArrayList<String> session_year_spinner_list, session_spinner_list, course_spinner_list, branch_spinner_list, subject_spinner_list, semester_spinner_list;
    private ArrayList<String> course_id, branch_id, subject_id;
//    private int uniqueCourse = 0, uniqueBranch = 0, uniqueSubject = 0;
    private View mProgressView;
    private View mErrorView;
    private Button refreshOnError;
    private CardView card;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defaulter_attendance_pre_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressView = findViewById(R.id.loader);
        mErrorView = findViewById(R.id.err);

        refreshOnError = (Button) mErrorView.findViewById(R.id.refresh_button);
        refreshOnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDetails(0);
            }
        });
        card = (CardView) findViewById(R.id.card_view);

        session_spinner = (Spinner) findViewById(R.id.session_spinner);
        session_year_spinner = (Spinner) findViewById(R.id.session_year_spinner);
        course_spinner = (Spinner) findViewById(R.id.course_spinner);
        branch_spinner = (Spinner) findViewById(R.id.branch_spinner);
        subject_spinner = (Spinner) findViewById(R.id.subject_spinner);
        semester_spinner = (Spinner) findViewById(R.id.semester_spinner);

        submit_button = (Button) findViewById(R.id.submit_button);
        reset_button = (Button) findViewById(R.id.reset_button);

        session_year_spinner_list = new ArrayList<>();
        session_spinner_list = new ArrayList<>();
        course_spinner_list = new ArrayList<>();
        branch_spinner_list = new ArrayList<>();
        subject_spinner_list = new ArrayList<>();
        semester_spinner_list = new ArrayList<>();

        course_id = new ArrayList<>();
        branch_id = new ArrayList<>();
        subject_id = new ArrayList<>(); // This is the id assigned to the subject in the database - And NOT the id assigned by the Institute.

        session_spinner_list.add("Select");
        ArrayAdapter<String> sessionAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, session_spinner_list);
        session_spinner.setEnabled(false);
        session_spinner.setClickable(false);
        session_spinner.setAdapter(sessionAdapter);

        course_spinner_list.add("Select");
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, course_spinner_list);
        course_spinner.setEnabled(false);
        course_spinner.setClickable(false);
        course_spinner.setAdapter(courseAdapter);

        branch_spinner_list.add("Select");
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, branch_spinner_list);
        branch_spinner.setEnabled(false);
        branch_spinner.setClickable(false);
        branch_spinner.setAdapter(branchAdapter);

        subject_spinner_list.add("Select");
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, subject_spinner_list);
        subject_spinner.setEnabled(false);
        subject_spinner.setClickable(false);
        subject_spinner.setAdapter(subjectAdapter);

        semester_spinner_list.add("Select");
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, semester_spinner_list);
        semester_spinner.setEnabled(false);
        semester_spinner.setClickable(false);
        semester_spinner.setAdapter(semesterAdapter);

        //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

//000000000000STEP 0
        /*
        Populate "Session Year" spinner; and attach onClickListener to the same (using which fetchDetails(1) is called).
         */
        fetchDetails(0);


        session_spinner.setOnItemSelectedListener(this);
        session_year_spinner.setOnItemSelectedListener(this);
        course_spinner.setOnItemSelectedListener(this);
        branch_spinner.setOnItemSelectedListener(this);
        subject_spinner.setOnItemSelectedListener(this);
        semester_spinner.setOnItemSelectedListener(this);


        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent currActivityIntent = getIntent();
                finish(); // Ends current activity at the end of this method
                startActivity(currActivityIntent); // Starts new instance of the current activity
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNotSelected(session_spinner) || isNotSelected(session_year_spinner) || isNotSelected(semester_spinner)) {
                    Util.viewSnackbar(findViewById(android.R.id.content), Urls.empty_message);
                } else {
                    Bundle extras = new Bundle();
                    extras.putString("session_year", session_year_spinner.getSelectedItem().toString());
                    extras.putString("session", session_spinner.getSelectedItem().toString());

                    extras.putString("course_id", course_id.get(course_spinner.getSelectedItemPosition() - 1)); // Because of additional 'Select' item in the spinner.
                    extras.putString("course_name", course_spinner.getSelectedItem().toString());

                    extras.putString("branch_id", branch_id.get(branch_spinner.getSelectedItemPosition() - 1)); // Because of additional 'Select' item in the spinner.
                    extras.putString("branch_name", branch_spinner.getSelectedItem().toString());

                    extras.putString("sub_id", subject_id.get(subject_spinner.getSelectedItemPosition() - 1)); // Because of additional 'Select' item in the spinner.
                    extras.putString("sub_name", subject_spinner.getSelectedItem().toString());

                    extras.putString("semester", semester_spinner.getSelectedItem().toString());
                    Util.moveToActivity(AttendanceDefaulterPreDetails.this, ViewDefaulterStudentAttendance.class, extras);
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {

//5555555555STEP 5
            case R.id.session_year_spinner:
                if (session_year_spinner_list.get(position).toLowerCase().contentEquals("select"))
                    return;
                else {
                    Toast.makeText(getApplicationContext(), session_year_spinner_list.get(position), Toast.LENGTH_SHORT).show();

                    /*
                    *   Populating "Session" spinner
                    */
                    session_spinner_list.clear();
                    session_spinner_list.add("Select");
                    session_spinner_list.add("Monsoon");
                    session_spinner_list.add("Winter");
                    session_spinner_list.add("Summer");

                    ArrayAdapter<String> sessionAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, session_spinner_list);

                    session_spinner.setEnabled(true);
                    session_spinner.setClickable(true);
                    session_spinner.setAdapter(sessionAdapter);

                }
                break;

            case R.id.session_spinner:
                if (session_spinner_list.get(position).toLowerCase().contentEquals("select"))
                    return;
                else {
                    Toast.makeText(getApplicationContext(), session_spinner_list.get(position), Toast.LENGTH_SHORT).show();
                    fetchDetails(1); // Fill Course List Adapter
                }
                break;

            case R.id.course_spinner:
                if (course_spinner_list.get(position).toLowerCase().contentEquals("select"))
                    return;
                else {
                    Toast.makeText(getApplicationContext(), course_spinner_list.get(position), Toast.LENGTH_SHORT).show();
                    fetchDetails(2); // Fill Branch List Adapter
                }
                break;

            case R.id.branch_spinner:
                if (branch_spinner_list.get(position).toLowerCase().contentEquals("select"))
                    return;
                else {
                    Toast.makeText(getApplicationContext(), branch_spinner_list.get(position), Toast.LENGTH_SHORT).show();
                    fetchDetails(3); // Fill Subjects List Adapter
                }
                break;

            case R.id.subject_spinner:
                if (session_spinner_list.get(position).toLowerCase().contentEquals("select"))
                    return;
                else {
                    fetchDetails(4);
                }
                break;

            case R.id.semester_spinner:
                break;
        }


    }

    private void fetchDetails(int tag) {
        switch (tag) {
//1111111111STEP 1
            /*
            * Fetching for Session Year List*/
            case 0:
                card.setVisibility(View.GONE);
                session_year_spinner_list.clear();
                session_year_spinner_list.add("Select");
                HashMap param0 = new HashMap();
                SessionManagement session0 = new SessionManagement(getApplicationContext());
                if (session0.isLoggedIn()) {
                    param0 = session0.getSessionDetails();
                }

//22222222222222STEP 2
                NetworkRequest netReq0 = new NetworkRequest(AttendanceDefaulterPreDetails.this, mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.session_year_url, param0, false, true, 0);
                netReq0.setSnackbar_message(Urls.error_connection_message);
                netReq0.initiateRequest();
                break;

//6666666666STEP 6
            case 1:  // For Course List
                course_spinner_list.clear();
                course_id.clear();
                course_spinner_list.add("Select");

                ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, course_spinner_list);
                course_spinner.setEnabled(true);
                course_spinner.setClickable(true);
                course_spinner.setAdapter(courseAdapter);

                HashMap param1 = new HashMap();
                SessionManagement session1 = new SessionManagement(getApplicationContext());
                if (session1.isLoggedIn()) {
                    param1 = session1.getSessionDetails();
                }
                param1.put("sessionyear", session_year_spinner.getSelectedItem().toString());
                param1.put("session", session_spinner.getSelectedItem().toString());

//99999999999999STEP 9 Course List Adapter, and others
                NetworkRequest netReq1 = new NetworkRequest(AttendanceDefaulterPreDetails.this, mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.subject_mapped_url, param1, false, false, 1);
                netReq1.setSnackbar_message(Urls.error_connection_message);
                netReq1.initiateRequest();
                break;

            case 2: // For Branch List
                branch_spinner_list.clear();
                branch_id.clear();
                branch_spinner_list.add("Select");

                ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, branch_spinner_list);
                branch_spinner.setEnabled(true);
                branch_spinner.setClickable(true);
                branch_spinner.setAdapter(branchAdapter);

                HashMap param2 = new HashMap();
                SessionManagement session2 = new SessionManagement(getApplicationContext());
                if (session2.isLoggedIn()) {
                    param2 = session2.getSessionDetails();
                }
                param2.put("sessionyear", session_year_spinner.getSelectedItem().toString());
                param2.put("session", session_spinner.getSelectedItem().toString());

                NetworkRequest netReq2 = new NetworkRequest(AttendanceDefaulterPreDetails.this, mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.subject_mapped_url, param2, false, false, 2);
                netReq2.setSnackbar_message(Urls.error_connection_message);
                netReq2.initiateRequest();

                //77777777777777STEP 7 Semester List Adapter

                break;

            case 3: // For Subject List
                subject_spinner_list.clear();
                subject_id.clear();
                subject_spinner_list.add("Select");

                ArrayAdapter<String> subjectAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, branch_spinner_list);
                subject_spinner.setEnabled(true);
                subject_spinner.setClickable(true);
                subject_spinner.setAdapter(subjectAdapter);

                HashMap param3 = new HashMap();
                SessionManagement session3 = new SessionManagement(getApplicationContext());
                if (session3.isLoggedIn()) {
                    param3 = session3.getSessionDetails();
                }
                param3.put("sessionyear", session_year_spinner.getSelectedItem().toString());
                param3.put("session", session_spinner.getSelectedItem().toString());

                NetworkRequest netReq3 = new NetworkRequest(AttendanceDefaulterPreDetails.this, mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.subject_mapped_url, param3, false, false, 3);
                netReq3.setSnackbar_message(Urls.error_connection_message);
                netReq3.initiateRequest();
                break;


            case 4: // For Semester List
                semester_spinner_list.clear();
                semester_spinner_list.add("Select");

                ArrayAdapter<String> semesterAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, semester_spinner_list);
                semester_spinner.setEnabled(true);
                semester_spinner.setClickable(true);
                semester_spinner.setAdapter(semesterAdapter);

                HashMap param4 = new HashMap();
                SessionManagement sessionn = new SessionManagement(getApplicationContext());
                if (sessionn.isLoggedIn()) {
                    param4 = sessionn.getSessionDetails();
                }
                param4.put("sessionyear", session_year_spinner.getSelectedItem().toString());
                param4.put("session", session_spinner.getSelectedItem().toString());


                //77777777777777STEP 7 Semester List Adapter
                NetworkRequest netReq4 = new NetworkRequest(AttendanceDefaulterPreDetails.this, mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.semester_url, param4, false, false, 4);
                netReq4.setSnackbar_message(Urls.error_connection_message);
                netReq4.initiateRequest();
                break;

        }
    }

    @Override
    public void performAction(String result, int tag) {
        switch (tag) {
//3333333333STEP 3
            case 0: // Setting Session Year Adapter
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        card.setVisibility(View.VISIBLE);
                        JSONArray details = json.getJSONArray("session_year");
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject sy = details.getJSONObject(i);
                            session_year_spinner_list.add(sy.getString("session_year"));
                        }

                        //Modification for incomplete database
                        if(details.length() == 0)
                        {
                            session_year_spinner_list.add("2015-2016");
                            session_year_spinner_list.add("2016-2017");
                            session_year_spinner_list.add("2017-2018");
                        }


//4444444444444444444444STEP 4
//                        session_year_spinner.setOnItemSelectedListener(this);
                        ArrayAdapter<String> sessionYearAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, session_year_spinner_list);
                        session_year_spinner.setAdapter(sessionYearAdapter);


                    } else {
                        Util.viewSnackbar(findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;

//8888888888STEP 8

//10101010110STEP 10
            case 1: //For adding data to the Course List Adapter, and others
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        card.setVisibility(View.VISIBLE);
                        JSONArray teacherSubsArray = json.getJSONArray("subjects");

                        if(teacherSubsArray.length() > 0)
                        {
                            System.out.println("#####################################################Fetching data:");
                            JSONObject jsonObj = teacherSubsArray.getJSONObject(0);
                            course_id.add(jsonObj.getString("course_id"));
                            course_spinner_list.add(jsonObj.getString("course_name"));

                            // i = Keeps count of the index of current JSON array object (to be processed)
                            // j = For storing index of course_id ArrayList while looking for unique elements.
                            int i, j, flag;


                            for ( i = 1; i < teacherSubsArray.length(); i++) {
                                jsonObj = teacherSubsArray.getJSONObject(i);

                                //To verify that each course is displayed only once
                                for(j = 0, flag = 0; j < course_id.size(); j++) // uniqueCourse == course_id.size()
                                {
                                    if( !((jsonObj.getString("course_id")).equals(course_id.get(j))) )
                                        flag++;
                                }

                                if(flag == (j))
                                {
                                    course_id.add(jsonObj.getString("course_id"));
                                    course_spinner_list.add(jsonObj.getString("course_name"));
                                    System.out.println("Id - " + jsonObj.getString("course_id") + " Name: " + jsonObj.getString("course_name"));
//                                    uniqueCourse++;
                                }
                            }
                            ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, course_spinner_list);
                            course_spinner.setAdapter(courseAdapter);


                        }

                    } else {
                        Util.viewSnackbar(findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;

            case 2 : //For adding data to the Branch List Adapter, and others
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        card.setVisibility(View.VISIBLE);
                        JSONArray teacherSubsArray = json.getJSONArray("subjects");


                        if(teacherSubsArray.length() > 0)
                        {
                            System.out.println("#####################################################Fetching data:");
                            JSONObject jsonObj = teacherSubsArray.getJSONObject(0);

                            // i = Keeps count of the index of current JSON array object (to be processed)
                            // j = For storing index of course_id ArrayList while looking for unique elements.
                            int i, j, flag;
                            String selectedCourse, currObjectCourse;
                            selectedCourse = course_spinner.getSelectedItem().toString();

                            for (i = 0; (i < teacherSubsArray.length()); i++) { //

                                currObjectCourse = (teacherSubsArray.getJSONObject(i)).getString("course_name");

                                if((selectedCourse).equals(currObjectCourse)) {

                                    jsonObj = teacherSubsArray.getJSONObject(i);

                                    if(branch_id.size() != 0)
                                    {
                                        for(j = 0, flag = 0; j < branch_id.size(); j++)
                                        {
                                            if( !( (jsonObj.getString("branch_id")).equals(branch_id.get(j)) ) )
                                                flag++;
                                        }

                                        if(flag == (j))
                                        {
                                            branch_id.add(jsonObj.getString("branch_id"));
                                            branch_spinner_list.add(jsonObj.getString("branch_name"));
                                            System.out.println("Id - " + jsonObj.getString("branch_id") + " Name: " + jsonObj.getString("branch_name"));
                                        }
                                    }
                                    else
                                    {
                                        branch_id.add(jsonObj.getString("branch_id"));
                                        branch_spinner_list.add(jsonObj.getString("branch_name"));
                                    }
                                }
                                ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, branch_spinner_list);
                                branch_spinner.setAdapter(branchAdapter);

                            }

                        }

                    } else {
                        Util.viewSnackbar(findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;


            case 3: // For adding data to the Subject List Adapter
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        card.setVisibility(View.VISIBLE);
                        JSONArray teacherSubsArray = json.getJSONArray("subjects");

                        if(teacherSubsArray.length() > 0)
                        {
                            System.out.println("#####################################################Fetching data:");
                            JSONObject jsonObj = teacherSubsArray.getJSONObject(0);

                            // i = Keeps count of the index of current JSON array object (to be processed)
                            // j = For storing index of course_id ArrayList while looking for unique elements.
                            int i, j, flag;
                            String selectedCourse, currObjectCourse, selectedBranch, currObjectBranch;
                            selectedCourse = course_spinner.getSelectedItem().toString();
                            selectedBranch = branch_spinner.getSelectedItem().toString();

                            for (i = 0; (i < teacherSubsArray.length()); i++)
                            {
                                currObjectCourse = (teacherSubsArray.getJSONObject(i)).getString("course_name");
                                currObjectBranch = (teacherSubsArray.getJSONObject(i)).getString("branch_name");

                                if(((selectedCourse).equals(currObjectCourse)) && ((selectedBranch).equals(currObjectBranch))) {

                                    jsonObj = teacherSubsArray.getJSONObject(i);

                                    if(subject_id.size() != 0)
                                    {
                                        for(j = 0, flag = 0; j < subject_id.size(); j++)
                                        {
                                            if( !( (jsonObj.getString("sub_id")).equals(subject_id.get(j)) ) )
                                                flag++;
                                        }

                                        if(flag == (j))
                                        {
                                            subject_id.add(jsonObj.getString("sub_id"));
                                            subject_spinner_list.add(jsonObj.getString("sub_name"));
                                            System.out.println("Id - " + jsonObj.getString("sub_id") + " Name: " + jsonObj.getString("sub_name"));
                                        }
                                    }
                                    else
                                    {
                                        subject_id.add(jsonObj.getString("sub_id"));
                                        subject_spinner_list.add(jsonObj.getString("sub_name"));
                                    }
                                }

                            }

                            ArrayAdapter<String> subjectAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, subject_spinner_list);
                            subject_spinner.setAdapter(subjectAdapter);

                        }

                    } else {
                        Util.viewSnackbar(findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;

            case 4 : // For adding data to the Semester List Adapter
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                        JSONArray details = json.getJSONArray("semester");
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject sy = details.getJSONObject(i);
                            semester_spinner_list.add(sy.getString("semster"));
                        }

                        if(details.length() == 0)
                        {
                            if( (session_spinner_list.get(session_spinner.getSelectedItemPosition())).equals("Monsoon") )
                            {
                                semester_spinner_list.add("1");
                                semester_spinner_list.add("3");
                                semester_spinner_list.add("5");
                                semester_spinner_list.add("7");
                                semester_spinner_list.add("9");
                            }
                            else if( (session_spinner_list.get(session_spinner.getSelectedItemPosition())).equals("Winter") )
                            {
                                semester_spinner_list.add("2");
                                semester_spinner_list.add("4");
                                semester_spinner_list.add("6");
                                semester_spinner_list.add("8");
                                semester_spinner_list.add("10");
                            }
                        }

                        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, semester_spinner_list);
                        semester_spinner.setAdapter(semesterAdapter);
                    } else {
                        Util.viewSnackbar(findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(findViewById(android.R.id.content), Urls.parsing_error_message);
                }

                break;

        }
    }

    private boolean isNotSelected(Spinner spinner) {
        if (spinner.getSelectedItem().toString().toLowerCase().contentEquals("select"))
            return true;
        return false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

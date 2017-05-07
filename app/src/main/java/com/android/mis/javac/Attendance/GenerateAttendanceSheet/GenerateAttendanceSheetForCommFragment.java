package com.android.mis.javac.Attendance.GenerateAttendanceSheet;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.controllers.Attendance.SubjectSpinnerAdapter;
import com.android.mis.controllers.CourseStructure.SpinnerAdapter;
import com.android.mis.models.Attendance.Subject;
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

public class GenerateAttendanceSheetForCommFragment extends Fragment implements Callback,AdapterView.OnItemSelectedListener{

    private Spinner session_spinner, session_year_spinner ,section_spinner,subject_spinner;
    private Button submit_button;
    private ArrayList<String> session_year_list, session_list,section_list;
    private ArrayList<Subject> subject_list;
    private View mProgressView;
    private View mErrorView;
    private Button refreshOnError;
    private CardView card;
    private FrameLayout layout;

    public GenerateAttendanceSheetForCommFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_generate_attendance_sheet_for_comm, container, false);
        mProgressView = rootView.findViewById(R.id.loader);
        mErrorView = rootView.findViewById(R.id.err);

        refreshOnError = (Button) mErrorView.findViewById(R.id.refresh_button);
        refreshOnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDetails(0);
            }
        });
        card = (CardView) rootView.findViewById(R.id.card_view);
        layout = (FrameLayout)rootView.findViewById(R.id.fragment_att_predetails_common);

        session_spinner = (Spinner)rootView.findViewById(R.id.session_spinner);
        session_year_spinner = (Spinner)rootView.findViewById(R.id.session_year_spinner);
        section_spinner = (Spinner)rootView.findViewById(R.id.section_spinner);
        subject_spinner = (Spinner)rootView.findViewById(R.id.subject_spinner);

        submit_button = (Button)rootView.findViewById(R.id.submit_button);
        session_year_list = new ArrayList<>();
        session_list = new ArrayList<>();
        section_list = new ArrayList<>();
        subject_list = new ArrayList<>();


        resetSectionSpinner();
        resetSessionSpinner();
        resetSubjectSpinner();
        fetchDetails(0);



        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNotSelected(section_spinner) || isNotSelected(session_spinner) || isNotSelected(session_year_spinner)) {
                   Toast.makeText(getActivity().getApplicationContext(),"Please Fill all the fields",Toast.LENGTH_SHORT).show();
                } else {
                    Bundle extras = new Bundle();
                    extras.putString("session_year", session_year_spinner.getSelectedItem().toString());
                    extras.putString("session", session_spinner.getSelectedItem().toString());
                    extras.putString("section", section_spinner.getSelectedItem().toString());
                    Subject sub = subject_list.get(subject_spinner.getSelectedItemPosition());
                    SubjectByTeacher temp = new SubjectByTeacher(sub.getSubId(),sub.getName(),sub.getnId(),sub.getSemester(),1,"comm","Bachelor of Technology","comm","comm","comm",0);
                    extras.putParcelable("subject",temp);
                    Util.moveToActivity(getActivity(), ViewAttendanceSheet.class,extras,false);
                }
            }
        });

        return rootView;
    }

    private void resetSessionSpinner(){
        session_list.clear();
        session_list.add("Select");
        session_list.add("Monsoon");
        session_list.add("Winter");
        session_list.add("Summer");
        session_spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> sessionAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_spinner_item, session_list);
        session_spinner.setAdapter(sessionAdapter);
    }
    private void resetSubjectSpinner(){
        subject_list.clear();
        subject_list.add(new Subject("","Select","nId",1));
        SubjectSpinnerAdapter dataAdapter = new SubjectSpinnerAdapter(getActivity(),R.layout.simple_spinner_item,subject_list);
        subject_spinner.setAdapter(dataAdapter);
    }
    private void resetSectionSpinner(){
        section_list.clear();
        section_list.add("Select");
        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_spinner_item, section_list);
        section_spinner.setAdapter(sectionAdapter);
    }

    private boolean isNotSelected(Spinner spinner) {
        if (spinner.getSelectedItem().toString().toLowerCase().contentEquals("select"))
            return true;
        return false;
    }

    private void fetchDetails(int tag) {
        HashMap params = new HashMap();
        SessionManagement session = new SessionManagement(getActivity().getApplicationContext());
        if (session.isLoggedIn()) {
            params = session.getTokenDetails();
        }
        switch (tag) {
            case 0:
                card.setVisibility(View.GONE);
                session_year_list.clear();
                session_year_list.add("Select");
                NetworkRequest nr = new NetworkRequest(getActivity(), mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.session_year_url, params,true, true, 0);
                nr.setSnackbar_message(Urls.error_connection_message);
                nr.initiateRequest();
                break;
            case 1:
                subject_list.clear();
                subject_list.add(new Subject("","Select","nId",1));
                params.put("session",session_spinner.getSelectedItem().toString());
                params.put("session_year",session_year_spinner.getSelectedItem().toString());
                NetworkRequest nr1 = new NetworkRequest(getActivity(), mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.subjects_comm_url, params,true, true, 1);
                nr1.setSnackbar_message(Urls.error_connection_message);
                nr1.initiateRequest();
                break;
            case 2:
                section_list.clear();
                section_list.add("Select");
                params.put("session",session_spinner.getSelectedItem().toString());
                params.put("session_year",session_year_spinner.getSelectedItem().toString());
                params.put("sub_id",subject_list.get(subject_spinner.getSelectedItemPosition()).getnId());
                NetworkRequest nr2 = new NetworkRequest(getActivity(), mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.section_comm_url, params,true, true, 2);
                nr2.setSnackbar_message(Urls.error_connection_message);
                nr2.initiateRequest();
                break;

        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {
            case R.id.session_year_spinner:
                if (session_year_list.get(position).toLowerCase().contentEquals("select"))
                    return;
                else{
                    resetSessionSpinner();
                    resetSectionSpinner();
                    resetSubjectSpinner();
                }
                break;

            case R.id.session_spinner:
                if (session_list.get(position).toLowerCase().contentEquals("select"))
                    return;
                else {
                    resetSubjectSpinner();
                    resetSectionSpinner();
                    fetchDetails(1);
                }
                break;

            case R.id.subject_spinner:
                if(subject_list.get(position).getName().toLowerCase().contentEquals("select"))
                    return;
                else{
                    resetSectionSpinner();
                    fetchDetails(2);
                }
                break;

            case R.id.section_spinner:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void performAction(String result, int tag) {
        switch (tag) {
            case 0:
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        card.setVisibility(View.VISIBLE);
                        JSONArray details = json.getJSONArray("session_year");
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject sy = details.getJSONObject(i);
                            session_year_list.add(sy.getString("session_year"));
                            Toast.makeText(getActivity().getApplicationContext(),sy.getString("session_year"),Toast.LENGTH_LONG).show();
                        }
                        session_year_spinner.setOnItemSelectedListener(this);
                        ArrayAdapter<String> sessionYearAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_spinner_item, session_year_list);
                        session_year_spinner.setAdapter(sessionYearAdapter);
                    } else {
                        Util.viewSnackbar(getActivity().findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getActivity().getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(getActivity().findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;
            case 1:
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        JSONArray details = json.getJSONArray("subjects");
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject object = details.getJSONObject(i);
                            subject_list.add(new Subject(object.getString("s_id"),object.getString("name"),object.getString("n_id"),object.getInt("semester")));
                        }
                        subject_spinner.setOnItemSelectedListener(this);
                        SubjectSpinnerAdapter dataAdapter = new SubjectSpinnerAdapter(getActivity(),R.layout.simple_spinner_item,subject_list);
                        subject_spinner.setAdapter(dataAdapter);
                    } else {
                        Util.viewSnackbar(getActivity().findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getActivity().getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(getView().findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;
            case 2:
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getBoolean("success") == true) {
                        JSONArray details = json.getJSONArray("sections");
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject object = details.getJSONObject(i);
                            section_list.add(object.getString("section"));
                        }
                        section_spinner.setOnItemSelectedListener(this);
                        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_spinner_item, section_list);
                        section_spinner.setAdapter(sectionAdapter);
                    } else {
                        Util.viewSnackbar(getActivity().findViewById(android.R.id.content), json.getString("err_msg"));
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    Toast.makeText(getActivity().getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    Util.viewSnackbar(getView().findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;
        }
    }
}

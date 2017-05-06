package com.android.mis.javac.Attendance.GenerateAttendanceSheet;

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


public class GenerateAttendanceSheetForRegularStudentsFragment extends Fragment implements Callback,AdapterView.OnItemSelectedListener{
    private Spinner session_spinner, session_year_spinner;
    private Button submit_button;
    private ArrayList<String> session_year_list, session_list;
    private View mProgressView;
    private View mErrorView;
    private Button refreshOnError;
    private CardView card;


    public GenerateAttendanceSheetForRegularStudentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_generate_attendance_sheet_for_regular_students, container, false);
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

        session_spinner = (Spinner)rootView.findViewById(R.id.session_spinner);
        session_year_spinner = (Spinner)rootView.findViewById(R.id.session_year_spinner);

        submit_button = (Button)rootView.findViewById(R.id.submit_button);
        session_year_list = new ArrayList<>();
        session_list = new ArrayList<>();

        session_list.add("Select");
        session_list.add("Monsoon");
        session_list.add("Winter");
        session_list.add("Summer");

        fetchDetails(0);


        session_spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> sessionAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.simple_spinner_item, session_list);

        session_spinner.setAdapter(sessionAdapter);


        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNotSelected(session_spinner) || isNotSelected(session_year_spinner)) {
                    Util.viewSnackbar(getView().findViewById(android.R.id.content), Urls.empty_message);
                } else {
                    Bundle extras = new Bundle();
                    extras.putString("session_year", session_year_spinner.getSelectedItem().toString());
                    extras.putString("session", session_spinner.getSelectedItem().toString());
                    Util.moveToActivity(getActivity(),SubjectsByTeacherActivity.class, extras,false);
                }
            }
        });

        return rootView;
    }

    private boolean isNotSelected(Spinner spinner) {
        if (spinner.getSelectedItem().toString().toLowerCase().contentEquals("select"))
            return true;
        return false;
    }

    private void fetchDetails(int tag) {
        switch (tag) {
            case 0:
                card.setVisibility(View.GONE);
                session_year_list.clear();
                session_year_list.add("Select");
                HashMap params = new HashMap();
                SessionManagement session = new SessionManagement(getActivity().getApplicationContext());
                if (session.isLoggedIn()) {
                    params = session.getTokenDetails();
                }
                NetworkRequest nr = new NetworkRequest(getActivity(), mProgressView, mErrorView, this, "get", Urls.server_protocol, Urls.session_year_url, params,true, true, 0);
                nr.setSnackbar_message(Urls.error_connection_message);
                nr.initiateRequest();
                break;
        }
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
                    Util.viewSnackbar(getView().findViewById(android.R.id.content), Urls.parsing_error_message);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

package com.android.mis.javac.Attendance.GenerateAttendanceSheet;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import com.android.mis.R;
import com.android.mis.controllers.ViewPagerAdapter;
import com.android.mis.javac.Home.HomeActivity;
import com.android.mis.javac.MoveToActivityFragment;
import com.android.mis.javac.ViewDetails.AddressDetailsFragment;
import com.android.mis.javac.ViewDetails.AdmissionDetailsFragment;
import com.android.mis.javac.ViewDetails.BankDetailsFragment;
import com.android.mis.javac.ViewDetails.EducationDetailsFragment;
import com.android.mis.javac.ViewDetails.EmpFamilyDetailsFragment;
import com.android.mis.javac.ViewDetails.FeeDetailsFragment;
import com.android.mis.javac.ViewDetails.PersonalDetailsFragment;
import com.android.mis.javac.ViewDetails.StayDetailsFragment;
import com.android.mis.javac.ViewDetails.StuFamilyDetailsFragment;
import com.android.mis.javac.ViewDetails.ViewDetails;
import com.android.mis.utils.Callback;
import com.android.mis.utils.NetworkRequest;
import com.android.mis.utils.SessionManagement;
import com.android.mis.utils.Urls;
import com.android.mis.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GenerateAttendanceSheetPreDetails extends AppCompatActivity implements Callback{

    private View mProgressView;
    private View mErrorView;
    private Button refreshOnError;

    private TabLayout tabLayout;
    private ArrayList<String> session_year_list;
    private ViewPager mViewPager;

    String[] tabTitle={"COMM","OTHERS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_attendance_sheet_pre_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewPager = (ViewPager)findViewById(R.id.pager);


        session_year_list = new ArrayList<>();
        mProgressView = findViewById(R.id.loader);
        mErrorView = findViewById(R.id.err);
        refreshOnError = (Button)mErrorView.findViewById(R.id.refresh_button);

        //Initializing the tablayout
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mViewPager.setCurrentItem(position,false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        refreshOnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDetails();
            }
        });

        fetchDetails();
    }

    private void setupViewPager(ViewPager viewPager, ArrayList<String> sessionYearList) throws JSONException {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        for(int i=0;i<2;i++)
        {
            switch (tabTitle[i]){
                case "COMM":
                    adapter.addFrag(new GenerateAttendanceSheetForCommFragment(),tabTitle[i]);
                    break;
                case "OTHERS":
                    adapter.addFrag(new GenerateAttendanceSheetForRegularStudentsFragment(),tabTitle[i]);
                    break;
            }
        }
        viewPager.setAdapter(adapter);

        try
        {
            setupTabIcons();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void fetchDetails(){
        HashMap<String,String> params = new HashMap<>();
        SessionManagement session = new SessionManagement(getApplicationContext());
        if(session.isLoggedIn())
        {
            params = session.getTokenDetails();
        }
        tabLayout.setVisibility(View.GONE);
        NetworkRequest nr = new NetworkRequest(GenerateAttendanceSheetPreDetails.this,mProgressView,mErrorView,this,"get", Urls.server_protocol,Urls.session_year_url,params,true,true,0);
        nr.setSnackbar_message(Urls.error_connection_message);
        nr.initiateRequest();
    }

    private View prepareTabView(int pos) {
        View view = getLayoutInflater().inflate(R.layout.custom_tab,null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_title.setText(tabTitle[pos]);
        tv_count.setVisibility(View.GONE);
        return view;
    }

    private void setupTabIcons()
    {

        for(int i=0;i<2;i++)
        {
            tabLayout.getTabAt(i).setCustomView(prepareTabView(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void performAction(String result, int tag) {
        try{
            JSONObject json = new JSONObject(result);
            if(json.getBoolean("success") == true)
            {
                tabLayout.setVisibility(View.VISIBLE);
                JSONArray details = json.getJSONArray("session_year");
                for (int i = 0; i < details.length(); i++) {
                    JSONObject sy = details.getJSONObject(i);
                    session_year_list.add(sy.getString("session_year"));
                }
                setupViewPager(mViewPager,session_year_list);
            }
            else{
                Util.viewSnackbar(findViewById(android.R.id.content),json.getString("err_msg"));
            }
        }catch (Exception e){
            Log.e("Exception",e.toString());
            Util.viewSnackbar(findViewById(android.R.id.content),Urls.parsing_error_message);
        }
    }

}

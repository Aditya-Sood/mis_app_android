package com.android.mis.javac.ViewDetails;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import com.android.mis.R;
import com.android.mis.controllers.ViewPagerAdapter;
import com.android.mis.javac.Home.HomeActivity;
import com.android.mis.javac.MainActivity;
import com.android.mis.javac.MoveToActivityFragment;
import com.android.mis.utils.Callback;
import com.android.mis.utils.NetworkRequest;
import com.android.mis.utils.SessionManagement;
import com.android.mis.utils.Urls;
import com.android.mis.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ViewDetails extends AppCompatActivity implements Callback {

    private ViewPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private ArrayList<String> tabTitle;
    private View mProgressView;
    private View mErrorView;
    private Button refreshOnError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_details);

        mProgressView = findViewById(R.id.loader);
        mErrorView = findViewById(R.id.err);
        refreshOnError = (Button)mErrorView.findViewById(R.id.refresh_button);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewPager = (ViewPager) findViewById(R.id.container);

        //Initializing the tablayout
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        tabTitle = new ArrayList<>();


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

    private void setupViewPager(ViewPager viewPager,ArrayList<String> list,JSONObject json,String auth) throws JSONException {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        for(int i=0;i<list.size();i++)
        {
            tabTitle.add(list.get(i));
            switch (list.get(i)){
                case "PERSONAL":
                    adapter.addFrag(new PersonalDetailsFragment(json.getJSONObject("personal"),auth),list.get(i));
                    break;
                case "ADDRESS":
                    adapter.addFrag(new AddressDetailsFragment(json,auth),list.get(i));
                    break;
                case "ADMISSION":
                    adapter.addFrag(new AdmissionDetailsFragment(json.getJSONObject("admission")),list.get(i));
                    break;
                case "FEE":
                    adapter.addFrag(new FeeDetailsFragment(json.getJSONObject("fee")),list.get(i));
                    break;
                case "BANK":
                    adapter.addFrag(new BankDetailsFragment(json.getJSONObject("bank")),list.get(i));
                    break;
                case "FAMILY":
                    if(auth.contentEquals("stu")){
                        adapter.addFrag(new StuFamilyDetailsFragment(json.getJSONObject("family")),list.get(i));
                    }
                    else{
                        adapter.addFrag(new EmpFamilyDetailsFragment(json),list.get(i));
                    }
                    break;
                case "STAY":
                    adapter.addFrag(new StayDetailsFragment(json),list.get(i));
                    break;
                case "EDUCATION":
                    adapter.addFrag(new EducationDetailsFragment(json),list.get(i));
                    break;
                default:
                    adapter.addFrag(new MoveToActivityFragment(),list.get(i));
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
        NetworkRequest nr = new NetworkRequest(ViewDetails.this,mProgressView,mErrorView,this,"get", Urls.server_protocol,Urls.view_details_url,params,false,true,0);
        nr.setSnackbar_message(Urls.error_connection_message);
        nr.initiateRequest();
    }


    private View prepareTabView(int pos) {
        View view = getLayoutInflater().inflate(R.layout.custom_tab,null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_count = (TextView) view.findViewById(R.id.tv_count);
        tv_title.setText(tabTitle.get(pos));
        tv_count.setVisibility(View.GONE);
        return view;
    }

    private void setupTabIcons()
    {

        for(int i=0;i<tabTitle.size();i++)
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
                JSONObject details = json.getJSONObject("details");
                Iterator keysToCopyIterator = details.keys();
                ArrayList<String> keysList = new ArrayList<String>();
                while(keysToCopyIterator.hasNext()) {
                    String key = (String) keysToCopyIterator.next();
                    keysList.add(key.toUpperCase());
                }
                String auth = json.getString("auth");
                setupViewPager(mViewPager,keysList,details,auth);
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

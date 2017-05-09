package com.android.mis.javac.Home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.javac.Attendance.GenerateAttendanceSheet.GenerateAttendanceSheetPreDetails;
import com.android.mis.javac.Attendance.ViewAttendance.AttendancePreDetails;
import com.android.mis.javac.CourseStructure.CourseStructureActivity;
import com.android.mis.javac.ViewDetails.ViewDetails;
import com.android.mis.models.Download;
import com.android.mis.utils.Callback;
import com.android.mis.utils.CircleTransform;
import com.android.mis.utils.NetworkRequest;
import com.android.mis.utils.SessionManagement;
import com.android.mis.utils.Urls;
import com.android.mis.utils.Util;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,Callback {

    View loader,error;
    HashMap<String,String> hmap;
    HashMap<String,String> sessionDetails;
    ImageView userPic;
    TextView userName,userEmailId;
    NavigationView navigationView;
    Menu menu;
    ImageButton refresh_button_header;
    public static final String MESSAGE_PROGRESS = "message_progress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        View headerview=getLayoutInflater().inflate(R.layout.nav_header_home, null);

        userPic = (ImageView)header.findViewById(R.id.user_profile_pic);
        userName = (TextView)header.findViewById(R.id.user_name);
        userEmailId = (TextView)header.findViewById(R.id.user_email);

        registerReceiver();
        hmap = new HashMap<>();
        sessionDetails = new HashMap<>();
        SessionManagement session = new SessionManagement(getApplicationContext());
        if(session.isLoggedIn())
        {
            hmap = session.getTokenDetails();
            sessionDetails = session.getSessionDetails();
            userName.setText(sessionDetails.get(SessionManagement.KEY_NAME));
            userEmailId.setText(sessionDetails.get(SessionManagement.KEY_EMAIL));
            Picasso.with(getApplicationContext()).load(Urls.image_base_path+sessionDetails.get(SessionManagement.KEY_PIC_PATH)).transform(new CircleTransform()).placeholder(R.mipmap.default_usr).error(R.mipmap.default_profile).resize(100,100).into(userPic);
        }

        loader = headerview.findViewById(R.id.loader);
        loadFragment(new HomeFragment(),"Home");
        performNetworkRequest();
    }

    private void registerReceiver(){
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(MESSAGE_PROGRESS)){

                Download download = intent.getParcelableExtra("download");
                if(download.getProgress() == 100){

                    Toast.makeText(getApplicationContext(),"File Downloaded",Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(),String.format("Downloaded (%d/%d) MB",download.getCurrentFileSize(),download.getTotalFileSize()),Toast.LENGTH_LONG).show();
                    //mProgressText.setText(String.format("Downloaded (%d/%d) MB",download.getCurrentFileSize(),download.getTotalFileSize()));

                }
            }
        }
    };

    public void performNetworkRequest(){
        NetworkRequest nr = new NetworkRequest(HomeActivity.this,loader,loader,this,"get","http", Urls.menu_url,hmap,true,false,0);
        nr.setSnackbar_message(Urls.error_connection_message);
        nr.initiateRequest();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String title="";
        Fragment fragment = null;
        switch (id)
        {
            case R.id.home:
                fragment = new HomeFragment();
                title = "Home";
                break;

            case R.id.view_details:
                Util.moveToActivity(HomeActivity.this,ViewDetails.class,null,false);
                return true;

            case R.id.edit_details:
                break;

            case R.id.view_attendance:
                Util.moveToActivity(HomeActivity.this,AttendancePreDetails.class,null,false);
                break;

            case R.id.view_defaulter_list:
                break;

            case R.id.generate_attendance_sheet:
                Util.moveToActivity(HomeActivity.this, GenerateAttendanceSheetPreDetails.class,null,false);
                break;

            case R.id.view_course_structure:
                Util.moveToActivity(HomeActivity.this,CourseStructureActivity.class,null,false);
                break;

            case R.id.logout:
                Util.logoutUser(HomeActivity.this,"");
                return true;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        loadFragment(fragment,title);
        return true;
    }

    private void loadFragment(Fragment fragment,String title)
    {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_home, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }else{
            //Toast.makeText(getApplicationContext(),title,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void performAction(String result, int tag) {
        try{
            Log.d("result",result);
            JSONObject json = new JSONObject(result);
            Boolean success = json.getBoolean("success");
            if(success){
                addToMenu(json.getJSONObject("modules"));
            }
            else{
                error.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            Log.e("Exception",e.toString());
        }
    }


    private void addToMenu(JSONObject modules) throws JSONException {
        HashMap<String,Integer> hmap = Util.getMenuMappedIds();
        Iterator iterator = modules.keys();
        menu = navigationView.getMenu();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
          //  Toast.makeText(getApplicationContext(),modules.optString(key),Toast.LENGTH_LONG).show();
            //Log.d("hurray",modules.optString(key));

            MenuItem current =  menu.findItem(hmap.get(modules.optString(key)));
            current.setVisible(true);
        }
    }
}

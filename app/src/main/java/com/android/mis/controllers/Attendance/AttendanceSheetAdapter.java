package com.android.mis.controllers.Attendance;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mis.R;
import com.android.mis.models.Attendance.Student;
import com.android.mis.models.Attendance.SubjectAttendanceItem;
import com.android.mis.models.Attendance.SubjectByTeacher;
import com.android.mis.utils.Callback;
import com.android.mis.utils.NetworkRequest;
import com.android.mis.utils.SessionManagement;
import com.android.mis.utils.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by rajat on 6/5/17.
 */

public class AttendanceSheetAdapter extends RecyclerView.Adapter<AttendanceSheetAdapter.MyViewHolder> implements Callback{
    private List<Student> studentList;
    private Context context;
    private Activity activity;
    private Bundle extras;
    private TableLayout layout;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView admn_no,name,percent;
        public TextView absents,presents;
        public Button details;
        public Layout layout;

        public MyViewHolder(View view) {
            super(view);
            admn_no = (TextView)view.findViewById(R.id.admn_no);
            name = (TextView)view.findViewById(R.id.name);
            absents = (TextView) view.findViewById(R.id.absents);
            presents = (TextView) view.findViewById(R.id.presents);
            percent = (TextView)view.findViewById(R.id.attendance);
            details = (Button)view.findViewById(R.id.attendance_details);
        }
    }


    public AttendanceSheetAdapter(List<Student> studentList,Context context,Activity activity,Bundle extra_info) {
        this.studentList = studentList;
        this.activity = activity;
        this.context = context;
        this.extras = extra_info;
    }

    @Override
    public AttendanceSheetAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attendance_sheet_item, parent, false);
        return new AttendanceSheetAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AttendanceSheetAdapter.MyViewHolder holder, int position) {
        final Student member = studentList.get(position);
        holder.admn_no.setText(member.getAdmn_no());
        holder.name.setText("("+member.getName()+")");
        holder.absents.setText(Integer.toString(member.getAbsents()));
        int total_presents = extras.getInt("total_classes")-member.getAbsents();
        holder.presents.setText(Integer.toString(total_presents));
        int percent = (int) Math.ceil((total_presents*100)/(float)(extras.getInt("total_classes")));
        holder.percent.setText(Integer.toString(percent)+" %");

        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View mProgressView;
                final View mErrorView;
                Button refreshOnError;

                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.dialog_attendance_details);
                dialog.setTitle("Details");
                dialog.show();

                mProgressView = dialog.findViewById(R.id.loader);
                mErrorView = dialog.findViewById(R.id.err);
                refreshOnError = (Button)mErrorView.findViewById(R.id.refresh_button);
                layout = (TableLayout)dialog.findViewById(R.id.attendance_details_table) ;

                refreshOnError.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchAttendanceDetails(mProgressView,mErrorView,member);
                    }
                });
                fetchAttendanceDetails(mProgressView,mErrorView,member);
            }
        });

        if(member.getTag()%2 == 0)
            holder.itemView.setBackgroundResource(R.color.details_background1);
        else
            holder.itemView.setBackgroundResource(R.color.details_background2);
    }

    private void fetchAttendanceDetails(View mProgressView,View mErrorView,Student member)
    {
        HashMap<String,String> params = new HashMap<>();
        SessionManagement session = new SessionManagement(context);
        if(session.isLoggedIn())
        {
            params = session.getTokenDetails();
        }
        params.put("map_id",extras.getString("map_id"));
        params.put("sub_id",extras.getString("sub_id"));
        params.put("adm_no",member.getAdmn_no());
        NetworkRequest nr = new NetworkRequest(activity,mProgressView,mErrorView,this,"get", Urls.server_protocol,Urls.view_detailed_attendance_all_url,params,false,true,0);
        nr.setSnackbar_message(Urls.error_connection_message);
        nr.initiateRequest();
    }

    @Override
    public void performAction(String result, int tag) {
        try{
            JSONObject json = new JSONObject(result);
            if(json.getBoolean("success") == true)
            {
                JSONObject attendance_details = json.getJSONObject("attendance");
                Iterator keysToCopyIterator = attendance_details.keys();
                ArrayList<String> keysList = new ArrayList<String>();
                while(keysToCopyIterator.hasNext()) {
                    String key = (String) keysToCopyIterator.next();
                    keysList.add(key);
                }
                showView(keysList,attendance_details);
            }
        }catch (Exception e){
            Log.e("Exception",e.toString());
        }
    }

    private void showView(ArrayList<String> keys,JSONObject json) throws JSONException {
        for(int i=0;i<keys.size();i++)
        {
            layout.addView(getTableRow(keys.get(i),json.getInt(keys.get(i))),new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }

    }

    private TableRow getTableRow(String date, int status){
        TableRow row = new TableRow(context);
        TextView date_class = new TextView(context);

        date_class.setText(date);
        date_class.setTextColor(Color.BLACK);
        date_class.setMaxLines(1);
        date_class.setGravity(Gravity.CENTER);
        date_class.setTextSize(12);
        date_class.setBackgroundResource(R.drawable.table_border);
        row.addView(date_class);

        TextView status_stu = new TextView(context);

        if(status == 1) {
            status_stu.setText("'P'");
            status_stu.setTextColor(Color.GREEN);
        }
        else{
            status_stu.setText("'A'");
            status_stu.setTextColor(Color.RED);
        }
        status_stu.setMaxLines(1);
        status_stu.setGravity(Gravity.CENTER);
        status_stu.setTextSize(12);
        status_stu.setBackgroundResource(R.drawable.table_border);
        row.addView(status_stu);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        return row;
    }


    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public void setFilter(List<Student> countryModels) {
        studentList = new ArrayList<>();
        studentList.addAll(countryModels);
        notifyDataSetChanged();
    }
}

package com.android.mis.controllers.Attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.mis.R;
import com.android.mis.models.Attendance.Subject;

import java.util.ArrayList;

/**
 * Created by rajat on 7/5/17.
 */

public class SubjectSpinnerAdapter extends BaseAdapter{
    Context context;
    int itemRes;
    ArrayList<Subject> objects;
    LayoutInflater inflter;

    public SubjectSpinnerAdapter(Context applicationContext,int item_res,ArrayList<Subject> objects) {
        this.context = applicationContext;
        this.objects = objects;
        this.itemRes = item_res;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(itemRes, null);
        TextView names = (TextView) view.findViewById(R.id.name);
        names.setText(getVal(objects.get(i).getSubId(),objects.get(i).getName()));
        return view;
    }

    public String getVal(String a,String b){
        if(a.length() == 0)
            return b;
        else{
            return a+" - "+b;
        }
    }
}

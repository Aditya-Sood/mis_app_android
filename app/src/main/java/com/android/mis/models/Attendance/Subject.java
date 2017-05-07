package com.android.mis.models.Attendance;

/**
 * Created by rajat on 7/5/17.
 */

public class Subject {
    private String subId,name,nId;
    private int semester;

    public Subject(String subId,String name,String nId,int semester){
        this.subId = subId;
        this.name = name;
        this.nId = nId;
        this.semester = semester;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getSubId(){
        return subId;
    }

    public String getName(){
        return name;
    }

    public String getnId(){
        return nId;
    }

    public int getSemester(){
        return semester;
    }
}

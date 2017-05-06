package com.android.mis.models.Attendance;

/**
 * Created by rajat on 6/5/17.
 */

public class Student {
    private String admn_no,first_name,middle_name,last_name;
    private int absents ,tag;

    public Student(String admn_no,String first_name,String middle_name,String last_name,int absents,int tag){
        this.admn_no = admn_no;
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.absents = absents;
        this.tag = tag;
    }

    public String getAdmn_no(){
        return admn_no;
    }

    public String getFirst_name(){
        return first_name;
    }

    public String getMiddle_name(){
        return middle_name;
    }

    public String getLast_name(){
        return last_name;
    }

    public String getName(){
        if(middle_name.length() == 0){
            return first_name+" "+last_name;
        }
        else{
            return first_name+" "+middle_name+" "+last_name;
        }
    }

    public int getAbsents(){
        return absents;
    }

    public int getTag(){
        return tag;
    }


}

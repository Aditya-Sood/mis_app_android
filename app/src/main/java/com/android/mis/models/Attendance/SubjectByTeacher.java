package com.android.mis.models.Attendance;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rajat on 5/5/17.
 */

public class SubjectByTeacher implements Parcelable{
    private String sId,subName,subId,branchName,courseName,branchId,courseId,aggrId;
    private int semester,group,tag;

    public SubjectByTeacher(String sId,String subName,String subId,int semester,int group,String branchName,String courseName,String branchId,String courseId,String aggrId,int tag){
        this.sId = sId;
        this.subName = subName;
        this.subId = subId;
        this.semester = semester;
        this.group = group;
        this.branchName = branchName;
        this.courseName = courseName;
        this.branchId = branchId;
        this.courseId = courseId;
        this.aggrId = aggrId;
        this.tag = tag;
    }

    protected SubjectByTeacher(Parcel in) {
        sId = in.readString();
        subName = in.readString();
        subId = in.readString();
        branchName = in.readString();
        courseName = in.readString();
        branchId = in.readString();
        courseId = in.readString();
        aggrId = in.readString();
        semester = in.readInt();
        group = in.readInt();
        tag = in.readInt();
    }

    public static final Creator<SubjectByTeacher> CREATOR = new Creator<SubjectByTeacher>() {
        @Override
        public SubjectByTeacher createFromParcel(Parcel in) {
            return new SubjectByTeacher(in);
        }

        @Override
        public SubjectByTeacher[] newArray(int size) {
            return new SubjectByTeacher[size];
        }
    };

    public String getsId(){
        return sId;
    }

    public String getSubName(){
        return subName;
    }

    public String getSubId(){
        return subId;
    }

    public String getBranchName(){
        return branchName;
    }

    public String getCourseName(){
        return courseName;
    }

    public String getBranchId(){
        return branchId;
    }

    public String getCourseId(){
        return courseId;
    }

    public String getAggrId(){
        return aggrId;
    }

    public int getSemester(){
        return semester;
    }

    public int getGroup(){
        return group;
    }

    public int getTag(){
        return tag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sId);
        dest.writeString(subName);
        dest.writeString(subId);
        dest.writeString(branchName);
        dest.writeString(courseName);
        dest.writeString(branchId);
        dest.writeString(courseId);
        dest.writeString(aggrId);
        dest.writeInt(semester);
        dest.writeInt(group);
        dest.writeInt(tag);
    }
}

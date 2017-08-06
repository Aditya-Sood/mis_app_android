package com.android.mis.models.Attendance;

/**
 * Created by Aditya Sood on 25-07-2017.
 *
 * {@link DefaulterStudentAttendanceItem} defines a 'Defaulter Student' item object required for displaying the list of
 * defaulters in the ViewDefaulterStudentAttendance activity.
 */

public class DefaulterStudentAttendanceItem {

    /**
     * Member variables of the class are same as the display fields required in the list item layout
     * file for the list of defaulters.
     */

    //Serial number of the student in the class
    private int serialNumber;

    private String studentName;

    private String admissionNumber;

    private int totalPresent;

    private int totalClasses;

    /**
     * Constructor for the object
     */
    public DefaulterStudentAttendanceItem(int serialNo, String name, String admNo, int totPres, int totClasses) {
        serialNumber = serialNo;
        studentName = name;
        admissionNumber = admNo;
        totalPresent = totPres;
        totalClasses = totClasses;
    }


    /**
     * Getter methods for the member variables:
     */
    public int getSerialNumber() {
        return serialNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getAdmissionNumber() {
        return admissionNumber;
    }

    public int getTotalPresent() {
        return totalPresent;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    /**
     * Methods for calculating values related to the object, computed using the values of member
     * variables, and returning those values.
     */
    public float getPercentagePresent() {
        return ( ((float) totalPresent * 100) / totalClasses );
    }


}

package ru.mse.dataserver;

import com.google.gson.annotations.SerializedName;

import java.sql.Date;

public class TimetableChange {
//    public Date date;
    public String message;
    @SerializedName("new_timetable") public Timetable newTimetable;
}

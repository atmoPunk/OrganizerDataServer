package ru.mse.dataserver;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Timetable {
    static public class Lesson {
        public String name;
        public String subtype;
        @SerializedName("start_time") public int StartTime;
        @SerializedName("end_time") public int EndTime;
        public String link;
    }

    public List<Lesson> lessons;
}

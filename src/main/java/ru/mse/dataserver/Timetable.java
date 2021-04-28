package ru.mse.dataserver;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Timetable {
    public Timetable() {
        lessons = new ArrayList<>();
    }

    static public class Lesson {
        public String name;

        public Lesson() {}

        public Lesson(String name, String subtype, int startTime, int endTime, String link) {
            this.name = name;
            this.subtype = subtype;
            StartTime = startTime;
            EndTime = endTime;
            this.link = link;
        }

        public String subtype;
        @SerializedName("start_time") public int StartTime;
        @SerializedName("end_time") public int EndTime;
        public String link;
    }

    public List<Lesson> lessons;
}

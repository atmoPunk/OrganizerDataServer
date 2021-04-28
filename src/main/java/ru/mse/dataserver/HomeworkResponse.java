package ru.mse.dataserver;

import java.sql.Date;
import java.time.LocalDate;

public class HomeworkResponse {
    public HomeworkResponse(LocalDate date, String subject, String text, String link) {
        this.date = date;
        this.subject = subject;
        this.text = text;
        this.link = link;
    }

    LocalDate date;
    String subject;
    String text;
    String link;
}

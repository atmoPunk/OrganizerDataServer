package ru.mse.dataserver;

import com.google.gson.Gson;

import java.sql.*;
import java.time.LocalDate;

public class DBConnection implements AutoCloseable {

    private final Connection conn;

    public DBConnection() throws SQLException {
        String url = "jdbc:sqlite:timetable.db";
        conn = DriverManager.getConnection(url);
    }

    public String getTimetableDay(int day) throws SQLException {
        String sql = "SELECT Timetable FROM week WHERE DayOfWeek = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, day);
        try (ResultSet result = stmt.executeQuery()) {
            if (!result.next()) {
                return "NO DATA";
            }

            return result.getString("Timetable");
        }
    }

    public void addTimetableChanges(LocalDate date, TimetableChange change) throws SQLException {
        String sql = "INSERT INTO changes(date, message, new_timetable) VALUES(?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDate(1, Date.valueOf(date));
        stmt.setString(2, change.message);
        stmt.setString(3, new Gson().toJson(change.newTimetable));
        stmt.executeUpdate();
    }

    // FIXME: Temporarily returns simple String
    public String getChangesForDate(LocalDate date) throws SQLException {
        System.err.println(date);
        String sql = "SELECT new_timetable FROM changes WHERE date = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDate(1, Date.valueOf(date));
        try (ResultSet result = stmt.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return result.getString("new_timetable");
        }
    }

    public String getHomeworkBySubject(String subject) throws SQLException {
        System.err.println(subject);
        String sql = "SELECT new_timetable FROM changes WHERE date = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
//        stmt.setDate(1, date);
        System.err.println(stmt);
        try (ResultSet result = stmt.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return result.getString("new_timetable");
        }
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}

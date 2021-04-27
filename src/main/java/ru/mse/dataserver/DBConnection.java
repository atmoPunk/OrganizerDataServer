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
        String sql = "SELECT * FROM homework WHERE subject = ? AND date >= ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        LocalDate curDate = LocalDate.now();
        stmt.setString(1, subject);
        stmt.setDate(2, Date.valueOf(curDate));
        try (ResultSet result = stmt.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return result.toString();
        }
    }

    public String getHomework() throws SQLException {
        String sql = "SELECT * FROM homework WHERE date >= ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        LocalDate curDate = LocalDate.now();
        stmt.setDate(1, Date.valueOf(curDate));
        try (ResultSet result = stmt.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return result.toString();
        }
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}

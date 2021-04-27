package ru.mse.dataserver;

import java.sql.*;

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

    public void addTimetableChanges(TimetableChange change) {

    }

    public TimetableChange getChangesForDate(Date date) {
        return null;
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}

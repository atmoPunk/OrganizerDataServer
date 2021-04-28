package ru.mse.dataserver;

import com.google.gson.Gson;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DBConnection implements AutoCloseable {

    private final Connection conn;

    public DBConnection() throws SQLException {
        String url = "jdbc:sqlite:timetable_no_json.db";
        conn = DriverManager.getConnection(url);
    }

    public boolean isRegistered(String user) throws SQLException {
        String sql = "SELECT 1 from users WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, user);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        }
    }

    public void registerUser(String user) throws SQLException {
        String sql = "INSERT INTO users(id) VALUES(?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, user);
        stmt.executeUpdate();
    }

    public void setLessons(String user, SetLessonsRequest req) throws SQLException {
        String sql = "UPDATE users SET " + req.subject +  " = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
//        stmt.setString(1, req.subject);
        stmt.setInt(1, req.group);
        stmt.setString(2, user);
        stmt.executeUpdate();
    }

    public Timetable getTimetableDay(int day, String user) throws SQLException {
        String getUserInfo = "SELECT * FROM users WHERE user = ?";
        PreparedStatement uiStmt = conn.prepareStatement(getUserInfo);
        int matlogic = 0;
        int formlang = 0;
        int algos = 0;
        int spec = 0;
        try (ResultSet rs = uiStmt.executeQuery()) {
            if (rs.next()) {
                matlogic = rs.getInt("matlogic");
                formlang = rs.getInt("formlang");
                algos = rs.getInt("algos");
                spec = rs.getInt("spec");
            }
        }

        String sql = "SELECT * FROM timetable WHERE weekday = ? AND (matlogic = ? OR matlogic IS NULL) AND is_temp = 0 ORDER BY start_time";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, day);
        stmt.setInt(2, matlogic);
        try (ResultSet result = stmt.executeQuery()) {
            System.err.println("Query executed");
            Timetable t = new Timetable();
            while (result.next()) {
                System.err.println("has result");
                t.lessons.add(new Timetable.Lesson(result.getString("name"),
                        result.getString("subtype"),
                        result.getInt("start_time"),
                        result.getInt("end_time"),
                        result.getString("link")));
                System.err.println("result processed");
            }
            System.err.println(t.lessons.size());
            return t;
        }
    }

    public void addTimetableChanges(LocalDate date, TimetableChange change) throws SQLException {
        String sql1 = "INSERT INTO changes(date, message) VALUES(?, ?) RETURNING id";
        PreparedStatement stmt1 = conn.prepareStatement(sql1);
        stmt1.setDate(1, Date.valueOf(date));
        stmt1.setString(2, change.message);
        Integer changeId = null;
        try (ResultSet rs1 = stmt1.executeQuery()) {
            if (!rs1.next()) {
                throw new RuntimeException("DID NOT INSERT CHANGE");
            }
            changeId = rs1.getInt("id");
        }

        String sql2 = "INSERT INTO timetable(weekday, link, name, subtype, start_time, end_time, is_temp) VALUES(-1, ?, ?, ?, ?, ?, 1) RETURNING id";
        PreparedStatement stmt2 = conn.prepareStatement(sql2);
        List<Integer> les_ids = new ArrayList<>();
        for (Timetable.Lesson lesson : change.newTimetable.lessons) {
            stmt2.setString(1, lesson.link);
            stmt2.setString(2, lesson.name);
            stmt2.setString(3, lesson.subtype);
            stmt2.setInt(4, lesson.StartTime);
            stmt2.setInt(5, lesson.EndTime);
            try (ResultSet rs2 = stmt2.executeQuery()) {
                if (!rs2.next()) {
                    throw new RuntimeException("DID NOT INSERT NEW LESSON");
                }
                les_ids.add(rs2.getInt("id"));
            }
        }

        String sql3 = "INSERT INTO change_to_tables(change_id, timetable_id) VALUES(?, ?)";
        PreparedStatement stmt3 = conn.prepareStatement(sql3);
        for (Integer les_id : les_ids) {
            stmt3.setInt(1, changeId);
            stmt3.setInt(2, les_id);
            stmt3.executeUpdate();
        }
//
//
//        String sql = "INSERT INTO changes(date, message, new_timetable) VALUES(?, ?, ?)";
//        PreparedStatement stmt = conn.prepareStatement(sql);
//        stmt.setDate(1, Date.valueOf(date));
//        stmt.setString(2, change.message);
//        stmt.setString(3, new Gson().toJson(change.newTimetable));
//        stmt.executeUpdate();
    }

    // FIXME: Temporarily returns simple String
    public Timetable getChangesForDate(LocalDate date) throws SQLException {
        System.err.println(date);
        String sql = "SELECT timetable.name, timetable.link, timetable.start_time, timetable.end_time, timetable.subtype FROM changes WHERE date = ? JOIN change_to_tables ON changes.id = change_to_tables.change_id JOIN timetable ON change_to_tables.timetable_id = timetable.id ORDER BY start_time";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDate(1, Date.valueOf(date));
        try (ResultSet result = stmt.executeQuery()) {
            Timetable t = new Timetable();
            while (result.next()) {
                System.err.println("has result");
                t.lessons.add(new Timetable.Lesson(result.getString("name"),
                        result.getString("subtype"),
                        result.getInt("start_time"),
                        result.getInt("end_time"),
                        result.getString("link")));
                System.err.println("result processed");
            }
            return t;
        }
    }

    public HomeworkResponse getHomeworkBySubject(String subject) throws SQLException {
        String sql = "SELECT date, link, data FROM homework WHERE subject = ? AND date >= ? ORDER BY date";
        PreparedStatement stmt = conn.prepareStatement(sql);
        LocalDate curDate = LocalDate.now();
        stmt.setString(1, subject);
        stmt.setDate(2, Date.valueOf(curDate));
        try (ResultSet result = stmt.executeQuery()) {
            if (!result.next()) {
                return null;
            }

            return new HomeworkResponse(result.getDate("date").toLocalDate(),
                    subject,
                    result.getString("data"),
                    result.getString("link"));
        }
    }

    public List<HomeworkResponse> getHomework() throws SQLException {
        String sql = "SELECT date, link, subject, data FROM homework WHERE date >= ? ORDER BY date";
        PreparedStatement stmt = conn.prepareStatement(sql);
        LocalDate curDate = LocalDate.now();
        stmt.setDate(1, Date.valueOf(curDate));
        try (ResultSet result = stmt.executeQuery()) {
            List<HomeworkResponse> res = new ArrayList<>();
            while (result.next()) {
                res.add(new HomeworkResponse(result.getDate("date").toLocalDate(),
                        result.getString("subject"),
                        result.getString("data"),
                        result.getString("link")));
            }
            return res;
        }
    }

    public void uploadHomework(HomeworkUploadRequest request) throws SQLException {
        String sql = "INSERT INTO homework (date, link, subject, data) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDate(1, Date.valueOf(request.date));
        stmt.setString(2, request.link);
        stmt.setString(3, request.subject);
        stmt.setString(4, request.data);
        stmt.executeUpdate();
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}

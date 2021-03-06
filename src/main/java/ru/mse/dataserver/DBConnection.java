package ru.mse.dataserver;

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
        String sql = "SELECT 1 from users WHERE user = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, user);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        }
    }

    public void registerUser(String user) throws SQLException {
        String sql = "INSERT INTO users(user) VALUES(?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, user);
        stmt.executeUpdate();
    }

    public List<UserInfo> getUsers() throws SQLException {
        String sql = "SELECT * FROM users";
        PreparedStatement stmt = conn.prepareStatement(sql);
        try (ResultSet rs = stmt.executeQuery()) {
            List<UserInfo>res = new ArrayList<>();
            while(rs.next()) {
                UserInfo ui = new UserInfo();
                ui.user = rs.getString("user");
                ui.formlang = rs.getInt("formlang");
                ui.spec = rs.getInt("spec");
                ui.matlogic = rs.getInt("matlogic");
                ui.algos = rs.getInt("algos");
                res.add(ui);
            }
            return res;
        }
    }

    public UserInfo getUserInfo(String user) throws SQLException {
        String sql = "SELECT * FROM users where user = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, user);
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("No such user: " + user);
            }
            UserInfo ui = new UserInfo();
            ui.user = user;
            ui.formlang = rs.getInt("formlang");
            ui.spec = rs.getInt("spec");
            ui.matlogic = rs.getInt("matlogic");
            ui.algos = rs.getInt("algos");
            return ui;
        }
    }

    public void setLessons(String user, SetLessonsRequest req) throws SQLException {
        String sql = "UPDATE users SET " + req.subject +  " = ? WHERE user = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, req.group);
        stmt.setString(2, user);
        stmt.executeUpdate();
    }

    public Timetable getTimetableDay(int day, UserInfo ui) throws SQLException {
        String sql = "SELECT * FROM timetable WHERE weekday = ? AND (matlogic = ? OR matlogic IS NULL) " +
                "AND (algos = ? OR algos IS NULL)" +
                "AND (spec = ? OR spec IS NULL) " +
                "AND (formlang = ? OR formlang IS NULL) AND is_temp = 0 ORDER BY start_time";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, day);
        stmt.setInt(2, ui.matlogic);
        stmt.setInt(3, ui.algos);
        stmt.setInt(4, ui.spec);
        stmt.setInt(5, ui.formlang);
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
        String sql1 = "INSERT INTO changes(date, message) VALUES(?, ?)";
        PreparedStatement stmt1 = conn.prepareStatement(sql1);
        stmt1.setDate(1, Date.valueOf(date));
        stmt1.setString(2, change.message);
        System.err.println(stmt1);
        Integer changeId;
        try {
            stmt1.executeUpdate();
            try (ResultSet rs = stmt1.getGeneratedKeys()) {
                changeId = rs.getInt(1);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw ex;
        }


        System.err.println("inserted change");
        String sql2 = "INSERT INTO timetable(weekday, link, name, subtype, start_time, end_time, is_temp) VALUES(-1, ?, ?, ?, ?, ?, 1)";
        PreparedStatement stmt2 = conn.prepareStatement(sql2);
        List<Integer> les_ids = new ArrayList<>();
        for (Timetable.Lesson lesson : change.newTimetable.lessons) {
            stmt2.setString(1, lesson.link);
            stmt2.setString(2, lesson.name);
            stmt2.setString(3, lesson.subtype);
            stmt2.setInt(4, lesson.StartTime);
            stmt2.setInt(5, lesson.EndTime);
            stmt2.executeUpdate();
            try (ResultSet rs2 = stmt2.getGeneratedKeys()) {
                if (!rs2.next()) {
                    throw new RuntimeException("DID NOT INSERT NEW LESSON");
                }
                les_ids.add(rs2.getInt(1));
            }
        }

        String sql3 = "INSERT INTO change_to_tables(change_id, timetable_id) VALUES(?, ?)";
        PreparedStatement stmt3 = conn.prepareStatement(sql3);
        for (Integer les_id : les_ids) {
            stmt3.setInt(1, changeId);
            stmt3.setInt(2, les_id);
            stmt3.executeUpdate();
        }
    }

    public Timetable getChangesForDate(LocalDate date) throws SQLException {
        System.err.println(date);
        String sql = "SELECT timetable.name, timetable.link, timetable.start_time, timetable.end_time, " +
                "timetable.subtype FROM changes JOIN change_to_tables ON " +
                "changes.id = change_to_tables.change_id JOIN timetable ON " +
                "change_to_tables.timetable_id = timetable.id  WHERE date = ? ORDER BY start_time";
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

    public HomeworkResponse getHomeworkBySubject(String subject, String user) throws SQLException {
        String sql = "SELECT date, link, data FROM homework JOIN users ON " +
                "(homework.formlang IS users.formlang OR homework.formlang IS NULL OR users.formlang IS NULL) " +
                "AND (homework.algos IS users.algos OR homework.algos IS NULL OR users.algos IS NULL) " +
                "AND (homework.matlogic IS users.matlogic OR homework.matlogic IS NULL OR users.matlogic IS NULL) " +
                "AND (homework.spec IS users.spec OR homework.spec IS NULL OR users.spec IS NULL)" +
                " WHERE subject = ? AND date >= ? AND date <= ? AND user = ? ORDER BY date";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, subject);
        stmt.setDate(2, Date.valueOf(LocalDate.now()));
        stmt.setDate(3, Date.valueOf(LocalDate.now().plusDays(7)));
        stmt.setString(4, user);
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

    public List<HomeworkResponse> getHomework(String user) throws SQLException {
        String sql = "SELECT date, link, homework.subject, data FROM homework JOIN users ON " +
                "(homework.formlang IS users.formlang OR homework.formlang IS NULL OR users.formlang IS NULL) " +
                "AND (homework.algos IS users.algos OR homework.algos IS NULL OR users.algos IS NULL) " +
                "AND (homework.matlogic IS users.matlogic OR homework.matlogic IS NULL OR users.matlogic IS NULL) " +
                "AND (homework.spec IS users.spec OR homework.spec IS NULL OR users.spec IS NULL)" +
                "WHERE date >= ? AND date <= ? AND user = ? ORDER BY date";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(3, user);
        stmt.setDate(1, Date.valueOf(LocalDate.now()));
        stmt.setDate(2, Date.valueOf(LocalDate.now().plusDays(7)));
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

    public String   getHomeworkAddress(String user, String subject) throws SQLException {
        String sql = "SELECT email FROM teacher_emails JOIN users ON " +
                "(teacher_emails.formlang IS users.formlang OR teacher_emails.formlang IS NULL OR users.formlang IS NULL) " +
                "AND (teacher_emails.algos IS users.algos OR teacher_emails.algos IS NULL OR users.algos IS NULL) " +
                "AND (teacher_emails.matlogic IS users.matlogic OR teacher_emails.matlogic IS NULL OR users.matlogic IS NULL) " +
                "AND (teacher_emails.spec IS users.spec OR teacher_emails.spec IS NULL OR users.spec IS NULL) WHERE subject = ? AND users.user = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, subject);
        stmt.setString(2, user);
        try (ResultSet result = stmt.executeQuery()){
            if (!result.next()) {
                return null;
            }
            return result.getString("email");
        }
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public String getReport(String user, String subject) throws SQLException {
        String sql = "SELECT perfreport FROM teacher_emails JOIN users ON " +
                "(teacher_emails.formlang IS users.formlang OR teacher_emails.formlang IS NULL OR users.formlang IS NULL) " +
                "AND (teacher_emails.algos IS users.algos OR teacher_emails.algos IS NULL OR users.algos IS NULL) " +
                "AND (teacher_emails.matlogic IS users.matlogic OR teacher_emails.matlogic IS NULL OR users.matlogic IS NULL) " +
                "AND (teacher_emails.spec IS users.spec OR teacher_emails.spec IS NULL OR users.spec IS NULL) WHERE subject = ? AND users.user = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, subject);
        stmt.setString(2, user);
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("NO PREF REPORT");
            }
            return rs.getString("perfreport");
        }
    }
}

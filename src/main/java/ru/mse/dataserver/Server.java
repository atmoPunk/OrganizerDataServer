package ru.mse.dataserver;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.bind.SqlDateTypeAdapter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;

import java.sql.Date;
import java.util.List;


import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;


public class Server {

    Server(){
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(5001), 0);
            HttpContext httpContext = server.createContext("/");
            httpContext.setHandler(new ScheduleHttpHandler());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private class ScheduleHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.err.println(httpExchange.getRequestURI().toString());

            String user = httpExchange.getResponseHeaders().getFirst("Ident");
            System.err.println(user);
            if (user != null && !user.isEmpty()) {
                try (DBConnection conn = new DBConnection()) {
                    if (!conn.isRegistered(user)) {
                        conn.registerUser(user);
                    }
                } catch (SQLException ex) {
                    handleResponse(httpExchange, "SERVER ERROR");
                }
            }

            try (var reader = new InputStreamReader(httpExchange.getRequestBody())) {
                Gson g = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
                String path = httpExchange.getRequestURI().toString().split("\\?")[0];

                switch (path) {
                    case "/day": {
                        System.err.println("ENTER /day");
                        DayRequest dayRequest = g.fromJson(reader, DayRequest.class);
                        try {
                            handleDaySchedule(httpExchange, dayRequest, user);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                    } break;
                    case "/change": {
                        System.err.println("ENTER /change");
                        try {
                            TimetableChangeRequest data = g.fromJson(reader, TimetableChangeRequest.class);
                            System.err.println("ABC");
                            handleScheduleChange(httpExchange, data);
                        } catch (JsonSyntaxException | IOException ex) {
                            ex.printStackTrace();
                            System.err.println(ex.getMessage());
                            throw ex;
                        }
//                        try {
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            System.err.println(e.getMessage());
//                        }
                        break;
                    }
                    case "/homework/subj" :{
                        System.err.println("ENTER /homework/subj");
                        HomeworkBySubjRequest hwRequest = g.fromJson(reader, HomeworkBySubjRequest.class);
                        try {
                            handleGetHomeworkBySubj(httpExchange, hwRequest);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;
                    }
                    case "/homework" :{
                        System.err.println("ENTER /homework");
                        try {
                            handleGetHomework(httpExchange);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;
                    }
                    case "/homework/upload" :{
                        System.err.println("ENTER /homework/upload");
                        HomeworkUploadRequest hwRequest = g.fromJson(reader, HomeworkUploadRequest.class);
                        try {
                            handleUploadHomework(httpExchange, hwRequest);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;}
                    case "/homework/send":{
                        System.err.println("ENTER /homework/send");

                        break;
                    }
                    case "/set_lessons": {
                        System.err.println("ENTER /set_lessons");
                        SetLessonsRequest slReq = g.fromJson(reader, SetLessonsRequest.class);
                        try {
                            handleSetLessons(httpExchange, user, slReq);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }

                    } break;
                    default: {
                        handleResponse(httpExchange, "UNKNOWN PATH");
                        break;
                    }
                }
            }
        }

        private void handleSetLessons(HttpExchange httpExchange, String user, SetLessonsRequest slReq) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                db.setLessons(user, slReq);
                ans = "Ok";
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "SERVER ERROR";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleDaySchedule(HttpExchange httpExchange, DayRequest dayRequest, String user) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                System.err.println("WOW");
                Timetable t = db.getTimetableDay(dayRequest.day, user);
                System.err.println("WOWZERS");
//                Timetable change = db.getChangesForDate(LocalDate.now());
//                System.err.println(change);
//                if (change != null && !change.lessons.isEmpty()) { // TODO: change.lessons.isEmpty is valid state
//                    t = change;
//                }
                ans = new Gson().toJson(t);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleScheduleChange(HttpExchange httpExchange, TimetableChangeRequest data) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                db.addTimetableChanges(data.date, data.change);
                System.err.println("QWE");
                ans = "Ok";
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleGetHomeworkBySubj(HttpExchange httpExchange, HomeworkBySubjRequest request) throws IOException {
            String ans;
            Gson g = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
            try (DBConnection db = new DBConnection()) {
                HomeworkResponse homework = db.getHomeworkBySubject(request.subject);
                ans = g.toJson(homework);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleGetHomework(HttpExchange httpExchange) throws IOException {
            String ans;
            Gson g = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
            try (DBConnection db = new DBConnection()) {
                List<HomeworkResponse> homework = db.getHomework();
                ans = g.toJson(homework);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }
        private void handleUploadHomework(HttpExchange httpExchange, HomeworkUploadRequest request) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                db.uploadHomework(request);
                ans = "Ok";
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void sendHomework(HttpExchange httpExchange, HomeworkSendRequest request) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
//                String sendTo = db.getHomeworkAdress();
//                Gmail.send();
                ans = "Ok";
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);

        }

        private void handleResponse(HttpExchange httpExchange, String toSend)  throws IOException {
            System.err.println(toSend);
            httpExchange.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = httpExchange.getResponseBody();
            try (var writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                writer.write(toSend);
            }
        }
    }
}

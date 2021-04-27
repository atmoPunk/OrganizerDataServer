package ru.mse.dataserver;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
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


import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;


public class Server {

    Server(){
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);
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

            try (var reader = new InputStreamReader(httpExchange.getRequestBody())) {
                Gson g = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
                String path = httpExchange.getRequestURI().toString().split("\\?")[0];

                switch (path) {
                    case "/day": {
                        System.err.println("ENTER /day");
                        DayRequest dayRequest = g.fromJson(reader, DayRequest.class);
                        try {
                            handleDaySchedule(httpExchange, dayRequest);
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
                    default: {
                        handleResponse(httpExchange, "UNKNOWN PATH");
                        break;
                    }
                }
            }
        }

        private void handleDaySchedule(HttpExchange httpExchange, DayRequest dayRequest) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                ans = db.getTimetableDay(dayRequest.day);
                String change = db.getChangesForDate(LocalDate.now());
                System.err.println(change);
                if (change != null) {
                    ans = change;
                }
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
            try (DBConnection db = new DBConnection()) {
                ans = db.getHomeworkBySubject(request.subject);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleGetHomework(HttpExchange httpExchange) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                ans = db.getHomework();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleResponse(HttpExchange httpExchange, String toSend)  throws IOException {
            httpExchange.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = httpExchange.getResponseBody();
            try (var writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                writer.write(toSend);
            }
        }
    }
}

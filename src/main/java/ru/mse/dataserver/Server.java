package ru.mse.dataserver;
import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;

import java.sql.Date;


import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;


public class Server {

    Server(){
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(5000), 0);
            HttpContext httpContext = server.createContext("/");
            httpContext.setHandler(new MyHttpHandler());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private class MyHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.err.println(httpExchange.getRequestURI().toString());

            try (var reader = new InputStreamReader(httpExchange.getRequestBody())) {
                Gson g = new Gson();
                String path = httpExchange.getRequestURI().toString().split("\\?")[0];

                switch (path) {
                    case "/day": {
                        System.err.println("ENTER /day");
                        JsonElement json = g.fromJson(reader, JsonElement.class);
                        Integer day = json.getAsJsonObject().get("day").getAsInt();
                        try {
                            handleDaySchedule(httpExchange, day);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                    } break;
                    case "/change": {
                        System.err.println("ENTER /change");
                        JsonElement json = g.fromJson(reader, JsonElement.class);
                        String date = json.getAsJsonObject().get("date").getAsString();
                        break;
                    }
                    default: {
                        handleResponse(httpExchange, "UNKNOWN PATH");
                        break;
                    }
                }
            }
        }

        private void handleDaySchedule(HttpExchange httpExchange, Integer day) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                ans = db.getTimetableDay(day);
                TimetableChange change = db.getChangesForDate(new Date(System.currentTimeMillis()));
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleScheduleChange(HttpExchange httpExchange, TimetableChange timetableChange) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                db.addTimetableChanges(timetableChange);
                ans = "Ok";
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


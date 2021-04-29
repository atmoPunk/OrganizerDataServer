package ru.mse.dataserver;

import com.google.gson.*;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;

import java.util.List;


import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;


public class Server {

    Server() {
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

        private HttpClient client = HttpClient.newHttpClient();

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.err.println(httpExchange.getRequestURI().toString());

            String user = httpExchange.getRequestHeaders().getFirst("Ident");
            System.err.println(user);
            if (user != null && !user.isEmpty()) {
                try (DBConnection conn = new DBConnection()) {
                    if (!conn.isRegistered(user)) {
                        conn.registerUser(user);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.err.println(ex.getMessage());
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
                    }
                    break;
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
                    case "/homework/subj": {
                        System.err.println("ENTER /homework/subj");
                        HomeworkBySubjRequest hwRequest = g.fromJson(reader, HomeworkBySubjRequest.class);
                        try {
                            handleGetHomeworkBySubj(httpExchange, hwRequest, user);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;
                    }
                    case "/homework": {
                        System.err.println("ENTER /homework");
                        try {
                            handleGetHomework(httpExchange, user);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;
                    }
                    case "/homework/upload": {
                        System.err.println("ENTER /homework/upload");
                        HomeworkUploadRequest hwRequest = g.fromJson(reader, HomeworkUploadRequest.class);
                        try {
                            handleUploadHomework(httpExchange, hwRequest);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;
                    }
                    case "/ui/homework": {
                        System.err.println("/ui/homework/");
                        handleHtmlResponse(httpExchange, Paths.get("pages/homework.html"));
                    }
                    break;
                    case "/ui/homework.css": {
                        System.err.println("/ui/homework/");
                        handleCssResponse(httpExchange, Paths.get("pages/homework.css"));
                    }
                    break;
                    case "/homework/upload/form": {
                        System.err.println("/homework/upload/form");
//                        IOUtils
                        System.err.println(IOUtils.toString(reader));
                        HomeworkUploadRequest req = new HomeworkUploadRequest();
//                        req.data;

                    }
                    break;
                    case "/homework/send": {
                        System.err.println("ENTER /homework/send");
                        HomeworkSendRequest req = g.fromJson(reader, HomeworkSendRequest.class);
                        try {
                            handleSendHomework(httpExchange, user, req);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;
                    }
                    case "/perfreport": {
                        System.err.println("ENTER /perfreport");
                        PerformanceReportRequest req = g.fromJson(reader, PerformanceReportRequest.class);
                        try {
                            handlePerfReport(httpExchange, user, req.subject);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            System.err.println(ex.getMessage());
                        }
                    }
                    break;
                    case "/set_lessons": {
                        System.err.println("ENTER /set_lessons");
                        SetLessonsRequest slReq = g.fromJson(reader, SetLessonsRequest.class);
                        try {
                            handleSetLessons(httpExchange, user, slReq);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }

                    }
                    break;
                    case "/send_alert":
                        System.err.println("ENTER /send_alert");
                        AlertRequest alertRequest = g.fromJson(reader, AlertRequest.class);
                        try {
                            handleSendAlert(httpExchange, alertRequest.text);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        break;
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
                UserInfo info = db.getUserInfo(user);
                Timetable t = db.getTimetableDay(dayRequest.day, info);
                //TODO: changes not only for today
                if (DayOfWeek.of(dayRequest.day) == LocalDate.now().getDayOfWeek()) {
                    Timetable change = db.getChangesForDate(LocalDate.now());
                    System.err.println(change);
                    if (change != null && !change.lessons.isEmpty()) { // TODO: change.lessons.isEmpty is valid state
                        t = change;
                    }
                }
                ans = new Gson().toJson(t);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handlePerfReport(HttpExchange httpExchange, String user, String subject) throws IOException {
            try (DBConnection db = new DBConnection()) {
//                UserInfo ui = db.getUserInfo(user);
                String reportLink = db.getReport(user, subject);
                handleResponse(httpExchange, "{\"link\":\"" + reportLink + "\"}");
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println(ex.getMessage());
                handleResponse(httpExchange, "SERVER ERROR");
            }
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

        private void handleGetHomeworkBySubj(HttpExchange httpExchange, HomeworkBySubjRequest request, String user) throws IOException {
            String ans;
            Gson g = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
            try (DBConnection db = new DBConnection()) {
                HomeworkResponse homework = db.getHomeworkBySubject(request.subject, user);
                ans = g.toJson(homework);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        private void handleGetHomework(HttpExchange httpExchange, String user) throws IOException {
            String ans;
            Gson g = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
            try (DBConnection db = new DBConnection()) {
                List<HomeworkResponse> homework = db.getHomework(user);
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

        private void handleSendHomework(HttpExchange httpExchange, String user, HomeworkSendRequest request) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                String sendTo = db.getHomeworkAddress(user, request.subject);
                System.err.println("SEND TO: " + sendTo);
                String filePath = getFile(request.fileId).filePath;
                String message = request.message + '\n' + filePath;
                String messageSubj = "ITMO.MSE homework on " + request.subject + " from " + user;
                Gmail.send(sendTo, messageSubj, message);
                ans = "Ok";
            } catch (SQLException | InterruptedException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);

        }

        private void handleSendAlert(HttpExchange httpExchange, String text) throws IOException {
            String ans;
            try (DBConnection db = new DBConnection()) {
                List<UserInfo> users = db.getUsers();
                users.forEach(userInfo -> {
                    sendMessage(userInfo.user, text);
                });
                ans = "Ok";
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                ans = "Ошибка сервера, попробуйте позже";
            }
            handleResponse(httpExchange, ans);
        }

        final String BOT_TOKEN = "1728118655:AAEQOKogNSnI0WgkTNzpbpufH6LXi6HP6lQ";

        private void sendMessage(String chatId, String text) {
            URIBuilder builder = new URIBuilder();
            try {
                URI uri = builder.setScheme("https")
                        .setHost("api.telegram.org")
                        .setPath("bot" + BOT_TOKEN + "/sendMessage")
                        .addParameter("chat_id", chatId)
                        .addParameter("text", text)
                        .build();
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .build();
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void handleHtmlResponse(HttpExchange httpExchange, Path pathToPage) throws IOException {
            System.err.println("handleHtmlResponse: " + pathToPage);
            httpExchange.getResponseHeaders().put("Content-Type", Collections.singletonList("text/html"));
            httpExchange.sendResponseHeaders(200, 0);
            try (var writer = new BufferedWriter(new OutputStreamWriter(httpExchange.getResponseBody())); var file = new FileInputStream(pathToPage.toFile())) {
                IOUtils.copy(file, writer, Charset.defaultCharset());
            }
            System.err.println("handleHtmlResponse: OK");
        }

        private void handleCssResponse(HttpExchange httpExchange, Path pathToPage) throws IOException {
            System.err.println("handleCssResponse: " + pathToPage);
            httpExchange.getResponseHeaders().put("Content-Type", Collections.singletonList("text/css"));
            httpExchange.sendResponseHeaders(200, 0);
            try (var writer = new BufferedWriter(new OutputStreamWriter(httpExchange.getResponseBody())); var file = new FileInputStream(pathToPage.toFile())) {
                IOUtils.copy(file, writer, Charset.defaultCharset());
            }
            System.err.println("handleCssResponse: OK");
        }

        private void handleResponse(HttpExchange httpExchange, String toSend) throws IOException {
            System.err.println(toSend);
            httpExchange.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = httpExchange.getResponseBody();
            try (var writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                writer.write(toSend);
            }
        }

        private TelegramFile getFile(String fileId) throws IOException, InterruptedException {
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("https://api.telegram.org/bot1728118655:AAEQOKogNSnI0WgkTNzpbpufH6LXi6HP6lQ/getFile?file_id=" + fileId))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.err.println(response.body());
            Gson g = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
            TelegramGetFileResponce parsedResponce = g.fromJson(response.body(), TelegramGetFileResponce.class);
            System.err.println(parsedResponce.result);
            return g.fromJson(parsedResponce.result, TelegramFile.class);
        }
    }
}

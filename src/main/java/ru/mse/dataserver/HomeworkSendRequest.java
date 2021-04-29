package ru.mse.dataserver;

import com.google.gson.annotations.SerializedName;

public class HomeworkSendRequest {
    public String subject;
    public String message;
    @SerializedName("file_id") public String fileId;
}

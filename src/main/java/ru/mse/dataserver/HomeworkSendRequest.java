package ru.mse.dataserver;

import com.google.gson.annotations.SerializedName;

public class HomeworkSendRequest {
    public String subject;
    @SerializedName("file_id") public String fileId;
}

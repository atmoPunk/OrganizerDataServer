package ru.mse.dataserver;

import com.google.gson.annotations.SerializedName;

public class TelegramGetFileResponce {
    @SerializedName("file_size") Integer fileSize;
    @SerializedName("file_path") String filePath;
}

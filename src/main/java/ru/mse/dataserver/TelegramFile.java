package ru.mse.dataserver;

import com.google.gson.annotations.SerializedName;

public class TelegramFile {
    @SerializedName("file_id") String fileId;
    @SerializedName("file_unique_id") String fileUniqueId;
    @SerializedName("file_size") Integer fileSize;
    @SerializedName("file_path") String filePath;
}

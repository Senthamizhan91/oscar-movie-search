package de.cyberport.core.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class TestDataAdapter {
    private static Gson gson = new Gson();

    public static JsonObject loadTestData(String filePath) throws FileNotFoundException {
        JsonObject dataSet = null;
        JsonElement jsonElement = new JsonParser().parse(new FileReader(filePath));
        dataSet = jsonElement.getAsJsonObject();
        return dataSet;
    }

    public static String getFilePath(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return TestConstants.RESOURCES_FOLDER + File.separator + fileName;
        }
        return org.apache.commons.lang3.StringUtils.EMPTY;
    }
}

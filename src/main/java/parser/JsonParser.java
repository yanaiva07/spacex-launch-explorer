package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import data.Launch;
import data.LaunchQueryResponse;
import exceptions.SpaceXApiException;

import java.util.List;
import java.lang.reflect.Type;

public class JsonParser {
    private final Gson gson;

    public JsonParser() {
        this.gson = new GsonBuilder().create();
    }

    //делаем из JSON строки список объектов Launch
    public List<Launch> parseLaunches(String json) throws SpaceXApiException {

        if (json == null || json.trim().isEmpty()) {
            throw new SpaceXApiException("Empty or null JSON string received from server");
        }
        try {
            try {
                LaunchQueryResponse wrapper =
                        gson.fromJson(json, LaunchQueryResponse.class);
                if (wrapper != null && wrapper.getDocs() != null) {
                    return wrapper.getDocs();   //возвращаем docs
                }
            } catch (Exception ignored) {
                //если это не объект — пробуем как массив
            }

            //если это не объект - значит это массив (ответ от GET)
            Type listType = new TypeToken<List<Launch>>(){}.getType();
            return gson.fromJson(json, listType);

        } catch (JsonSyntaxException e) {
            throw new SpaceXApiException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }


    //делает из JSON строки один объект Launch
    public Launch parseLaunch(String json) throws SpaceXApiException {

        if (json == null || json.trim().isEmpty()) {
            throw new SpaceXApiException("Empty or null JSON string received from server");
        }

        try {
            return gson.fromJson(json, Launch.class);

        } catch (JsonSyntaxException e) {
            System.err.println("Ошибка парсинга объекта: " + e.getMessage());
            throw new SpaceXApiException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
}
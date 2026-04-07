package client;

import cashe.CacheManager;
import exceptions.SpaceXApiException;



import java.io.*;

//для отправки запросов к серверу
import java.net.HttpURLConnection;

//чтобы строку "https://api.spacexdata.com" превращать в спец объяект URL
import java.net.URL;
import java.nio.charset.StandardCharsets;




public class SpaceXHttpClient {

    private static final String BASE_URL = "https://api.spacexdata.com";


    //private static final String BASE_URL = "http://localhost:8080";
    private static final int TIMEOUT = 60000 ;


    private final CacheManager cacheManager = new CacheManager();

    //endpoint  - часть URL после BASE_URL
    public String get(String endpoint, String cacheKey) throws SpaceXApiException {
        HttpURLConnection connection = null;

        if (cacheManager.hasValidCache(cacheKey)) {
            return cacheManager.readCache(cacheKey);
        }

        try {
            //собрали весь url
            //сделали url = "http://localhost:8080/v5/launches"
            URL url = new URL(BASE_URL + endpoint);

            //открыли соединение по адресу
            //сделали стандартное Http соединение
            connection = (HttpURLConnection) url.openConnection();

            //хочу получить данные
            connection.setRequestMethod("GET");

            connection.setConnectTimeout(TIMEOUT);

            connection.setReadTimeout(TIMEOUT);

            //получаю HTTP статус ответа
            int statusCode = connection.getResponseCode();

            //читаем доп данные моим же методом (тело ответа)
            String serverAnswer = readAnswer(connection, statusCode);


            if (statusCode != HttpURLConnection.HTTP_OK) {
                //кидаем исключение
                //передаем в конструктор:
                throw new SpaceXApiException(
                        "HTTP error: " + statusCode,  //сообщение
                        statusCode,                    //код ошибки
                        serverAnswer                   //ответ сервера
                );
            }


            cacheManager.writeCache(cacheKey, serverAnswer);

            //вернем нашу JSON строку
            return serverAnswer;
        } catch (IOException e) {

            if (cacheManager.hasValidCache(cacheKey)) {
                System.out.println("[!] Сервер недоступен. Показаны данные из кеша.");
                return cacheManager.readCache(cacheKey);
            }

            throw new SpaceXApiException("Network error: " + e.getMessage(), e);
        } finally {

            if (connection != null) {

                connection.disconnect();
            }
        }
    }



    //для чтения ответа сервера
    private String readAnswer(HttpURLConnection connection, int statusCode) throws IOException {

        //если ошибка (>=400) то берем поток-ошибку, иначе берем поток-успех
        InputStream is = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (is == null) {
            return "";
        }

        //InputStreamReader - превращает байты в символы (с учетом кодировки)
        //StandardCharsets.UTF_8 - кодировка UTF-8 (стандарт для JSON)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            //читаем построчно
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            //делаем обычной строкой
            return response.toString();
        }
    }




    //отправка данных на сервер
    public String post(String endpoint, String jsonBody, String cacheKey) throws SpaceXApiException {
        HttpURLConnection connection = null;

        if (cacheManager.hasValidCache(cacheKey)) {
            return cacheManager.readCache(cacheKey);
        }

        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            //устанавливаю HTTP заголовок Content-Type
            //я говорю серверу какой тип данных отправляю
            //application/json
            //без этого заголовка сервер может не понять наш запрос
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true); // разрешаем отправку тела

            //отправляем тело запроса
            try (OutputStream os = connection.getOutputStream()) {
                //превращаем JSON строку в массив байтов в кодировке UTF-8
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);

                //данные, с какой позиции начать, сколько записать
                os.write(input, 0, input.length);

            }


            int statusCode = connection.getResponseCode();
            String serverAnswer = readAnswer(connection, statusCode);

            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new SpaceXApiException("HTTP error: " + statusCode, statusCode, serverAnswer);
            }


            cacheManager.writeCache(cacheKey, serverAnswer);

            return serverAnswer;
        } catch (IOException e) {

            if (cacheManager.hasValidCache(cacheKey)) {
                System.out.println("[!] Сервер недоступен. Показаны данные из кеша.");
                return cacheManager.readCache(cacheKey);
            }

            throw new SpaceXApiException("Network error: " + e.getMessage(), e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

}

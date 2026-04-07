package builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import data.Launch; //если нужно для сериализации

public class JsonBuilder {

    //gson — объект который будет превращать объекты Java в JSON
    private final Gson gson;

    public JsonBuilder() {
        this.gson = new GsonBuilder().create();
    }

    //сериализует объект Launch в JSON
    public String toJson(Launch launch) {
        return gson.toJson(launch);
    }

    //сериализует список Launch в JSON
    public String toJson(java.util.List<Launch> launches) {
        return gson.toJson(launches);
    }


    //строим запрос по дате
    public String buildDateQuery(String startDate, String endDate) {
        //объект в котором храню условия фильтрации
        JsonObject query = new JsonObject();
        JsonObject dateUtc = new JsonObject();

        //пример: "$gte": "2020-01-01" - значит дата должна быть >= 2020-01-01
        dateUtc.addProperty("$gte", startDate); //$gte = >= (больше или равно)

        dateUtc.addProperty("$lte", endDate); //$lte = <= (меньше или равно)


        //добавим в объект query свойство date_utc со значением dateUtc
        query.add("date_utc", dateUtc);

        //сортировка или пагинация
        JsonObject options = new JsonObject();

        //сортируем по номеру полета
        options.addProperty("sort", "flight_number");

        //как сортировать по убыванию или по возрастанию - по возрастанию
        options.addProperty("order", "asc");

        //отключаем пагинацию у API, чтобы получить все документы в поле docs
        //по умолчанию API возвращает только первые 10-20 результатов
        //c pagination: false API вернет ВСЕ результаты в поле docs
        //это нужно чтобы получить полный список
        options.addProperty("pagination", false);

        //root содержит весь запрос целиком
        JsonObject root = new JsonObject();

        //добавляем в объект поле "query" со значением query
        root.add("query", query);

        //добавляем в объект поле "options" со значением options
        root.add("options", options);


        //превращаем объект в JSON-строку и возвращаем
        //gson.toJson(root) - сериализует root в строку
        return gson.toJson(root);
    }




    //строим JSON-запросы для фильтрации по успешности
    public String buildSuccessQuery(boolean success) {

        //условия запроса
        JsonObject query = new JsonObject();

        //найти все запуски, у которых поле success равно true
        query.addProperty("success", success);


        //исключаем предстоящие запуски (upcoming = false)
        //нам нужны только состоявшиеся запуски с определенным результатом

        query.addProperty("upcoming", false);

        JsonObject options = new JsonObject();

        //сортируем по номеру полета
        options.addProperty("sort", "flight_number");

        //по возрастанию
        options.addProperty("order", "asc");

        //отключаем пагинацию у API чтобы получить все документы в поле docs
        //чтобы получить все результаты
        options.addProperty("pagination", false);

        JsonObject root = new JsonObject();
        root.add("query", query);
        root.add("options", options);
        return gson.toJson(root);
    }

}
package exceptions;

public class SpaceXApiException extends Exception {
    //код HTTP статуса
    private final int statusCode;

    //тело ответа сервера
    // может содержать доп инфу об ошибке
    private final String serverAnswer;

    //конструктор - когда сервер вернул ошибку
    public SpaceXApiException(String textError, int statusCode, String serverAnswer) {
        super(textError);
        this.statusCode = statusCode;
        this.serverAnswer = serverAnswer;
    }

    //конструктор - для сетевых ошибок
    public SpaceXApiException(String message, Throwable cause) {
        //super(message, cause) - вызываем конструктор родителя Exception
        //который принимает сообщение и причину (оригинальное исключение)
        //сохраняем стек вызовов оригинальной ошибки
        super(message, cause);

        //нет кода поставим -1  "неизвестно"
        this.statusCode = -1; // это HTTP ошибка

        this.serverAnswer = null;
    }


    //конструктор - для всех ошибок
    public SpaceXApiException(String message) {
        super(message);
        this.statusCode = -1;
        this.serverAnswer = null;
    }

    //чтобы показывать код ошибки
    public int getStatusCode() {
        return statusCode;
    }

    //инфа об ошибке (доп.инфа)
    public String getServerAnswer() {
        return serverAnswer;
    }
}


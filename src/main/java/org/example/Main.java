package org.example;


import builder.JsonBuilder;
import cashe.CacheKey;
import cashe.CacheManager;
import client.SpaceXHttpClient;
import data.Launch;
import exceptions.SpaceXApiException;
import parser.JsonParser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;


public class Main {
    private static final SpaceXHttpClient httpClient = new SpaceXHttpClient();
    private static final JsonParser jsonParser = new JsonParser();
    private static final Scanner scanner = new Scanner(System.in);

    private static final JsonBuilder jsonBuilder = new JsonBuilder();
    private static final CacheManager cacheManager = new CacheManager();

    public static void main(String[] args) {
        System.out.println("=== SpaceX Launch Explorer ===");

        while (true) {
            printMenu();
            int choice = readIntInput();

            try {
                switch (choice) {
                    case 1:
                        showAllLaunches();
                        break;
                    case 2:
                        showLatestLaunch();
                        break;
                    case 3:
                        searchByDate();
                        break;
                    case 4:
                        showSuccessLaunches(true);
                        break;
                    case 5:
                        showSuccessLaunches(false);
                        break;
                    case 6:
                        cacheManager.clearCache();
                        System.out.println("Кеш очищен!");
                        break;
                    case 7:
                        System.out.println("До свидания!");
                        return;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (SpaceXApiException e) {
                System.err.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Непредвиденная ошибка: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("\nНажмите Enter для продолжения...");
            scanner.nextLine();
        }
    }

    private static void printMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("1. Показать все запуски");
        System.out.println("2. Показать последний запуск");
        System.out.println("3. Поиск запусков по дате");
        System.out.println("4. Показать только успешные запуски");
        System.out.println("5. Показать только неудачные запуски");
        System.out.println("6. Очистить кеш");
        System.out.println("7. Выход");
        System.out.print("Выберите пункт (1-7): ");
    }


    private static int readIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    private static void showAllLaunches() throws SpaceXApiException {
        System.out.println("\nЗагрузка данных... (это может занять 2-3 минуты)");

        long startTime = System.currentTimeMillis();
        String json = httpClient.get("/v5/launches", CacheKey.forAllLaunches());
        long endTime = System.currentTimeMillis();

        System.out.println("Загрузка завершена за " + (endTime - startTime) / 1000 + " секунд");
        System.out.println("Размер полученных данных: " + (json.length() / 1024) + " KB");

        System.out.println("Парсинг данных...");
        List<Launch> launches = jsonParser.parseLaunches(json);

        if (launches == null || launches.isEmpty()) {
            System.out.println("Не удалось получить список запусков или список пуст.");
            return;
        }

        System.out.println("\n=== Все запуски ===");

        for (Launch launch : launches) {
            System.out.println(launch);
        }

        System.out.println("\nВсего запусков: " + launches.size());
    }


    private static void showLatestLaunch() throws SpaceXApiException {
        System.out.println("\nЗагрузка данных...");

        long startTime = System.currentTimeMillis();
        String json = httpClient.get("/v5/launches/latest", CacheKey.forLatestLaunch());
        long endTime = System.currentTimeMillis();

        System.out.println("Загрузка завершена за " + (endTime - startTime) / 1000 + " секунд");

        Launch launch = jsonParser.parseLaunch(json);

        System.out.println("\n=== Последний запуск ===");
        System.out.println(launch.toDetailedString());
    }

    private static void searchByDate() throws SpaceXApiException {
        System.out.print("Введите дату начала (YYYY-MM-DD): ");
        String start = scanner.nextLine().trim();
        System.out.print("Введите дату конца (YYYY-MM-DD): ");
        String end = scanner.nextLine().trim();

        LocalDate startDate = parseDateOrNull(start);
        LocalDate endDate = parseDateOrNull(end);

        if (startDate == null || endDate == null) {
            System.out.println("Неверная дата. Используйте формат YYYY-MM-DD.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            System.out.println("Неверный диапазон дат: дата начала позже даты конца.");
            return;
        }

        String queryBody = jsonBuilder.buildDateQuery(start, end);
        System.out.println("\nЗагрузка данных...");
        String json = httpClient.post("/v5/launches/query", queryBody, CacheKey.forDateRange(start, end));
        List<Launch> launches = jsonParser.parseLaunches(json);
        displayLaunches(launches);
    }

    private static LocalDate parseDateOrNull(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }


    private static void showSuccessLaunches(boolean success) throws SpaceXApiException {
        String queryBody = jsonBuilder.buildSuccessQuery(success);
        System.out.println("\nЗагрузка данных...");
        String json = httpClient.post("/v5/launches/query", queryBody, CacheKey.forSuccess(success));
        List<Launch> launches = jsonParser.parseLaunches(json);
        System.out.println("\n=== " + (success ? "Успешные" : "Неудачные") + " запуски ===");
        displayLaunches(launches);
    }


    private static void displayLaunches(List<Launch> launches) {
        if (launches == null || launches.isEmpty()) {
            System.out.println("Нет запусков, удовлетворяющих критериям.");
            return;
        }
        for (Launch l : launches) {
            System.out.println(l);
        }
        System.out.println("Всего: " + launches.size());
    }

}


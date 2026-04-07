import java.net.*;
import java.io.*;

public class SimpleHttpTest {
    public static void main(String[] args) {
        try {
            System.out.println("Пробуем подключиться...");
            URL url = new URL("https://api.spacexdata.com/v5/launches/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int code = conn.getResponseCode();
            System.out.println("HTTP Status: " + code);

            if (code == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine();
                System.out.println("Первая строка ответа: " + line.substring(0, Math.min(200, line.length())));
                reader.close();
                System.out.println("✅ Успех! Java может подключиться к API");
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
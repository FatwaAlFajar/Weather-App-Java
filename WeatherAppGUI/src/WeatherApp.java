import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApp {
    
    // Mengambil data cuaca untuk lokasi yang diberikan
    public static JSONObject getWeatherData(String locationName){
        // Mendapatkan data lokasi (koordinat) berdasarkan nama lokasi
        JSONArray locationData = getLocationData(locationName);
        if (locationData == null || locationData.isEmpty()) {
            return null; // Jika data lokasi tidak ditemukan, kembalikan null
        }

        // Ekstrak data lintang dan bujur dari data lokasi
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // Membangun URL permintaan API dengan koordinat lokasi dan parameter cuaca
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FLos_Angeles";

        try{
            // Memanggil API dan mendapatkan respon
            HttpURLConnection conn = fetchApiResponse(urlString);
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Gagal Terkoneksi Dengan API");
                return null;
            }

            // Membaca respon JSON dari API
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            // Mengurai respon JSON
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // Mendapatkan data per jam
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            // Mendapatkan indeks waktu saat ini dalam data per jam
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // Mengambil data suhu berdasarkan indeks waktu saat ini
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // Mengambil kode cuaca berdasarkan indeks waktu saat ini
            JSONArray weathercode = (JSONArray) hourly.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // Mengambil data kelembapan berdasarkan indeks waktu saat ini
            JSONArray relativeHumidity = (JSONArray) hourly.get("relativehumidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // Mengambil data kecepatan angin berdasarkan indeks waktu saat ini
            JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            // Membangun objek JSON untuk data cuaca
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData; // Mengembalikan data cuaca dalam bentuk JSON
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // Mengambil data koordinat geografis untuk nama lokasi yang diberikan
    public static JSONArray getLocationData(String locationName){
        // Mengganti spasi pada nama lokasi dengan + untuk mengikuti format permintaan API
        locationName = locationName.replaceAll(" ", "+");

        // Membangun URL API dengan parameter lokasi
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            // Memanggil API dan mendapatkan respon
            HttpURLConnection conn = fetchApiResponse(urlString);
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Gagal Terkoneksi Dengan API");
                return null;
            }else{
                // Membaca respon JSON dari API
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();
                conn.disconnect();

                // Mengurai respon JSON
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // Mendapatkan daftar data lokasi yang dihasilkan API
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // Membuat koneksi dan mengambil respon API berdasarkan URL yang diberikan
    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        return null;
    }

    // Menemukan indeks waktu saat ini dalam daftar waktu yang diberikan
    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                return i;
            }
        }
        return 0; // Jika tidak ditemukan, kembalikan indeks 0
    }

    // Mendapatkan waktu saat ini dalam format yang sesuai dengan API
    private static String getCurrentTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        return currentDateTime.format(formatter);
    }

    // Mengonversi kode cuaca menjadi deskripsi yang lebih mudah dibaca
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            weatherCondition = "Cerah";
        }else if(weathercode > 0L && weathercode <= 3L){
            weatherCondition = "Berawan";
        }else if((weathercode >= 51L && weathercode <= 67L) || (weathercode >= 80L && weathercode <= 99L)){
            weatherCondition = "Hujan";
        }else if(weathercode >= 71L && weathercode <= 77L){
            weatherCondition = "Salju";
        }
        return weatherCondition;
    }
}

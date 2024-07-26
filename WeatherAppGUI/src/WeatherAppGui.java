import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui(){
        // mengatur antarmuka pengguna dan menambahkan judul
        super("Weather App");

        // mengkonfigurasi antarmuka pengguna untuk mengakhiri proses program setelah ditutup
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // mengatur ukuran antarmuka pengguna (dalam piksel)
        setSize(450, 650);

        // memuat antarmuka pengguna di tengah layar
        setLocationRelativeTo(null);

        // membuat layout manager menjadi null untuk memposisikan komponen secara manual di dalam antarmuka pengguna
        setLayout(null);

        // mencegah perubahan ukuran antarmuka pengguna
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents(){
        // bidang teks pencarian
        JTextField searchTextField = new JTextField();

        // mengatur lokasi dan ukuran komponen
        searchTextField.setBounds(15, 15, 351, 45);

        // mengubah gaya dan ukuran font
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        // gambar cuaca
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/clear.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // teks suhu
        JLabel temperatureText = new JLabel("0 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // meratakan teks di tengah
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // deskripsi kondisi cuaca
        JLabel weatherConditionDesc = new JLabel(" ");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // gambar Kelembaban
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // teks Kelembaban
        JLabel humidityText = new JLabel("<html><b>Kelembaban</b> 0%</html>");
        humidityText.setBounds(90, 500, 95, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // gambar kecepatan angin
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // teks kecepatan angin
        JLabel windspeedText = new JLabel("<html><b>Kec.Angin\n</b>    0km/h</html>");
        windspeedText.setBounds(310, 500, 90, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // tombol pencarian
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // mengubah kursor menjadi kursor tangan saat mengarahkan ke tombol ini
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // mendapatkan lokasi dari pengguna
                String userInput = searchTextField.getText();

                // memvalidasi input - menghapus spasi untuk memastikan teks tidak kosong
                if(userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }

                // mengambil data cuaca
                weatherData = WeatherApp.getWeatherData(userInput);

                // memperbarui antarmuka pengguna

                // memperbarui gambar cuaca
                String weatherCondition = (String) weatherData.get("weather_condition");

                // tergantung pada kondisi, kita akan memperbarui gambar cuaca yang sesuai dengan kondisi tersebut
                switch(weatherCondition){
                    case "Cerah":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Berawan":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Hujan":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Salju":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.png"));
                        break;
                }

                // memperbarui teks suhu
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " C");

                // memperbarui teks kondisi cuaca
                weatherConditionDesc.setText(weatherCondition);

                // memperbarui teks Kelembaban
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Kelembaban</b> " + humidity + "%</html>");

                // memperbarui teks kecepatan angin
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Kec.Angin\n</b>" + windspeed + "km/h</html>");
            }
        });
        add(searchButton);
    }

    // digunakan untuk membuat gambar dalam komponen antarmuka pengguna kita
    private ImageIcon loadImage(String resourcePath){
        try{
            // membaca file gambar dari jalur yang diberikan
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // mengembalikan ikon gambar sehingga komponen kita dapat merendernya
            return new ImageIcon(image);
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Tidak dapat menemukan sumber daya");
        return null;
    }
}

package com.example.projectethreads1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_refresh = (Button) findViewById(R.id.button);
        TextView text_value = (TextView) findViewById(R.id.textView);
        ImageView changingImage = (ImageView) findViewById(R.id.imageView);



        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExecutorService executor = Executors.newSingleThreadExecutor();

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // Tasques en background (xarxa)

                        // consegui JSON string de API
                        error = getDataFromUrl("https://randomfox.ca/floof/");
                        // url de la imatge
                        String urldisplay = getImageURL(error);
                        try {
                            InputStream in = new java.net.URL(urldisplay).openStream();
                            bitmap = BitmapFactory.decodeStream(in);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Tasques a la interfície gràfica (GUI)
                                changingImage.setImageBitmap(bitmap);
                                text_value.setText(error);
                            }
                        });
                    }
                });
            }
        });

    }

    String error = ""; // string field
    private String getDataFromUrl(String demoIdUrl) {

        String result = null;
        int resCode;
        InputStream in;
        try {
            URL url = new URL(demoIdUrl);
            URLConnection urlConn = url.openConnection();

            HttpsURLConnection httpsConn = (HttpsURLConnection) urlConn;
            httpsConn.setAllowUserInteraction(false);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.setRequestMethod("GET");
            httpsConn.connect();
            resCode = httpsConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpsConn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = sb.toString();
            } else {
                error += resCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return result;
    }

    private String getImageURL(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String ImageURL = jsonObject.getString("image");
            return ImageURL;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
package com.admin.claire.myweather.Helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by claire on 2017/6/14.
 */

public class Helper {
    static String stream = null;

    public Helper() {
    }

    public String getHttpData(String urlString){
        try {
            URL url = new URL(urlString);

            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

            if (httpURLConnection.getResponseCode() == 200) //OK -200 連線成功
            {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine())!= null)
                    sb.append(line);
                stream = sb.toString();

                httpURLConnection.disconnect();
            }

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return stream;
    }
}

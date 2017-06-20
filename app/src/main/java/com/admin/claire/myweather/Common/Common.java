package com.admin.claire.myweather.Common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by claire on 2017/6/13.
 */
//城市名查尋 http://api.openweathermap.org/data/2.5/weather?q=Taipei&appid=04474e9c2eb9c1c9434da7c032db5959
//座標 http://api.openweathermap.org/data/2.5/weather?lat=25.05&lon=121.53&appid=04474e9c2eb9c1c9434da7c032db5959

public class Common {
    public static String API_KEY = "04474e9c2eb9c1c9434da7c032db5959";
    public static String API_LINK = "http://api.openweathermap.org/data/2.5/weather";

    public static String apiRequest(String lat, String lng){
        StringBuffer sb = new StringBuffer(API_LINK);
        sb.append(String.format("?lat=%s&lon=%s&APPID=%s&units=metric",lat,lng,API_KEY));
        return sb.toString();
    }

    public static String apiRequestCity(String city){
        StringBuffer sb = new StringBuffer(API_LINK);
        sb.append(String.format("?q=%s&APPID=%s&units=metric",city,API_KEY));
        return sb.toString();
    }

    public static String unixTimeStampToDateTime (double unixTimeStamp){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        date.setTime((long)unixTimeStamp * 1000);
        return dateFormat.format(date);
    }

    public static String getImg(String icon){
        return String.format("http://openweathermap.org/img/w/%s.png",icon);
    }

    public static String getDateNow(){
        DateFormat dateFormat = new SimpleDateFormat("MMMMdd yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }
}

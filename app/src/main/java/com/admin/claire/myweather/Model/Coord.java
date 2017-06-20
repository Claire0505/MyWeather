package com.admin.claire.myweather.Model;

/**
 * Created by claire on 2017/6/14.
 */

public class Coord { //座標

    private double lat;
    private double lon;

    public Coord(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}

package com.example.projectosa.data;

import com.google.android.gms.maps.model.LatLng;

public class Geofence {
    private String name;
    private float latitude, longitude, radius;

    public LatLng getLatLng(){
        return new LatLng(latitude,longitude);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}

package com.example.projectosa.data;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Geofence {
    private String name;
    private double latitude, longitude, radius;

    // Métodos úteis
    public LatLng getLatLng(){
        return new LatLng(latitude,longitude);
    }

    /**
     *Método para verificar se as coordenadas estão dentro da geovedação
     */
    public boolean isInside(double latitude, double longitude) {
        Location inputLocation = new Location(name);
        inputLocation.setLatitude(latitude);
        inputLocation.setLongitude(longitude);
        Location geofenceCenter = new Location(name);
        geofenceCenter.setLatitude(this.latitude);
        geofenceCenter.setLongitude(this.longitude);

        // Calculan a distância entre as duas localizações (centro da geofence e a localização do utilizador)
        float distance = inputLocation.distanceTo(geofenceCenter);

        // Verificando se a distância é menor que o raio da geovedação (por outras palavras, se a localização do user está dentro da geovedação)
        return distance <= radius;
    }

    /**
     * Método para verificar se as coordenadas estão dentro da geovedação
     */
    public boolean isInside(Location location){
        return isInside(location.getLatitude(), location.getLongitude());
    }

    /**
     * Verifica se uma localização está dentro de pelo menos uma das geovedações fornecidas.
     */
    public static boolean insideOfGeofences(Iterable<Geofence> geofences, double latitude, double longitude){
        for (Geofence geofence: geofences) {
            if (geofence.isInside(latitude,longitude)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se uma localização está dentro de pelo menos uma das geovedações fornecidas.
     */
    public static boolean insideOfGeofences(Iterable<Geofence> geofences, Location location){
        return insideOfGeofences(geofences, location.getLatitude(), location.getLongitude());
    }

    /**
     * Verifica se uma localização está dentro de pelo menos uma das geovedações fornecidas.
     */
    public static boolean insideOfGeofences(Iterable<Geofence> geofences, LatLng location){
        return insideOfGeofences(geofences, location.latitude, location.longitude);
    }

    /**
     * Método para verificar se as coordenadas estão dentro da geovedação
     */
    public boolean isInside(LatLng latlng){
        return isInside(latlng.latitude, latlng.longitude);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}

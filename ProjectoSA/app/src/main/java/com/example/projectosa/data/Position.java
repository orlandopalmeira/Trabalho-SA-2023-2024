package com.example.projectosa.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Position {
    private double latitude, longitude;
    private LocalDateTime timestamp;

    private String idTrabalhador;

    Position(){}

    public Position(double latitude, double longitude, LocalDateTime timestamp, String idTrabalhador) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.idTrabalhador = idTrabalhador;
    }

    public Position(double latitude, double longitude, LocalDateTime timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.idTrabalhador = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Position(LatLng latlng){
        this.latitude = latlng.latitude;
        this.longitude = latlng.longitude;
        this.timestamp = LocalDateTime.now();
        this.idTrabalhador = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    // Método para converter uma instância da classe Position em um Map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", this.latitude);
        map.put("longitude", this.longitude);
        map.put("timestamp", this.timestamp.toString());
        map.put("idTrabalhador", this.idTrabalhador);
        return map;
    }

    // Método para converter um Map em uma instância da classe Position
    public static Position fromMap(Map<String, Object> map) {
        double latitude = (double) map.get("latitude");
        double longitude = (double) map.get("longitude");
        LocalDateTime timestamp = LocalDateTime.parse((String)map.get("timestamp"));
        String idTrabalhador = (String)map.get("idTrabalhador");
        return new Position(latitude, longitude, timestamp, idTrabalhador);
    }

}
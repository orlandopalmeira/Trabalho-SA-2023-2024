package com.example.projectosa.data;

import com.example.projectosa.state.EstadoApp;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Position {
    public static final String VIAGEM = "viagem", PEDONAL = "pedonal";
    private double latitude, longitude;
    private LocalDateTime timestamp;
    private String idTrabalhador;
    private String username;
    private String tipoTrabalho;
    private String viagemID;
    Position(){}

    public Position(double latitude, double longitude, LocalDateTime timestamp, String idTrabalhador, String username, String tipoTrabalho, String viagemID) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.idTrabalhador = idTrabalhador;
        this.username = username;
        this.tipoTrabalho = tipoTrabalho;
        this.viagemID = viagemID;
    }
    public Position(double latitude, double longitude, LocalDateTime timestamp, String idTrabalhador, String username) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.idTrabalhador = idTrabalhador;
        this.username = username;
        this.tipoTrabalho = PEDONAL;
        this.viagemID = "";
    }
    public Position(double latitude, double longitude, LocalDateTime timestamp, String idTrabalhador) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.idTrabalhador = idTrabalhador;
        this.username = EstadoApp.getUsername();
        this.tipoTrabalho = PEDONAL;
        this.viagemID = "";
    }

    public Position(LatLng latlng){
        this.latitude = latlng.latitude;
        this.longitude = latlng.longitude;
        this.timestamp = LocalDateTime.now();
        this.idTrabalhador = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.username = EstadoApp.getUsername();
        this.tipoTrabalho = PEDONAL;
        this.viagemID = "";
    }

    public String getTipoTrabalho() {
        return tipoTrabalho;
    }

    public void setTipoTrabalho(String tipoTrabalho) {
        this.tipoTrabalho = tipoTrabalho;
    }

    public String getViagemID() {
        return viagemID;
    }

    public void setViagemID(String viagemID) {
        this.viagemID = viagemID;
    }

    // Método para converter uma instância da classe Position num Map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("latitude", this.latitude);
        map.put("longitude", this.longitude);
        map.put("timestamp", this.timestamp.toString());
        map.put("idTrabalhador", this.idTrabalhador);
        map.put("username", this.username);
        map.put("tipoTrabalho", this.tipoTrabalho);
        map.put("viagemID", this.viagemID);
        return map;
    }

    // Método para converter um Map numa instância da classe Position
    public static Position fromMap(Map<String, Object> map) {
        double latitude = (double) map.get("latitude");
        double longitude = (double) map.get("longitude");
        LocalDateTime timestamp = LocalDateTime.parse((String)map.get("timestamp"));
        String idTrabalhador = (String)map.get("idTrabalhador");
        String username = (String)map.get("username");
        String tipoTrabalho = (String)map.get("tipoTrabalho");
        String viagemID = (String)map.get("viagemID");
        return new Position(latitude, longitude, timestamp, idTrabalhador,username, tipoTrabalho, viagemID);
    }

}

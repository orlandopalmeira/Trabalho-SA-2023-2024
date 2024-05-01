package com.example.projectosa.state;

import android.os.SystemClock;
import android.util.Log;

import com.example.projectosa.data.Database;
import com.example.projectosa.data.Geofence;
import com.example.projectosa.data.Position;
import com.example.projectosa.data.WorkTime;
import com.example.projectosa.utils.Observer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class EstadoApp {
    // Nomes dos estados da aplicação
    public static final int DESLIGADO = 0;
    public static final int FORA_DA_AREA = 1;
    public static final int DENTRO_AREA_PARADO = 2;
    public static final int DENTRO_AREA_EM_MOVIMENTO = 3;

    // Estado da aplicação
    private static int currentState = DESLIGADO; // estado da monitorização
    private static final List<Observer<Integer>> observersEstado = new ArrayList<>(); // Observadores do estado da aplicação
    private static Float oldXacc = 0.0f, oldYacc = 0.0f, oldZacc = 0.0f; // valores do acelerómetro anteriores
    private static Long milissegundosDeTrabalho = 0L; // Tempo de trabalho útil (em movimento e dentro da geovedação) contabilizado.
    private static long startTime = 0;
    private static final List<Observer<Long>> observersSegundosTrabalho = new ArrayList<>();
    private static boolean moving = false; // indica se o telemóvel está em movimento
    private static List<Geofence> geofences = new ArrayList<>(); // geovedações registadas no sistema
    private static LatLng currentLocation = null; // localização actual
    private static final List<Observer<LatLng>> locationObservers = new ArrayList<>(); // observadores da localização
    private static boolean insideGeofences = false;

    private EstadoApp() {} // Esta classe não foi feita para ser instanciada.

    /**
     * Actualiza a localização actual
     */
    public static void setCurrentLocation(LatLng newLocation) {
        currentLocation = newLocation;
        insideGeofences = Geofence.insideOfGeofences(geofences, currentLocation);
        if(currentState != DESLIGADO){ // Só regista a localização se o utilizador ligou a monitorização
            Database.addPosition(new Position(newLocation)).addOnFailureListener(e -> {
                try { throw e; }
                catch (Exception ex) { throw new RuntimeException(ex); }
            });
        }
        for (Observer<LatLng> observer : locationObservers) {
            observer.onVariableChanged(currentLocation);
        }
    }

    public static LatLng getCurrentLocation(){
        return currentLocation != null ? currentLocation : new LatLng(0,0);
    }
    /**
     * Vai à base de dados buscar as geofences e coloca no estado da aplicação.
     */
    public static void fetchGeofences(){
        Task<List<Geofence>> taskGetGeofences = Database.getGeofences();

        taskGetGeofences.addOnSuccessListener(geofencesList -> {
            geofences = geofencesList;
        }).addOnFailureListener(e -> {
            Log.e("DEBUG", e.getMessage());
        });
    }

    /**
     * Devolve as geofences
     */
    public static List<Geofence> getGeofences(){
        return geofences;
    }

    public static WorkTime getWorkTimeData(){
        assert FirebaseAuth.getInstance().getCurrentUser() != null; // evita warnings
        return new WorkTime(FirebaseAuth.getInstance().getCurrentUser().getUid(), milissegundosDeTrabalho/1000);
    }

    /**
     * Reinicia o contador de tempo de trabalho
     */
    public static void restartWorkTime(){
        milissegundosDeTrabalho = 0L;
        for(Observer<Long> observer: observersSegundosTrabalho){
            observer.onVariableChanged(milissegundosDeTrabalho);
        }
    }

    /**
     * Altera o estado da aplicação tendo em conta a informação do acelerómetro e a verificação da presença do utilizador no interior da geovedação.
     */
    public static void updateAccelerometerData(Float x, Float y, Float z){
        double movement = Math.sqrt((x-oldXacc)*(x-oldXacc) + (y-oldYacc)*(y-oldYacc) + (z-oldZacc)*(z-oldZacc)); // "grau de movimento"

        if(currentState != EstadoApp.DESLIGADO){
            if (movement >= 0.75) { // Em movimento
                if (insideGeofences) { // É aqui que o trabalhador está efectivamente a trabalhar
                    setDentroDaAreaEmMovimento();
                    if(!moving){
                        startTime = SystemClock.elapsedRealtime();
                        moving = true;
                    }
                } else {
                    updateSegundosDeTrabalho();
                    setForaDaArea();
                }
            } else { // Parado
                updateSegundosDeTrabalho();
                if (insideGeofences) {
                    setDentroDaAreaParado();
                } else {
                    setForaDaArea();
                }
            }
        }

        oldXacc = x; oldYacc = y; oldZacc = z; // Actualiza os valores do acelerómetro
    }
    public static void registerLocationObserver(Observer<LatLng> observer){
        locationObservers.add(observer);
    }
    public static void registerEstadoObserver(Observer<Integer> observer){
        observersEstado.add(observer);
    }
    public static void registerSegundosTrabalhoObserver(Observer<Long> observer){
        observersSegundosTrabalho.add(observer);
    }
    public static void setDesligado() {
        currentState = DESLIGADO;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(currentState);
        }
        startTime = 0;
        moving = false;
    }
    public static void setForaDaArea() {
        currentState = FORA_DA_AREA;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(currentState);
        }
    }
    public static void setDentroDaAreaParado() {
        currentState = DENTRO_AREA_PARADO;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(currentState);
        }
    }
    public static void setDentroDaAreaEmMovimento() {
        currentState = DENTRO_AREA_EM_MOVIMENTO;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(currentState);
        }
    }
    private static void updateSegundosDeTrabalho(){
        if(moving){
            long endTime = SystemClock.elapsedRealtime();
            milissegundosDeTrabalho += endTime - startTime;
            moving = false;
        }
        for(Observer<Long> observer: observersSegundosTrabalho){
            observer.onVariableChanged(milissegundosDeTrabalho);
        }
    }
}

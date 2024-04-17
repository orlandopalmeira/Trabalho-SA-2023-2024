package com.example.projectosa.state;

import android.os.SystemClock;
import android.util.Log;

import com.example.projectosa.data.Database;
import com.example.projectosa.data.Geofence;
import com.example.projectosa.data.WorkTime;
import com.example.projectosa.utils.Observer;
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
    private static int estado = DESLIGADO; // estado da monitorização
    private static final List<Observer<Integer>> observersEstado = new ArrayList<>(); // Observadores do estado da aplicação
    private static Float oldXacc = 0.0f, oldYacc = 0.0f, oldZacc = 0.0f; // valores do acelerómetro anteriores
    private static Long milissegundosDeTrabalho = 0L; // Tempo de trabalho útil (em movimento e dentro da geovedação) contabilizado.
    private static long startTime = 0;
    private static final List<Observer<Long>> observersSegundosTrabalho = new ArrayList<>();
    private static boolean moving = false; // indica se o telemóvel está em movimento
    private static List<Geofence> geofences = null; // geovedações registadas no sistema
    private static boolean insideGeofences = true; //TODO: indica se o utilizador está dentro de uma das geofences definidas
    private EstadoApp() {} // Esta classe não foi feita para ser instanciada.

    /**
     * Vai a base de dados buscar as geofences e coloca no estado da aplicação.
     */
    public static void fetchGeofences(){
        Task<List<Geofence>> taskGetGeofences = Database.getGeofences();

        taskGetGeofences.addOnSuccessListener(geofencesList -> {
            geofences = geofencesList;
        }).addOnFailureListener(e -> {
            Log.e("EstadoApp", e.getMessage());
        });
    }

    public static WorkTime getWorkTimeData(){
        assert FirebaseAuth.getInstance().getCurrentUser() != null; // evita warnings
        return new WorkTime(FirebaseAuth.getInstance().getCurrentUser().getUid(), milissegundosDeTrabalho/1000);
    }
    /**
     * Altera o estado da aplicação tendo em conta a informação do acelerómetro e a verificação da presença do utilizador no interior da geovedação.
     */
    public static void updateAccelerometerData(Float x, Float y, Float z){
        double movement = Math.sqrt((x-oldXacc)*(x-oldXacc) + (y-oldYacc)*(y-oldYacc) + (z-oldZacc)*(z-oldZacc)); // "grau de movimento"

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

        oldXacc = x; oldYacc = y; oldZacc = z; // Actualiza os valores do acelerómetro
    }
    public static void registerEstadoObserver(Observer<Integer> observer){
        observersEstado.add(observer);
    }
    public static void registerSegundosTrabalhoObserver(Observer<Long> observer){
        observersSegundosTrabalho.add(observer);
    }
    public static void setDesligado() {
        estado = DESLIGADO;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(estado);
        }
    }
    public static void setForaDaArea() {
        estado = FORA_DA_AREA;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(estado);
        }
    }
    public static void setDentroDaAreaParado() {
        estado = DENTRO_AREA_PARADO;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(estado);
        }
    }
    public static void setDentroDaAreaEmMovimento() {
        estado = DENTRO_AREA_EM_MOVIMENTO;
        for (Observer<Integer> observer: observersEstado) {
            observer.onVariableChanged(estado);
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

package com.example.projectosa.state;

import android.os.SystemClock;

import com.example.projectosa.data.WorkTime;
import com.example.projectosa.utils.Observer;

import java.util.ArrayList;
import java.util.List;

public class EstadoApp {
    private static String estado = "DESLIGADO"; // estado da monitorização
    private static final List<Observer<String>> observersEstado = new ArrayList<>(); // Observadores do estado da aplicação
    private static Float oldXacc = 0.0f, oldYacc = 0.0f, oldZacc = 0.0f; // valores do acelerómetro anteriores
    private static Long milissegundosDeTrabalho = 0L; // Tempo de trabalho útil (em movimento e dentro da geovedação) contabilizado.
    private static long startTime = 0;
    private static final List<Observer<Long>> observersSegundosTrabalho = new ArrayList<>();

    private static boolean moving = false; // indica se o telemóvel está em movimento

    private EstadoApp() {} // Esta classe não foi feita para ser instanciada.

    public static WorkTime getWorkTimeData(){
        //TODO: Tratar da parte do id do trabalhador
        return new WorkTime("TODO", milissegundosDeTrabalho/1000);
    }
    /**
     * Altera o estado da aplicação tendo em conta a informação do acelerómetro e a verificação da presença do utilizador no interior da geovedação.
     */
    public static void updateAccelerometerData(Float x, Float y, Float z){
        double movement = Math.sqrt((x-oldXacc)*(x-oldXacc) + (y-oldYacc)*(y-oldYacc) + (z-oldZacc)*(z-oldZacc)); // "grau de movimento"
        boolean dentroDaGeovedacao = true; //TODO: Verificar se se encontra dentro da geovedação

        if (movement >= 0.75) { // Em movimento
            if (dentroDaGeovedacao) { // É aqui que o trabalhador está efectivamente a trabalhar
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
            if (dentroDaGeovedacao) {
                setDentroDaAreaParado();
            } else {
                setForaDaArea();
            }
        }

        oldXacc = x; oldYacc = y; oldZacc = z; // Actualiza os valores do acelerómetro
    }
    public static void registerEstadoObserver(Observer<String> observer){
        observersEstado.add(observer);
    }
    public static void registerSegundosTrabalhoObserver(Observer<Long> observer){
        observersSegundosTrabalho.add(observer);
    }
    public static void setDesligado() {
        estado = "DESLIGADO";
        for (Observer<String> observer: observersEstado) {
            observer.onVariableChanged(estado);
        }
    }
    public static void setForaDaArea() {
        estado = "FORA_AREA_DE_TRABALHO";
        for (Observer<String> observer: observersEstado) {
            observer.onVariableChanged(estado);
        }
    }
    public static void setDentroDaAreaParado() {
        estado = "DENTRO_AREA_PARADO";
        for (Observer<String> observer: observersEstado) {
            observer.onVariableChanged(estado);
        }
    }
    public static void setDentroDaAreaEmMovimento() {
        estado = "DENTRO_AREA_EM_MOVIMENTO";
        for (Observer<String> observer: observersEstado) {
            observer.onVariableChanged(estado);
        }
    }
    // Outros métodos úteis
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

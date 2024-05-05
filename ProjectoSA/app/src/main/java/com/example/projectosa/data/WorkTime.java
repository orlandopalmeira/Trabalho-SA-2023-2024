package com.example.projectosa.data;

import com.example.projectosa.state.EstadoApp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class WorkTime implements Comparable<WorkTime>{
    public static final String VIAGEM = "viagem", PEDONAL = "pedonal";
    private String idTrabalhador;
    private String username;
    private LocalDateTime data; // data em que foi feito o registo
    private long segundosDeTrabalho;
    private String tipoTrabalho;
    private String viagemID;

    public WorkTime(){} // Necessário ao firebase

    public WorkTime(String idTrabalhador, String username, LocalDateTime today, long segundosDeTrabalho, String tipoTrabalho, String viagemID) {
        this.idTrabalhador = idTrabalhador;
        this.username = username;
        this.data = today;
        this.segundosDeTrabalho = segundosDeTrabalho;
        this.tipoTrabalho = tipoTrabalho;
        this.viagemID = viagemID;
    }

    public WorkTime(String idTrabalhador, String username, LocalDateTime today, long segundosDeTrabalho) {
        this.idTrabalhador = idTrabalhador;
        this.username = username;
        this.data = today;
        this.segundosDeTrabalho = segundosDeTrabalho;
        this.tipoTrabalho = PEDONAL;
        this.viagemID = "";
    }
    public WorkTime(String idTrabalhador, LocalDateTime today, long segundosDeTrabalho) {
        this.idTrabalhador = idTrabalhador;
        this.username = EstadoApp.getUsername();
        this.data = today;
        this.segundosDeTrabalho = segundosDeTrabalho;
        this.tipoTrabalho = PEDONAL;
        this.viagemID = "";
    }

    /**
     * Este construtor mete a data do registo com o momento em que o objecto é criado
     * */
    public WorkTime(String idTrabalhador, long segundosDeTrabalho){
        this.idTrabalhador = idTrabalhador;
        this.username = EstadoApp.getUsername();
        this.data = LocalDateTime.now();
        this.segundosDeTrabalho = segundosDeTrabalho;
        this.tipoTrabalho = PEDONAL;
        this.viagemID = "";
    }

    /**
     * Pega em vários registos do utilizador agrega-os por cada dia de trabalho somando os segundos de trabalho.
     * @param workTimes
     * @return
     */
    public static Map<LocalDate,WorkTime> reduce(Iterable<WorkTime> workTimes){
        Map<LocalDate, WorkTime> map = new HashMap<>();
        for(WorkTime wt : workTimes){
            LocalDate date = wt.data.toLocalDate();
            if (map.containsKey(date)) {
                WorkTime toReduce = map.get(date);
                long segundosTrabalho = toReduce.segundosDeTrabalho + wt.segundosDeTrabalho;
                WorkTime newWorkTime = new WorkTime(wt.idTrabalhador, date.atStartOfDay(), segundosTrabalho);
                map.put(date,newWorkTime);
            } else {
                map.put(wt.data.toLocalDate(), wt);
            }
        }
        return  map;
    }

    public static Map<LocalDate,WorkTime> reduce(Iterable<WorkTime> workTimes, String tipoTrabalho){
        Map<LocalDate, WorkTime> map = new HashMap<>();
        for(WorkTime wt : workTimes){
            LocalDate date = wt.data.toLocalDate();
            if (map.containsKey(date)) {
                WorkTime toReduce = map.get(date);
                long segundosTrabalho = toReduce.segundosDeTrabalho + wt.segundosDeTrabalho;
                WorkTime newWorkTime = new WorkTime(wt.idTrabalhador, date.atStartOfDay(), segundosTrabalho);
                newWorkTime.setTipoTrabalho(tipoTrabalho);
                map.put(date,newWorkTime);
            } else {
                map.put(wt.data.toLocalDate(), wt);
            }
        }
        return  map;
    }

    public String getIdTrabalhador() {
        return idTrabalhador;
    }

    public void setIdTrabalhador(String idTrabalhador) {
        this.idTrabalhador = idTrabalhador;
    }

    public String getViagemID() {
        return viagemID;
    }

    public void setViagemID(String viagemID) {
        this.viagemID = viagemID;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getTipoTrabalho() {
        return tipoTrabalho;
    }

    public void setTipoTrabalho(String tipoTrabalho) {
        this.tipoTrabalho = tipoTrabalho;
    }

    public long getSegundosDeTrabalho() {
        return segundosDeTrabalho;
    }

    public void setSegundosDeTrabalho(long segundosDeTrabalho) {
        this.segundosDeTrabalho = segundosDeTrabalho;
    }

    @Override
    public int compareTo(WorkTime o) {
        return this.data.compareTo(o.data);
    }

    /**
     * Método de desserialização personalizado para converter um Map num objeto WorkTime.
     */
    public static WorkTime fromMap(Map<String, Object> map) {
        String idTrabalhador = (String) map.get("idTrabalhador");
        String username = (String) map.get("username");
        LocalDateTime data = LocalDateTime.parse((String)map.get("data"));
        long segundosDeTrabalho = (Long) map.get("segundosDeTrabalho");
        String tipoTrabalho = (String) map.get("tipoTrabalho");
        String viagemID = (String) map.get("viagemID");
        return new WorkTime(idTrabalhador, username, data, segundosDeTrabalho, tipoTrabalho, viagemID);
    }

    /**
     * Método para converter um objeto WorkTime em um Map para ser serializado pelo Firebase Firestore.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("idTrabalhador", idTrabalhador);
        map.put("username", username);
        map.put("data", data.toString());
        map.put("segundosDeTrabalho", segundosDeTrabalho);
        map.put("tipoTrabalho", tipoTrabalho);
        map.put("viagemID", viagemID);
        return map;
    }
}

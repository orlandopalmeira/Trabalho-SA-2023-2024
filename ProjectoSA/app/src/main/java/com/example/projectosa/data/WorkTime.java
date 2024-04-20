package com.example.projectosa.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class WorkTime implements Comparable<WorkTime>{
    private String idTrabalhador;
    private LocalDateTime data; // data em que foi feito o registo
    private long segundosDeTrabalho;

    public WorkTime(){} // Necessário ao firebase
    public WorkTime(String idTrabalhador, LocalDateTime today, long segundosDeTrabalho) {
        this.idTrabalhador = idTrabalhador;
        this.data = today;
        this.segundosDeTrabalho = segundosDeTrabalho;
    }

    /**
     * Este construtor mete a data do registo com o momento em que o objecto é criado
     * */
    public WorkTime(String idTrabalhador, long segundosDeTrabalho){
        this.idTrabalhador = idTrabalhador;
        this.data = LocalDateTime.now();
        this.segundosDeTrabalho = segundosDeTrabalho;
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

    public String getIdTrabalhador() {
        return idTrabalhador;
    }

    public void setIdTrabalhador(String idTrabalhador) {
        this.idTrabalhador = idTrabalhador;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
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
     * Método de desserialização personalizado para converter um Map em um objeto WorkTime.
     */
    public static WorkTime fromMap(Map<String, Object> map) {
        String idTrabalhador = (String) map.get("idTrabalhador");
        LocalDateTime data = LocalDateTime.parse((String)map.get("data"));
        long segundosDeTrabalho = (Long) map.get("segundosDeTrabalho");
        return new WorkTime(idTrabalhador, data, segundosDeTrabalho);
    }

    /**
     * Método para converter um objeto WorkTime em um Map para ser serializado pelo Firebase Firestore.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("idTrabalhador", idTrabalhador);
        map.put("data", data.toString());
        map.put("segundosDeTrabalho", segundosDeTrabalho);
        return map;
    }
}

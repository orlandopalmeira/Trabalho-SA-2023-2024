package com.example.projectosa.data;

import java.time.LocalDateTime;

public class WorkTime {
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
}

package com.example.projectosa.data;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

/**
 * Classe que controla interacções com a base de dados
 */
public class Database {
    /**
     * Função que insere na base de dados o registo de trabalho de um utlizador.
     */
    public static Task<DocumentReference> addWorkTime(WorkTime worktime){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbWorkTime = db.collection("work_time");
        return dbWorkTime.add(worktime);
    }
}

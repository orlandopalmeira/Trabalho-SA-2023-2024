package com.example.projectosa.data;


import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe que controla interacções com a base de dados
 */
public class Database {
    /**
     * Função que insere na base de dados o registo de trabalho de um utlizador.
     */
    public static Task<DocumentReference> addWorkTime(WorkTime worktime){
        if(worktime.getSegundosDeTrabalho() > 0){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference dbWorkTime = db.collection("work_time");
            return dbWorkTime.add(worktime.toMap());
        }
        return null;
    }

    /**
     * Função que regista a posição do utilizador numa determinada timestamp
     */
    public static Task<DocumentReference> addPosition(Position position){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbWorkTime = db.collection("positions");
        return dbWorkTime.add(position.toMap());
    }

    /**
     * Obtém as geofences registadas no sistema.
     * Atenção: Ainda não se tem implementado o registo de geofences por um administrador, por isso as geofences são estáticas na base de dados.
     */
    public static Task<List<Geofence>> getGeofences() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbGeofences = db.collection("geofences");
        TaskCompletionSource<List<Geofence>> source = new TaskCompletionSource<>();

        // asynchronous operation:
        new Thread(() -> {
            try {
                QuerySnapshot querySnapshot = Tasks.await(dbGeofences.get());
                List<Geofence> result = querySnapshot.toObjects(Geofence.class);
                source.setResult(result);
            } catch (Exception e) {
                source.setException(e);
            }
        }).start();

        return source.getTask();
    }

    /**
     * Obtém os registos Worktime do utilizador
     */
    public static Task<List<WorkTime>> getWorkTimeHistoryOfUser(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbWorkTime = db.collection("work_time");
        assert FirebaseAuth.getInstance().getCurrentUser() != null; // evita warnigns
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TaskCompletionSource<List<WorkTime>> source = new TaskCompletionSource<>();

        // asynchronous operation
        new Thread(() -> {
            try {
                Task<QuerySnapshot> task = dbWorkTime.whereEqualTo("idTrabalhador", userId).get();
                QuerySnapshot querySnapshot = Tasks.await(task);
                List<Object> resultobjects = querySnapshot.toObjects(Object.class);
                List<WorkTime> result = new ArrayList<>(resultobjects.size());
                for (Object o: resultobjects) {
                    result.add(WorkTime.fromMap( (Map<String, Object>) o));
                }
                source.setResult(result);
            } catch (Exception e) {
                source.setException(e);
            }
        }).start();

        return source.getTask();
    }
}

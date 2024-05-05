package com.example.projectosa.services;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.projectosa.MainActivity;
import com.example.projectosa.data.Database;
import com.example.projectosa.data.Position;
import com.example.projectosa.data.WorkTime;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.LocationHelper;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;

public class MonitoringTripService extends Service{
    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "NOTIF-CHANNEL-2";
    private NotificationManagerCompat notificationManager;
    private NotificationChannel notificationChannel;
    private LocationHelper locationHelper;
    private long timestamp;
    private String viagemID;
    public static boolean onGoing(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MonitoringTripService.class.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("DEBUG", "Notification service started...");
        // Notificação do serviço foreground
        notificationManager = NotificationManagerCompat.from(this);
        notificationChannel = new NotificationChannel(CHANNEL_ID,"Notificacoes servico foreground 2", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
        // Localização
        Observer<LatLng> observerLocation = novaLocalizacao -> {
            // Acção quando recebe uma actualização de localização.
            long newTimestamp = SystemClock.elapsedRealtime();
            long elapsedTime = newTimestamp - timestamp;
            timestamp = newTimestamp;
            Position p = new Position(novaLocalizacao);
            p.setTipoTrabalho(WorkTime.VIAGEM);
            p.setViagemID(viagemID);
            EstadoApp.setCurrentLocationOnly(novaLocalizacao);
            EstadoApp.increaseWorkTime(elapsedTime);
            Database.addPosition(p).addOnFailureListener(e -> {
                try { throw e; }
                catch (Exception ex) { throw new RuntimeException(ex); }
            });
        };
        locationHelper = new LocationHelper(getApplicationContext(), observerLocation);
        // Observer do tempo de trabalho
        Observer<Long> observerTempoTrabalho = tempoTrabalho -> {
            actualizarNotificacao("Tempo contabilizado: " + Utils.milisecondsToFormattedString(tempoTrabalho));
        };
        EstadoApp.registerSegundosTrabalhoObserver(observerTempoTrabalho);
        viagemID = EstadoApp.getUserID() + "_" + LocalDateTime.now().toString();
        EstadoApp.setViagemID(viagemID);
        criarNotificacao();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationHelper.requestLocationUpdates();
        timestamp = SystemClock.elapsedRealtime();
        return START_STICKY;
    }

    private void criarNotificacao() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Monitorização")
                .setContentText("A ligar...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void actualizarNotificacao(String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Monitorização")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("DEBUG", "Notification service stopped...");
        locationHelper.stopLocationUpdates();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

package com.example.projectosa.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.projectosa.MainActivity;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.LocationHelper;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;

public class MonitoringService extends Service implements SensorEventListener {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "NOTIF-CHANNEL";

    private NotificationManagerCompat notificationManager;
    private NotificationChannel notificationChannel;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private LocationHelper locationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("DEBUG", "Notification service started...");
        // Notificação do serviço foreground
        notificationManager = NotificationManagerCompat.from(this);
        notificationChannel = new NotificationChannel(CHANNEL_ID,"Notificacoes servico foreground", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
        // Acelerómetro
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Localização
        locationHelper = new LocationHelper(getApplicationContext(), EstadoApp::setCurrentLocation);
        // Observer do estado da monitorização
        Observer<Integer> observerMonitoringState = state -> {
            String text = "";
            switch (state) {
                case EstadoApp.DESLIGADO: text = "Desligado\n"; break;
                case EstadoApp.DENTRO_AREA_EM_MOVIMENTO: text = "Trabalho em curso...\n"; break;
                case EstadoApp.DENTRO_AREA_PARADO: text = "Em pausa\n"; break;
                case EstadoApp.FORA_DA_AREA: text = "Fora da área de trabalho\n"; break;
            }
            actualizarNotificacao(text + "Tempo contabilizado: " + Utils.milisecondsToFormattedString(EstadoApp.getWorkTime()));
        };
        EstadoApp.registerEstadoObserver(observerMonitoringState);

        criarNotificacao();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager.registerListener(this,accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        locationHelper.requestLocationUpdates();
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
        sensorManager.unregisterListener(this);
        locationHelper.stopLocationUpdates();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Obter os valores do acelerómetro
        float x = event.values[0], y = event.values[1], z = event.values[2];
        // Actualizar o estado da aplicação
        EstadoApp.updateAccelerometerData(x,y,z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não é necessário para o nosso caso
    }
}

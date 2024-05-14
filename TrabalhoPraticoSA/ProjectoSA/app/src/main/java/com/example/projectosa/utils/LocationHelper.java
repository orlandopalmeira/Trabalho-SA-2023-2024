package com.example.projectosa.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Classe para ajudar a utilizar o sensor de GPS. (Assume que o utilizador deu permissões de localização.)
 */
public class LocationHelper {
    private final Context context;
    private final LocationManager locationManager;
    private final LocationListener locationListener;

    public LocationHelper(Context context, Observer<LatLng> locationObserver) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // localização actual do utilizador
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locationObserver.onVariableChanged(new LatLng(latitude, longitude));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        if (!Permissions.locationPermission(context)) {
            // Se as permissões de localização não foram concedidas
            Toast.makeText(context, "Permissão de localização não concedida.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "Existem permissões de localização", Toast.LENGTH_SHORT).show();
        // Registar para receber atualizações de localização
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void stopLocationUpdates() {
        // Parar de receber atualizações de localização quando não são mais necessárias
        locationManager.removeUpdates(locationListener);
    }
}

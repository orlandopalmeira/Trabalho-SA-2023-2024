package com.example.projectosa.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public final class Permissions {
    public static final int LOCATION_ONLY = 100;
    public static final int NOTIFICATIONS_ONLY = 101;
    public static final int NOTIFICATIONS_AND_LOCATION = 102;

    public static boolean locationPermission(Context context){
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean notificationPermission(Context context){
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean allPermissions(Context context){
        return locationPermission(context) && notificationPermission(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void requestPermissions(Context context, Activity activity){
        boolean locationOnly = !locationPermission(context);
        boolean notificationsOnly = !notificationPermission(context);
        boolean locationAndNotifications = locationOnly && notificationsOnly;

        if (locationAndNotifications) {
            Log.e("DEBUG", "requestPermissions: NOTIFICATIONS_AND_LOCATION");
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    NOTIFICATIONS_AND_LOCATION
            );
        } else if (locationOnly) {
            Log.e("DEBUG", "requestPermissions: LOCATION_ONLY");
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_ONLY);
        } else if (notificationsOnly) {
            Log.e("DEBUG", "requestPermissions: NOTIFICATIONS_ONLY");
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATIONS_ONLY);
        }
    }
}

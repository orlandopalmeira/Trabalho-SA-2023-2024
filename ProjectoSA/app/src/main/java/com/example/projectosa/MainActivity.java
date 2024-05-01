package com.example.projectosa;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.LocationHelper;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Permissions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.example.projectosa.pages.ViewPagerAdapter;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private TabLayout tabLayout; // aquela barra com as páginas que o user quer seleccionar
    private ViewPager2 viewPager; // para permitir a "deslocação" entre páginas
    ViewPagerAdapter viewPagerAdapter; // para permitir a "deslocação" entre páginas

    // Localização
    private LocationHelper locationHelper;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // O que veio de origem com o projecto
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // A partir daqui é relativo à parte de deslizar com as tabs nas diversas páginas.
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        viewPager.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null)
                    tab.select();// o IF evita o warning: "Method invocation 'select' may produce 'NullPointerException'"
            }
        });
        // Vai buscar as geofences ao Firestore database
        EstadoApp.fetchGeofences();
        // Permissões
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Permissions.requestPermissions(getApplicationContext(), this);
        }
        // Localização geográfica
        Observer<LatLng> locationObserver = EstadoApp::setCurrentLocation;
        locationHelper = new LocationHelper(this.getApplicationContext(), locationObserver);
        if(Permissions.allPermissions(getApplicationContext())){
            locationHelper.requestLocationUpdates();
            //Intent notificationServiceIntent = new Intent(this, NotificationService.class);
            //startService(notificationServiceIntent);
        } else if (Permissions.locationPermission(getApplicationContext())) {
            locationHelper.requestLocationUpdates();
        } else if (Permissions.notificationPermission(getApplicationContext())) {
            //Intent notificationServiceIntent = new Intent(this, NotificationService.class);
            //startService(notificationServiceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        Log.e("DEBUG", "REQUESTCODE: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Permissions.NOTIFICATIONS_AND_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Intent notificationServiceIntent = new Intent(this, NotificationService.class);
                //startForegroundService(notificationServiceIntent);
                locationHelper.requestLocationUpdates();
            }
        } else if (requestCode == Permissions.NOTIFICATIONS_ONLY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Intent notificationServiceIntent = new Intent(this, NotificationService.class);
                //startForegroundService(notificationServiceIntent);
            }
        } else if (requestCode == Permissions.LOCATION_ONLY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationHelper.requestLocationUpdates();
            }
        }
    }
    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public ViewPager2 getViewPager(){
        return viewPager;
    }


}
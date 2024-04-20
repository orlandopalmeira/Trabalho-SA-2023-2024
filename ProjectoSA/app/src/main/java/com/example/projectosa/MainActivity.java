package com.example.projectosa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import android.Manifest;
import android.os.Bundle;

import com.example.projectosa.data.WorkTime;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.LocationHelper;
import com.example.projectosa.utils.Observer;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.example.projectosa.pages.ViewPagerAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout; // aquela barra com as páginas que o user quer seleccionar
    private ViewPager2 viewPager; // para permitir a "deslocação" entre páginas
    ViewPagerAdapter viewPagerAdapter; // para permitir a "deslocação" entre páginas

    // Localização
    private LocationHelper locationHelper;

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
        // Localização geográfica
        // Solicitação das permissões de localização
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LocationHelper.REQUEST_LOCATION_CODE); // é apenas um código de requisição de permissão, pode-se usar qualquer valor

        Observer<LatLng> locationObserver = EstadoApp::setCurrentLocation;
        locationHelper = new LocationHelper(this.getApplicationContext(), locationObserver);
        locationHelper.requestLocationUpdates();
    }

    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public ViewPager2 getViewPager(){
        return viewPager;
    }


}
package com.example.projectosa.pages;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectosa.MainActivity;
import com.example.projectosa.R;
import com.example.projectosa.data.Database;
import com.example.projectosa.data.Geofence;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.gms.maps.model.CircleOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapaPage extends Fragment implements OnMapReadyCallback {
    private TextView textViewEstado_mapa, textViewTempo_mapa;
    private GoogleMap map;
    private Geocoder geocoder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mapa_page, container, false);
        textViewEstado_mapa = rootView.findViewById(R.id.textViewEstado_mapa);
        textViewTempo_mapa = rootView.findViewById(R.id.textViewTempo_mapa);
        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<String> textViewEstadoObserver = novoEstado -> {
            switch (novoEstado){
                case "DESLIGADO": Utils.coloredTextView(textViewEstado_mapa, "red", "Desligado", this); break;
                case "FORA_AREA_DE_TRABALHO": {
                    Utils.coloredTextView(textViewEstado_mapa, "red", "Fora da área de trabalho", this);
                    break;
                }
                case "DENTRO_AREA_PARADO": Utils.coloredTextView(textViewEstado_mapa, "red", "Em pausa", this); break;
                case "DENTRO_AREA_EM_MOVIMENTO": Utils.coloredTextView(textViewEstado_mapa, "green", "Trabalho em curso...", this); break;
            }
        };
        EstadoApp.registerEstadoObserver(textViewEstadoObserver);
        // Este observer permite à textView do tempo contabilizado ser actualizada
        Observer<Long> textViewSegundosTrabalhoObserver = novoTempo -> textViewTempo_mapa.setText(Utils.milisecondsToFormattedString(novoTempo));
        EstadoApp.registerSegundosTrabalhoObserver(textViewSegundosTrabalhoObserver);

        // Mapa
        // Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null; // evita warnings
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        // Botão de bloqueio de deslizamento de páginas
        Button botaoBloqueioDeslizamento = rootView.findViewById(R.id.botaoBloqueioDeslizamento);
        botaoBloqueioDeslizamento.setOnClickListener(view -> {
            assert getActivity() != null; // evita warnings
            TabLayout tabLayout = ((MainActivity)getActivity()).getTabLayout();
            if(tabLayout.isEnabled()){
                botaoBloqueioDeslizamento.setText("Desbloquear deslizamento");
                bloquearTabLayout();
            } else {
                botaoBloqueioDeslizamento.setText("Bloquear deslizamento");
                desbloquearTabLayout();
            }
        });
        return rootView;
    }

    private void bloquearTabLayout() {
        assert getActivity() != null; // evita warnings
        TabLayout tabLayout = ((MainActivity)getActivity()).getTabLayout();
        ViewPager2 viewPager = ((MainActivity)getActivity()).getViewPager();
        // Desactiva a interação com todas as abas
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.view.setClickable(false);
            }
        }
        // Desactiva o TabLayout para evitar cliques
        tabLayout.setEnabled(false);
        // Desactiva a capacidade de deslizar do ViewPager
        viewPager.setUserInputEnabled(false);
    }

    private void desbloquearTabLayout() {
        assert getActivity() != null; // evita warnings
        TabLayout tabLayout = ((MainActivity)getActivity()).getTabLayout();
        ViewPager2 viewPager = ((MainActivity)getActivity()).getViewPager();
        // Activa a interação com todas as abas
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.view.setClickable(true);
            }
        }
        // Activa o TabLayout para permitir cliques
        tabLayout.setEnabled(true);
        // Restaura a capacidade de deslizar do ViewPager
        viewPager.setUserInputEnabled(true);
    }

    private void desenharGeofences(){
        Task<List<Geofence>> taskGetGeofences = Database.getGeofences();

        taskGetGeofences.addOnSuccessListener(geofences -> {
            for (Geofence geofence: geofences) {
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(geofence.getLatLng());
                circleOptions.radius(geofence.getRadius()); // raio em metros
                circleOptions.strokeWidth(2);
                circleOptions.strokeColor(Color.RED);
                circleOptions.fillColor(Color.argb(70, 150, 50, 50));
                map.addCircle(circleOptions);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Erro a obter as geovedações", Toast.LENGTH_SHORT).show();
            Log.d("MapaPage", e.getMessage());
        });
    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
        addMarkerFromCityName("Braga");
        desenharGeofences();
    }

    private void addMarkerFromCityName(String cityName)  {
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title(cityName));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            } else {
                Log.e("MapaPage", "Nenhum endereço encontrado para " + cityName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.example.projectosa.pages;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectosa.MainActivity;
import com.example.projectosa.R;
import com.example.projectosa.data.Geofence;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.gms.maps.model.CircleOptions;

import java.util.List;

public class MapaPage extends Fragment implements OnMapReadyCallback {
    private TextView textViewEstado_mapa, textViewTempo_mapa;
    private GoogleMap map;
    private Marker mapMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mapa_page, container, false);
        textViewEstado_mapa = rootView.findViewById(R.id.textViewEstado_mapa);
        textViewTempo_mapa = rootView.findViewById(R.id.textViewTempo_mapa);
        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<Integer> textViewEstadoObserver = novoEstado -> {
            switch (novoEstado){
                case EstadoApp.DESLIGADO: Utils.coloredTextView(textViewEstado_mapa, "red", "Desligado", this); break;
                case EstadoApp.FORA_DA_AREA: {
                    Utils.coloredTextView(textViewEstado_mapa, "red", "Fora da área de trabalho", this);
                    break;
                }
                case EstadoApp.DENTRO_AREA_PARADO: Utils.coloredTextView(textViewEstado_mapa, "red", "Em pausa", this); break;
                case EstadoApp.DENTRO_AREA_EM_MOVIMENTO: Utils.coloredTextView(textViewEstado_mapa, "green", "Trabalho em curso...", this); break;
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

        // Botão que leva o mapa à localização do utilizador
        Button goToLocationButton = rootView.findViewById(R.id.goToLocationButton);
        goToLocationButton.setOnClickListener(view -> {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(EstadoApp.getCurrentLocation(), 20));
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
        List<Geofence> geofences = EstadoApp.getGeofences(); // Se der null, ou não tem geofences ou ainda não foi feito o fetch
        if (geofences != null){
            for (Geofence geofence: geofences) {
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(geofence.getLatLng());
                circleOptions.radius(geofence.getRadius()); // raio em metros
                circleOptions.strokeWidth(2);
                circleOptions.strokeColor(Color.RED);
                circleOptions.fillColor(Color.argb(70, 150, 50, 50));
                map.addCircle(circleOptions);
            }
        } else {
            Toast.makeText(requireContext(), "Sem geofences", Toast.LENGTH_SHORT).show();
        }
    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        mapMarker = map.addMarker(new MarkerOptions().position(EstadoApp.getCurrentLocation()));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(EstadoApp.getCurrentLocation(), 20));
        desenharGeofences();
        // Observer da localização do utilizador para actualizar o mapa
        Observer<LatLng> locationObserver = newLocation -> {
            mapMarker.remove();
            mapMarker = map.addMarker(new MarkerOptions().position(EstadoApp.getCurrentLocation()));
        };
        EstadoApp.registerLocationObserver(locationObserver);
    }
}
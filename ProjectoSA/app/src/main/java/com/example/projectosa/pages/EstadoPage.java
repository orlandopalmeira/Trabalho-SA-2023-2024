package com.example.projectosa.pages;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectosa.R;
import com.example.projectosa.data.Database;
import com.example.projectosa.services.MonitoringService;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.Locale;

public final class EstadoPage extends Fragment {
    TextView textViewEstado; // Indica o estado actual da aplicação (Desligado, Fora da área de trabalho, ...)
    SwitchCompat switchLigado; // Activa ou desactiva a monitorização da aplicação.
    TextView viewX, viewY, viewZ; // textviews para se ver os valores do acelerómetro
    TextView textViewTempoTrabalhoContabilizado; // textview que mostra ao utilizador o tempo de trabalho que já foi contabilizado

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_estado_page, container, false);
        textViewEstado = rootView.findViewById(R.id.textViewEstado);
        textViewTempoTrabalhoContabilizado = rootView.findViewById(R.id.textViewTempoTrabalhoContabilizado);
        switchLigado = rootView.findViewById(R.id.switchLigado);
        Utils.coloredTextView(textViewEstado, "red", "Desligado", this);
        viewX = rootView.findViewById(R.id.textViewX);
        viewY = rootView.findViewById(R.id.textViewY);
        viewZ = rootView.findViewById(R.id.textViewZ);
        handleSwitch();

        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<Integer> textViewEstadoObserver = novoEstado -> {
            switch (novoEstado){
                case EstadoApp.DESLIGADO: Utils.coloredTextView(textViewEstado, "red", "Desligado", this); break;
                case EstadoApp.FORA_DA_AREA: {
                    Utils.coloredTextView(textViewEstado, "red", "Fora da área de trabalho", this);
                    break;
                }
                case EstadoApp.DENTRO_AREA_PARADO: Utils.coloredTextView(textViewEstado, "red", "Em pausa", this); break;
                case EstadoApp.DENTRO_AREA_EM_MOVIMENTO: Utils.coloredTextView(textViewEstado, "green", "Trabalho em curso...", this); break;
            }
        };
        EstadoApp.registerEstadoObserver(textViewEstadoObserver);
        // Este observer permite à textView do tempo contabilizado ser actualizada
        Observer<Long> textViewSegundosTrabalhoObserver = novoTempo -> textViewTempoTrabalhoContabilizado.setText(Utils.milisecondsToFormattedString(novoTempo));
        EstadoApp.registerSegundosTrabalhoObserver(textViewSegundosTrabalhoObserver);
        // Observer para os valores do acelerómetro
        Observer<Float[]> observerAccelerometer = coords -> actualizarInfoAcelerometro(coords[0], coords[1], coords[2]);
        EstadoApp.registerAccelerometerObserver(observerAccelerometer);

        return rootView;
    }

    /**
     * Actualiza os valores do acelerómetro no ecrã e o estado da aplicação.
     */
    private void actualizarInfoAcelerometro(Float x, Float y, Float z){
        String casasDecimais = "%.6f"; // 6 casas decimais
        viewX.setText(String.format(Locale.getDefault(), casasDecimais, x)); // Locale.getDefault() serve para evitar warnings
        viewY.setText(String.format(Locale.getDefault(), casasDecimais, y));
        viewZ.setText(String.format(Locale.getDefault(), casasDecimais, z));
    }

    /**
     * Prepara tudo o que é relativo ao switch que o utilizador usa para indicar se quer ou não a monitorização da aplicação.
     * */
    private void handleSwitch(){
        // Código para lidar com a mudança de estado "ON/OFF" do Switch
        switchLigado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {// O Switch está ligado
                EstadoApp.setDentroDaAreaParado(); // "Em pausa..."
                if(getActivity() == null) {
                    throw new RuntimeException("EstadoPage: handleSwitch(ON): getActivity is null");
                }
                Intent monitoringService = new Intent(getActivity(), MonitoringService.class);
                getActivity().startForegroundService(monitoringService);
            } else {// O Switch está desligado
                Utils.coloredTextView(textViewEstado, "red", "Desligado", this);
                EstadoApp.setDesligado();
                // Adiciona o registo na base de dados
                Task<DocumentReference> task = Database.addWorkTime(EstadoApp.getWorkTimeData());
                if(task != null){ // Se der null é porque o registo ia com tempo = 0 e por isso não valia a pena enviar
                    task.addOnSuccessListener(documentReference -> {
                        Toast.makeText(this.getContext(), "Informação registada no sistema", Toast.LENGTH_SHORT).show();
                        EstadoApp.restartWorkTime();
                    }).addOnFailureListener(ex -> {
                        ex.printStackTrace(); // DEBUG - mostra o erro que acontece
                        Toast.makeText(this.getContext(), "Erro a registar a informação no sistema.", Toast.LENGTH_SHORT).show();
                    });
                }
                if(getActivity() == null) {
                    throw new RuntimeException("EstadoPage: handleSwitch(OFF): getActivity is null");
                }
                Intent monitoringService = new Intent(getActivity(), MonitoringService.class);
                getActivity().stopService(monitoringService);
            }
        });
    }

}
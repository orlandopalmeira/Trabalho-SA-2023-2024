package com.example.projectosa.pages;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.projectosa.R;
import com.example.projectosa.state.*;
import com.example.projectosa.utils.Observer;

import java.util.Locale;

public class EstadoPage extends Fragment {
    TextView textViewEstado; // Indica o estado actual da aplicação (Desligado, Fora da área de trabalho, ...)
    SwitchCompat switchLigado; // Activa ou desactiva a monitorização da aplicação.

    // Acelerómetro
    SensorManager sensorManager; // relacionado ao acelerómetro
    Sensor accelerometerSensor; // relacionado ao acelerómetro
    SensorEventListener accelerometerEventListener; // relacionado ao acelerómetro
    TextView viewX, viewY, viewZ; // textviews para se ver os valores do acelerómetro

    // Tempo de trabalho
    TextView textViewTempoTrabalhoContabilizado; // textview que mostra ao utilizador o tempo de trabalho que já foi contabilizado


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_estado_page, container, false);
        textViewEstado = rootView.findViewById(R.id.textViewEstado);
        textViewTempoTrabalhoContabilizado = rootView.findViewById(R.id.textViewTempoTrabalhoContabilizado);
        switchLigado = rootView.findViewById(R.id.switchLigado);
        coloredTextView(textViewEstado, "red", "Desligado");
        switchLigado.setChecked(false);

        handleSwitch();
        handleAccelerometer(rootView);
        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<String> textViewEstadoObserver = novoEstado -> {
            switch (novoEstado){
                case "DESLIGADO": coloredTextView(textViewEstado, "red", "Desligado"); break;
                case "FORA_AREA_DE_TRABALHO": {
                    sensorManager.unregisterListener(accelerometerEventListener);
                    coloredTextView(textViewEstado, "red", "Fora da área de trabalho");
                    break;
                }
                case "DENTRO_AREA_PARADO": coloredTextView(textViewEstado, "red", "Em pausa"); break;
                case "DENTRO_AREA_EM_MOVIMENTO": coloredTextView(textViewEstado, "green", "Trabalho em curso..."); break;
            }
        };
        EstadoApp.registerEstadoObserver(textViewEstadoObserver);
        // Este observer permite à textView do tempo contabilizado ser actualizada
        Observer<Long> textViewSegundosTrabalhoObserver = novoTempo -> textViewTempoTrabalhoContabilizado.setText(milisecondsToFormattedString(novoTempo));
        EstadoApp.registerSegundosTrabalhoObserver(textViewSegundosTrabalhoObserver);
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
        EstadoApp.updateAccelerometerData(x,y,z);
    }

    /**
     * Para meter texto numa textview colorido com uma certa cor.
     */
    private void coloredTextView(TextView textView, String color, String text){
        switch (color){
            case "red":   textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.red)); break; // cores definidas no ficheiro app/res/values/colors.xml
            case "green": textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.green)); break;
            default:      textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.black)); break;
        }
        textView.setText(text);
    }

    /**
     * Prepara tudo o que é relativo ao switch que o utilizador usa para indicar se quer ou não a monitorização da aplicação.
     * */
    private void handleSwitch(){
        // Código para lidar com a mudança de estado "ON/OFF" do Switch
        switchLigado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {// O Switch está ligado
                sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {// O Switch está desligado
                coloredTextView(textViewEstado, "red", "Desligado");
                sensorManager.unregisterListener(accelerometerEventListener);
                EstadoApp.setDesligado();
            }
        });
    }

    /**
     * Prepara tudo o que é relativo à utilização do acelerómetro.
     */
    private void handleAccelerometer(View rootView){
        // Lidar com o acelerómetro
        viewX = rootView.findViewById(R.id.textViewX); // textviews que apresentam os valores do acelerómetro
        viewY = rootView.findViewById(R.id.textViewY);
        viewZ = rootView.findViewById(R.id.textViewZ);

        assert getActivity() != null; // evita warnings
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor != null) {
            accelerometerEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) { // O que fazer quando o valor do acelerómetro altera
                    // Obter os valores do acelerómetro
                    float x = event.values[0], y = event.values[1], z = event.values[2];
                    // Atualizar a interface do utilizador com os valores do acelerómetro
                    actualizarInfoAcelerometro(x,y,z);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Opcional: Lidar com mudanças na precisão dos dados
                }
            };
        } else {
            Log.d("EstadoPage", "Dispositivo não possui acelerómetro");
        }
    }

    /**
     * Função que converte um long (segundos) para o formato H:M:s em string
     */
    private String milisecondsToFormattedString(long milissegundos){
        long segundos = milissegundos/1000;
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long segundosRestantes = segundos % 60;

        return String.format(Locale.getDefault(),"%02d:%02d:%02d", horas, minutos, segundosRestantes);
    }
}
package com.example.projectosa.pages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectosa.LoginActivity;
import com.example.projectosa.MainActivity;
import com.example.projectosa.R;
import com.example.projectosa.data.Database;
import com.example.projectosa.services.MonitoringService;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDateTime;

public final class EstadoPage extends Fragment {
    TextView textViewEstado; // Indica o estado actual da aplicação (Desligado, Fora da área de trabalho, ...)
    SwitchCompat switchLigado; // Activa ou desactiva a monitorização da aplicação.
    //TextView viewX, viewY, viewZ; // textviews para se ver os valores do acelerómetro
    TextView textViewTempoTrabalhoContabilizado; // textview que mostra ao utilizador o tempo de trabalho que já foi contabilizado

    // Botão de logout
    Button buttonLogout;
    private boolean destroyed = false;
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_estado_page, container, false);
        textViewEstado = rootView.findViewById(R.id.textViewEstado);
        textViewTempoTrabalhoContabilizado = rootView.findViewById(R.id.textViewTempoTrabalhoContabilizado);
        switchLigado = rootView.findViewById(R.id.switchLigado);
        buttonLogout = rootView.findViewById(R.id.buttonLogout);
        Utils.coloredTextView(textViewEstado, "red", "Desligado", this);
        handleSwitch();
        // Colocar o nome do utilizador no ecrã
        ((TextView)rootView.findViewById(R.id.textViewUserName)).setText(EstadoApp.getUsername());
        // Colocar as informações do registo mais recente
        TextView textViewUltimoRegisto = rootView.findViewById(R.id.textViewUltimoRegisto);
        Database.getLastWorkTimeInfo().addOnSuccessListener(workTime -> {
            if(workTime == null) {
                textViewUltimoRegisto.setText("Sem registo...");
            } else {
                LocalDateTime data = workTime.getData();
                int dia = data.getDayOfMonth(), mes = data.getMonthValue(), ano = data.getYear();
                long tempoDeTrabalho = workTime.getSegundosDeTrabalho();
                String text = dia + "/" + mes + "/" + ano + " " + Utils.milisecondsToFormattedString(tempoDeTrabalho * 1000);
                textViewUltimoRegisto.setText(text);
            }
        }).addOnFailureListener(e -> {
            try {
                throw e;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<Integer> textViewEstadoObserver = novoEstado -> {
            if(!destroyed) {
                switch (novoEstado) {
                    case EstadoApp.DESLIGADO:
                        Utils.coloredTextView(textViewEstado, "red", "Desligado", this);
                        break;
                    case EstadoApp.FORA_DA_AREA: {
                        Utils.coloredTextView(textViewEstado, "red", "Fora da área de trabalho", this);
                        break;
                    }
                    case EstadoApp.DENTRO_AREA_PARADO:
                        Utils.coloredTextView(textViewEstado, "red", "Em pausa", this);
                        break;
                    case EstadoApp.DENTRO_AREA_EM_MOVIMENTO:
                        Utils.coloredTextView(textViewEstado, "green", "Trabalho em curso...", this);
                        break;
                }
            }
        };
        EstadoApp.registerEstadoObserver(textViewEstadoObserver);
        // Este observer permite à textView do tempo contabilizado ser actualizada
        Observer<Long> textViewSegundosTrabalhoObserver = novoTempo -> {
            if(!destroyed) {
                textViewTempoTrabalhoContabilizado.setText(Utils.milisecondsToFormattedString(novoTempo));
            }
        };
        EstadoApp.registerSegundosTrabalhoObserver(textViewSegundosTrabalhoObserver);
        // Acção do botão de logout
        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        return rootView;
    }

    /**
     * Prepara tudo o que é relativo ao switch que o utilizador usa para indicar se quer ou não a monitorização da aplicação.
     * */
    private void handleSwitch(){
        boolean monitorServiceOngoing = MonitoringService.onGoing(getContext());
        switchLigado.setChecked(monitorServiceOngoing);
        buttonLogout.setEnabled(!monitorServiceOngoing);
        // Código para lidar com a mudança de estado "ON/OFF" do Switch
        switchLigado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {// O Switch está ligado
                buttonLogout.setEnabled(false); // O utilizador só faz logout se não estiver a ser monitorizado.
                EstadoApp.setDentroDaAreaParado(); // "Em pausa..."
                if(getActivity() == null) {
                    throw new RuntimeException("EstadoPage: handleSwitch(ON): getActivity is null");
                }
                Intent monitoringService = new Intent(getActivity(), MonitoringService.class);
                getActivity().startForegroundService(monitoringService);
            } else {// O Switch está desligado
                buttonLogout.setEnabled(true);
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

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}
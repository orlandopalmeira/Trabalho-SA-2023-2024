package com.example.projectosa.pages;

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

import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;
import com.google.android.gms.tasks.Task;

import com.example.projectosa.LoginActivity;
import com.example.projectosa.MainActivity;
import com.example.projectosa.R;
import com.example.projectosa.data.Database;
import com.example.projectosa.data.WorkTime;
import com.example.projectosa.services.MonitoringTripService;
import com.example.projectosa.state.EstadoApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDateTime;


public class ViagemPage extends Fragment {
    private Button buttonLogout;
    private SwitchCompat switchLigado;
    private boolean destroyed = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_viagem_page, container, false);
        buttonLogout = rootView.findViewById(R.id.buttonLogout_Viagem);
        switchLigado = rootView.findViewById(R.id.switchLigado_Viagem);
        TextView textViewEstado = rootView.findViewById(R.id.textViewEstado_Viagem);
        TextView textViewTempoTrabalhoContabilizado = rootView.findViewById(R.id.textViewTempoTrabalhoContabilizado_Viagem);
        TextView textViewUserName = rootView.findViewById(R.id.textViewUserName_Viagem);
        TextView textViewUltimoRegisto = rootView.findViewById(R.id.textViewUltimoRegisto_Viagem);
        Utils.coloredTextView(textViewEstado, "red", "Desligado", this);

        // Observer do estado da monitorização
        EstadoApp.registerEstadoObserver(new EstadoObserverTextView(textViewEstado, this));

        // Observer de tempo de trabalho
        Observer<Long> observerTempoDeTrabalho = tempo -> {
            if(!destroyed){
                textViewTempoTrabalhoContabilizado.setText(Utils.milisecondsToFormattedString(tempo));
            }
        };
        EstadoApp.registerSegundosTrabalhoObserver(observerTempoDeTrabalho);

        // Colocar o username no ecrã
        textViewUserName.setText(EstadoApp.getUsername());

        handleSwitch();

        // Colocar a informação do último registo no ecrã
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
            try { throw e;}
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        return rootView;
    }

    public void setEnabledDangerousButtons(boolean lock){
        // Página viagem
        buttonLogout.setEnabled(lock);
        // Página Pedonal

    }
    private void handleSwitch(){
        boolean monitorServiceOngoing = MonitoringTripService.onGoing(getContext());
        switchLigado.setChecked(monitorServiceOngoing);
        if(monitorServiceOngoing){
            EstadoApp.setLigadoEmViagem();
        }
        // Página de viagem
        if(getActivity() == null) {
            throw new RuntimeException("ViagemPage: handleSwitch: getActivity is null");
        }
        setEnabledDangerousButtons(!monitorServiceOngoing);
        switchLigado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                setEnabledDangerousButtons(false);
                EstadoApp.setLigadoEmViagem();

                Intent monitoringService = new Intent(getActivity(), MonitoringTripService.class);
                getActivity().startForegroundService(monitoringService);
            } else {
                setEnabledDangerousButtons(true);
                EstadoApp.setDesligado();

                Intent monitoringService = new Intent(getActivity(), MonitoringTripService.class);
                getActivity().stopService(monitoringService);

                WorkTime novoRegisto = EstadoApp.getWorkTimeData();
                novoRegisto.setTipoTrabalho(WorkTime.VIAGEM);
                novoRegisto.setViagemID(EstadoApp.getViagemID());
                Task<DocumentReference> task = Database.addWorkTime(novoRegisto);
                if(task != null){ // null quando o registo de trabalho tem 0s
                    task.addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Informação registada no sistema", Toast.LENGTH_SHORT).show();
                        EstadoApp.restartWorkTime();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Erro a registar a informação no sistema", Toast.LENGTH_SHORT).show();
                        switchLigado.setChecked(true);
                    });
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}
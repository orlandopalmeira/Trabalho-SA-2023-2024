package com.example.projectosa.pages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.projectosa.R;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;

public class HistoricoPage extends Fragment {

    TextView textViewEstado_historico, textViewTempo_historico;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_historico_page, container, false);
        textViewEstado_historico = rootView.findViewById(R.id.textViewEstado_historico);
        textViewTempo_historico = rootView.findViewById(R.id.textViewTempo_historico);

        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<String> textViewEstadoObserver = novoEstado -> {
            switch (novoEstado){
                case "DESLIGADO": Utils.coloredTextView(textViewEstado_historico, "red", "Desligado", this); break;
                case "FORA_AREA_DE_TRABALHO": {
                    Utils.coloredTextView(textViewEstado_historico, "red", "Fora da área de trabalho", this);
                    break;
                }
                case "DENTRO_AREA_PARADO": Utils.coloredTextView(textViewEstado_historico, "red", "Em pausa", this); break;
                case "DENTRO_AREA_EM_MOVIMENTO": Utils.coloredTextView(textViewEstado_historico, "green", "Trabalho em curso...", this); break;
            }
        };
        EstadoApp.registerEstadoObserver(textViewEstadoObserver);
        // Este observer permite à textView do tempo contabilizado ser actualizada
        Observer<Long> textViewSegundosTrabalhoObserver = novoTempo -> textViewTempo_historico.setText(Utils.milisecondsToFormattedString(novoTempo));
        EstadoApp.registerSegundosTrabalhoObserver(textViewSegundosTrabalhoObserver);

        return rootView;
    }
}
package com.example.projectosa.pages;

import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;

public class EstadoObserverTextView implements Observer<Integer> {
    private TextView textView;
    private Fragment page;
    private EstadoObserverTextView(){}

    public EstadoObserverTextView(TextView textView, Fragment page){
        this.textView = textView;
        this.page = page;
    }

    @Override
    public void onVariableChanged(Integer novoEstado) {
        if(this.page.isAdded() && this.page.getActivity() != null){
            switch (novoEstado) {
                case EstadoApp.DESLIGADO:
                    Utils.coloredTextView(textView, "red", "Desligado", page);
                    break;
                case EstadoApp.FORA_DA_AREA:
                    Utils.coloredTextView(textView, "red", "Fora da área de trabalho", page);
                    break;
                case EstadoApp.DENTRO_AREA_PARADO:
                    Utils.coloredTextView(textView, "red", "Em pausa", page);
                    break;
                case EstadoApp.DENTRO_AREA_EM_MOVIMENTO:
                    Utils.coloredTextView(textView, "green", "Trabalho em curso...", page);
                    break;
                case EstadoApp.LIGADO_EM_VIAGEM:
                    Utils.coloredTextView(textView, "green", "Ligado...", page);
                    break;
            }
        }
    }
}

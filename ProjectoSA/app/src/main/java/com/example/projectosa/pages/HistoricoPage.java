package com.example.projectosa.pages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.projectosa.R;
import com.example.projectosa.data.Database;
import com.example.projectosa.data.WorkTime;
import com.example.projectosa.state.EstadoApp;
import com.example.projectosa.utils.Observer;
import com.example.projectosa.utils.Utils;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class HistoricoPage extends Fragment {
    TextView textViewEstado_historico, textViewTempo_historico;
    TableLayout tableLayout;
    private boolean destroyed = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_historico_page, container, false);
        textViewEstado_historico = rootView.findViewById(R.id.textViewEstado_historico);
        textViewTempo_historico = rootView.findViewById(R.id.textViewTempo_historico);

        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<Integer> textViewEstadoObserver = novoEstado -> {
            if(!destroyed) {
                switch (novoEstado) {
                    case EstadoApp.DESLIGADO:
                        Utils.coloredTextView(textViewEstado_historico, "red", "Desligado", this);
                        break;
                    case EstadoApp.FORA_DA_AREA:
                        Utils.coloredTextView(textViewEstado_historico, "red", "Fora da área de trabalho", this);
                        break;
                    case EstadoApp.DENTRO_AREA_PARADO:
                        Utils.coloredTextView(textViewEstado_historico, "red", "Em pausa", this);
                        break;
                    case EstadoApp.DENTRO_AREA_EM_MOVIMENTO:
                        Utils.coloredTextView(textViewEstado_historico, "green", "Trabalho em curso...", this);
                        break;
                }
            }
        };
        EstadoApp.registerEstadoObserver(textViewEstadoObserver);
        // Este observer permite à textView do tempo contabilizado ser actualizada
        Observer<Long> textViewSegundosTrabalhoObserver = novoTempo -> {
            if(!destroyed) {
                textViewTempo_historico.setText(Utils.milisecondsToFormattedString(novoTempo));
            }
        };
        EstadoApp.registerSegundosTrabalhoObserver(textViewSegundosTrabalhoObserver);
        // Preencher a tabela de histórico
        Database.getWorkTimeHistoryOfUser().addOnSuccessListener((List<WorkTime> workTimes) -> {
            Map<LocalDate,WorkTime> registos = WorkTime.reduce(workTimes);
            for(WorkTime wt: registos.values()){
                addTableRow(rootView, wt);
            }
        }).addOnFailureListener(e -> {
            try {
                throw e;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        return rootView;
    }

    private void addTableRow(View rootView, WorkTime registo){
        // Cria uma nova instância de TableRow
        TableRow tableRow = new TableRow(requireContext());

        // Define os parâmetros de layout para a nova TableRow
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(layoutParams);

        // Cria e configura o primeiro TextView
        TextView textView1 = new TextView(requireContext());
        textView1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
        textView1.setText(registo.getData().toLocalDate().toString());
        textView1.setGravity(Gravity.CENTER_HORIZONTAL);
        textView1.setPadding(5, 5, 5, 5);
        textView1.setTextSize(14);
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // Adiciona o primeiro TextView à TableRow
        tableRow.addView(textView1);

        // Cria e configura o segundo TextView
        TextView textView2 = new TextView(requireContext());
        textView2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
        textView2.setText(Utils.milisecondsToFormattedString(registo.getSegundosDeTrabalho()*1000));
        textView2.setGravity(Gravity.CENTER_HORIZONTAL);
        textView2.setPadding(5, 5, 5, 5);
        textView2.setTextSize(14);
        textView2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // Adiciona o segundo TextView à TableRow
        tableRow.addView(textView2);

        // Obtém uma referência ao TableLayout do teu layout XML
        TableLayout tableLayout = rootView.findViewById(R.id.tableLayout);

        // Adiciona a TableRow ao TableLayout
        tableLayout.addView(tableRow);
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}
package com.example.projectosa.pages;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class HistoricoPage extends Fragment {
    TextView textViewEstado_historico, textViewTempo_historico;

    private LineChart lineChart;
    TableLayout tableLayout;
    private boolean destroyed = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_historico_page, container, false);
        textViewEstado_historico = rootView.findViewById(R.id.textViewEstado_historico);
        textViewTempo_historico = rootView.findViewById(R.id.textViewTempo_historico);

        // Este observer permite à textview que apresenta o estado alterar o seu conteúdo automaticamente.
        Observer<Integer> textViewEstadoObserver = new EstadoObserverTextView(textViewEstado_historico, this);
        EstadoApp.registerEstadoObserver(textViewEstadoObserver);
        // Este observer permite à textView do tempo contabilizado ser actualizada
        Observer<Long> textViewSegundosTrabalhoObserver = novoTempo -> {
            if(!destroyed) {
                textViewTempo_historico.setText(Utils.milisecondsToFormattedString(novoTempo));
            }
        };
        EstadoApp.registerSegundosTrabalhoObserver(textViewSegundosTrabalhoObserver);

        // Preencher a tabela de histórico e o plot
        lineChart = rootView.findViewById(R.id.lineChart);
        List<Entry> entriesPedonal = new ArrayList<>(), entriesViagem = new ArrayList<>();
        Database.getWorkTimeHistoryOfUser().addOnSuccessListener((List<WorkTime> workTimes) -> {
            List<WorkTime> pedonal = workTimes.stream().filter(wt -> wt.getTipoTrabalho().equals("pedonal")).collect(Collectors.toList());
            List<WorkTime> viagem = workTimes.stream().filter(wt -> wt.getTipoTrabalho().equals("viagem")).collect(Collectors.toList());
            List<WorkTime> pedonalOrd = WorkTime.reduce(pedonal).values().stream().sorted((w1,w2) ->  w2.getData().compareTo(w1.getData())).collect(Collectors.toList());
            List<WorkTime> viagemOrd = WorkTime.reduce(viagem).values().stream().sorted((w1,w2) ->  w2.getData().compareTo(w1.getData())).collect(Collectors.toList());
            int i = 0;
            for(WorkTime wt: pedonalOrd){
                entriesPedonal.add(new Entry(i++, (float) wt.getSegundosDeTrabalho()/3600));
                addTableRow(rootView, wt, WorkTime.PEDONAL); // tabela
            }
            i = 0;
            for(WorkTime wt: viagemOrd){
                entriesViagem.add(new Entry(i++, (float) wt.getSegundosDeTrabalho()/3600));
                addTableRow(rootView, wt, WorkTime.VIAGEM); // tabela
            }
            LineDataSet dataSetPedonal = new LineDataSet(entriesPedonal, "Tempo de trabalho diário (pedonal)");
            dataSetPedonal.setColor(Color.BLUE);
            dataSetPedonal.setValueTextColor(Color.BLACK);
            LineDataSet dataSetViagem = new LineDataSet(entriesViagem, "Tempo de trabalho diário (viagem)");
            dataSetViagem.setColor(Color.RED);
            dataSetViagem.setValueTextColor(Color.BLACK);
            LineData lineData = new LineData(dataSetPedonal, dataSetViagem);
            lineChart.setData(lineData);
            lineChart.invalidate();
        }).addOnFailureListener(e -> {
            try {
                throw e;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        return rootView;
    }

    private void addTableRow(View rootView, WorkTime registo, String tipoTrabalho){
        tipoTrabalho = tipoTrabalho.equals(WorkTime.VIAGEM) ? "Viagem" : "Pedonal";
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

        // Adiciona o terceiro TextView à TableRow
        tableRow.addView(textView2);

        // Cria e configura o terceiro TextView
        TextView textView3 = new TextView(requireContext());
        textView3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
        textView3.setText(tipoTrabalho);
        textView3.setGravity(Gravity.CENTER_HORIZONTAL);
        textView3.setPadding(5, 5, 5, 5);
        textView3.setTextSize(14);
        textView3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // Adiciona o segundo TextView à TableRow
        tableRow.addView(textView3);

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
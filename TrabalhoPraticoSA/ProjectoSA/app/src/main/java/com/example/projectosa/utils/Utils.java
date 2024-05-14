package com.example.projectosa.utils;

import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.Locale;
import androidx.fragment.app.Fragment;

import com.example.projectosa.R;

public class Utils {

    /**
     * Função que converte um long (milissegundos) para o formato H:M:s em string
     */
    public static String milisecondsToFormattedString(long milissegundos){
        long segundos = milissegundos/1000;
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long segundosRestantes = segundos % 60;

        return String.format(Locale.getDefault(),"%02d:%02d:%02d", horas, minutos, segundosRestantes);
    }

    /**
     * Text view com texto colorido.
     * @param textView Text view que se vai alterar o texto e cor
     * @param color_name Nome da cor
     * @param text Texto que aparece na textview
     */
    public static void coloredTextView(TextView textView, String color_name, String text, Fragment page){
        int color = R.color.black;
        switch (color_name){
            case "red":   color = R.color.red; break; // cores definidas no ficheiro app/res/values/colors.xml
            case "green": color = R.color.green; break;
        }
        textView.setTextColor(ContextCompat.getColor(page.requireContext(),color)); // altera a cor
        textView.setText(text); // altera o texto-
    }
}

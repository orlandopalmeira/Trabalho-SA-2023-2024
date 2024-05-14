package com.example.projectosa.utils;

public interface Observer<T> {
    void onVariableChanged(T novoValor);
}

package com.example.projectosa.pages;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            //case 0: return new EstadoPage();
            case 1: return new ViagemPage();
            case 2: return new MapaPage();
            case 3: return new HistoricoPage();
            default: return new EstadoPage();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // retornar o número de tabs que temos
    }
}

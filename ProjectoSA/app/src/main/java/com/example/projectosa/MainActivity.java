package com.example.projectosa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.example.projectosa.pages.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout; // aquela barra com as páginas que o user quer seleccionar
    ViewPager2 viewPager; // para permitir a "deslocação" entre páginas
    ViewPagerAdapter viewPagerAdapter; // para permitir a "deslocação" entre páginas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // O que veio de origem com o projecto
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // A partir daqui é relativo à parte de deslizar com as tabs nas diversas páginas.
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewPager.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) tab.select();// o IF evita o warning: "Method invocation 'select' may produce 'NullPointerException'"
            }
        });
    }
}
package io.github.pranavgade20.pbridge;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import io.github.pranavgade20.pbridge.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {
    static String ip = "192.168.1.7";
    static int port = 11111;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewPager.setNestedScrollingEnabled(false);
        }
        viewPager.setOnTouchListener((v, event) -> true);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            Snackbar.make(view, "Reconnecting...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            switch (tabs.getSelectedTabPosition()) {
                case 0:
                    TextFragment.connect();
                    break;
                case 1:
                    TrackpadFragment.connect();
                    break;
            }
        });
    }
}
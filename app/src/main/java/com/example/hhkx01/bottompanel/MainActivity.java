package com.example.hhkx01.bottompanel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lxj.dragpanel.DragPanel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DragPanel bottomPanel = (DragPanel) findViewById(R.id.bottomPanel);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomPanel.open();
            }

        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomPanel.close();
            }

        });

        final View view = findViewById(R.id.iv);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_LONG).show();
            }
        });

        bottomPanel.setOnPanelDragListener(new DragPanel.OnPanelDragListener() {
            @Override
            public void onOpen() {
                Toast.makeText(MainActivity.this, "open", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onClose() {
                Toast.makeText(MainActivity.this, "close", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDragging(float fraction) {
//                Log.d(TAG, "onDragging: fraction: "+fraction);
            }
        });

//        view.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                view.setVisibility(View.GONE);
//            }
//        }, 2000);


    }
}

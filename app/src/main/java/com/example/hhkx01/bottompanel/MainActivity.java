package com.example.hhkx01.bottompanel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lxj.dragpanel.DragPanel;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DragPanel bottomPanel = (DragPanel) findViewById(R.id.bottomPanel);
        final View tv1 = findViewById(R.id.tv1);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                bottomPanel.setDefaultShowHeight(1400);
                bottomPanel.open();
            }

        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomPanel.close();
            }

        });
        findViewById(R.id.circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }

        });

        final View view = findViewById(R.id.headerView);
        //can not set header click listener like this.
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Click headerView", Toast.LENGTH_LONG).show();
//
//            }
//        });

        bottomPanel.setOnTapListener(new DragPanel.OnHeaderClickListener() {
            @Override
            public void onHeaderClick() {
                Log.e(TAG, "onClick: Header");
//                Toast.makeText(MainActivity.this, "Click Header", Toast.LENGTH_LONG).show();
            }
        });

        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: tv1");
//                Toast.makeText(MainActivity.this, "Click tv1", Toast.LENGTH_LONG).show();
            }
        });

        bottomPanel.setOnPanelDragListener(new DragPanel.OnPanelDragListener() {
            @Override
            public void onOpen() {
                Log.e(TAG, "onClick: onOpen");
//                Toast.makeText(MainActivity.this, "open", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onClose() {
                Log.e(TAG, "onClick: onClose");
//                Toast.makeText(MainActivity.this, "close", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDragging(float fraction) {
                Log.d(TAG, "onDragging: fraction: "+fraction);
            }
        });



        view.postDelayed(new Runnable() {
            @Override
            public void run() {
//                tv1.setVisibility(View.GONE);
                findViewById(R.id.tv2).setVisibility(View.GONE);
            }
        }, 2000);


        final TextView text = findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMax){
                    text.setMaxLines(3);
                }else {
                    text.setMaxLines(Integer.MAX_VALUE);
                }
                isMax = !isMax;
            }
        });


    }

    boolean isMax;
}

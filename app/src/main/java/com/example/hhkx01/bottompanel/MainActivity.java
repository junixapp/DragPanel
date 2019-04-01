package com.example.hhkx01.bottompanel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.lxj.dragpanel.DragPanel;
import com.lxj.easyadapter.CommonAdapter;
import com.lxj.easyadapter.ViewHolder;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    private DragPanel bottomPanel;
    TextView tv2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomPanel = (DragPanel) findViewById(R.id.bottomPanel);
        final View tv1 = findViewById(R.id.tv1);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);
        tv2 = findViewById(R.id.tv2);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                bottomPanel.setDefaultShowHeight(1400);
                Toast.makeText(MainActivity.this, "isOpenToTop: " + bottomPanel.isOpenToTop(), 1).show();
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

        bottomPanel.setOnHeaderClickListener(new DragPanel.OnHeaderClickListener() {
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

        bottomPanel.addOnPanelDragListener(new DragPanel.OnPanelDragListener() {
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



//        final TextView text = findViewById(R.id.text);
//        text.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(isMax){
//                    text.setMaxLines(3);
//                }else {
//                    text.setMaxLines(Integer.MAX_VALUE);
//                }
//                isMax = !isMax;
//            }
//        });


        // prepare data
        prepareData();


        bottomPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                int windowHeight = getWindow().getDecorView().getMeasuredHeight();
                Log.e(TAG, "windowHeight: " + windowHeight );
                Log.e(TAG, "onGlobalLayout: bottomPanel " + bottomPanel.getMeasuredHeight() );
                Log.e(TAG, "onGlobalLayout: bottomPanel content " + bottomPanel.getChildAt(0).getMeasuredHeight() );
                Log.e(TAG, "onGlobalLayout tv2: " + tv2.getMeasuredHeight() );
            }
        });
    }

    private void prepareData() {
        final ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            datas.add("name " + i);
        }
        LinearLayoutManager layout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layout);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CommonAdapter<String>(R.layout.item, datas) {
            @Override
            protected void convert(ViewHolder holder, String s, int position) {
                holder.setText(R.id.text, datas.get(position));
            }

        });
        recyclerView2.setAdapter(new CommonAdapter<String>(R.layout.item, datas) {
            @Override
            protected void convert(ViewHolder holder, String s, int position) {
                holder.setText(R.id.text, datas.get(position));
            }

        });
    }

    boolean isMax;



}

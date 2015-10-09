package com.bitandik.labs.firebasedrawing.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bitandik.labs.firebasedrawing.FirebaseDrawingApplication;
import com.bitandik.labs.firebasedrawing.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ColorPickerActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.llFirstRow) LinearLayout llFirstRow;
    @Bind(R.id.llSecondRow) LinearLayout llSecondRow;
    String[] colors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        ButterKnife.bind(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        FirebaseDrawingApplication app = (FirebaseDrawingApplication)getApplicationContext();
        colors = app.getColors();

        LinearLayout currentLayout;
        for (int i = 0; i < colors.length; i++) {
            if (i < colors.length/2) {
                currentLayout = llFirstRow;
            } else {
                currentLayout = llSecondRow;
            }

            Button btnNewColor = new Button(this);
            btnNewColor.setId(i);
            btnNewColor.setOnClickListener(this);
            btnNewColor.setBackgroundColor(Color.parseColor(colors[i]));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            currentLayout.addView(btnNewColor,lp);
        }
    }

    @Override
    public void onClick(View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("color", colors[view.getId()]);
        startActivity(i);
    }
}

package io.breadcrumbs.breadcrumbs;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class Walking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView t = (TextView) findViewById(R.id.distance);
        t.setText("300 m");

        TextView u = (TextView) findViewById(R.id.time);
        u.setText("10 min");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}

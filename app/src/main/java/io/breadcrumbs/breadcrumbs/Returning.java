package io.breadcrumbs.breadcrumbs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Returning extends AppCompatActivity {
    private LocationManager locationManager;
    private String provider = "gps";
    private boolean enabled;

    private ArrayList<Location> locations;
    private float distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_returning);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        //TODO: what if locations was null (i.e. nothing got saved)
        locations = extras.getParcelableArrayList("locations");
        distance = extras.getFloat("distance", 0);

        TextView t = (TextView) findViewById(R.id.distance);
        t.setText(distance + " m");

        TextView u = (TextView) findViewById(R.id.time);
        u.setText("10 min");

        ImageView i = (ImageView) findViewById(R.id.arrow);
        i.setRotation(45);
        i.setColorFilter(Color.BLUE);
        //Drawable.setColorFilter(Color.BLUE , PorterDuff.Mode.MULTIPLY);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    /** Called when the user clicks the Drop Crumbs button */
    public void startWalking(View view) {
        Intent intent = new Intent(this, Walking.class);
        intent.putExtra("locations", locations);
        intent.putExtra("distance", distance);
        startActivity(intent);
    }

}

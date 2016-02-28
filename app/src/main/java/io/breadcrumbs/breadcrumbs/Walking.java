package io.breadcrumbs.breadcrumbs;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Walking extends AppCompatActivity implements LocationListener {
    private LocationManager locationManager;
    private String provider = "gps";
    private boolean enabled;

    private ArrayList<Location> locations;
    private float distance;
    private long elapsedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Bundle extras = getIntent().getExtras();
        //TODO: what if locations was null (i.e. nothing got saved)
        if (extras != null){
            locations = extras.getParcelableArrayList("locations");
            distance = extras.getFloat("distance", 0);
            elapsedTime = extras.getLong("elapsedTime", 0);
        }

        TextView t = (TextView) findViewById(R.id.distance);
        t.setText((int) distance + " m");
        TextView u = (TextView) findViewById(R.id.time);
        int minutes = (int) ((elapsedTime / (1000*60)) % 60);
        u.setText(minutes + " min");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            // Create a dialog here that requests the user to enable GPS, and use an intent
            // with the android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS action
            // to take the user to the Settings screen to enable GPS when they click "OK"
            new AlertDialog.Builder(this)
                    .setTitle("Enable GPS")
                    .setMessage("This Application requires the use of Mobile GPS. " +
                            "Do you wish to enable this feature")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // show system settings
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }
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
    public void startReturning(View view) {
        Intent intent = new Intent(this, Returning.class);
        intent.putExtra("locations", locations);
        intent.putExtra("distance", distance);
        intent.putExtra("elapsedTime", elapsedTime);
        startActivity(intent);
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Long time = location.getTime();

        if (locations != null){
            updateTime(time, location);

            Location lastLocation = locations.get(locations.size()-1);
            if(lastLocation.distanceTo(location) >= 10) {
                locations.add(location);
                updateDist();
            }
        }
        else{
            locations = new ArrayList<>();
            locations.add(location);
        }

    }

    public void updateDist() {
        Location currLoc = locations.get(locations.size() - 1);
        Location prevLoc = locations.get(locations.size() - 2);
        distance += prevLoc.distanceTo(currLoc);
        TextView t = (TextView) findViewById(R.id.distance);
        t.setText((int) distance + " m");
    }

    public void updateTime(Long time, Location location) {
        long currTime = location.getTime();
        long prevTime = locations.get(locations.size() - 1).getTime();
        elapsedTime += currTime - prevTime;
        long totalTime = elapsedTime + time - currTime;
        int minutes = (int) ((totalTime / (1000*60)) % 60);
        TextView t = (TextView) findViewById(R.id.time);
        t.setText(minutes + " min");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    public void confirmExit(View view){
        AlertDialog.Builder exitAlert = new AlertDialog.Builder(this);
        exitAlert.setMessage("Are you sure you want to exit?");
        exitAlert.setTitle("Confirm Exit");
        exitAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        exitAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(Walking.this, MainActivity.class);
                startActivity(i);
            }
        });

        AlertDialog exit = exitAlert.create();
        exit.show();
    }
}
package io.breadcrumbs.breadcrumbs;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Returning extends AppCompatActivity implements LocationListener {
    private LocationManager locationManager;
    private String provider = "gps";
    private boolean enabled;

    private ArrayList<Location> locations;
    private float distance;

    private float myBearing;
    //private float compassBearing;
    private float heading;
    private GeomagneticField geoField;
    private Location currentLoc;
    private RotateAnimation ra;
    private float degree=0;
    private ImageView arrow;
    private Location previousLocation;
    private float prevArrowAngle = 0;
    private float arrowAngle=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_returning);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Bundle extras = getIntent().getExtras();
        //TODO: what if locations was null (i.e. nothing got saved)
        locations = extras.getParcelableArrayList("locations");
        distance = extras.getFloat("distance", 0);

        TextView t = (TextView) findViewById(R.id.distance);
        t.setText(distance + " m");

        TextView u = (TextView) findViewById(R.id.time);
        u.setText("10 min");

        arrow = (ImageView) findViewById(R.id.arrow);
        arrow.setColorFilter(Color.BLUE);

        //Drawable.setColorFilter(Color.BLUE , PorterDuff.Mode.MULTIPLY);

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
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            onLocationChanged(location);
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

        if (locations != null) {
            previousLocation = location;
            updateArrow(location);
            if (locations.get(locations.size() - 1).distanceTo(location) >= 10) {
                //TODO: need to change updateDist to subtract distance when necessary
                //TODO: need to figure out when to remove current lastLocation
                updateDist();
            }
        } else {
            locations = new ArrayList<>();
            locations.add(location);
        }
    }

    public void updateDist() {
        Location currLoc = locations.get(locations.size() - 1);
        Location prevLoc = locations.get(locations.size() - 2);
        distance += prevLoc.distanceTo(currLoc);
        TextView t = (TextView) findViewById(R.id.distance);
        t.setText(distance + " m");
    }

    public void updateArrow(Location location){
        if (locations.size() <= 0) {
            return;
        }
        float shouldHead = location.bearingTo(locations.get(locations.size() - 1)) % 360;
        float myHeading = location.bearingTo(previousLocation) % 360;
        arrowAngle = (shouldHead - myHeading) % 360;
        ra = new RotateAnimation(prevArrowAngle, arrowAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(2000);
        ra.setFillAfter(true);
        arrow = (ImageView) findViewById(R.id.arrow);
        arrow.startAnimation(ra);
        prevArrowAngle = arrowAngle;
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

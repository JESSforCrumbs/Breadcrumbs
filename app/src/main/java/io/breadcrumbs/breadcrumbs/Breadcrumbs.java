package io.breadcrumbs.breadcrumbs;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;

/**
 * Created by root on 28/02/16.
 */
public class Breadcrumbs extends Application {
    public ArrayList<Location> locations;
    public float distance;
    public long elapsedTime;
}

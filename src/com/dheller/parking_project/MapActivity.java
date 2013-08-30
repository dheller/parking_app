package com.dheller.parking_project;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

/* This activity is responsible for creating the map.
 * It also generates the polygon overlay which shows the risk of parking
 * in various zones within Toronto.
 */

public class MapActivity extends Activity{
    
    //Required variables
    private GoogleMap map;
    Intent intent;
    double lat;
    double lon;
    LatLng coords;
    LatLng searchHere;
    
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        //Inflates the action bar
        ActionBar bar = getActionBar();
        bar.hide();
        
        //Makes sure Google Play Services are installed
        int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        //Get the intent and sees if there is a starting coordinate value.  If not, assigns general Toronto coordinates
        intent = getIntent();
        lat = intent.getDoubleExtra(HomeActivity.latName, 43.6481);
        lon = intent.getDoubleExtra(HomeActivity.lonName, -79.4042);
        coords = new LatLng(lat,lon);
        
        //Checks if GooglePlayServices are installed, informs the user if they're not
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
            Toast.makeText(getBaseContext(), "Google Play Services not installed", Toast.LENGTH_LONG).show();
        } else {       
            setUpMapIfNeeded();
        }
        
    }
    
    //Handles the initialization of the map if required
    private void setUpMapIfNeeded() {
        
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {

            MapFragment fragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.GMap));
            
            //Makes sure that the map fragment exists before attempting to inflate the map
            if (fragment != null) {
                map = fragment.getMap();
            } else {
                Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }
            
            // Confirm we were successful in obtaining the map.
            if (map != null) {
                
                //Checks to see if we have custom coordinates so we can zoom in on them
                if (lat != 43.6481 || lon != -79.4042) {
                    map.moveCamera(CameraUpdateFactory.zoomTo(16));
                } else {
                    map.moveCamera(CameraUpdateFactory.zoomTo(12));
                }
                
                //Sets the initial camera position
                map.moveCamera(CameraUpdateFactory.newLatLng(coords));
                
                if (lat != 43.6481 || lon != -79.4042) {
                
                    //Generates the polygons showing the risk of a zone
                    generatePolygons();
                    
                    //Marks the current search point
                    MarkerOptions marker = new MarkerOptions().position(coords).title("Search Zone");
                    map.addMarker(marker);
                }
                
                //Sets a one use listener for clicks to the map
                map.setOnMapClickListener(new OnMapClickListener() {
                    
                    //Adds a marker to the map if the user clicks to search there
                    @Override
                    public void onMapClick(LatLng point) {
                        
                        searchHere = point;
                        map.clear();
                        map.addMarker(new MarkerOptions().position(point).title("Search Here"));
                        
                    }
                });
                
            }
        }
    }
    
    //Method to turn the risk score into a color
    int turnRiskIntoColor(double risk) {
        
        double red;
        double green;
        final int blue = 80;
        
        if (risk <= .5) {
            green = 255;
            red = 255 * (2 * risk);
        } else {
            red = 255;
            green = (255 - (255 * risk)) * 2;
        }
        
        int red_int = (int) red;
        int green_int = (int) green;
    
        return Color.argb(blue, red_int, green_int, 0);
    }
    
    //This handles the polygon overlays showing the risk of a zone
    public void generatePolygons() {
        
        //Creates 25 unique polygons
        for (int x = 0; x<5; x++) {
            for (int y = 0; y<5; y++) {
                
                //Determines what color the polygon should be
                int color = turnRiskIntoColor(ResultActivity.risks.get(x + (y * 5)));
                
                double latAdj = x * .002;
                double lonAdj = y * .002;
                double majorPoint = .005;
                double minorPoint = .003;
                
                //Adds the new polygon to the map
                map.addPolygon(new PolygonOptions()
                .add(new LatLng(lat + majorPoint - latAdj, lon + majorPoint - lonAdj),
                        new LatLng(lat + majorPoint - latAdj, lon + minorPoint - lonAdj),
                        new LatLng(lat + minorPoint - latAdj, lon + minorPoint - lonAdj),
                        new LatLng(lat + minorPoint - latAdj, lon + majorPoint - lonAdj))
                .fillColor(color)
                .strokeColor(Color.argb(0, 0, 0, 0)));

            }
        }
    }
    
    //Handles user clicks to search via the map
    public void mapSearch(View view) {
        
        if (searchHere == null) {
            Toast.makeText(getBaseContext(), "Click somewhere on the map to add a search marker", Toast.LENGTH_LONG).show();
        } else {
        
            double latMap = searchHere.latitude;
            double lonMap = searchHere.longitude;
            
            searchHere = null;
            
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(HomeActivity.latViaMapName, String.valueOf(latMap));
            intent.putExtra(HomeActivity.lonViaMapName, String.valueOf(lonMap));
            startActivity(intent);
        }
    }
}
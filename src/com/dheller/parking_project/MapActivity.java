package com.dheller.parking_project;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class MapActivity extends Activity{
    
	private GoogleMap map;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        //Makes sure Google Play Services are installed
        int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
        	Toast.makeText(getBaseContext(), "Sucka you need Google Play Services installed", Toast.LENGTH_LONG).show();
        } else {       
        	setUpMapIfNeeded();
        }
	}
	
	private void setUpMapIfNeeded() {
	    
		// Do a null check to confirm that we have not already instantiated the map.
	    if (map == null) {

	        MapFragment a = ((MapFragment) getFragmentManager().findFragmentById(R.id.GMap));
	        
	        if (a != null) {
	        	map = a.getMap();
	        } else {
	        	Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_LONG).show();
	        }
	        
	        // Check if we were successful in obtaining the map.
	        if (map != null) {
	            
	        	// The Map is verified. It is now safe to manipulate the map.

	        }
	    }
	}
}
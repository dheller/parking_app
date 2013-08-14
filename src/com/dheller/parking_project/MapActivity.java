package com.dheller.parking_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

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
	    
        //Get the intent and sees if there is a starting coordinate value.  If not, assigns general Toronto coordinates
		Intent intent = getIntent();
		double lat = intent.getDoubleExtra(HomeActivity.lat_name, 43.6481);
		double lon = intent.getDoubleExtra(HomeActivity.lon_name, -79.4042);
		LatLng coords = new LatLng(lat,lon);
		
		Log.e("LAT", String.valueOf(lat));
		Log.e("LON", String.valueOf(lon));
		
		// Do a null check to confirm that we have not already instantiated the map.
	    if (map == null) {

	        MapFragment a = ((MapFragment) getFragmentManager().findFragmentById(R.id.GMap));
	        
	        //Makes sure that the map fragment exists before attempting to inflate the map
	        if (a != null) {
	        	map = a.getMap();
	        } else {
	        	Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_LONG).show();
	        }
	        
	        // Check if we were successful in obtaining the map.
	        if (map != null) {
	        	
	        	//Checks to see if we have custom coordinates so we can zoom in
	        	if (lat != 43.6481 || lon != -79.4042) {
	        		map.moveCamera(CameraUpdateFactory.zoomTo(14));
	        	}
	        	
	        	//Sets the initial camera position
	        	map.moveCamera(CameraUpdateFactory.newLatLng(coords));
	        }
	    }
	}
}
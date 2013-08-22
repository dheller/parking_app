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

public class MapActivity extends Activity{
    
	private GoogleMap map;
	
	//Da variables
	Intent intent;
	double lat;
	double lon;
	LatLng coords;
	LatLng searchHere;
	
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        //Inflates the action bar and sets a couple of variables
        ActionBar bar = getActionBar();
        bar.hide();
        
        //Makes sure Google Play Services are installed
        int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        //Get the intent and sees if there is a starting coordinate value.  If not, assigns general Toronto coordinates
		intent = getIntent();
		lat = intent.getDoubleExtra(HomeActivity.lat_name, 43.6481);
		lon = intent.getDoubleExtra(HomeActivity.lon_name, -79.4042);
		coords = new LatLng(lat,lon);
        
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
        	Toast.makeText(getBaseContext(), "Google Play Services not installed", Toast.LENGTH_LONG).show();
        } else {       
        	setUpMapIfNeeded();
        }
        
	}
	
	private void setUpMapIfNeeded() {
		
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
	        		map.moveCamera(CameraUpdateFactory.zoomTo(16));
	        	} else {
	        		map.moveCamera(CameraUpdateFactory.zoomTo(12));
	        	}
	        	
	        	//Sets the initial camera position
	        	map.moveCamera(CameraUpdateFactory.newLatLng(coords));
	        	
	        	if (lat != 43.6481 || lon != -79.4042) {
	        	
		        	//Generates the polygons
		        	generatePolygons();
		        	
		        	//Marks the current search point
		        	MarkerOptions marker = new MarkerOptions().position(coords).title("Search Zone");
		        	map.addMarker(marker);
	        	}
	        	
	        	map.setOnMapClickListener(new OnMapClickListener() {
	        		
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
	
	int turnRiskIntoColor(double risk) {
		
		double red;
		double green;
		
		if (risk <= .5) {
			green = 255;
			red = 255 * (2 * risk);
		} else {
			red = 255;
			green = (255 - (255 * risk)) * 2;
		}
		
		int red_int = (int) red;
		int green_int = (int) green;
	
		return Color.argb(80, red_int, green_int, 0);
	}
	
	public void generatePolygons() {
		
		for (int x = 0; x<5; x++) {
			for (int y = 0; y<5; y++) {
				
	        	int color = turnRiskIntoColor(ResultActivity.risks.get(x + (y * 5)));
				
	        	double lat_adj = x * .002;
	        	double lon_adj = y * .002;
	        	
	            map.addPolygon(new PolygonOptions()
	            .add(new LatLng(lat + .005 - lat_adj, lon + .005 - lon_adj),
	            		new LatLng(lat + .005 - lat_adj, lon + .003 - lon_adj),
	            		new LatLng(lat + .003 - lat_adj, lon + .003 - lon_adj),
	            		new LatLng(lat + .003 - lat_adj, lon + .005 - lon_adj))
	            .fillColor(color)
	            .strokeColor(Color.argb(0, 0, 0, 0)));

			}
		}
	}
	
	public void mapSearch(View view) {
		
		if (searchHere == null) {
			Toast.makeText(getBaseContext(), "Click somewhere on the map to add a search marker", Toast.LENGTH_LONG).show();
		} else {
		
			double lat_map = searchHere.latitude;
			double lon_map = searchHere.longitude;
			
			searchHere = null;
			
			Intent intent = new Intent(this, HomeActivity.class);
			intent.putExtra(HomeActivity.lat_via_map_name, String.valueOf(lat_map));
			intent.putExtra(HomeActivity.lon_via_map_name, String.valueOf(lon_map));
			startActivity(intent);
		}
	}
}
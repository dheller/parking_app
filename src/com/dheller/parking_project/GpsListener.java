package com.dheller.parking_project;

import java.util.Calendar;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GpsListener implements LocationListener {
	
	// flag for GPS status
	public boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	Location location;
	double latitude;
	double longitude;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 1;

	// Declaring a Location Manager
	protected LocationManager locationManager;
	
	private Context mContext;
	
	public GpsListener(Context context) {
		this.mContext = context;
	    
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        //Makes sure that GPS is enabled in the device
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //Makes sure network access is enabled in the device
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        if (isGPSEnabled == false && isNetworkEnabled == false) {
            Log.e("NETWORK", "NO GPS OR NETWORK");
		} else {
			this.canGetLocation = true;
			
			if (isNetworkEnabled) {
				
				ResultActivity.loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, this);

			} else {
				
				ResultActivity.loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, this);

			}
		}
	}
	
	public Location getLocation() {
		try {
	        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

	        //Makes sure that GPS is enabled in the device
	        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

	        //Makes sure network access is enabled in the device
	        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	        
	        //Checks if either the network or GPS is available before continuing
	        if (isGPSEnabled == false && isNetworkEnabled == false) {
	            Log.e("NETWORK", "NO GPS OR NETWORK");
	        } else {
	        	this.canGetLocation = true;
	            
	        	//If the network is enabled, get lon and lat
	        	if (isNetworkEnabled) {
	        		locationManager.requestLocationUpdates(
	                        LocationManager.NETWORK_PROVIDER,
	                        MIN_TIME_BW_UPDATES,
	                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                if (locationManager != null) {
	                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                    
	                    //Grabs the time of the last update
	                    long last_update = location.getTime();
	                    long current = System.currentTimeMillis();

	                    //Checks to see if the current location is actually new
	                    if (current - last_update > (1000 * 60 * 2)) {
	                    	Log.e("Old location", "Fix this somehow");
	                    } else {
		                    
	                    	//If the location is new and exists, pull the latitude and longitude
	                    	if (location != null) {
		                        ResultActivity.loc = location;
	                    		
	                    		latitude = location.getLatitude();
		                        longitude = location.getLongitude();
		                    }
	                    }
	                }
	            }
	            // if GPS Enabled get lat/long using GPS Services
	            if (isGPSEnabled) {
	            	if (location == null) {
	                	locationManager.requestLocationUpdates(
	                            LocationManager.GPS_PROVIDER,
	                            MIN_TIME_BW_UPDATES,
	                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                	
	                	if (locationManager != null) {

	                		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	                        if (location != null) {
	                        	ResultActivity.loc = location;
			                    long banana = location.getTime();
			                    Log.e("BAAaaaa", String.valueOf(banana));
	                        	
	                            latitude = location.getLatitude();
	                            longitude = location.getLongitude();
	                        }
	                    }
	            	}
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return location;
	}
	
	public double getLatitude(Location location) {
	    if (location != null) {
	        latitude = location.getLatitude();
	    }
	    return latitude;
	}

	public double getLongitude(Location location) {
	    if (location != null) {
	        longitude = location.getLongitude();
	    }
	    return longitude;
	}
	
	public boolean canGetLocation() {
	    return this.canGetLocation;
	}
	
	@Override
	public void onLocationChanged(Location loc) {
		ResultActivity.loc = loc;
		location = loc;
		Log.e("you wish", "as if this would work");
	}
	
	@Override
	public void onProviderEnabled(String provider) {
	}
	
	@Override
	public void onProviderDisabled(String provider) {
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
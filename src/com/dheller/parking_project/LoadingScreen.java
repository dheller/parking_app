package com.dheller.parking_project;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class LoadingScreen extends Activity  
{  
    //A ProgressDialog object
    private ProgressDialog progressDialog;  
	Intent intent = new Intent(this, ResultActivity.class);
	GpsListener mGPS = new GpsListener(this);
	
    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        //Initialize a LoadViewTask object and call the execute() method  
        new LoadViewTask().execute();         
    }  
  
    //To use the AsyncTask, it must be subclassed  
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>  
    {  
        //Before running code in separate thread  
        @Override  
        protected void onPreExecute()  
        {  
            //Create a new progress dialog  
            progressDialog = new ProgressDialog(LoadingScreen.this);  
            //Set the progress dialog to display a horizontal progress bar  
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
            //Set the dialog title to 'Loading...'  
            progressDialog.setTitle("Loading...");  
            //Set the dialog message to 'Loading application View, please wait...'  
            progressDialog.setMessage("Loading application View, please wait...");  
            //This dialog can't be canceled by pressing the back key  
            progressDialog.setCancelable(false);  
            //This dialog isn't indeterminate  
            progressDialog.setIndeterminate(false);  
            //The maximum number of items is 100  
            progressDialog.setMax(100);  
            //Set the current progress to zero  
            progressDialog.setProgress(0);  
            //Display the progress dialog  
            progressDialog.show();  
        }  
  
        //The code to be executed in a background thread.  
        @Override  
        protected Void doInBackground(Void... params)  
        {  
            
            Intent HomeIntent = getIntent();
        	
        	//Creates variables to store the current time and duration of stay
        	String duration = HomeActivity.options.getItemAtPosition(HomeActivity.options.getSelectedItemPosition()).toString();
        	Integer hour = HomeIntent.getIntExtra(HomeActivity.hour_name, 0);
        	
        	//Stores the timing variables in the intent
        	intent.putExtra(HomeActivity.duration_name, duration);
        	intent.putExtra(HomeActivity.hour_name, hour);
        	
        	//Grabs the address
        	String final_lookup = HomeIntent.getStringExtra(HomeActivity.lookup_name);
        	
        	//Grabs the checkbox preferences
        	boolean use_current = HomeIntent.getBooleanExtra(HomeActivity.current_name, false);
        	
        	//Creates variables necessary to geocode the user inputted address
        	Geocoder geocode = new Geocoder(getApplicationContext(), Locale.getDefault());
        	List<Address> fromLocationName = null;
        	
        	//Checks to make sure a valid address was entered and that the user doesn't want to use their current location
        	if (final_lookup != null && !final_lookup.isEmpty() && !use_current) {
    	    	
        		try {
    				fromLocationName = geocode.getFromLocationName(final_lookup, 1);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
        		
            	//Geocodes the address if one exists
            	if (fromLocationName != null && fromLocationName.size() > 0) {
            	    Address a = fromLocationName.get(0);
                	double lat = a.getLatitude();
                	double lon = a.getLongitude();
                	
                	//Passes the coordinates to the result activity and starts it
                	intent.putExtra(HomeActivity.lat_name, lat);
                	intent.putExtra(HomeActivity.lon_name, lon);
                	startActivity(intent);
            	} else {
            		Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_LONG).show();    		        		
            	}	
        	}
        	
        	//Finds the current location of the user
        	else if (use_current) {
        		
        		if(mGPS.canGetLocation){

    	    		double lat = mGPS.getLatitude();
    	    		double lon = mGPS.getLongitude();
    	
    	        	//Passes the coordinates to the result activity and starts it
    	        	intent.putExtra(HomeActivity.lat_name, lat);
    	        	intent.putExtra(HomeActivity.lon_name, lon);
        		
        		} else {
        			Log.e("HELP", "Hmm, couldn't get the location");
        		}    		
    	    }
        	
        	//Prompts the user to enter an address or choose to use their current location
        	else {
        		Log.e("NO ADDRESS/CURRENT", "IDIOT");
        	}
        	
        	return null;  
        }  
  
        //Update the progress  
        @Override  
        protected void onProgressUpdate(Integer... values)  
        {  
            //set the current progress of the progress dialog  
            progressDialog.setProgress(values[0]);  
        }  
  
        //after executing the code in the thread  
        @Override  
        protected void onPostExecute(Void result)  
        {  
            //close the progress dialog  
            progressDialog.dismiss();  
            //initialize the View  
            startActivity(intent);
        }
    }
}  
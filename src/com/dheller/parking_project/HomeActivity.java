package com.dheller.parking_project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	//Declares name variables used to identify data passed through intents
	static String lat_name = "com.dheller.parking_project.LATITUDE";
	static String lon_name = "com.dheller.parking_project.LONGITUDE";
	static String duration_name = "com.dheller.parking_project.DURATION";
	static String hour_name = "com.dheller.parking_project.HOUR";
	
	//Declares opening variables for the search screen
	private Spinner options;
	Button button_map;
	Button button_search;
	EditText lookup;
	CheckBox current_location;
	TimePicker timePicker;
	static public String data;
	
	//OnCreate is called when the app is opened
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Initializes the spinner object
        options = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 
        		R.array.length_of_stay, R.layout.row);
        adapter.setDropDownViewResource(R.layout.title);
        options.setAdapter(adapter);
        options.setSelection(0);
        
        //Opens the file containing all the parking data and converts it to a string
        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream inputStream = assetManager.open("final.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder builder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            data = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    //Creates intent to start the map view
    public void GoToMap(View view) {
    	Intent intent = new Intent(this, MapActivity.class);
    	startActivity(intent);
    }
    
    //Creates intent to display search results
    public void GoToResults(View view) {
    	Intent intent = new Intent(this, ResultActivity.class);
    	
    	//Creates variables to store the current time and duration of stay
    	timePicker = (TimePicker) findViewById(R.id.time_picker);
    	String duration = options.getItemAtPosition(options.getSelectedItemPosition()).toString();
    	Integer hour = timePicker.getCurrentHour();
    	Integer minute = timePicker.getCurrentMinute();
    	
    	//Stores the timing variables in the intent
    	intent.putExtra(duration_name, duration);
    	intent.putExtra(hour_name, hour);
    	
    	//Creates a variable storing the address the user wishes to lookup
    	lookup = (EditText) findViewById(R.id.lookup);
    	String final_lookup = lookup.getText().toString();
    	
    	//Creates a variable to store the checkbox value
    	current_location = (CheckBox) findViewById(R.id.current_location);
    	boolean use_current = current_location.isChecked();
    	
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
            	intent.putExtra(lat_name, lat);
            	intent.putExtra(lon_name, lon);
            	startActivity(intent);
        	} else {
        		Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_LONG).show();    		        		
        	}	
    	}
    	
    	//Finds the current location of the user
    	else if (use_current) {

    		GpsListener mGPS = new GpsListener(this);
    		
    		if(mGPS.canGetLocation){

	    		double lat = mGPS.getLatitude();
	    		double lon = mGPS.getLongitude();
	
	        	//Passes the coordinates to the result activity and starts it
	        	intent.putExtra(lat_name, lat);
	        	intent.putExtra(lon_name, lon);
	        	startActivity(intent);
    		
    		} else {
    			Log.e("HELP", "Hmm, couldn't get the location");
    		}    		
	    }
    	
    	//Prompts the user to enter an address or choose to use their current location
    	else {
    		Toast.makeText(getBaseContext(), "Please enter an address or choose \"use current location\"", Toast.LENGTH_LONG).show();
    	}
    }
}
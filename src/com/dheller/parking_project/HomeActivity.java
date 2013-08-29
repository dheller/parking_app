package com.dheller.parking_project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	//Declares name variables used to uniquely identify data passed through intents
	static String lat_name = "com.dheller.parking_project.LATITUDE";
	static String lon_name = "com.dheller.parking_project.LONGITUDE";
	static String duration_name = "com.dheller.parking_project.DURATION";
	static String hour_name = "com.dheller.parking_project.HOUR";
	static String lookup_name = "com.dheller.parking_project.LOOKUP";
	static String current_name = "com.dheller.parking_project.CURRENT";
	static String error_geocode_name = "com.dheller.parking_project.geocode";
	static String error_gps_name = "com.dheller.parking_project.gps";
	static String error_loc_name = "com.dheller.parking_project.loc";
	static String risk_name = "com.dheller.parking_project.risk";
	static String lat_via_map_name = "com.dheller.parking_project.lat";
	static String lon_via_map_name = "com.dheller.parking_project.lon";
	
	//Declares various opening variables for the search screen
	static Spinner options;
	Button button_map;
	Button button_search;
	static EditText lookup;
	static CheckBox current_location;
	static TimePicker timePicker;
	static public String data;
	static int hour = 100;
	
	//OnCreate is called when the app is opened
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Inflates the main action bar and sets the title
        ActionBar bar = getActionBar();
        bar.setTitle("Search by Address");
        bar.setDisplayHomeAsUpEnabled(false);
        bar.show();
        
        //Initializes the checkbox object and sets it to checked by default
        CheckBox now = (CheckBox) findViewById(R.id.now);
        now.setChecked(true);
        
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
    
    //onResume is called after onCreate and if the activity is resumed instead of newly created
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	//Requests the intent which opened the activity
    	Intent intent = getIntent();
    	
    	//Collects various error messages stored within the intent
    	String error_geocode = intent.getStringExtra(error_geocode_name);
    	String error_gps = intent.getStringExtra(error_gps_name);
    	String error_loc = intent.getStringExtra(error_loc_name);
    	
    	//Checks which (if any) error message exists and generates a toast notification for the user
    	if (error_geocode != null) {
    		Log.e("JJ", "d'd");
    		Toast.makeText(getBaseContext(), error_geocode, Toast.LENGTH_LONG).show();
    	} else if (error_gps != null) {
    		Toast.makeText(getBaseContext(), error_gps, Toast.LENGTH_LONG).show();
    	} else if (error_loc != null) {
    		Toast.makeText(getBaseContext(), error_loc, Toast.LENGTH_LONG).show();
    	}
    	
    	//Pulls the latitude and longitude if the intent came from the map screen
    	String lat = intent.getStringExtra(lat_via_map_name);
    	String lon = intent.getStringExtra(lon_via_map_name);
    	
    	//Cleans the intent of the old coordinates
    	intent.putExtra(lat_via_map_name, "");
    	intent.putExtra(lon_via_map_name, "");
    	
    	//Checks to see if coordinates came with the intent
    	if (lat != null && lon != null && !lat.isEmpty() && !lon.isEmpty()) {
	    	
    		//Populates the address search box with the supplied coordinates
	    	lookup = (EditText) findViewById(R.id.lookup);
	    	lookup.setText(lat + ", " + lon);
    	}
    	
    }
    
    //Method to create intent to start the map activity
    public void GoToMap(View view) {
    	Intent intent = new Intent(this, MapActivity.class);
    	startActivity(intent);
    }
    
    //Method to create intent to search an address
    public void GoToResults(View view) {
    	Intent intent = new Intent(this, ResultActivity.class);
    	
    	//Loads the checkbox to see if the user wants to park now
    	CheckBox now = (CheckBox) findViewById(R.id.now);
    	
    	//Verifies that the user has entered a time to start parking
    	if (hour == 100 && !now.isChecked()) {
    		Toast.makeText(getBaseContext(), "Please choose a start time", Toast.LENGTH_LONG).show();
    		return;
    	} else if (now.isChecked()) {
    		Time current = new Time();
    		current.setToNow();
    		hour = current.hour;
    	}
    	
    	//Stores the current hour in the intent and resets it to the defaul value
    	intent.putExtra(hour_name, hour);
    	hour = 100;
    	
    	//Creates a variable storing the address the user wishes to lookup
    	lookup = (EditText) findViewById(R.id.lookup);
    	String final_lookup = lookup.getText().toString();
    	
    	//Tries to make sure Google understands this should be a toronto address and adds it to the intent
    	if ((final_lookup.contains("toronto")
    			|| final_lookup.contains("Toronto")
    			|| final_lookup.contains("Ontario")
    			|| final_lookup.contains("ontario")
    			|| final_lookup.contains("Canada")
    			|| final_lookup.contains("canada"))
    			|| final_lookup.isEmpty()) {
    		
    		intent.putExtra(lookup_name, final_lookup);
    	} else {
    		final_lookup = final_lookup + ", Toronto, Ontario, Canada";
    		intent.putExtra(lookup_name,  final_lookup);
    	}

    	//Creates a variable to store the checkbox value determining which location to use
    	current_location = (CheckBox) findViewById(R.id.current_location);
    	boolean use_current = current_location.isChecked();
    	intent.putExtra(current_name, use_current);
    	
    	//Verifies the user has either entered an address or used their current location, but not both
    	if ((lookup != null && !final_lookup.isEmpty()) && use_current) {
    		Toast.makeText(getBaseContext(), "Uncheck \"use current location\" to search for a specific address", Toast.LENGTH_LONG).show();
    	} else if ((lookup != null && !final_lookup.isEmpty())) {
    		if (isNetworkAvailable()) {
    			startActivity(intent);
    		} else {
    			Toast.makeText(getBaseContext(), "No network connection available.", Toast.LENGTH_LONG).show();
    		}
    	} else if (use_current) {
    		startActivity(intent);
    	} else {
    		Toast.makeText(getBaseContext(), "Please select \"use current location\" or enter an address", Toast.LENGTH_LONG).show();
    	}
    	
    }
    
    //Checks for network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    //Method that creates an intent to go to the about page
    public void GoToAbout(View view) {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
    }
    
    //Called when the ActionBar is initialized
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        
        MenuItem item = menu.getItem(0);
        item.getSubMenu().getItem(1).setVisible(false);
        
        return true;
    }
    
    //Called when users select an option from the ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	
    	if (item.toString().equals("About")) {
    		GoToAbout(findViewById(R.layout.activity_result));
    		return true;
    	} else if (item.toString().equals("Map")) {
    		GoToMap(findViewById(R.layout.activity_result));
    		return true;
    	}
    	
    	return true;
    }
    
    //Called if the user chooses to enter a custom start time for parking
    public void ParkLater (View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
        
        //Unchecks the checkbox if it isn't already
        CheckBox now = (CheckBox) findViewById(R.id.now);
        now.setChecked(false);
    }
    
    //Some brilliant code that makes the keyboard go away if you tap away from it
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
    return ret;
    }
    
}
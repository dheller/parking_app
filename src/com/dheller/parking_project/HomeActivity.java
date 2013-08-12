package com.dheller.parking_project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

public class HomeActivity extends Activity {
	
	//Declares name variables used to identify data passed through intents
	static String lat_name = "com.dheller.parking_project.LATITUDE";
	static String lon_name = "com.dheller.parking_project.LONGITUDE";
	static String duration_name = "com.dheller.parking_project.DURATION";
	static String hour_name = "com.dheller.parking_project.HOUR";
	static String lookup_name = "com.dheller.parking_project.LOOKUP";
	static String current_name = "com.dheller.parking_project.CURRENT";
	
	//Declares opening variables for the search screen
	static Spinner options;
	Button button_map;
	Button button_search;
	static EditText lookup;
	static CheckBox current_location;
	static TimePicker timePicker;
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
    	Intent intent = new Intent(this, LoadingScreen.class);
    	
    	//This was being gay so I need to do it in this class
    	timePicker = (TimePicker) findViewById(R.id.time_picker);
    	Integer hour = timePicker.getCurrentHour();
    	intent.putExtra(hour_name, hour);    
    	
    	//Creates a variable storing the address the user wishes to lookup
    	lookup = (EditText) findViewById(R.id.lookup);
    	String final_lookup = lookup.getText().toString();
    	intent.putExtra(lookup_name, final_lookup);
    	
    	//Creates a variable to store the checkbox value
    	current_location = (CheckBox) findViewById(R.id.current_location);
    	boolean use_current = current_location.isChecked();
    	intent.putExtra(current_name, use_current);
    	
    	startActivity(intent);
    	
    }
}
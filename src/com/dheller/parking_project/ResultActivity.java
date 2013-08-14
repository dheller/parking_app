package com.dheller.parking_project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

public class ResultActivity extends Activity{
    
    //A ProgressDialog object  
    private ProgressDialog progressDialog;  
	Intent intent = new Intent(this, ResultActivity.class);
	
	//Various objects on the results screen
	static ProgressBar progressBar;
	
	//Static maximum and minimum coordinates, other variables
	double min_lat = 43.586584;
	double min_lon = -79.639299;
	double max_lat = 43.8517;
	double max_lon = -79.121999;
	int number_of_tickets;
	int average_ticket_price;
	int tickets_at_time;
	int tickets_at_next_time;
	int tickets_by_day;
	double risk_factor;
	int lat_index;
	int lon_index;
	int duration_length;
	int next_section;
	double lat;
	double lon;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Initialize a LoadViewTask object that handles the heavy lifting
        new LoadViewTask().execute();

	}
	
	//Handles the logic associated with searching for the proper information
	public void Search(double lat, double lon, int hour, String duration, HashMap<String, StringMap> map) {
	    
	    //Changes the coordinates to indexes
		lat = (lat - min_lat) / .002;
	    lat = Math.floor(lat); 
	    lon = (lon - min_lon) / .002;
	    lon = Math.floor(lon);
	    
	    //Makes sure that the log function doesn't explode with 0s as input
	    if (lat != 0 && lon != 0) {	    	
	    	lat_index = (int) lat;
		    lon_index = (int) lon;
		    
	    } else {
	    	Log.e("Coordinates", "Somewhere you went wrong and got 0s for coordinates");
	    	lat_index = 0;
	    	lon_index = 0;
	    }
	    
		//Grabs the proper parking data from the list
		ArrayList array = (ArrayList) map.get(String.valueOf(lat_index)).get(String.valueOf(lon_index));
		
		//I hate how I'm doing this.  Should come back to it at some point.
		//Creates a temp variable to get the length of the string.
		int temp1 = array.get(0).toString().length();
		int temp2 = array.get(1).toString().length();
		number_of_tickets = Integer.parseInt(array.get(0).toString().substring(0, temp1 - 2));
		average_ticket_price = Integer.parseInt(array.get(1).toString().substring(0, temp2 - 2));
		
		//Pulls the number of tickets at the current time and the next time period
		if (hour < 8) {
			int temp = array.get(3).toString().length();
			int temp3 = array.get(4).toString().length();
			tickets_at_time = Integer.parseInt(array.get(3).toString().substring(0, temp - 2));
			tickets_at_next_time = Integer.parseInt(array.get(4).toString().substring(0, temp3 - 2));
		} else if (hour < 17) {
			int temp = array.get(4).toString().length();
			int temp3 = array.get(5).toString().length();
			tickets_at_time = Integer.parseInt(array.get(4).toString().substring(0, temp - 2));
			tickets_at_next_time = Integer.parseInt(array.get(5).toString().substring(0, temp3 - 2));
		} else {
			int temp = array.get(5).toString().length();
			int temp3 = array.get(3).toString().length();
			tickets_at_time = Integer.parseInt(array.get(5).toString().substring(0, temp - 2));
			tickets_at_next_time = Integer.parseInt(array.get(3).toString().substring(0, temp3 - 2));
		}
		
		//Figures out how long until the start of the next section
		next_section = hour % 8;
		
		//Grabs the # of weekend tickets
		tickets_by_day = Integer.parseInt(array.get(2).toString().substring(0, 1));
	}
	
	//This is the algorithm to calculate how risky a given parking decision is
	public void riskFactor(int number_of_tickets, int tickets_at_time, int tickets_at_next_time, int tickets_by_day, int length_of_stay) {
		Log.e("#OFTIX", String.valueOf(number_of_tickets));
		
		//Makes sure the number of tickets in the zone isn't 0 before calculating the log
		if (number_of_tickets > 0) {
			
			double current_danger = tickets_at_time / (double) number_of_tickets;
			double future_danger = tickets_at_next_time / (double) number_of_tickets;
			double length_risk = 0;
			int count = 0;
			
			Log.e("NEXT", String.valueOf(next_section));
			
			//Adds up the risk associated with hours in the current time block
			while (count <= next_section && count < length_of_stay) {
				length_risk = length_risk + current_danger;
				count = count + 1;
			}			
			
			//Calculates number of hours in the second time block
			double total_length_of_stay = length_of_stay;
			length_of_stay = length_of_stay - count;
			count = 0;
			
			//Adds up the risk associated with hours in the next time block
			while (count <= length_of_stay) {
				length_risk = length_risk + future_danger;
				count = count + 1;
			}
			
			length_risk = length_risk * number_of_tickets * total_length_of_stay;
			risk_factor = length_risk / (200 + length_risk);
			
			if (risk_factor > 1) {
				risk_factor = 1;
			}
			
		} else {
			risk_factor = 0;
		}
	}
	
	//Don't think you use this anywhere anymore.  Schedule for deletion
	public double logOfBase(int base, double num) {
	    return Math.log(num) / Math.log(base);
	}
	
	//To use the AsyncTask, it must be subclassed  
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>  
    {  
        //This initializes the loading screen
        @Override  
        protected void onPreExecute()  
        {  
        	progressDialog = ProgressDialog.show(ResultActivity.this,"Calculating...",  
        		    "Calculating risk, please wait...", false, false);
        }  
  
        //The code to be executed in a background thread.  
        @SuppressWarnings("unchecked")
		@Override  
        protected Void doInBackground(Void... params)  
        {  
        	Looper.prepare();
        	GpsListener mGPS = new GpsListener(getApplicationContext());
            Intent HomeIntent = getIntent();
            
    	    //Gets the intent and pulls relevant data from it
    	    int hour = HomeIntent.getIntExtra(HomeActivity.hour_name, 0);
        	String duration = HomeActivity.options.getItemAtPosition(HomeActivity.options.getSelectedItemPosition()).toString();
        	
        	//Converts the duration to a workable amount of time
        	if (!duration.equals("Less than 1 hour") && !duration.equals("More than 8 hours")) {
        		duration_length = Integer.parseInt(duration);
        	} else if (duration.equals("Less than 1 hour")) {
        		duration_length = 1;
        	} else {
        		duration_length = 8;
        	}
        	
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
                	lat = a.getLatitude();
                	lon = a.getLongitude();
                	
            	} else {
            		Log.e("FOOL", "WHAT WRONG WITH YOU FOOL");	        		
            	}	
        	}
        	
        	//Finds the current location of the user
        	else if (use_current) {
        		
        		if(mGPS.canGetLocation){

    	    		lat = mGPS.getLatitude();
    	    		lon = mGPS.getLongitude();
        		
        		} else {
        			Log.e("HELP", "Hmm, couldn't get the location");
        		}    		
    	    }
        	
        	//Prompts the user to enter an address or choose to use their current location
        	else {
        		Log.e("NO ADDRESS/CURRENT", "IDIOT");
        	}
        	
    	    //Generates the HashMap of parking ticket information
    		Gson gson = new Gson();
    		HashMap <String, StringMap> map = new HashMap <String, StringMap>();
    		map = (HashMap <String, StringMap>) gson.fromJson(HomeActivity.data, map.getClass());

    		if (map != null) {
	    	    //Searches the parking list for data
	            Search(lat, lon, hour, duration, map);
	            riskFactor(number_of_tickets, tickets_at_time, tickets_at_next_time, tickets_by_day, duration_length);
    		} else {
    			Log.e("MAP IS NULL", "WHYYYY");
    		}
            
            Log.e("RISKY?", String.valueOf(risk_factor));
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
            setContentView(R.layout.activity_result);
            
            //Sets the progress bar to the appropriate risk factor
            progressBar = (ProgressBar) findViewById(R.id.risk);
            progressBar.setProgress((int) (risk_factor * 100));
            
            //Defines the various text fields to be set
            TextView number_of_tickets_text = (TextView) findViewById(R.id.number_of_tickets);
            TextView average_ticket_price_text = (TextView) findViewById(R.id.average_ticket_price);
            TextView recommendation = (TextView) findViewById(R.id.recommendation);
            
            //Sets the text fields to the appropriate values
            number_of_tickets_text.setText(String.valueOf(number_of_tickets));
            average_ticket_price_text.setText("$" + String.valueOf(average_ticket_price));
            recommendation.setText("You probably shouldn't rely on an app for committing crimes, partner.");
            
        }
    }

    public void GoToMap(View view) {
    	Intent intent = new Intent(this, MapActivity.class);
    	
    	//Stores the starting coordinates for the map
    	intent.putExtra(HomeActivity.lat_name, lat);
    	intent.putExtra(HomeActivity.lon_name, lon);

    	startActivity(intent);
    }
    
    public void GoToSearch(View view) {
    	Intent intent = new Intent(this, HomeActivity.class);
    	startActivity(intent);
    }
}
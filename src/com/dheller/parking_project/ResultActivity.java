package com.dheller.parking_project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	int number_of_tickets_real;
	int average_ticket_price_real;
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
	static Location loc;
	String error_geocode;
	String error_gps;
	String error_loc;
	
	//Where risks go
    public static ArrayList<Double> risks = new ArrayList();
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Initialize a LoadViewTask object that handles the heavy lifting
        new LoadViewTask().execute();

	}
	
	//Handles the logic associated with searching for the proper information
	public ArrayList Search(double lat, double lon, int hour, String duration, HashMap<String, StringMap> map) {
	    
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
	    
	    //This is the array of all the ticket data associated with the given coords
	    ArrayList<ArrayList> values_list = new ArrayList();
	    
	    //This gets the appropriate ticket information for each box on the map
	    for (int x=0;x<5;x++) {
	    	for (int y=0;y<5;y++) {
	    		values_list.add((ArrayList) map.get(String.valueOf(lat_index - 2 + x)).get(String.valueOf(lon_index - 2 + y)));
	    	}
	    }
		
	    //This parses the information for each box and appends it to the values_list
	    for (int i = 0; i < 25; i++) {
	    	
	    	ArrayList array = values_list.get(i);
	    	
			//I hate how I'm doing this.  Should come back to it at some point.
			//Creates a temp variable to get the length of the string. Then uses that variable to chop off the decimals
	    	//so it can be properly evaluated as an integer
			int temp1 = array.get(0).toString().length();
			int temp2 = array.get(1).toString().length();
			number_of_tickets = Integer.parseInt(array.get(0).toString().substring(0, temp1 - 2));
			average_ticket_price = Integer.parseInt(array.get(1).toString().substring(0, temp2 - 2));
			
			//Grabs the # of weekend tickets
			tickets_by_day = Integer.parseInt(array.get(2).toString().substring(0, 1));
			
			//Pulls the number of tickets for the current time and the next time period
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

			//Sets the appropriate values to the values list
			ArrayList fixed = new ArrayList();
			fixed.add(number_of_tickets);
			fixed.add(average_ticket_price);
			fixed.add(tickets_by_day);
			fixed.add(tickets_at_time);
			fixed.add(tickets_at_next_time);
			
			values_list.set(i, fixed);
			
			//Scope shit is making this necessary.  Lazy way to keep track of the actual number of tickets (as opposed to nearby zones)
			if (i == 12) {
				number_of_tickets_real = number_of_tickets;
				average_ticket_price_real = average_ticket_price;
			}
			
	    }
		//Figures out how long until the start of the next section
		next_section = hour % 8;
		
		return values_list;
		
	}
	
	//This is the algorithm to calculate how risky a given parking decision is
	public double riskFactor(int number_of_tickets, int tickets_at_time, int tickets_at_next_time, int tickets_by_day, int length_of_stay) {
		
		//Makes sure the number of tickets in the zone isn't 0 before calculating the log
		if (number_of_tickets > 0) {
			
			double current_danger = tickets_at_time / (double) number_of_tickets;
			double future_danger = tickets_at_next_time / (double) number_of_tickets;
			double length_risk = 0;
			int count = 0;
			
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
		
		return risk_factor;
		
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
       	
        	//Who knows what this does
        	try {
        		Looper.prepare();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	
        	//Starts the GPS listener early cause it sucks and is slow
        	GpsListener mGPS = new GpsListener(getApplicationContext());
        	
    	    //Gets the intent and pulls relevant data from it
        	Intent HomeIntent = getIntent();
        	int hour = HomeIntent.getIntExtra(HomeActivity.hour_name, 0);
        	String duration = HomeActivity.options.getItemAtPosition(HomeActivity.options.getSelectedItemPosition()).toString();
        	
        	//Converts the duration to a workable amount of time
        	if (!duration.equals("Less than 1 hour") && !duration.equals("More than 8 hours")) {
        		duration_length = Integer.parseInt(duration.substring(0,1));
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
    	    	
        		//Gets the geocoded coordinates from the address string
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
            		Log.e("Ouch", "Probably the address is invalid or the network isn't work");
        			
            		error_geocode = "Couldn't find the address. Are you sure your network is working and the address is correct?";
            		
            		//Shuts down the results screen if the address can't be geocoded
                    progressDialog.dismiss();
        			GoToSearch(findViewById(R.layout.activity_result));
        			cancel(true);
            	}	
        	}
        	
        	//Finds the current location of the user
        	else if (use_current) {
        		if (mGPS.canGetLocation && loc != null) {
    	    		lat = mGPS.getLatitude(loc);
    	    		lon = mGPS.getLongitude(loc);
        		} else {
        			Log.e("HELP", "Hmm, couldn't get the location");
        			
        			error_gps = "Couldn't get the location using GPS";
        			
        			//Shuts down the results screen if the GPS can't find anything
                    progressDialog.dismiss();
        			GoToSearch(findViewById(R.layout.activity_result));
        			cancel(true);
        		}    		
    	    }
        	
        	//Prompts the user to enter an address or choose to use their current location.  Shouldn't ever end up here.
        	else {
        		Log.e("NO ADDRESS/CURRENT", "IDIOT");
        	}
        	
        	//Checks to make sure the coordinates are within Toronto boundaries
        	if (lat > max_lat || lat < min_lat || lon > max_lon || lon < min_lon) {
    			
        		error_loc = "The address you searched was outside of Toronto's boundaries.";
        		
        		//Shuts down the results screen because the search was outside of Toronto
                progressDialog.dismiss();
    			GoToSearch(findViewById(R.layout.activity_result));
    			cancel(true);
        	}
        	
        	
    	    //Generates the HashMap of parking ticket information
    		Gson gson = new Gson();
    		HashMap <String, StringMap> map = new HashMap <String, StringMap>();
    		map = (HashMap <String, StringMap>) gson.fromJson(HomeActivity.data, map.getClass());

    		if (map != null) {
	    	    //Searches the parking list for data
	            ArrayList<ArrayList> values_list = Search(lat, lon, hour, duration, map);
	            	            
	            //Calculates risk factor for each box and generates an array	            
	            for (int i = 0; i<25;i++) {
	            	
	            	ArrayList<Integer> current = values_list.get(i);
	            	number_of_tickets = current.get(0);
	            	tickets_at_time = current.get(3);
	            	tickets_at_next_time = current.get(4);
	            	tickets_by_day = current.get(2);
	            	
	            	double risk = riskFactor(number_of_tickets, tickets_at_time, tickets_at_next_time, tickets_by_day, duration_length);
	            	
	            	if (risks.size() >= 25) {
	            		risks.set(i, risk);
	            	} else {
	            		risks.add(risk);
	            	}
	            }
    		} else {
    			Log.e("MAP IS NULL", "WHYYYY");
    		}
    		
    		//Kills the looper which I think will help with the crashes if you click too many buttons too fast
    		Looper.myLooper().quit();

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
            progressBar.setProgress((int) (risks.get(12) * 100));
            
            if (risks.get(12) < .33) {
            	progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_low));
            } else if (risks.get(12) < .66) {
            	progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_mid));
            } else {
            	progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_high));
            }
            
            //Inflates the action bar and sets a couple of variables
            ActionBar bar = getActionBar();
            bar.setTitle("Search Results");
            bar.show();
            
            //Defines the various text fields to be set
            TextView number_of_tickets_text = (TextView) findViewById(R.id.number_of_tickets);
            TextView average_ticket_price_text = (TextView) findViewById(R.id.average_ticket_price);
            TextView recommendation = (TextView) findViewById(R.id.recommendation);
            
            //Sets the text fields to the appropriate values
            number_of_tickets_text.setText(String.valueOf(number_of_tickets_real));
            average_ticket_price_text.setText("$" + String.valueOf(average_ticket_price_real));
            
            if (risks.get(12) < .33) {
            	recommendation.setText("This is a low risk zone. Parking here is probably safe, but remember to always use caution.");
            } else if (risks.get(12) < .66) {
            	recommendation.setText("This is a medium risk zone. You're taking a moderate risk parking illegally here. Use caution and seek out a legal parking spot if possible.");
            } else {
            	recommendation.setText("This is a high risk zone. Parking here is extremely risky. Find a legal spot or check the map for a lower risk zone.");
            }
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
    	
    	if (error_geocode != null) {
    		intent.putExtra(HomeActivity.error_geocode_name, error_geocode);
    	} else if (error_gps != null) {
    		intent.putExtra(HomeActivity.error_gps_name, error_gps);
    	} else if (error_loc != null) {
    		intent.putExtra(HomeActivity.error_loc_name, error_loc);
    	}
    	startActivity(intent);
    }
    
    public void GoToAbout(View view) {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // use an inflater to populate the ActionBar with items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	
    	if (item.toString().equals("About")) {
    		GoToAbout(findViewById(R.layout.activity_result));
    		return true;
    	} else if (item.toString().equals("Map")) {
    		GoToMap(findViewById(R.layout.activity_result));
    		return true;
    	} else if (item.toString().equals("Search")) {
    		GoToSearch(findViewById(R.layout.activity_result));
    		return true;
    	}
    	
    	return true;
    	
    }
}
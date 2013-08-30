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

/* This is the main activity in the app.  It is responsible for running search queries,
 * calculating the risk score associated with said query and displaying the search results
 * view.
 */

public class ResultActivity extends Activity{
    
    //A ProgressDialog object  
    private ProgressDialog progressDialog;
    static ProgressBar progressBar;
    
    //Static maximum and minimum coordinates outlining Toronto, other required variables
    static final double minLat = 43.586584;
    static final double minLon = -79.639299;
    static final double maxLat = 43.8517;
    static final double maxLon = -79.121999;
    int numberOfTickets;
    int averageTicketPrice;
    int numberOfTicketsReal;
    int averageTicketPriceReal;
    int ticketsAtTime;
    int ticketsAtNextTime;
    int ticketsByDay;
    double riskFactor;
    int latIndex;
    int lonIndex;
    int durationLength;
    int nextSection;
    double lat;
    double lon;
    static Location loc;
    String errorGeocode;
    String errorGps;
    String errorLoc;
    final int riskIndex = 12;
    final double riskLow = .33;
    final double riskMid = .66;
    final double morning = 8;
    final double afternoon = 17;
    
    //Where the risks for each zone will live
    public static ArrayList<Double> risks = new ArrayList();
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Initialize a LoadViewTask object that handles the heavy lifting in a separate thread
        new LoadViewTask().execute();

    }
    
    //Handles the logic associated with searching for the proper information
    public ArrayList Search(double lat, double lon, int hour, String duration, HashMap<String, StringMap> map) {
        
        //Changes the coordinates to indexes representing zones
        lat = (lat - minLat) / .002;
        lat = Math.floor(lat);
        lon = (lon - minLon) / .002;
        lon = Math.floor(lon);
        
        //Makes sure that the log function doesn't explode with 0s as input
        if (lat != 0 && lon != 0) {            
            latIndex = (int) lat;
            lonIndex = (int) lon;
        } else {
            Log.e("Coordinates", "Somewhere you went wrong and got 0s for coordinates");
            latIndex = 0;
            lonIndex = 0;
        }
        
        //This is the array of all the ticket data associated with the given coords
        ArrayList<ArrayList> valuesList = new ArrayList();
        
        //This gets the appropriate ticket information for each of the 25 zones on the map
        for (int x=0;x<5;x++) {
            for (int y=0;y<5;y++) {
                valuesList.add((ArrayList) map.get(String.valueOf(latIndex - 2 + x)).get(String.valueOf(lonIndex - 2 + y)));
            }
        }
        
        //Parses the information for each box and appends it to the values_list
        for (int i = 0; i < 25; i++) {
            
            ArrayList array = valuesList.get(i);
            
            //I hate how I'm doing this.  Should come back to it at some point.
            //Creates a temp variable to get the length of the string. Then uses that variable to chop off the decimals
            //so it can be properly evaluated as an integer
            int temp1 = array.get(0).toString().length();
            int temp2 = array.get(1).toString().length();
            numberOfTickets = Integer.parseInt(array.get(0).toString().substring(0, temp1 - 2));
            averageTicketPrice = Integer.parseInt(array.get(1).toString().substring(0, temp2 - 2));
            
            //Grabs the # of weekend tickets
            ticketsByDay = Integer.parseInt(array.get(2).toString().substring(0, 1));
            
            //Pulls the number of tickets for the current time period and the upcoming time period
            if (hour < morning) {
                int temp = array.get(3).toString().length();
                int temp3 = array.get(4).toString().length();
                ticketsAtTime = Integer.parseInt(array.get(3).toString().substring(0, temp - 2));
                ticketsAtNextTime = Integer.parseInt(array.get(4).toString().substring(0, temp3 - 2));
            } else if (hour < afternoon) {
                int temp = array.get(4).toString().length();
                int temp3 = array.get(5).toString().length();
                ticketsAtTime = Integer.parseInt(array.get(4).toString().substring(0, temp - 2));
                ticketsAtNextTime = Integer.parseInt(array.get(5).toString().substring(0, temp3 - 2));
            } else {
                int temp = array.get(5).toString().length();
                int temp3 = array.get(3).toString().length();
                ticketsAtTime = Integer.parseInt(array.get(5).toString().substring(0, temp - 2));
                ticketsAtNextTime = Integer.parseInt(array.get(3).toString().substring(0, temp3 - 2));
            }

            //Sets the appropriate values to the values list
            ArrayList fixedArray = new ArrayList();
            fixedArray.add(numberOfTickets);
            fixedArray.add(averageTicketPrice);
            fixedArray.add(ticketsByDay);
            fixedArray.add(ticketsAtTime);
            fixedArray.add(ticketsAtNextTime);
            
            valuesList.set(i, fixedArray);
            
            //Scope problems are making this necessary. Lazy way to keep track of the number of tickets 
            // in the current zone (as opposed to nearby zones)
            if (i == riskIndex) {
                numberOfTicketsReal = numberOfTickets;
                averageTicketPriceReal = averageTicketPrice;
            }
            
        }
        //Figures out how long until the start of the next time period
        nextSection = hour % 8;
        
        return valuesList;
        
    }
    
    //This is the algorithm to calculate how risky a given parking decision is
    public double riskFactor(int numberOfTickets, int ticketsAtTime, int ticketsAtNextTime, int ticketsByDay, int lengthOfStay) {
        
        //Makes sure the number of tickets in the zone isn't 0 before calculating the log
        if (numberOfTickets > 0) {
            
            double currentDanger = ticketsAtTime / (double) numberOfTickets;
            double futureDanger = ticketsAtNextTime / (double) numberOfTickets;
            double lengthRisk = 0;
            int count = 0;
            
            //Adds up the risk associated with hours in the current time block
            while (count <= nextSection && count < lengthOfStay) {
                lengthRisk = lengthRisk + currentDanger;
                count = count + 1;
            }            
            
            //Calculates number of hours in the second time block
            double totalLengthOfStay = lengthOfStay;
            lengthOfStay = lengthOfStay - count;
            count = 0;
            
            //Adds up the risk associated with hours in the next time block
            while (count <= lengthOfStay) {
                lengthRisk = lengthRisk + futureDanger;
                count = count + 1;
            }
            
            lengthRisk = lengthRisk * numberOfTickets * totalLengthOfStay;
            riskFactor = lengthRisk / (200 + lengthRisk);
            
            if (riskFactor > 1) {
            	riskFactor = 1;
            }
            
        } else {
            riskFactor = 0;
        }
        
        return riskFactor;
        
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
           
            //Prepares the looper
            try {
                Looper.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            //Starts the GPS listener early cause it sucks and is slow
            GpsListener mGPS = new GpsListener(getApplicationContext());
            mGPS.getLocation();
            
            //Generates the HashMap of parking ticket information. This takes a long time
            //which I'm hoping will help with the GPS slowness
            Gson gson = new Gson();
            HashMap <String, StringMap> map = new HashMap <String, StringMap>();
            map = (HashMap <String, StringMap>) gson.fromJson(HomeActivity.data, map.getClass());
            
            //Gets the intent and pulls relevant data from it
            Intent HomeIntent = getIntent();
            int hour = HomeIntent.getIntExtra(HomeActivity.hourName, 0);
            String duration = HomeActivity.options.getItemAtPosition(HomeActivity.options.getSelectedItemPosition()).toString();
            
            //Converts the parking duration to a workable integer
            if (!duration.equals("Less than 1 hour") && !duration.equals("More than 8 hours")) {
                durationLength = Integer.parseInt(duration.substring(0,1));
            } else if (duration.equals("Less than 1 hour")) {
                durationLength = 1;
            } else {
                durationLength = 8;
            }
            
            //Grabs the address
            String finalLookup = HomeIntent.getStringExtra(HomeActivity.lookupName);
            
            //Grabs the checkbox preferences
            boolean useCurrent = HomeIntent.getBooleanExtra(HomeActivity.currentName, false);
            
            //Creates variables necessary to geocode the user inputted address
            Geocoder geocode = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> fromLocationName = null;
            
            //Checks to make sure a valid address was entered and that the user doesn't want to use their current location
            if (finalLookup != null && !finalLookup.isEmpty() && !useCurrent) {
                
                //Gets the geocoded coordinates from the address string
                try {
                    fromLocationName = geocode.getFromLocationName(finalLookup, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                //Geocodes the address if one exists
                if (fromLocationName != null && fromLocationName.size() > 0) {
                    Address a = fromLocationName.get(0);
                    lat = a.getLatitude();
                    lon = a.getLongitude();
                    
                } else {
                    Log.e("Ouch", "Probably the address is invalid or the network isn't working");
                    
                    errorGeocode = "Couldn't find the address. Are you sure your network is working and the address is correct?";
                    
                    //Shuts down the results screen if the address can't be geocoded
                    progressDialog.dismiss();
                    goToSearch(findViewById(R.layout.activity_result));
                    cancel(true);
                }    
            }
            
            //Finds the current location of the user
            else if (useCurrent) {
                if (mGPS.canGetLocation && loc != null) {
                    
                    mGPS.getLocation();
                    
                    lat = mGPS.getLatitude(loc);
                    lon = mGPS.getLongitude(loc);
                } else {
                    
                    errorGps = "Couldn't get the location using GPS";
                    
                    //Shuts down the results screen if the GPS can't find anything
                    progressDialog.dismiss();
                    goToSearch(findViewById(R.layout.activity_result));
                    cancel(true);
                }            
            }
            
            //Prompts the user to enter an address or choose to use their current location.  Shouldn't ever end up here.
            else {
                Log.e("NO ADDRESS/CURRENT", "WHOOPS");
            }
            
            //Checks to make sure the coordinates are within Toronto boundaries
            if (lat > maxLat || lat < minLat || lon > maxLon || lon < minLon) {
                
                errorLoc = "The address you searched was outside of Toronto's boundaries.";
                
                //Shuts down the results screen because the search was outside of Toronto
                progressDialog.dismiss();
                goToSearch(findViewById(R.layout.activity_result));
                cancel(true);
            }

            if (map != null) {
                //Searches the parking list for data
                ArrayList<ArrayList> valuesList = Search(lat, lon, hour, duration, map);
                                
                //Calculates risk factor for each box and generates an array                
                for (int i = 0; i<25;i++) {
                    
                    ArrayList<Integer> current = valuesList.get(i);
                    numberOfTickets = current.get(0);
                    ticketsAtTime = current.get(3);
                    ticketsAtNextTime = current.get(4);
                    ticketsByDay = current.get(2);
                    
                    double risk = riskFactor(numberOfTickets, ticketsAtTime, ticketsAtNextTime, ticketsByDay, durationLength);
                    
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
  
        //Starts after executing the code in the thread  
        @Override  
        protected void onPostExecute(Void result)  
        {  
            //close the progress dialog  
            progressDialog.dismiss();
            //initialize the View  
            setContentView(R.layout.activity_result);
            
            //Sets the progress bar to the appropriate risk factor
            progressBar = (ProgressBar) findViewById(R.id.risk);
            progressBar.setProgress((int) (risks.get(riskIndex) * 100));
            
            if (risks.get(riskIndex) < riskLow) {
                progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_low));
            } else if (risks.get(riskIndex) < riskMid) {
                progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_mid));
            } else {
                progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_high));
            }
            
            //Inflates the action bar and sets a couple of variables
            ActionBar bar = getActionBar();
            bar.setTitle("Search Results");
            bar.show();
            
            //Defines the various text fields to be set
            TextView numberOfTicketsText = (TextView) findViewById(R.id.number_of_tickets);
            TextView averageTicketPriceText = (TextView) findViewById(R.id.average_ticket_price);
            TextView recommendation = (TextView) findViewById(R.id.recommendation);
            
            //Sets the text fields to the appropriate values
            numberOfTicketsText.setText(String.valueOf(numberOfTicketsReal));
            averageTicketPriceText.setText("$" + String.valueOf(averageTicketPriceReal));
            
            if (risks.get(riskIndex) < riskLow) {
                recommendation.setText("This is a low risk zone. Parking here is probably safe, but remember to always use caution.");
            } else if (risks.get(riskIndex) < riskMid) {
                recommendation.setText("This is a medium risk zone. You're taking a moderate risk parking illegally here. Use caution and seek out a legal parking spot if possible.");
            } else {
                recommendation.setText("This is a high risk zone. Parking here is extremely risky. Find a legal spot or check the map for a lower risk zone.");
            }
        }
    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        
        //Stores the starting coordinates for the map
        intent.putExtra(HomeActivity.latName, lat);
        intent.putExtra(HomeActivity.lonName, lon);

        startActivity(intent);
    }
    
    public void goToSearch(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        
        if (errorGeocode != null) {
            intent.putExtra(HomeActivity.errorGeocodeName, errorGeocode);
        } else if (errorGps != null) {
            intent.putExtra(HomeActivity.errorGpsName, errorGps);
        } else if (errorLoc != null) {
            intent.putExtra(HomeActivity.errorLocName, errorLoc);
        }
        startActivity(intent);
    }
    
    public void goToAbout(View view) {
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
            goToAbout(findViewById(R.layout.activity_result));
        } else if (item.toString().equals("Map")) {
            goToMap(findViewById(R.layout.activity_result));
        } else if (item.toString().equals("Search")) {
            goToSearch(findViewById(R.layout.activity_result));
        }
        
        return true;
        
    }
}
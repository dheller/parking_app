package com.dheller.parking_project;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

public class ResultActivity extends Activity{
    
	//Static maximum and minimum coordinates, other variables
	double min_lat = 43.586584;
	double min_lon = -79.639299;
	double max_lat = 43.8517;
	double max_lon = -79.121999;
	int number_of_tickets;
	int average_ticket_price;
	int tickets_at_time;
	int tickets_by_day;
	double risk_factor;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
	    //Gets the intent and pulls relevant data from it
	    Intent intent = getIntent();
	    double lat = intent.getDoubleExtra(HomeActivity.lat_name, 0);
	    double lon = intent.getDoubleExtra(HomeActivity.lon_name, 0);
	    int hour = intent.getIntExtra(HomeActivity.hour_name, 0);
	    String duration = intent.getStringExtra(HomeActivity.duration_name);
        
	    //Generates the HashMap of parking ticket information
		Gson gson = new Gson();
		HashMap <String, StringMap> map = new HashMap <String, StringMap>();
		map = (HashMap <String, StringMap>) gson.fromJson(HomeActivity.data, map.getClass());
	    
	    //Searches the parking list for data
        Search(lat, lon, hour, duration, map);
        
        TextView a = (TextView) findViewById(R.id.number_of_tickets);
        TextView b = (TextView) findViewById(R.id.number_of_tickets_time);
        TextView c = (TextView) findViewById(R.id.number_of_tickets_weekend);
        
        a.setText(String.valueOf(number_of_tickets));
        b.setText(String.valueOf(average_ticket_price));
        c.setText(String.valueOf(tickets_at_time));
	}
	
	public void Search(double lat, double lon, int hour, String duration, HashMap<String, StringMap> map) {
	    
	    //Changes the coordinates to indexes
	    lat = (lat - min_lat) / .002;
	    lat = Math.floor(lat); 
	    lon = (lon - min_lon) / .002;
	    lon = Math.floor(lon);
	    int lat_index = (int) lat;
	    int lon_index = (int) lon;
	    
		//Grabs the proper parking data from the list
		ArrayList array = (ArrayList) map.get(String.valueOf(lat_index)).get(String.valueOf(lon_index));
		
		//I hate how I'm doing this.  Should come back to it at some point.  Creates a temp variable to get the length of the string.
		int temp1 = array.get(0).toString().length();
		int temp2 = array.get(1).toString().length();
		number_of_tickets = Integer.parseInt(array.get(0).toString().substring(0, temp1 - 2));
		average_ticket_price = Integer.parseInt(array.get(1).toString().substring(0, temp2 - 2));
		
		//Pulls the number of tickets at the given time
		if (hour < 8) {
			int temp = array.get(3).toString().length();
			tickets_at_time = Integer.parseInt(array.get(3).toString().substring(0, temp - 2));
		} else if (hour < 17) {
			int temp = array.get(4).toString().length();
			tickets_at_time = Integer.parseInt(array.get(4).toString().substring(0, temp - 2));
		} else {
			int temp = array.get(5).toString().length();
			tickets_at_time = Integer.parseInt(array.get(5).toString().substring(0, temp - 2));
		}
		
		//Grabs the # of weekend tickets
		tickets_by_day = Integer.parseInt(array.get(2).toString().substring(0, 1));
	}
}
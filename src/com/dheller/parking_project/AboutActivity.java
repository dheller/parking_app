package com.dheller.parking_project;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class AboutActivity extends Activity{
	
	protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);
        
        //Inflates the action bar and sets a couple of variables
        ActionBar bar = getActionBar();
        bar.setTitle("About");
        bar.show();
        
        TextView google = (TextView) findViewById(R.id.google);
        google.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
        
	}
	
	 @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // use an inflater to populate the ActionBar with items
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        
        MenuItem item = menu.getItem(0);
        item.getSubMenu().getItem(0).setVisible(false);
        
        return true;
    }
	    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	
    	if (item.toString().equals("Search")) {
    		GoToSearch(findViewById(R.layout.activity_result));
    		return true;
    	} else if (item.toString().equals("Map")) {
    		GoToMap(findViewById(R.layout.activity_result));
    		return true;
    	}
    	
    	return true;
    }
    
    public void GoToSearch (View view) {
    	Intent intent = new Intent (this, HomeActivity.class);
    	startActivity(intent);
    }
    
    public void GoToMap (View view) {
    	Intent intent = new Intent (this, MapActivity.class);
    	startActivity(intent);
    }
}
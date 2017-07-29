 /* Copyright (C) 2017 Todd Murphey's Lab
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sacgames.hopper;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sacgames.R;
import com.sacgames.R.id;
import com.sacgames.R.layout;

public class MainActivity2 extends Activity {
	public final static String EXTRA_MESSAGE = "com.polebalance.MESSAGE";
	private Button resumebutton;
	private Button new_game, ok;
	private Button high_scores;
	private ProgressBar progressbar;
	private ProgressDialog progress;
	private RadioButton radio_easy, radio_medium, radio_hard, radio_inv;
	private RadioButton yes, no;
	public static SharedPreferences[] gamePrefs = new SharedPreferences[3];
	public static SharedPreferences username;
	public static final String USERFILE = "USER";
	public EditText user_name2;
	public static String current_user = "Empty";
	
	//These variables store the important states etc
	public static float[] saved_states = {0,0,0,0,0,0,0};//this is to recreate state after pause etc
	public static int counter = 0;//this is to recreate timer value
	public static double dir = 0;
	public static float angle = 0;
	public static boolean collision = false;
	public static int no_lives = 0;//number of lives
    public static Projectiles[] projectile = new Projectiles[21];//this is to restore the state of the active projectiles. 
    public static SensorManager mSensorManager;
    public static Sensor mAccelerometer;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);//set layout
        
        //Get layout identifiers
        radio_easy = (RadioButton)findViewById(R.id.radio_easy2);
        radio_medium = (RadioButton)findViewById(R.id.radio_medium2);
        radio_hard = (RadioButton)findViewById(R.id.radio_hard2);
        radio_inv = (RadioButton)findViewById(R.id.radio_hard_invisible);
        yes = (RadioButton)findViewById(R.id.yes);
        no = (RadioButton)findViewById(R.id.no);
        resumebutton = (Button)findViewById(R.id.resumebutton2);//extract resume button from xml to disable it when appropriate  
        new_game = (Button)findViewById(R.id.new_2); 
        high_scores = (Button)findViewById(R.id.high_scores);
        ok = (Button)findViewById(R.id.set_user);
        
        progressbar = (ProgressBar) findViewById(R.id.progressBar2);
        progress = new ProgressDialog(this);
        
	    TextView diff_level = (TextView) findViewById(R.id.diff_level);
	    TextView user_name = (TextView) findViewById(R.id.user_name);
	    TextView accel = (TextView) findViewById(R.id.accel);
	    user_name2 = (EditText) findViewById(R.id.user_name2);
        	    
	    //Set fonts - I could do that better but I am too lazy
	    //Typeface tf = Typeface.createFromAsset(getAssets(),"3Dumb.ttf");
	    Typeface tf = Typeface.createFromAsset(getAssets(),"JUNEBUG_.ttf");
	    diff_level.setTypeface(tf);
	    resumebutton.setTypeface(tf);
	    new_game.setTypeface(tf);
	    accel.setTypeface(tf);
	    yes.setTypeface(tf);
	    no.setTypeface(tf);
	    radio_easy.setTypeface(tf);
	    radio_medium.setTypeface(tf);
	    radio_hard.setTypeface(tf);
	    radio_inv.setTypeface(tf);
	    high_scores.setTypeface(tf);
	    user_name.setTypeface(tf);
	    user_name2.setTypeface(tf);
	    ok.setTypeface(tf);

		//File creation mode: the default mode, where the created file can only be accessed by the calling application
		for(int i = 0;i<3;i++) 
			gamePrefs[i] = getSharedPreferences(HighScore.GAME_PREFS[i], Context.MODE_PRIVATE);

    	username = getSharedPreferences(USERFILE, Context.MODE_PRIVATE);//open file for username storage

	    //Enable button if there is an accelerometer
	    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    if (mAccelerometer == null){
	      // NO ACCELEROMETER!!
	    	accel.setVisibility(View.INVISIBLE);
	    	yes.setVisibility(View.INVISIBLE);
	    	no.setVisibility(View.INVISIBLE);
	      }
		
	}
	
	
	@Override
	public void onResume(){
	    super.onResume();
	    Log.i("myApp", "Main Resuming");
	    
	    if((saved_states[2]==0 && saved_states[3]==0) ){//Simple condition checking whether the initial state has changed in order to activate resume button
	    	resumebutton.setEnabled(false);
	    } else {
	    	resumebutton.setEnabled(true);
	    }
	    
	    progressbar.setVisibility(View.INVISIBLE);
	    //progress.cancel();
	    //make sure the radio buttons is checked if appropriate
	    if(HopperActivity.accelerometer) 
	    	yes.setChecked(true);
	    if(GameView2.total_active_projectiles == 4){
	    	if(!Projectiles.invisible)
	    		radio_hard.setChecked(true);
	    	else
	    		radio_inv.setChecked(true);
		}else if(GameView2.total_active_projectiles == 4)
	    	radio_medium.setChecked(true);
	    
	    
	    //Set the current username in the edittext
		current_user = username.getString("username", "");
		
		if(current_user.length()>0)   
			user_name2.setText(current_user);
	    
	}
	
	
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    
	    unbindDrawables(findViewById(R.id.RootView1));//release resources (bitmaps etc) (otherwise I get out of memory error!!)
	    System.gc();
	    
	    Log.i("myApp", "Destroying");
	}
	
    
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
        view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
        ((ViewGroup) view).removeAllViews();
        }
    }
	
	
    /** Called when the user clicks the new button */
    public void startnewGame2(View view) {

    	progressbar.setVisibility(View.VISIBLE);//show the progressbar
    	
    	final MainActivity2 temp = this;
        Handler handler = new Handler(); 
        handler.postDelayed(new Runnable() { //need to create the activity in a separate thread, otherwise the UI freezes when I press the button
        	//and the progressbar does not appear
             public void run() { 
            	 Intent intent = new Intent(temp, HopperActivity.class); //create explicit intent to start game
             	intent.putExtra(EXTRA_MESSAGE, "new");//this will allow me to distinguish between a resume and a new game
             	
             	startActivity(intent);
             } 
        }, 500);   	
    }
    
    /** Called when the user clicks the resume button */
    public void resumeGame2(View view) {
    	Intent intent = new Intent(this, HopperActivity.class); //create explicit intent to start game
    	intent.putExtra(EXTRA_MESSAGE, "resume");//this will allow me to distinguish between a resume and a new game

    	startActivity(intent);    	
    }
    
    
    public void onRadioButtonClicked2(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_easy2:
                if (checked) {
                	resumebutton.setEnabled(false);
                	GameView2.total_active_projectiles = 2;
                	Projectiles.invisible = false;}
                break;
            case R.id.radio_medium2:
                if (checked) {
                	resumebutton.setEnabled(false);
                	GameView2.total_active_projectiles = 3;
                	Projectiles.invisible = false;}
                break;
            case R.id.radio_hard2:
                if (checked) {
                	resumebutton.setEnabled(false);
                	GameView2.total_active_projectiles = 4;
                	Projectiles.invisible = false;}
                break;
            case R.id.radio_hard_invisible:
                if (checked) {
                	resumebutton.setEnabled(false);
                	GameView2.total_active_projectiles = 4;
                	Projectiles.invisible = true;}
                break;
        }
    }

    public void onAccelclicked(View view) {
    	
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.yes:
                if (checked) 
                	HopperActivity.accelerometer = true;//we will  be using the accelerometer
                break;
            case R.id.no:
                if (checked) 
                	HopperActivity.accelerometer = false;//we will not be using the accelerometer
                break;
        }
    }    
    
    /** Called when the user clicks the high score button */
    public void high_scores_screen(View view) {
		
    	Intent intent = new Intent(this, HighScore.class); //create explicit intent to start game

    	startActivity(intent); 
	
    }
    
    
    public void set_username(View view) {
    	
    	SharedPreferences.Editor scoreEdit = username.edit();//open file to edit
    	scoreEdit.clear(); //clear file 
    	
		//write to entry to file
		scoreEdit.putString("username", user_name2.getText().toString());
		scoreEdit.commit();
		
		//Remove keyboard
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    
    
}

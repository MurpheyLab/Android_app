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
package com.sacgames.pendulumbalance;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.sacgames.R;


public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.polebalance.MESSAGE";
	public static float l = 6;//this is to set difficulty level
	public static float u_sat = 100;//this is to set saturation for user input in maxwell's demon shared control
	public static float length_ratio = 250f/370f;//heuristic (changes with difficulty)
	public static int TPS = 30;//this is to set difficulty level
	private Button resumebutton;
	private ProgressBar progressbar;
	private ProgressDialog progress;

	
	//These variables store the important states etc
	public static float[] saved_states = {0,0,0,0};//this is to recreate state after pause etc
	public static int counter = 0;//this is to recreate timer value
	public static float accepted_inputs = 0;
	public static float[] K = {0,0,0,0};
	public static boolean vib_cues = false;//this is for the togglebutton
	public static boolean vis_cues = false;//this is for the togglebutton
	public static boolean help = false;//this is for the togglebutton
	public static boolean SAC = false;//this is for the togglebutton
	public static boolean LQR_ON = false;//this is for the togglebutton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);//set layout
        resumebutton = (Button)findViewById(R.id.resumebutton);//extract resume button from xml to disable it when appropriate  
        
        progressbar = (ProgressBar) findViewById(R.id.progressBar1);
        progress = new ProgressDialog(this);
        
    }
    
    
	@Override
	public void onResume(){
	    super.onResume();
	    Log.i("myApp", "Main Resuming");
	    
	    if((saved_states[2]==0) || MainActivity.SAC){//Simple condition checking whether the initial state has changed in order to activate resume button
	    	resumebutton.setEnabled(false);
	    } else {
	    	resumebutton.setEnabled(true);
	    }
	    
	    progressbar.setVisibility(View.INVISIBLE);
	    //progress.cancel();
	    
	}
    
	
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    
	    unbindDrawables(findViewById(R.id.RootView0));//release resources (bitmaps etc) (otherwise I get out of memory error!!)
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
    public void startnewGame(View view) {
        //progress.setMessage("Loading...");
        //progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //progress.setIndeterminate(true);
        //progress.show();

    	progressbar.setVisibility(View.VISIBLE);//show the progressbar
    	
    	final MainActivity temp = this;
        Handler handler = new Handler(); 
        handler.postDelayed(new Runnable() { //need to create the activity in a separate thread, otherwise the UI freezes when I press the button
        	//and the progressbar does not appear
             public void run() { 
            	 Intent intent = new Intent(temp, PendulumActivity.class); //create explicit intent to start game
             	intent.putExtra(EXTRA_MESSAGE, "new");//this will allow me to distinguish between a resume and a new game
             	
             	startActivity(intent);
             } 
        }, 500);

    	
    }
    
    /** Called when the user clicks the resume button */
    public void resumeGame(View view) {
    	Intent intent = new Intent(this, PendulumActivity.class); //create explicit intent to start game
    	intent.putExtra(EXTRA_MESSAGE, "resume");//this will allow me to distinguish between a resume and a new game

    	startActivity(intent);    	
    }
    
    
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_easy:
                if (checked)
                    l = 6; //set pendulum length
                	TPS = 30;
                	resumebutton.setEnabled(false);
                	length_ratio = 250f/370f;
                	u_sat = 150;
                break;
            case R.id.radio_medium:
                if (checked)
                    l = 2.8f;
                	TPS = 30;
                	resumebutton.setEnabled(false);
                	length_ratio = 180f/370f;
                	u_sat = 100;
                break;
            case R.id.radio_hard:
                if (checked)
                    l = 1.0f;
                	TPS = 30;
                	resumebutton.setEnabled(false);
                	length_ratio = 120f/370f;
                	u_sat = 35;
                break;
        }
    }
    
   
}

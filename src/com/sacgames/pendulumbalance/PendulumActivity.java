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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sacgames.GameLoopThread;
import com.sacgames.R;

public class PendulumActivity extends Activity {
    public static GameView demo;
    private static String message;
    public static TextView countertext;
    private ToggleButton vib_cues;
    private ToggleButton vis_cues;
    private ToggleButton help;
    private ToggleButton SAC;
    private ToggleButton LQR_ON;
    private Button newbutton;
    public static Vibrator vibrator;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Log.i("myApp", "Creating");
		
		//Get message from the intent
		Intent intent = getIntent();
		message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		
		setContentView(R.layout.activity_pendulum);//set layout for game screen
		
		demo = (GameView) findViewById(R.id.gameview);//get the demo reference
		
		countertext = (TextView)findViewById(R.id.text);//extract counter text from xml
		
        vib_cues = (ToggleButton)findViewById(R.id.vib_cues); 
        vis_cues = (ToggleButton)findViewById(R.id.vis_cues);//extract toggle buttons
        help = (ToggleButton)findViewById(R.id.shared);
        SAC = (ToggleButton)findViewById(R.id.swing_up);
        LQR_ON = (ToggleButton)findViewById(R.id.lqr_on);
        newbutton = (Button)findViewById(R.id.new_game);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
	}

	@Override
	public void onPause(){
	    super.onPause();
	    Log.i("myApp", "Pausing");
	    
	    demo.gameLoopThread.setRunning(false);//block thread otherwise I get error. 
	    
	    //Save state shit for the case where the user presses the back button and then wants to come back (activity destroyed by user). 
	    //If I did not call finish right afterwards, this would also include pressing the Home button, but this case is also covered 
	    //by the system by default since the activity is not destroyed then. In general those should not be saved here but in onStop, 
	    //but it's only a few variables
	    MainActivity.saved_states = demo.state_vector;
	    MainActivity.counter = demo.counter;
	    MainActivity.accepted_inputs = demo.accepted_inputs;
	    MainActivity.K = demo.K;
	    MainActivity.vib_cues = vib_cues.isChecked();
	    MainActivity.vis_cues = vis_cues.isChecked();
	    MainActivity.help = help.isChecked();
	    MainActivity.SAC = SAC.isChecked();
	    MainActivity.LQR_ON = LQR_ON.isChecked();
	    
	    vibrator.cancel();
	    
	    finish();//if i put this, it cannot resume the second activity directly (this is how I have to do this in order to use the resume game button)
	    //calling finish also makes it necessary to use fixed orientations in the activity
	    
	    //Log.i("myApp", String.valueOf(demo.xSpeed));

	}
	
	@Override
	public void onResume(){
	    super.onResume();
	    
	    demo.gameLoopThread.setRunning(true);
	    
	    if(!demo.gameLoopThread.isAlive())
        {
             demo.gameLoopThread = new GameLoopThread(demo);//for some reason it does not resume without this - this is the drawing thread
        }
	    
	    
	    // Restore state if we are resuming
	    if(message.equals("resume")){
		    demo.resume = true;//restore states in graphics initialization function called when a new surface is created
		    //Restore Togglebuttons
		    vib_cues.setChecked (MainActivity.vib_cues);
		    if(MainActivity.vib_cues){//if the button help is on give cues
		    	demo.vib_cues = true;
		    	//startVibrate();
		    }
		    
		    vis_cues.setChecked (MainActivity.vis_cues);
		    if(MainActivity.vis_cues){//if the button help is on give cues
		    	demo.vis_cues = true;
		    }
		    
		    SAC.setChecked (MainActivity.SAC);
		    LQR_ON.setChecked(MainActivity.LQR_ON);
		    if(MainActivity.SAC){
		    	buttons_states ();
		    	
		    	if(MainActivity.LQR_ON){
		    		demo.lqr_on = true;
		    	}
			    
		    }
		    	
		    help.setChecked (MainActivity.help);
    	    if(MainActivity.help){//if the button help is on, the alternate with the controller
    		    //demo.help = true;
    	    	demo.help_button = true;
    	    }

	    } else {//start new game
	    	vibrator.cancel();
	    	demo.resume = false;
		    demo.need_lqr = true;// set that here so that when init_game_states runs in the surface_created callback, it solves the lqr problem 
		    //it has to be solved there because we need the width of the canvas to get appropriately scaled state
		    
	    }
	    
	    Log.i("myApp", "Resuming");
	    //Log.i("myApp", String.valueOf(MainActivity.x));
	}
	
	
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    Log.i("myApp", "Destroying");
	    
	    unbindDrawables(findViewById(R.id.RootView));//release resources (bitmaps etc) (otherwise I get out of memory error!!)
	    System.gc();
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
	
	
	@Override
	public void onStop(){
	    super.onStop();
	    Log.i("myApp", "Stopping");
	}
	
		
    /** Called when the user clicks the new button */
    public void resetGame(View view) {

    	demo.resume = false;
    	demo.init_game_states();  	//call initialization function 
    }
    
    public void onSharedClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {
	    	demo.help_button = true;
	    	demo.resume = false;
	    	demo.init_game_states();  	//call initialization function 
        } else {
	    	demo.help_button = false;
	    	demo.resume = false;
	    	demo.init_game_states();  	//call initialization function 
        }
    }
    
    public void onVibCuesClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {
        	demo.vib_cues = true;
        	//startVibrate();
	    	demo.resume = false;
	    	demo.init_game_states();  	//call initialization function 
        } else {
        	demo.vib_cues = false;
        	vibrator.cancel();
	    	demo.resume = false;
	    	demo.init_game_states();  	//call initialization function 
        }
    }
    
    public void onVisCuesClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {
        	demo.vis_cues = true;
	    	demo.resume = false;
	    	demo.init_game_states();  	//call initialization function 
        } else {
        	demo.vis_cues = false;
	    	demo.resume = false;
	    	demo.init_game_states();  	//call initialization function 
        }
    }
    
    public void onSACClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {//disable all other buttons and reset them
        	buttons_states ();
        	
        } else {
        	demo.SAC = false;
        	demo.lqr_on = false;
        	demo.resume = false;
        	//enable buttons
	    	vis_cues.setEnabled(true);
	    	vib_cues.setEnabled(true);
	    	help.setEnabled(true);
	    	LQR_ON.setVisibility(View.INVISIBLE);
	    	LQR_ON.setChecked(false);
	    	//newbutton.setEnabled(true);
	    	demo.init_game_states();
        }
    }
    
    
    private void buttons_states () {
    	demo.SAC = true;
    	demo.lqr_on = false;
    	demo.vis_cues = false;
    	demo.vib_cues = false;
    	vibrator.cancel();
    	demo.help_button = false;
    	demo.init_game_states();  	
    	
    	//disable other buttons
    	vis_cues.setEnabled(false);
    	vib_cues.setEnabled(false);
    	help.setEnabled(false);
    	LQR_ON.setVisibility(View.VISIBLE);
    	//newbutton.setEnabled(false);
    	
    	//uncheck those buttons
    	vis_cues.setChecked(false);
    	vib_cues.setChecked(false);
    	help.setChecked(false);
    }
    
    public void onLQRClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        
        if (on) {//disable all other buttons and reset them
        	demo.lqr_on = true;
        	
        } else {
        	demo.lqr_on = false;
        }
    }
    
    /*
    //handle progress bar using threads and handlers
    public void progress_open(){
    	 t = new Thread(new Runnable() {
    	     public void run() {
    	        while (progressStatus < 100) {
    	           progressStatus += 1;
    	    // Update the progress bar and display the current value in the text view
    	    handler.post(new Runnable() {
    	    public void run() {
    	       progressbar.setProgress(progressStatus);
    	       Log.i("myApp", String.valueOf(progressStatus));
    	       Log.i("myApp", "bla");
    	       //textView.setText(progressStatus+"/"+progressbar.getMax());
    	    }
    	        });
    	        try {
    	           // Sleep for 200 milliseconds just to display the progress slowly
    	           Thread.sleep(50);
    	        } catch (InterruptedException e) {
    	           e.printStackTrace();
    	        }
    	     }
    	  }
    	  });
    	t.start();
 
    }*/

}

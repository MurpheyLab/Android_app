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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sacgames.GameLoopThread;
import com.sacgames.R;

public class HopperActivity extends Activity implements SensorEventListener {
    public static GameView2 demo;
    private static String message;
    public static TextView countertext;
    public static TextView livestext;
    private static LinearLayout ll;
    private static int rotation;//provides rotation from "natural orientation"
    public static float mSensorX = 0;
    public static float mSensorZ = 0;
    public static float mSensorY = 0;//axes of sensor (they are always fixed in reality, but depending on the natural orientation and activity orientation
    //I will define the appropriate *new* x and y axes)

    public static boolean accelerometer = false;//are we using the accelerometer?
	private float f_cutoff = 10;//in Hz (used to filter angle)
	private float tau_cutoff = (float) (1/(2*Math.PI*f_cutoff));
	private float alpha = tau_cutoff/(tau_cutoff + 20/1000f);//this is used to filter angle
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//hide status bar
		
		//get rotation from natural orientation
		WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();		
	    rotation = display.getRotation();
	    //Log.i("myApp", String.valueOf(rotation));
	    

		//Get message from the intent
		Intent intent = getIntent();
		message = intent.getStringExtra(MainActivity2.EXTRA_MESSAGE);
		
		setContentView(R.layout.activity_hopper);
		
		demo = (GameView2) findViewById(R.id.gameview2);//get the demo reference
		
		countertext = (TextView)findViewById(R.id.text2);//extract counter text from xml
		livestext = (TextView)findViewById(R.id.text3);//extract lives text from xml
		
		//Set fonts
	    Typeface tf = Typeface.createFromAsset(getAssets(),"JUNEBUG_.ttf");
	    //countertext.setTypeface(tf);
	    livestext.setTypeface(tf);
	    
		
		//ll = (LinearLayout)findViewById(R.id.linlayout);//get linear layout id
    	
		Log.i("myApp", "Creating");
		//Log.i("myApp", String.valueOf(Build.VERSION.SDK_INT));
	}
	
	
	@Override
	public void onPause(){
	    super.onPause();
	    Log.i("myApp", "Pausing");
	    
	    //Unregister listener
	    if(accelerometer)
	    	MainActivity2.mSensorManager.unregisterListener(this);
	    
	    demo.gameLoopThread.setRunning(false);//block thread otherwise I get error. 
	    
	    //Save state shit for the case where the user presses the back button and then wants to come back (activity destroyed by user). 
	    //If I did not call finish right afterwards, this would also include pressing the Home button, but this case is also covered 
	    //by the system by default since the activity is not destroyed then. In general those should not be saved here but in onStop, 
	    //but it's only a few variables
	    MainActivity2.saved_states = demo.state_vector;
	    MainActivity2.counter = demo.counter;
	    MainActivity2.no_lives = demo.no_lives;
	    MainActivity2.dir = demo.dir;
	    MainActivity2.collision = demo.collision;
	    MainActivity2.angle = demo.angle;
	    //store states of active projectiles
		MainActivity2.projectile = demo.projectile; 

	    
	    finish();//if i put this, it cannot resume the second activity directly (this is how I have to do this in order to use the resume game button)
	    //calling finish also makes it necessary to use fixed orientations in the activity
	}
	
	@Override
	public void onResume(){
	    super.onResume();
	    
	    //Register listener for the sensor
	    if(accelerometer)
	    	MainActivity2.mSensorManager.registerListener(this, MainActivity2.mAccelerometer , SensorManager.SENSOR_DELAY_GAME);//CHANGE THAT POSSIBLY!!

	    
	 // Converts 45 dip into its equivalent px and uses that as the linear layout height
	    Resources r = getResources();
	    demo.llayout_height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, r.getDisplayMetrics());


    	
	    demo.gameLoopThread.setRunning(true);
	    
	    if(!demo.gameLoopThread.isAlive())
        {
             demo.gameLoopThread = new GameLoopThread(demo);//for some reason it does not resume without this - this is the drawing thread
        }
	    	    
	    // Restore state if we are resuming
	    if(message.equals("resume")){
		    demo.resume = true;//restore states in graphics initialization function called when a new surface is created
		    
	    } else {//start new game
	    	demo.resume = false;
	    }
	    
	    Log.i("myApp", "Resuming");

	}
	
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    
	    unbindDrawables(findViewById(R.id.RootView2));//release resources (bitmaps etc) (otherwise I get out of memory error!!)
	    System.gc();
	    
	    Log.i("myApp", "Destroying");
	}
	
	@Override
	public void onStop(){
	    super.onStop();
	    
	    Log.i("myApp", "Stopping");
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
    public void resetGame2(View view) {
    	demo.resume = false;
    	demo.new_game = true;
    	demo.init_game_states();  	//call initialization function 
    }
    
    public void arrow_left(View view) {
    	if(demo.dir > -0.8f)
    		demo.dir = demo.dir - 0.25f;
    	else
    		demo.dir = -1;
    	//Log.i("myApp", String.valueOf(demo.dir));
    }
    
    public void arrow_right(View view) {
    	if(demo.dir < 0.8f)
    		demo.dir = demo.dir + 0.25f;
    	else
    		demo.dir = 1;
    	//Log.i("myApp", String.valueOf(demo.dir));
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {//callback for sensor events
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        
        switch (rotation) {//depending on the angle from the natural orientation, we are reading different values from the sensor
            case Surface.ROTATION_0:
            	if(GameView2.isEqual(mSensorX, 0) && GameView2.isEqual(mSensorY, 0) && GameView2.isEqual(mSensorZ, 0)) {//if it's the first entry
	                mSensorX = event.values[0];
	                mSensorY = event.values[1];
	                mSensorZ = event.values[2];
            	} else {//low-pass filter measurement
                    mSensorX = alpha * mSensorX + (1 - alpha) * event.values[0];
                    mSensorY = alpha * mSensorY + (1 - alpha) * event.values[1];
                    mSensorZ = alpha * mSensorZ + (1 - alpha) * event.values[2];
            	}
                break;
            case Surface.ROTATION_90:
            	if(GameView2.isEqual(mSensorX, 0) && GameView2.isEqual(mSensorY, 0) && GameView2.isEqual(mSensorZ, 0)) {//if it's the first entry
	                mSensorX = -event.values[1];
	                mSensorY = event.values[0];
	                mSensorZ = event.values[2];
		    	} else {//low-pass filter measurement
		            mSensorX = alpha * mSensorX - (1 - alpha) * event.values[1];
		            mSensorY = alpha * mSensorY + (1 - alpha) * event.values[0];
		            mSensorZ = alpha * mSensorZ + (1 - alpha) * event.values[2];
		    	}
                break;
            case Surface.ROTATION_180:
            	if(GameView2.isEqual(mSensorX, 0) && GameView2.isEqual(mSensorY, 0) && GameView2.isEqual(mSensorZ, 0)) {//if it's the first entry
	                mSensorX = -event.values[0];
	                mSensorY = -event.values[1];
	                mSensorZ = event.values[2];
		    	} else {//low-pass filter measurement
		            mSensorX = alpha * mSensorX - (1 - alpha) * event.values[0];
		            mSensorY = alpha * mSensorY - (1 - alpha) * event.values[1];
		            mSensorZ = alpha * mSensorZ + (1 - alpha) * event.values[2];
		    	}
                break;
            case Surface.ROTATION_270:
            	if(GameView2.isEqual(mSensorX, 0) && GameView2.isEqual(mSensorY, 0) && GameView2.isEqual(mSensorZ, 0)) {//if it's the first entry
	                mSensorX = event.values[1];
	                mSensorY = -event.values[0];
	                mSensorZ = event.values[2];
		    	} else {//low-pass filter measurement
		            mSensorX = alpha * mSensorX + (1 - alpha) * event.values[1];
		            mSensorY = alpha * mSensorY - (1 - alpha) * event.values[0];
		            mSensorZ = alpha * mSensorZ + (1 - alpha) * event.values[2];
		    	}
                break;
        }
        

        //Compute the angle between the gravity vector (projected on the XY plane) and the Y axis of the phone based on the inner product
        //double mag_phone = Math.sqrt(mSensorY*mSensorY+mSensorX*mSensorX);
        //double mag_vertical = Math.sqrt(mSensorY*mSensorY);
        //double angle = Math.acos(mag_vertical/mag_phone);
        
        //angle of 2 relative to 1= atan2(v2.y,v2.x) - atan2(v1.y,v1.x)
        double angle = (180/Math.PI) * (Math.atan2(mSensorY, mSensorX) - Math.atan2(mSensorY, 0));
        

		//Log.i("myApp", String.valueOf(Math.atan2(mSensorX, mSensorY)/(Math.PI/180)));
        //Log.i("myApp", String.valueOf(angle));
        
        set_dir(angle);//sets direction of hopper based on accelerometer
        

    }
    
    private void set_dir( double angle) {//set direction based on accelerometer angle
    	if((angle > -5) && (angle < 5))
    		demo.dir = 0;
    	else if((angle > 5) && (angle < 15))
    		demo.dir = 0.25;
    	else if((angle > 15) && (angle < 25))
    		demo.dir = 0.5;
    	else if((angle > 25) && (angle < 35))
    		demo.dir = 0.75;
    	else if(angle > 35)
    		demo.dir = 1.0;
    	else if((angle > -15) && (angle < -5))
    		demo.dir = -0.25;
    	else if((angle > -25) && (angle < -15))
    		demo.dir = -0.5;
    	else if((angle > -35) && (angle < -25))
    		demo.dir = -0.75;
    	else if(angle < -35)
    		demo.dir = -1.0;
    }
    
    
    private double AngleWrap(double angle) {
        angle = (angle + Math.PI) % (2 * Math.PI);
        if ( angle < 0 ) { angle = angle + 2 * Math.PI; }
        return  (angle - Math.PI);
        
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
     
    }

}

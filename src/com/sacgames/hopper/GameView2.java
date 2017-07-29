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

import java.text.DecimalFormat;
import android.graphics.Region;
import java.util.Random;

//import Jama.Matrix;
import android.content.Context;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import com.sacgames.GameLoopThread;
import com.sacgames.R;
import com.sacgames.SACWrapper;
import com.sacgames.R.drawable;
import com.sacgames.pendulumbalance.GameView;
import com.sacgames.pendulumbalance.MainActivity;
import com.sacgames.pendulumbalance.Optimal_Controller;
import com.sacgames.pendulumbalance.Pendulum;

public class GameView2 extends GameView { //this class defines the custom view where the game will be played
    
	
	//These variables are for the display
    private SurfaceHolder holder;
    public GameLoopThread gameLoopThread;   
    public float width;//get GameView dimensions
    public float llayout_height;//get GameView dimensions
    public float height; 
    public int hopper_width;
    private int spring_width;
    public int hopper_height;
    private final float hopper_ratio = 48f/800f;//hopper ratio for multiple resolutions
    public int no_lives = 3;
    public final int total_no_projectiles = 21;
    public static int total_active_projectiles = 2;//set by the difficulty level
    public int active_projectiles;
    private Random rn = new Random();
    private Paint p_wall = new Paint();
    private Path ground_path;
    private Paint p_ground = new Paint();
	public static long startTime;//keep the sign up for some time
    
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Handler mHandler2 = new Handler(Looper.getMainLooper());

    public static boolean high_score_checked = false;//have we checked for a high score?
	public static boolean high_score = false;//no high score found
    public boolean resume = false;//when true, restore vars
    public boolean collision = false;
    public boolean new_game = false;
    
    private Bitmap bmp, bmp2, bmp3, game_over, hopper_down, new_high_score;
    private Bitmap sun, sun2, stop, brickwall, nature, go;
    public Projectiles[] projectile = new Projectiles[total_no_projectiles];
    
    //speedometer images
    private int[] speed_names = new int[]{R.drawable.meterzero, R.drawable.meterfirstright, R.drawable.metersecondright,  
    		R.drawable.meterthirdright, R.drawable.meterfourthright, R.drawable.meterfirstleft, R.drawable.metersecondleft, 
    		R.drawable.meterthirdleft, R.drawable.meterfourthleft};
    
    private Bitmap[] speed = new Bitmap[9];
    
    //initial screen names
    private int[] init_names = new int[]{R.drawable.three, R.drawable.two, R.drawable.one};
    
    private Bitmap[] init_screen = new Bitmap[3];
    


    
    //These are the variables that update the game state
    public float[] state_vector = new float[7];// these are set in the initialization function
    double[] xinit = new double[5];//initial value for swing up with SAC
	double dir = 0;
	float angle;//angle of hopper

    public int counter;//counter for the timer shown onscreen
    public int counter2;//counter to change pogoface
    public final int TPS = 25;//define frequency (game state is update at that freq and FPS is at maximum equal to that)
    private final float h = (float) 1/TPS;//integration step size and sampling frequency for SAC
	private float f_cutoff = 1;//in Hz (used to filter angle)
	private float tau_cutoff = (float) (1/(2*Math.PI*f_cutoff));
	private float alpha = tau_cutoff/(tau_cutoff + TPS/1000f);//this is used to filter angle
	    
    private float scalex_ratio = 85f/800f;//this is used to scale motion on x axis according to screen width in pixels (800 is my current screen width
    private float scaley_ratio = 70f/480f;//this is used to scale motion on x axis according to screen width in pixels (800 is my current screen width
    
    private float scalex;
    private float scaley;


    
    
    public GameView2(Context context) {
        super(context);
                
        init();//initialize view, game states etc
    }

    public GameView2(Context context, AttributeSet attrs) {
        super(context, attrs);
 
        init();//initialize view, game states etc
    }

    public GameView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
 
        init();//initialize view, game states etc
    }
    
    private void init(){
    	
        holder = getHolder(); // get holder of the surface/view        
        holder.addCallback(this);//notify SurfaceHolder that I would like to receive callbacks        
    	gameLoopThread = new GameLoopThread(this);           
    }
    
    
    public void init_game_states(){
    	
		if(!resume){
			high_score_checked = false;//have not checked for high score yet
			high_score = false;//no high score found
			
	        //Create instances of projectiles
	        projectile[0] = new Pikachu(1);
	        projectile[1] = new Dragon(1.5f);
	        projectile[2] = new Comet(2);
	        projectile[3] = new PhD(0.8f);
	        projectile[4] = new Hammer(1.3f);
	        projectile[5] = new Hulk(1.65f);
	        projectile[6] = new Rocket(1.4f);
	        projectile[7] = new Torpedo(1.4f);
	        projectile[8] = new Sword(1.25f);
	        projectile[9] = new DarthVader(1.0f);
	        projectile[10] = new Bomb(1.2f);
	        projectile[11] = new Dynamite(1.0f);
	        projectile[12] = new Parachute(0.6f);
	        projectile[13] = new Trex(1.4f);
	        projectile[14] = new Beer(0.8f);
	        projectile[15] = new HotDog(0.8f);
	        projectile[16] = new Darwin(1.3f);
	        projectile[17] = new Ghost(0.7f);
	        projectile[18] = new Sauron(1.0f);
	        projectile[19] = new Life(0.3f);
	        projectile[20] = new Dollar(0.7f);
	        
	    	counter = 0;//this is for the counter
	    	angle = 0;
	    	
	    	SACWrapper.initialize2(h, width/(2*scalex), total_active_projectiles - 1);//initialize stuff
	    	
	    	no_lives = 3;//number of lives
	    	dir = 0;//initial direction of hopper
	    	collision = false;//reset collision variable
	    	
	        for(int i=0; i<total_no_projectiles; i++)//make projectiles invisible
		    	projectile[i].visible = false;

	    	//Initialize game states
	    	state_vector[0] = width/2;//width/2;//put the hopper in the middle of the screen
	    	state_vector[1] = 0;//zero initial velocity
    		state_vector[2] = (float) (zero_ground() - 1.75 * scaley - hopper_height/2);
	    	state_vector[3] = 0;
	    	state_vector[4] = width/2;//zero toe position at the beginning
	    	state_vector[5] = 1*scaley ;//length of spring
	    	state_vector[6] = 0;//is the hopper down?	
    	
    	} else {//if we are resuming restore states etc
	    	counter = MainActivity2.counter;
	    	angle = MainActivity2.angle;
    		state_vector = MainActivity2.saved_states;
    		no_lives = MainActivity2.no_lives;
    	    dir = MainActivity2.dir;
	    	projectile = MainActivity2.projectile;
	    	collision = MainActivity2.collision;
		}
    	 
    	//Set no of lives
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This gets executed on the UI thread so it can safely modify Views
                HopperActivity.livestext.setText("X " + String.valueOf(no_lives));
            }
        });

       startTime = System.currentTimeMillis();
        
    }
    
    public void init_game_after_fall(){

    	angle = 0;

    	
    	no_lives = no_lives - 1;//number of lives
    	dir = 0;//initial direction of hopper
    	collision = false;//reset collision variable
    	
        for(int i=0; i<total_no_projectiles; i++)//make projectiles invisible
	    	projectile[i].visible = false;

    	//Initialize game states
    	//state_vector[0] = width/2;//width/2;//put the hopper in the middle of the screen
    	state_vector[1] = 0;//zero initial velocity
		state_vector[2] = (float) (zero_ground() - 1.75 * scaley - hopper_height/2);
    	state_vector[3] = 0;
    	state_vector[4] = state_vector[0];//zero toe position at the beginning
    	state_vector[5] = 1*scaley ;//length of spring
    	state_vector[6] = 0;//is the hopper down?
    	
    	//Set no of lives
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This gets executed on the UI thread so it can safely modify Views
                HopperActivity.livestext.setText("X " + String.valueOf(no_lives));
            }
        });
    }
    
    
    //I wrote a separate function for this because I need to find the dimensions of the provided canvas first.
    private void graphics_initialization() { 
    	
		width = getWidth();//get dimensions of canvas
		height = getHeight();
		//Log.i("myApp", String.valueOf(alpha));
		//Log.i("myApp", String.valueOf(height));
		scalex = scalex_ratio * width;
		scaley = scaley_ratio * height;
		
		hopper_width = Math.round(width * hopper_ratio);//calculate dimensions of hopper based on that
		hopper_height = Math.round(1.916f * hopper_width);
		spring_width = Math.round(hopper_width/7f);
		
		//Hopper
    	bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pogo);
        bmp = Bitmap.createScaledBitmap(bmp, hopper_width, hopper_height, true);        
    	bmp3 = BitmapFactory.decodeResource(getResources(), R.drawable.pogo2);
        bmp3 = Bitmap.createScaledBitmap(bmp3, hopper_width, hopper_height, true);
    	bmp2 = BitmapFactory.decodeResource(getResources(), R.drawable.spring);
    	
    	//game over image
    	game_over = BitmapFactory.decodeResource(getResources(), R.drawable.game_over);
        game_over = Bitmap.createScaledBitmap(game_over, (int)(width/3), (int)(3.0/12*width), true);
    	hopper_down = BitmapFactory.decodeResource(getResources(), R.drawable.hopper_down);
        hopper_down = Bitmap.createScaledBitmap(hopper_down, (int)width, (int)(width/6), true);
    	new_high_score = BitmapFactory.decodeResource(getResources(), R.drawable.new_high_score);
        new_high_score = Bitmap.createScaledBitmap(new_high_score, (int)(width/2), (int)(width/8), true);
    	
    	//sun
    	sun = BitmapFactory.decodeResource(getResources(), R.drawable.sun);
        sun = Bitmap.createScaledBitmap(sun, 2*hopper_width, 2*hopper_width, true);
    	sun2 = BitmapFactory.decodeResource(getResources(), R.drawable.sun2);
        sun2 = Bitmap.createScaledBitmap(sun2, 2*hopper_width, 2*hopper_width, true);
        
        //initial screen
    	go = BitmapFactory.decodeResource(getResources(), R.drawable.go);
        go = Bitmap.createScaledBitmap(go, 4*hopper_width, 2*hopper_width, true);
        
        for(int i = 0; i<3; i++) {
	    	init_screen[i] = BitmapFactory.decodeResource(getResources(), init_names[i]);
            init_screen[i] = Bitmap.createScaledBitmap(init_screen[i], (int) (2*hopper_width), (int) (4*hopper_width), true);
        }

        
        //speedometer images
        for(int i = 0; i<9; i++) {
	    	speed[i] = BitmapFactory.decodeResource(getResources(), speed_names[i]);
            speed[i] = Bitmap.createScaledBitmap(speed[i], (int) (2*hopper_width), (int) (2*hopper_width), true);
        }
        
    	//stop sign
    	stop = BitmapFactory.decodeResource(getResources(), R.drawable.stop);
        stop = Bitmap.createScaledBitmap(stop, (int) (1.5*hopper_width), (int) (1.5*hopper_width), true);
        
        //brick wall
    	brickwall = BitmapFactory.decodeResource(getResources(), R.drawable.brickwall);
        brickwall = Bitmap.createScaledBitmap(brickwall, (int) (1.5*hopper_width), 3*hopper_height, true);
        
        //backgrounf picture
    	nature = BitmapFactory.decodeResource(getResources(), R.drawable.nature);
        nature = Bitmap.createScaledBitmap(nature, (int) width, (int) height, true);
        
        
        //create sine
        create_ground();
        
   	
		init_game_states(); //initialize game states	
  
    }
    
    
    //Implement those callbacks defined in the interface SurfaceHolder.Callback so that we know when the surface is created/destroyed    

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
   	 //dont do anything as we won't be changing the size of the view
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	
    	graphics_initialization();//find where to draw things, set their colors etc
    	
        gameLoopThread.setRunning(true);
        gameLoopThread.start();//start game loop
    }

    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
           boolean retry = true;
           gameLoopThread.setRunning(false);
           
           while (retry) {
                  try {
                        gameLoopThread.join();//wait for game loop thread to die
                        retry = false;
                  } catch (InterruptedException e) {
                  }
           }
    }
    
    


    @Override
    protected void onDraw(Canvas canvas) { 

    	//this is to reset the game display after new game is pressed (otherwise it gets stuck)
    	if(new_game) {
    		while(state_vector[0] > width/2 || state_vector[1] > 0 || state_vector[0] < width/2)//CHANGE THIS TO THE NEW INITIAL CONDITION!!!!
    			init_game_states();
    		  	  //Log.i("myApp", String.valueOf(state_vector[0]));
        	new_game = false;
    	}

    	//canvas.drawColor(Color.BLUE);//draw background
    	
    	//draw background picture
		canvas.drawBitmap(nature, 0, 0, null);
    	
		//left, top, right, bottom
		//canvas.drawRect(0, height/3, hopper_width, height, p_wall);
		//canvas.drawRect(width - hopper_width, height/3, width, height, p_wall);
    	
    	//draw brick wall
		canvas.drawBitmap(brickwall, 0, height/3, null);
		canvas.drawBitmap(brickwall, (float) (width - 1.5*hopper_width), height/3, null);
		
		//draw stop sign
		canvas.drawBitmap(stop, 0, height/2, null);
		canvas.drawBitmap(stop, (float) (width - 1.5*hopper_width), height/2, null);
 
    	if((no_lives > 0) && (state_vector[6] < 1)) {
	    	//Draw sun
	        if((counter % TPS) > TPS/2)
	        	canvas.drawBitmap(sun, 0, 0, null);
	        else 
	        	canvas.drawBitmap(sun2, 0, 0, null);
	        
	        //draw speedometer
	        draw_speedometer(canvas);

	        
	        //Draw hopper
	        if(!collision) {	  	 
	        	//float angle = (float) Math.toDegrees(Math.asin((state_vector[0] - state_vector[4])/state_vector[5]));
	        	Rotate_Draw_Bitmap(bmp, bmp2, angle, state_vector[0] - hopper_width/2, state_vector[2] - hopper_height/2, canvas);
	        } else {
	        	//float angle = (float) Math.toDegrees(Math.asin((state_vector[0] - state_vector[4])/state_vector[5]));
	        	Rotate_Draw_Bitmap(bmp3, bmp2, angle, state_vector[0] - hopper_width/2, state_vector[2] - hopper_height/2, canvas);
	        	if(counter - counter2 > 2000/TPS)
	        		collision = false;//keep that face for a couple of seconds
	         }
	        
	        //Draw projectile
	        for(int i=0; i<total_no_projectiles; i++){
	    	if(projectile[i].visible)
	    		projectile[i].onDraw(canvas);
	        }	        
	        
	      //Update score value
	        update_counter();
    	} else{
	    	state_vector[5] = 1*scaley ;//length of spring
    		Rotate_Draw_Bitmap(bmp3, bmp2, 90, state_vector[0] - hopper_width/2, state_vector[2] - hopper_width, canvas);
    		if(no_lives <= 0) {
    			canvas.drawBitmap(game_over, width/2 - game_over.getWidth()/2, height/2 - game_over.getHeight()/2, null);//draw game over
    			
    			//look for and set highscore in the corresponding preference file (based on level of difficulty)
    			if(total_active_projectiles == 2)
    				HighScore.setHighScore((int)(counter*1/TPS), MainActivity2.gamePrefs[0]);
    			else if(total_active_projectiles == 3)
    				HighScore.setHighScore((int)(counter*1/TPS), MainActivity2.gamePrefs[1]);
    			else if(total_active_projectiles == 4){
    				if(!Projectiles.invisible)
    					HighScore.setHighScore((int)(counter*1/TPS), MainActivity2.gamePrefs[2]);
    			}
    			
    			if(high_score)
    				canvas.drawBitmap(new_high_score, width/2 - new_high_score.getWidth()/2, 0, null);//draw high score
    			Log.i("myApp", String.valueOf(high_score));
    		}
    		else {//hopper is down
    			canvas.drawBitmap(hopper_down, width/2 - hopper_down.getWidth()/2, height/2 - hopper_down.getHeight()/2, null);
    		}
    		
    	}

    	
    	canvas.drawPath(ground_path, p_ground);//draw ground
    	draw_countdown(canvas);//draw initial screen
}
    
    
    public void draw_countdown(Canvas canvas) {
    	if(no_lives > 0) {
			//this is for the initial screen!
	    	long temp = System.currentTimeMillis() - startTime;
	    	if((temp > 0) && (temp < 1000))
	    		canvas.drawBitmap(init_screen[0], width/2 - init_screen[0].getWidth()/2, height/2 - init_screen[0].getHeight()/2, null);
	    	else if((temp > 1000) && (temp < 2000))
	    		canvas.drawBitmap(init_screen[1], width/2 - init_screen[1].getWidth()/2, height/2 - init_screen[1].getHeight()/2, null);
	    	else if((temp > 2000) && (temp < 3000))
	    		canvas.drawBitmap(init_screen[2], width/2 - init_screen[2].getWidth()/2, height/2 - init_screen[2].getHeight()/2, null);
	    	else if((temp > 3000) && (temp < 4000))
	    		canvas.drawBitmap(go, width/2 - go.getWidth()/2, height/2 - go.getHeight()/2, null);
    	}
    	
    }
    
    
    public void draw_speedometer(Canvas canvas) {
        
        //Draw speedometer
    	if(isEqual(dir, 0.0))
    		canvas.drawBitmap(speed[0], width - speed[0].getWidth(), 0, null);
    	else if(isEqual(dir, 0.25))
    		canvas.drawBitmap(speed[1], width - speed[1].getWidth(), 0, null);
    	else if(isEqual(dir, 0.5))
    		canvas.drawBitmap(speed[2], width - speed[2].getWidth(), 0, null);
    	else if(isEqual(dir, 0.75))
    		canvas.drawBitmap(speed[3], width - speed[3].getWidth(), 0, null);
    	else if(isEqual(dir, 1.0))
    		canvas.drawBitmap(speed[4], width - speed[4].getWidth(), 0, null);
    	else if(isEqual(dir, -0.25))
    		canvas.drawBitmap(speed[5], width - speed[5].getWidth(), 0, null);
    	else if(isEqual(dir, -0.5))
    		canvas.drawBitmap(speed[6], width - speed[6].getWidth(), 0, null);
    	else if(isEqual(dir, -0.75))
    		canvas.drawBitmap(speed[7], width - speed[7].getWidth(), 0, null);
    	else//(isEqual(dir, -1.0))
    		canvas.drawBitmap(speed[8], width - speed[8].getWidth(), 0, null);
    	
    }
        
        public static boolean isEqual(double x, double y)
        {
          double epsilon = 1e-3;
          return Math.abs(x - y) <= epsilon;

        }
    
    public void Rotate_Draw_Bitmap(Bitmap source, Bitmap source2, float angle, float trans_x, float trans_y, Canvas canvas)
    {       
    	  int spring_height = (int) state_vector[5];//1+x;
    	
    	  //Draw hopper
          Matrix matrix = new Matrix();
          matrix.setRotate(angle, trans_x + hopper_width/2, trans_y + hopper_height + spring_height);
          matrix.preTranslate(trans_x, trans_y);
          canvas.drawBitmap(source,matrix,null);
          
          //draw spring
    	  Bitmap scaledBitmap2 = Bitmap.createScaledBitmap(source2, spring_width, spring_height, true); //scale the spring to look like real
          matrix.preTranslate((float) (hopper_width/2 - spring_width/2), hopper_height);
          canvas.drawBitmap(scaledBitmap2, matrix, null);
          //return Bitmap.createBitmap(bmp , 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

    }
    
    //update score value in the TextView of the main thread. Cannot do it directly from the worker game thread, so we need handlers..
    public void update_counter(){
    	final String curTime; 
    	
        //Update timer value
        curTime = "<i>SCORE: </i>" + String.valueOf((int)(counter*1/TPS));
        //Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This gets executed on the UI thread so it can safely modify Views
                HopperActivity.countertext.setText(Html.fromHtml(curTime));
            }
        });
    }
    
    private void create_ground(){
		p_ground.setColor(0xfff4a460);//red
	    //p_ground.setStrokeWidth(3);

	    //p_ground.setStyle(Paint.Style.STROKE);

        ground_path = new Path();


        
        if(total_active_projectiles == 4){
            ground_path.moveTo(width/6f, zero_ground());
	        for(float i = width/(6f*scalex); scalex*i < width/2; i = i + 0.01f)
	        	ground_path.lineTo((float) (scalex*i), (float) (zero_ground() - scaley*0.2*Math.cos(4*i - Math.PI) - scaley*0.2));
	        
	        for(float i = width/(2f*scalex); scalex*i < 4*width/6f; i = i + 0.01f)
	        	ground_path.lineTo((float) (scalex*i), (float) (zero_ground() - scaley*0.3*Math.cos(4*i - Math.PI) - scaley*0.3));
		}else if(total_active_projectiles == 3){
	        ground_path.moveTo(0, zero_ground());
	        for(float i = 0; scalex*i < width; i = i + 0.001f)
	        	ground_path.lineTo((float) (scalex*i), (float) (zero_ground() - scaley*0.1*Math.cos(2*i - Math.PI) - scaley*0.1));
		}
    	
    }

    
    //Update game display method
    public void update_game_display() {
        SurfaceHolder holder;
        Canvas c = null;
        
        //Update canvas drawings
        try {
       	 holder = getHolder();
       	 
               c = holder.lockCanvas();//get a camvas
               synchronized (holder) {
                      onDraw(c);
               }
            
        } finally {
               if (c != null) {
                      getHolder().unlockCanvasAndPost(c);//release the canvas
               }
        }

    }
        
    
    
    //Update game display method
    public void update_game_state() {
    	
    	if((System.currentTimeMillis() - startTime) < 4000)
    		return;//this is for the initial screen
    	
    	if(no_lives > 0) {
    		if(state_vector[6] < 1){
	    	counter++;
	    
	    	//update state of hopper
	        mHandler2.post(new Runnable() {
	            @Override
	            public void run() {
	                // This gets executed on the UI thread so it can safely modify Views
	    		if((state_vector[6] < 1) && !new_game)//if the hopper is not down and new game has not been requested
	            	run_SAC();//this is where most of the work is done!!
	            }
	        });
	
    		//update angle of hopper based on the new states and FILTER IT!!!
    		angle = (float) (alpha * angle + (1 - alpha) * Math.toDegrees(Math.asin((state_vector[0] - state_vector[4])/state_vector[5])));

	    	//update state of projectiles
	    	active_projectiles = 0;
	        for(int i=0; i<total_no_projectiles; i++){
		    	if(projectile[i].visible){
		    		active_projectiles++;
		    		projectile[i].update();
		    	}
	
	        }
	    	
	    	//Log.i("myApp", String.valueOf(active_projectiles));
	    	//Check active projectiles
	    	if(active_projectiles < total_active_projectiles) {
	        	int temp = total_active_projectiles - active_projectiles;
	    		for(int i = 0; i < temp; i++){
	    			int j = rn.nextInt(total_no_projectiles);
	    			while(projectile[j].visible)//if it is already visible find another projectile
	    				j = rn.nextInt(total_no_projectiles);
	    				
	    			projectile[j].update_init_cond();//make it visible and set init condition
	    		}
	    	}
	    	
    		} else {//if the hopper is down
    			long startTime = System.currentTimeMillis();//keep the sign up for some time
    			while((System.currentTimeMillis() - startTime) < 2000){	}
	    		init_game_after_fall();//continue by losing a life
    		}
    	
    	}

    }
    
    
    private float zero_ground(){ 
    	return (float) (height - llayout_height);
    }
    
    private void run_SAC(){
    	
    	//don't let the hopper het out of bounds
    	if(((state_vector[0] > width - 1.5*hopper_width) && (dir > 0) || (state_vector[0] < 1.5*hopper_width) && (dir < 0)))
    		dir = 0;
    	
    	//scale down the states
	    xinit[0] = state_vector[0]/scalex;
	    xinit[1] = state_vector[1]/scalex;
	    xinit[2] = (zero_ground() - state_vector[2] - hopper_height/2) / scaley ;
	    xinit[3] = state_vector[3]/scaley;
	    xinit[4] = state_vector[4]/scalex;
	    

	  //Log.i("myApp", String.valueOf("x0:"+xinit[0]));
	  //Log.i("myApp", String.valueOf(width/(2*scalex)));
	  //Log.i("myApp", String.valueOf("x1:"+xinit[1]));
	  //Log.i("myApp", String.valueOf("x2:"+xinit[2]));
	  //Log.i("myApp", String.valueOf(height));
	  //Log.i("myApp", String.valueOf(llayout_height));
	  //Log.i("myApp", String.valueOf(hopper_height));
	  //Log.i("myApp", String.valueOf("x3:"+xinit[3]));
	  //Log.i("myApp", String.valueOf("x4:"+xinit[4]));
	  //Log.i("myApp", String.valueOf("length:"+state_vector[5]/scaley));
    	
    	double rvec[] = SACWrapper.sac_stepper2(xinit, 0.0, dir);//run SAC
    	
    	//scale it back
	    state_vector[0] = (float) (rvec[0]*scalex);
	    state_vector[1] = (float) (rvec[1]*scalex);   
	    state_vector[3] = (float) (rvec[3]*scaley); 
	    state_vector[4] = (float) (rvec[4]*scalex); 
	    state_vector[5] = (float) (rvec[5]*scaley); 
	    state_vector[6] = (float) (rvec[6]); 
	    state_vector[2] = (float) (zero_ground() - rvec[2] * scaley - hopper_height/2);

    }
    
    

        

}

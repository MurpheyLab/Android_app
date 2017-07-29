package com.sacgames.pendulumbalance;

import java.text.DecimalFormat;
import java.util.Random;

import Jama.Matrix;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sacgames.GameLoopThread;
import com.sacgames.R;
import com.sacgames.SACWrapper;


public class GameView extends SurfaceView implements SurfaceHolder.Callback { //this class defines the custom view where the game will be played
    
	//These variables are for the display
    private SurfaceHolder holder;
    public GameLoopThread gameLoopThread;   
    private DecimalFormat df = new DecimalFormat("#.00");
    private Paint p_line = new Paint();
    private Paint p_cart = new Paint();
    private Paint p_cart_opt = new Paint();
    private Paint p_pend = new Paint();
    private Paint p_trust = new Paint();
    protected Paint arrow = new Paint();
    private final float line_ratio = 275f/370f;//heuristic based on my current screen height
    private final float r_ratio = 55f/370f;//cart edge ratio
    private final float vib_ratio = 20f/800f;//cart edge ratio
    private float err_max_vib; //this shows for which velocity error we will get the maximum vibration
    private float line_height;//need that to draw the line in the same spot no matter what the screen is
    private float pend_length;
    private float r;//cart edge
    private float width;//get GameView dimensions
    private float height; 
	private float eventXinit = 0;
	public float x_touch;//position of cart when touch was made
    private boolean initialized = false; //this is to use differences in touch motions to move the cart
    public boolean resume = false;//when true, restore vars
    public boolean need_lqr = false;//when to solve the lqr problem
    public boolean help = false;
    public boolean help_button = false;
    public boolean vib_cues = false;
    public boolean vis_cues = false;
    public boolean SAC = false;
    public boolean lqr_on = false;
    private boolean maxdemon = false;
    private boolean accepted = false;//shows if user input was accepted
    public float accepted_inputs = 0;
    private float deltadx = 0;//difference between current and optimal cart velocity
    private long pattern[] = { 0, 0 };//this determines the ON duration of vibration
    private final int vib_period = 200;//how often vibration will be updated in ms
    
    //System parameters
	private final float m = 0.2f;//mass at tip of pendulum
	private final float b = 0.01f;//viscous damping coefficient
	private final float g = 9.8f;
	private double [][] Q = {{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};//tracking effort for LQR
    private float R1 = 1;//control effort
    
    //These are the variables that update the game state
    private Pendulum sim = new Pendulum(MainActivity.l, m, g, b);	//new instance;
    public float[] state_vector = new float[Pendulum.numstates];// these are set in the initialization function
    private float[] state_temp = new float[Pendulum.numstates];// these are set in the initialization function
    public float[] state_vector_opt_cart = new float[Pendulum.numstates];// these are set in the initialization function
    private Optimal_Controller opt = new Optimal_Controller(m, MainActivity.l, b, Q, R1, g); 
    private float[][] P_vector;  // array of variables - Riccati Solution - it stores the whole P evolution starting from the end
    public float[] K = new float[Pendulum.numstates];//optimal gain vector
    private float u; //lateral acceleration input
    private float u_optimal; //LQR output (optimal acceleration)
    public int counter;//counter for the timer shown onscreen
    public final int TPS = MainActivity.TPS;//define frequency (game state is update at that freq and FPS is at maximum equal to that)
    private final float h = (float) 1.0/TPS;//integration step size and sampling frequency for SAC
    public float finger_delta;//need that to calculate lateral acceleration
    private float finger_delta_prev = 0;
    private float vcur;
    private float vprev = 0;
    private float scale_ratio = 70f/800f;//this is used to scale acceleration input according to screen width in pixels (800 is my current screen width
    private float scale;
    double[] xinit = new double[4];//initial value for swing up with SAC
    private float PI = (float) Math.PI;
    Handler mHandler = new Handler(Looper.getMainLooper());
    
    private Bitmap bmp;//for the checkmark
    

    
    
    
    public GameView(Context context) {
        super(context);
                
        init();//initialize view, game states etc
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
 
        init();//initialize view, game states etc
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
 
        init();//initialize view, game states etc
    }
    
    private void init(){
        
        holder = getHolder(); // get holder of the surface/view        
        holder.addCallback(this);//notify SurfaceHolder that I would like to receive callbacks        
    	gameLoopThread = new GameLoopThread(this);   
    	
    	//init_game_states();//initialize game states
        
    }
    
    
    public void init_game_states(){




    	if((!resume) && (!SAC)){//if we are not resuming, reset states
        	//do {
		    	counter = 0;//this is for the counter
	
		    	//Initialize game states
		    	state_vector[0] = width/2 - r/2;//put the cart in the middle of the screen
		    	state_vector[1] = 0;//zero initial velocity for cart
		    	//Log.i("myApp", String.valueOf(height));
		    	
		    /*	if(!help_button) {
			    	Random rn = new Random();
			    	//rn.nextInt(max - min + 1) + min
			    	if(rn.nextInt(2)==1){	//generate a number between 0 and 1
			    		state_vector[2] = 0.01f;
			    	} else {
			    		state_vector[2] = -0.01f;
			    	}
		    	} else {*/
		    		state_vector[2] = PI;
		    	//}
		    	
		    	state_vector[3] = 0;//zero angular velocity for pendulum
		    	
		    	help = false; //no help at the beginning so that the pole moves a bit
		    	
		    	accepted_inputs = 0;
		    	
		    	//Initialize SAC stuff
		    	SACWrapper.initialize((double)MainActivity.l, h, (width-r)/(2*scale));//initialize saturation, pendulum length and sampling frequency for SAC
	
		    	
		    	if(need_lqr){
		    		solve_LQR();// solve LQR problem to get optimal gains
		            
		        /*	
		    		try {
		    		MainActivity.t.join();
		    		} catch (InterruptedException e) {
	
		    		}*/    		
		    		
		    	}

        	//} while (Math.abs(state_vector[2]) > 0.02);//run this repeatedly for cases where SAC freezes things
        	update_game_state();
        	update_game_display();
    	}

    	
    	if(SAC) {//if SAC for swing up
	    	counter = 0;//this is for the counter
	    	
	    	SACWrapper.initialize((double)MainActivity.l, h, (width-r)/(2*scale));//initialize saturation, pendulum length and sampling frequency for SAC

	    	//Initialize game states
	    	state_vector[0] = width/2 - r/2;//put the cart in the middle of the screen
	    	state_vector[1] = 0;//zero initial velocity for cart
    		state_vector[2] = PI;
	    	state_vector[3] = 0;//zero angular velocity for pendulum
	    	
	    	help = false; //no help at the beginning so that the pole moves a bit
	    	
	    	update_game_state();
        	update_game_display();
		    
    	}
	    	
	    	
    	if(resume) {//if we are resuming restore states etc

		    K = MainActivity.K;
		    if(!SAC) {
		    	counter = MainActivity.counter;
	    		state_vector = MainActivity.saved_states;
	    		accepted_inputs = MainActivity.accepted_inputs;
		    }
		    
    	}

    }  
    
    
    //I wrote a separate function for this because I need to find the dimensions of the provided canvas first.
    private void graphics_initialization() {    	
    	
		width = getWidth();//get dimensions of canvas
		height = getHeight();
		line_height = line_ratio * height;//find where to draw the ground line
		r = height * r_ratio;//dimension of cart
		pend_length = MainActivity.length_ratio  * height;//find length of pendulum	
		
		err_max_vib = width * vib_ratio; //this shows for which velocity error we will get the maximum vibration
		scale = width * scale_ratio;//this is used to scale acceleration input according to screen pixel width 
		

		//Line stuff
		p_line.setColor(Color.GRAY);
		p_line.setStrokeWidth(10f*r/55f);
		
		//Cart Rectangle stuff
        p_cart.setColor(Color.BLACK);
        p_cart.setStrokeWidth(3);
        
		//Optimal cart velocoty as a visual cue (rectangle)
        p_cart_opt.setColor(Color.GREEN);
        p_cart_opt.setStrokeWidth(3);
        
        //Pole stuff
		p_pend.setColor(Color.RED);
		p_pend.setStrokeWidth(13f*r/55f);		
		
		p_trust.setColor(Color.BLACK);
    	// Convert the dips to pixels
    	float density = getContext().getResources().getDisplayMetrics().density;    	
    	float trust_dp = 25f;//text size for trust text
		p_trust.setTextSize((int) (trust_dp * density + 0.5f)); 	
    	//p_trust.setTextSize(30);
		
		init_game_states(); //initialize game states (solve the lqr as well)
		need_lqr = false; // (no need to solve the lqr again)	
		
		//checkmark for maxwell's demon shared case
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.check_mark_green);
		bmp = Bitmap.createScaledBitmap(bmp, (int)(width/8), (int)(width/8), true);
  
    }
    
    //draw arrow
    private void fillArrow(Canvas canvas, float x0, float y0, float x1, float y1) {
	
	    arrow.setStyle(Paint.Style.FILL);
	    
	    arrow.setColor(Color.YELLOW);
	
	    float deltaX = x1 - x0;
	    float deltaY = y1 - y0;
	    float frac = (float) 0.1;
	
	    float point_x_1 = x0 + ((1 - frac) * deltaX + frac * deltaY);
	    float point_y_1 = y0 + ((1 - frac) * deltaY - frac * deltaX);
	
	    float point_x_2 = x1;
	    float point_y_2 = y1;
	
	    float point_x_3 = x0 + ((1 - frac) * deltaX - frac * deltaY);
	    float point_y_3 = y0 + ((1 - frac) * deltaY + frac * deltaX);
	
	    Path path = new Path();
	    path.setFillType(Path.FillType.EVEN_ODD);
	
	    path.moveTo(point_x_1, point_y_1);
	    path.lineTo(point_x_2, point_y_2);
	    path.lineTo(point_x_3, point_y_3);
	    path.lineTo(point_x_1, point_y_1);
	    path.lineTo(point_x_1, point_y_1);
	    path.close();
	
	    canvas.drawPath(path, arrow);
	}
    
    
    public void solve_LQR(){
	      final int optnumstates = opt.numstates;//system states
	      final float final_time = 10.0f;//final time of simulation
	      float sim_time = 0f;//initial simulation time
	      int i;//iter counter
	      float h = 0.01f;//step size for num integration

	      float[] final_condP = new float[optnumstates]; //initial conditions for P. No terminal condition so all are zero  
	      P_vector = new float[optnumstates][(int) (final_time/h)+1];  // array of variables - Riccati Solution - it stores the whole P evolution starting from the end
	      
	      for(int j=0; j < optnumstates; j++){	    	  
	    	  final_condP[j] = 0;
	    	  P_vector[j][(int) (final_time/h)] = 0; 
	      }  
	      
	      
	      sim_time = final_time;//backwards in time
	      RKSolver.states = final_condP;//set final condition of Riccati equation
	      
	      
	      i = (int) (final_time/h - 1);//start counter from the end
	      
	      //Integration Loop that solves the riccati
	      while(sim_time>=0){
	    	  RKSolver.solve(-h, optnumstates, opt, 0);//update current states going backwards in time. the zero argument (u) is not used here
	    	  
		      for(int j=0; j < optnumstates; j++){
		    	  P_vector[j][i] = RKSolver.states[j]; //this stores the whole P evolution
		      }  
	          sim_time -= h;

	          i--;	
	      }
	      	      
	     // for(i=0; i<P_vector[14].length; i++)//
	    	//  Log.i("myApp", String.valueOf(P_vector[14][i]));
	      //-----------------------------------------------------------------------------------------------------

	      //Find optimal gains
	      K = find_K();
	    //Log.i("myApp", String.valueOf(K[0]));
	    //Log.i("myApp", String.valueOf(K[1]));
	    //Log.i("myApp", String.valueOf(K[2]));
	    //Log.i("myApp", String.valueOf(K[3]));
    }
    
    
    //find optimal LQR gains
	private float[] find_K(){

		float[] K = new float[Pendulum.numstates];//optimal gain vector	      
	      
		 //Convert P back to matrix (it was column). Keep only last values of P (first in terms of time) to emulate infinite horizon problem
		 double[][] Pfinal = new double[Pendulum.numstates][Pendulum.numstates];
		 for(int i=0; i < Pendulum.numstates; i++){
			 for(int j=0; j < Pendulum.numstates; j++){
				 Pfinal[j][i] = P_vector[i*Pendulum.numstates+j][0];
	 			 }
		 }
		Matrix P = new Matrix(Pfinal);
			 
		//Get linearized B matrix
		Matrix Bm = new Matrix(opt.Bm());
		
		//Calculate Optimal Gains
		double[][] Ktemp = Bm.transpose().times(P).timesEquals(-1/R1).getArray();//minus of negative feedback included here
		
		//Convert optimal gains to floats
	    for (int i = 0 ; i < Pendulum.numstates; i++)
	        K[i] = (float) Ktemp[0][i];
		
		return K;

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
    
    
    public long pulse_duration(float error) {
    	    	
    	float slope = vib_period/err_max_vib; //I use this numerator to make sure that the maximum pulse duration is equal to the period
    	long duration = (long) (Math.abs(error) * slope);
    	
    	return duration;
    }
    


    @Override
    protected void onDraw(Canvas canvas) {

    	canvas.drawColor(Color.CYAN);//background color 
    	
        //Show user if their input is accepted
        if(help_button) {
        	if(accepted && !help) {
        	//draw checkmard for Maxwell's demon
    		canvas.drawBitmap(bmp, width/2 - bmp.getWidth()/2, height/2 - bmp.getHeight(), null);
        	//accepted = false;
        	}     	
        	
        	canvas.drawText(String.valueOf(df.format(100*accepted_inputs/counter)), 0, height, p_trust);
        }
    	
    	// drawLine (float startX, float startY, float stopX, float stopY, Paint paint)    	
		canvas.drawLine(0, line_height, width, line_height, p_line);
		
        // drawLine pendulum as a thick line    	
		canvas.drawLine(state_vector[0] + r/2, line_height, (float) (state_vector[0] + r/2 + pend_length * Math.cos(Math.PI/2 + state_vector[2])), (float) (line_height - pend_length * Math.sin(Math.PI/2 + state_vector[2])), p_pend);
		
		//draw cart
        canvas.drawRect(state_vector[0], line_height - r/4, state_vector[0] + r, line_height + r/4, p_cart);
                
        
        //VIBROTACTILE CUES
        //Log.i("myApp", String.valueOf((int)(h*1000)));
        if(vib_cues){
            //Show user if their input is accepted
            if(help_button) {//if we are in shared mode
            	if(accepted && !help) {
            	//vibrate
            		PendulumActivity.vibrator.vibrate((long) ((int)(h*1000)));
            		//accepted = false;
            	} else {
            		PendulumActivity.vibrator.cancel();   
            	}            	
            	
            } else {//if we are not in shared mode
            	if(((100*counter*1/TPS) % 50) > 20){
		        	//draw arrow using the sign of the error for direction
		        	if(deltadx > 0)
		        		fillArrow(canvas, width - height, height/2, width, height/2);//draw the arrow if the cues button is on and make it flash
		        	else
		        		fillArrow(canvas, height, height/2, 0f, height/2);//draw the arrow if the cues button is on and make it flash
		        	}       	
        	
	            //VIBRATION
	        	pattern[1] = pulse_duration(deltadx);
	        	//Log.i("myApp", String.valueOf(deltadx));
	        	//Log.i("myApp", String.valueOf(pulse_duration(deltadx)));
	            if(((100*counter*1/TPS) % (vib_period/10)) == 0){
	            	PendulumActivity.vibrator.vibrate(pattern, -1);
	            } 
            }
        } else {
        	PendulumActivity.vibrator.cancel();
        }
        
        
        //VISUAL CUES
        if(vis_cues){
        	//if(Math.abs(state_vector[2]) < Math.PI/2){
    		//draw "optimal" cart
            canvas.drawRect(state_vector[0] + deltadx, line_height - r/4, state_vector[0] + deltadx + r, line_height + r/4, p_cart_opt);
        }
        

        accepted = false;//used reset that for next iter 
}
    
    
    
    //update counter value in the TextView of the main thread. Cannot do it directly from the worker game thread, so we need handlers..
    private void update_counter(){
    	final String curTime; 
    	
        //Update timer value
        curTime = String.valueOf(df.format((double)counter*1/TPS));
        //Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This gets executed on the UI thread so it can safely modify Views
                PendulumActivity.countertext.setText(curTime + "s");
            }
        });
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
        
      //Update timer value
        update_counter();

    }
    
    
    //update input u (lateral acceleration)
    private void update_lateral_input() {        

     	  if(help==false){
	      vcur =  (finger_delta - finger_delta_prev) / h; //finite differences to calculate finger speed on the x axis
	      //u = (vcur - vprev) / (scale * h); //lateral acceleration
	      
	      u = (vcur - state_vector[1]) / (scale * h); //lateral acceleration (no need to use vprev)
	    //Log.i("myApp", String.valueOf(u));
	      //vprev = vcur;
	      finger_delta_prev = finger_delta;
	      //if(u==0)
	    	//  state_vector[1] = 0;//otherwise even if input (accel) is zero, the cart may continue moving
    	  }
	     
    }
    
    
    
    
    //Update game display method
    public void update_game_state() {
    	
    	if(!SAC) {//if it's not for the swing up demonstration
    		if (counter*1/TPS < 50){
    		if((Math.abs(state_vector[2]) > 0.15) || (Math.abs(state_vector[3]) > 0.6))//then run with the LQR
    		{
    			
    		
	    	//if(Math.abs(state_vector[2]) < Math.PI/2)
	    		counter++; //increase timer counter   
	    		
	        
	        if(vis_cues || vib_cues){//if you need cues
	        	
	        	System.arraycopy(state_vector, 0, state_temp, 0, state_vector.length);//store the current state	  
	    	    
	        	combined_sac_lqr(false);//run combination of sac and lqr	    	    
	
	    	    deltadx =  state_vector[1] - state_temp[1]; // get difference between optimal velocity and current velocity (used to display visual cues)
		    	    
	    	    if(!help)//if shared mode as well, in the no help case restore state..
	    	    	System.arraycopy(state_temp, 0, state_vector, 0, state_vector.length);//restore current state	
	    	   
	    	    
	        }
	  	  
	        
	        if(help_button && help) { //use controls
        
		        	if(vis_cues || vib_cues)//if you need cues
		        		Log.i("myApp", "1");//do nothing since you have already integrated above
		        	else
		        		combined_sac_lqr(false);//run shared mode with lqr and sac	   
	        	
	        } else if(help_button && !help && initialized) { //use controls
		        		combined_sac_lqr(true);//run shared mode with lqr and sac and maxwell's demon
	        	
	        } else {//use finger input
	        	
		        update_lateral_input();//calculate finger input u 
	        
		        // PENDULUM SIMULATION ------------------------------------------------------------------------------------
		        System.arraycopy(state_vector, 0, RKSolver.states, 0, state_vector.length);
		        RKSolver.states[0] = RKSolver.states[0]/scale;
		        RKSolver.states[1] = RKSolver.states[1]/scale;
		
			    RKSolver.solve(h, Pendulum.numstates, sim, u);//update current states with numerical integration
		
			    System.arraycopy(RKSolver.states, 0, state_vector, 0, state_vector.length);
			    
			    state_vector[0] = state_vector[0]*scale;
			    state_vector[1] = state_vector[1]*scale;
			    
	    	    AngleWrap();//because the output allows any angle
		    
	        }
    	}
    	}
	        
	
		    //-------------------------------------------------------------------------------------------------------
    	} else {//if it's for the swing up demonstration
			counter++;     		
    		if(lqr_on)  {
    			combined_sac_lqr(false);//run combination of sac and lqr
    	    	
    		}  else { //run SAC           		
    			
    			//Run SAC in a separate thread because it freezes buttons etc
    			
    		    //Handler mHandler = new Handler(Looper.getMainLooper());
    		    mHandler.post(new Runnable() {
    		        @Override
    		        public void run() {      		        	
    		        	run_SAC(false);
    		            }
    		        });  	
    				
    		   }
    	    	
    	  }

    }
    
    
    
    
    
    public void combined_sac_lqr(boolean max_demon) {
	  //run swing up problem combining SAC and lqr	
		if((Math.abs(state_vector[2]) < 0.15) && (Math.abs(state_vector[3]) < 0.6))//then run with the LQR
		{
			
			
	/*		//if(!help && help_button) {
				//LQR output (balance anywhere on the screen)
	    	 //   u_optimal =  K[1]*state_vector[1]/scale + K[2]*state_vector[2] + K[3]*state_vector[3];//here i should apply take finite differences twice
			//} else {	
				//LQR output (balance in the middle of the creen)
				u_optimal = K[0]*(state_vector[0] - (width/2 - r/2))/scale + K[1]*state_vector[1]/scale + K[2]*state_vector[2] + K[3]*state_vector[3];//here i should apply take finite differences twice
			//}
			
	    	  u = u_optimal;
	    	  
	        // PENDULUM SIMULATION ------------------------------------------------------------------------------------
	        System.arraycopy(state_vector, 0, RKSolver.states, 0, state_vector.length);
	        RKSolver.states[0] = RKSolver.states[0]/scale;
	        RKSolver.states[1] = RKSolver.states[1]/scale;
	
	        if(!max_demon)
	        	RKSolver.solve(h, Pendulum.numstates, sim, u);//update current states with numerical integration
	        else {//apply user input only if it is in the same direction as the LQR
	        	update_lateral_input();//calculate finger input u 
        		if((u*u_optimal > 0) || ((Math.abs(u_optimal)<1)&&(Math.abs(u)<5))){//if inner product is positive or if close to zero (it's hard to keep finger completely still - touch screen is very sensitive)
        			accepted = true;
        			accepted_inputs = accepted_inputs + 1;
        			u = u;//apply user's input
	        		if(u > MainActivity.u_sat)//saturate user's input to create a never-failing interface
	        			u = MainActivity.u_sat;
	        		if(u < -MainActivity.u_sat)
	        			u = -MainActivity.u_sat;
        		}
        		else {
        			accepted = false;
        			u = 0;//u_optimal;//else apply LQR	
        		}
        		

        		
	        	RKSolver.solve(h, Pendulum.numstates, sim, u);//update current states with numerical integration
	        }
	
		    System.arraycopy(RKSolver.states, 0, state_vector, 0, state_vector.length);

		    
		    state_vector[0] = state_vector[0]*scale;
		    state_vector[1] = state_vector[1]*scale;     
		  */  
		}   //run SAC to swing up first             		
		
			//When you need cues, I don't run it in a thread, because it is hard to synchronize. That is the only case when the reset bug will appear..
	//    Handler mHandler = new Handler(Looper.getMainLooper());
    	if((vis_cues || vib_cues) && !help){//if you need cues
    			run_SAC(max_demon);
	    
        } else {
	    	maxdemon = max_demon;//we need class scope below..
		    mHandler.post(new Runnable() {
		        @Override
		        public void run() {	        	
		        	run_SAC(maxdemon);
		
		            }
		        });  
        		
        	}
			
	    
	
    }
    
    private void run_SAC(boolean max_demon){
	    xinit[0] = state_vector[2];
	    xinit[1] = state_vector[3];
	    xinit[2] = state_vector[0]/scale;
	    xinit[3] = state_vector[1]/scale;
    	

    	double[] rvec = SACWrapper.sac_stepper(xinit, 0.0);
    	//Log.i("myApp", "bike");
	    
        if(!max_demon){    	
    	    state_vector[0] = (float) (rvec[2]*scale);
    	    state_vector[1] = (float) (rvec[3]*scale);   
    	    state_vector[2] = (float) rvec[0];
    	    state_vector[3] = (float) rvec[1];
        } else {//apply user input only if it is in the same direction as the LQR
        	update_lateral_input();//calculate finger input u 
        	double rvec2[] = SACWrapper.input_stepper(xinit, 0.0, rvec[4], u);//this applies mexwell's principle in c++
    	    state_vector[0] = (float) (rvec2[2]*scale);
    	    state_vector[1] = (float) (rvec2[3]*scale);   
    	    state_vector[2] = (float) rvec2[0];
    	    state_vector[3] = (float) rvec2[1];
    	    
    	    //Was the user input accepted??
    	    if (rvec2[4]==1) {
    	    	accepted = true;
    	    	accepted_inputs = accepted_inputs + 1;
    	    }
    	    else
    	    	accepted = false;

        }
	    
	    


	    AngleWrap();//because the output of SAC allows any angle
	   //Log.i("myApp", String.valueOf(state_vector[0]/scale));
    }
    
    
    private void AngleWrap() {
        state_vector[2] = (state_vector[2] + PI) % (2 * PI);
        if ( state_vector[2] < 0 ) { state_vector[2] = state_vector[2] + 2 * PI; }
        state_vector[2] = (state_vector[2] - PI);
        
    }
        
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
	  

      
     if(!SAC) {
    	
	  	  if (initialized == false){
			  eventXinit = event.getX();//this is used to create relative motion of finger/cart
			  initialized = true;
			  //x_touch = state_vector[0];//same with above
		  }
	
	      switch (event.getAction()) {
	      case MotionEvent.ACTION_DOWN:
	    	  finger_delta = event.getX() - eventXinit;
	    	  state_vector[1] = 0;//otherwise even if input (accel) is zero, the cart may continue moving
	    	  help = false;
	    	  //Log.i("myApp", String.valueOf(df.format((double)counter*1/TPS)));
	    	  //finger_position = x_touch + event.getX() - eventXinit;
	    	  return true;
	      case MotionEvent.ACTION_MOVE:
	    	  finger_delta = event.getX() - eventXinit;
	    	  help = false;

	          return true;
	      case MotionEvent.ACTION_UP://finger lifted
			  finger_delta_prev = 0;//this has to set to zero every time the finger is lifted
			  vprev = 0;
			  finger_delta = 0;
			  state_vector[1] = 0;//otherwise even if input (accel) is zero, the cart may continue moving
	    	  initialized = false; //finger lifted so we have to re-initialize x_touch in the next event
	    	  if(help_button)//if the button help is on, the alternate with the controller
	    		  //help = true; I don't want that in this mode
	    	  break;
	      default:
	        return false;
	      }
	      
     }
      
      
      
      return true;     
    }

}

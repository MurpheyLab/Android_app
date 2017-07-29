package com.sacgames;

import com.sacgames.pendulumbalance.GameView;

import android.view.SurfaceView;


public class GameLoopThread extends Thread {
    private GameView view;
    public boolean running = false;
    
    

    public GameLoopThread(GameView view) {
          this.view = view;
    }

    
    public void setRunning(boolean run) {
          running = run;
    }


    @Override

    public void run() {
    	
    	int MAX_FRAMESKIP = 10;
    	int SKIP_TICKS = 1000 / view.TPS;
        long next_game_tick;
        int loops;
    	
        
        
        //In this loop, the FPS is dependent on constant update game time (TPS). If state computations take too long, th game will be slower etc.
        long startTime;
          long sleepTime;
          int ticksPS = 1000 / view.TPS;
          
          while (running) {
        	  	startTime = System.currentTimeMillis();
        	  
        	  	view.update_game_display();
        	  	view.update_game_state();

                 
                 sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
                 try {
                	 if (sleepTime > 0)
                            sleep(sleepTime);
                        else {// we are behind
                        	//Log.i("myApp", "shit");
                        }
                 } catch (Exception e) {}
          }
        
        
	  	//In this loop, the game states will be updated at a steady rate times per second (TPS), and rendering (displaying) is done as fast as possible.
        /*next_game_tick = System.currentTimeMillis();
        while (running) {
        	loops = 0;
        	
            while( (System.currentTimeMillis() > next_game_tick) && (loops < MAX_FRAMESKIP)) {
            	view.update_game_state(); //update states of game
            	next_game_tick += SKIP_TICKS;
                loops++;    
                //Log.i("myApp", String.valueOf(loops));
            }   
            view.update_game_display(); // update display
        }
        */
        
    }
}
    
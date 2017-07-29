package com.sacgames.hopper;

import java.util.Random;

import com.sacgames.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public class Projectiles {
  protected final float speed_ratio = 4f/800f;//hopper ratio for multiple resolutions
  protected float width;//projectile dimensions
  protected float height;
  protected Bitmap bmp;
  protected int x;
  protected int y;
  protected int dir;
  protected int counter;//to make fall realistic I need a time reference
  protected boolean visible = false;
  protected float g;
  public static boolean invisible = false;
  Handler mHandler = new Handler(Looper.getMainLooper());
  Random rn = new Random();
  

  //Methods to override
  protected void update() {
	  //Check for collision first
	  if((Math.abs(y + height/2 - HopperActivity.demo.state_vector[2]) > (HopperActivity.demo.hopper_height/2 + height/2)) || (Math.abs(x + width/2 - HopperActivity.demo.state_vector[0]) > (HopperActivity.demo.hopper_width/2 + width/2))){
		  this.counter++;
		  //update position of image
		  x = x + dir;
		  y = (int) (y + 1.0/2*g*Math.pow(counter*1.0/HopperActivity.demo.TPS,2));
		  visible = isVisible();
	  } else {
		  visible = false;
		  HopperActivity.demo.collision = true;
		  HopperActivity.demo.counter2 = HopperActivity.demo.counter;
		  if(!invisible)
			  HopperActivity.demo.no_lives--;
		  //post change
	        mHandler.post(new Runnable() {
	            @Override
	            public void run() {
	                // This gets executed on the UI thread so it can safely modify Views
	                HopperActivity.livestext.setText("X " + String.valueOf(HopperActivity.demo.no_lives));
	            }
	        });//reduce no of lives
	  }

	  //Log.i("myApp", String.valueOf(visible));
  }

  protected void onDraw(Canvas canvas) {
      Rotate_Draw_Bitmap(0, x, y, canvas);
  }
  
  protected boolean isVisible(){
	  //if it's nt visible anymore change the flag so that this part does not execute
	  if((x > HopperActivity.demo.width)||(x < -width) || (y > HopperActivity.demo.height)){
		  return false;
	  }
	  else
		  return true;
  }
  
  protected void update_init_cond(){
	  this.x = rn.nextInt((int) HopperActivity.demo.width);
	  while((HopperActivity.demo.width - x) < width)
		  this.x = rn.nextInt((int) HopperActivity.demo.width);//so that the image appears in the screen

	  this.y = 0;
	  this.counter = 0;
	  this.visible = true;
	  
	  if (x > HopperActivity.demo.width/2)
		  dir = -rn.nextInt((int) (speed_ratio*HopperActivity.demo.width));//move towards the middle of the screen
	  else
		  dir = rn.nextInt((int) (speed_ratio*HopperActivity.demo.width));
	  
  }
  
  protected void Rotate_Draw_Bitmap(float angle, float trans_x, float trans_y, Canvas canvas)
  {       
        Matrix matrix = new Matrix();
        matrix.setRotate(angle, trans_x + width/2, trans_y + height/2);
        matrix.preTranslate(trans_x, trans_y);
        canvas.drawBitmap(bmp,matrix,null);

  }
  
  //plus impact detection!!!!!!!!!!

}


 class Pikachu extends Projectiles {
	
    public Pikachu(float g) {
    	this.g = g;//acceleration of free fall
        //pikachu
    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.pikachufull);
        bmp = Bitmap.createScaledBitmap(bmp, HopperActivity.demo.hopper_width, HopperActivity.demo.hopper_width, true);

        this.width = bmp.getWidth();//get dimensions of projectile
        this.height = bmp.getHeight();
  }
      
}

 
 class Dragon extends Projectiles {
		
	    public Dragon(float g) {
	    	this.g = g;
	    	
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.dragon);
	        bmp = Bitmap.createScaledBitmap(bmp, 2*HopperActivity.demo.hopper_width, 2*HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();
	  }

	}
 
 class Comet extends Projectiles {
		
	    public Comet(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.comet);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (0.7*HopperActivity.demo.hopper_width), 2*HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void update_init_cond(){
	  	  this.x = rn.nextInt((int) HopperActivity.demo.width);
		  while((HopperActivity.demo.width - x) < width)
			  this.x = rn.nextInt((int) HopperActivity.demo.width);//so that the image appears in the screen
	  	  this.y = 0;
	  	  this.counter = 0;
	  	  this.visible = true;
	  	  this.dir = 0;//want that to fall vertically always
	    }

	}
 
 
 class PhD extends Projectiles {
		
	    public PhD(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.phd);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.5*HopperActivity.demo.hopper_width), (int) (1.0f*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void onDraw(Canvas canvas) {
	        Rotate_Draw_Bitmap(x, x, y, canvas);
	    }
	    
	    
	    //Methods to override
	    public void update() {
	  	  //Check for collision first
	  	  if((Math.abs(y + height/2 - HopperActivity.demo.state_vector[2]) > (HopperActivity.demo.hopper_height/2 + height/2)) || (Math.abs(x + width/2 - HopperActivity.demo.state_vector[0]) > (HopperActivity.demo.hopper_width/2 + width/2))){
	  		  this.counter++;
	  		  //update position of image
	  		  x = x + dir;
	  		  y = (int) (y + 1.0/2*g*Math.pow(counter*1.0/HopperActivity.demo.TPS,2));
	  		  visible = isVisible();
	  	  } else {
	  		  visible = false;
	  		  HopperActivity.demo.counter = (int) (HopperActivity.demo.counter + 15000f/HopperActivity.demo.TPS);//increase score
	  		  HopperActivity.demo.update_counter();
			  /*HopperActivity.demo.no_lives++;
			  //post change
		        mHandler.post(new Runnable() {
		            @Override
		            public void run() {
		                // This gets executed on the UI thread so it can safely modify Views
		                HopperActivity.livestext.setText("X " + String.valueOf(HopperActivity.demo.no_lives));
		            }
		        });//reduce no of lives*/
	  	  }
	    }
	}
 
 
 class Dollar extends Projectiles {
		
	    public Dollar(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.dollar);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.5*HopperActivity.demo.hopper_width), (int) (1.0f*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void onDraw(Canvas canvas) {
	        Rotate_Draw_Bitmap(x, x, y, canvas);
	    }
	    
	    
	    //Methods to override
	    public void update() {
	  	  //Check for collision first
	  	  if((Math.abs(y + height/2 - HopperActivity.demo.state_vector[2]) > (HopperActivity.demo.hopper_height/2 + height/2)) || (Math.abs(x + width/2 - HopperActivity.demo.state_vector[0]) > (HopperActivity.demo.hopper_width/2 + width/2))){
	  		  this.counter++;
	  		  //update position of image
	  		  x = x + dir;
	  		  y = (int) (y + 1.0/2*g*Math.pow(counter*1.0/HopperActivity.demo.TPS,2));
	  		  visible = isVisible();
	  	  } else {
	  		  visible = false;
	  		  HopperActivity.demo.counter = (int) (HopperActivity.demo.counter + 10000f/HopperActivity.demo.TPS);//increase score
	  		  HopperActivity.demo.update_counter();

	  	  }
	    }
	}
 
 
 class Hammer extends Projectiles {
		
	    public Hammer(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.hammer);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.0/2*HopperActivity.demo.hopper_width), HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void onDraw(Canvas canvas) {
	        Rotate_Draw_Bitmap(10*x, x, y, canvas);
	    }
	}
 
 class Sword extends Projectiles {
		
	    public Sword(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.sword);
	        bmp = Bitmap.createScaledBitmap(bmp, HopperActivity.demo.hopper_width, HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void onDraw(Canvas canvas) {
	        Rotate_Draw_Bitmap(10*x, x, y, canvas);
	    }
	}
 
 
 class Hulk extends Projectiles {
		
	    public Hulk(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.hulk);
	        bmp = Bitmap.createScaledBitmap(bmp, 2*HopperActivity.demo.hopper_width, 2*HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 class Rocket extends Projectiles {
		
	    public Rocket(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.rocket);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.0/2*HopperActivity.demo.hopper_width), HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void update_init_cond(){
	  	  this.x = rn.nextInt((int) HopperActivity.demo.width);
		  while((HopperActivity.demo.width - x) < width)
			  this.x = rn.nextInt((int) HopperActivity.demo.width);//so that the image appears in the screen
	  	  this.y = 0;
	  	  this.counter = 0;
	  	  this.visible = true;
	  	  this.dir = 0;//want that to fall vertically always
	    }

	}
 
 class Torpedo extends Projectiles {
		
	    public Torpedo(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.torpedo);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.0/2*HopperActivity.demo.hopper_width), HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void update_init_cond(){
	  	  this.x = rn.nextInt((int) HopperActivity.demo.width);
		  while((HopperActivity.demo.width - x) < width)
			  this.x = rn.nextInt((int) HopperActivity.demo.width);//so that the image appears in the screen
	  	  this.y = 0;
	  	  this.counter = 0;
	  	  this.visible = true;
	  	  this.dir = 0;//want that to fall vertically always
	    }

	}
 
 class DarthVader extends Projectiles {
		
	    public DarthVader(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.darthvader);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.5*HopperActivity.demo.hopper_width), (int) (1.5*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 class Bomb extends Projectiles {
		
	    public Bomb(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.bomb);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.0/2*HopperActivity.demo.hopper_width), HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 class Parachute extends Projectiles {
		
	    public Parachute(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.parachute);
	        bmp = Bitmap.createScaledBitmap(bmp, HopperActivity.demo.hopper_width, 2*HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 class Dynamite extends Projectiles {
		
	    public Dynamite(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.dynamite);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (1.0/2*HopperActivity.demo.hopper_width), HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 class Trex extends Projectiles {
		
	    public Trex(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.trex);
	        bmp = Bitmap.createScaledBitmap(bmp, 2*HopperActivity.demo.hopper_width, 2*HopperActivity.demo.hopper_width, true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 class Beer extends Projectiles {
		
	    public Beer(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.beer);
	        bmp = Bitmap.createScaledBitmap(bmp, (int) (0.75*HopperActivity.demo.hopper_width), (int) (1.5*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    public void onDraw(Canvas canvas) {
	        Rotate_Draw_Bitmap(5*x, x, y, canvas);
	    }
	    
	    //Methods to override
	    public void update() {
	  	  //Check for collision first
	  	  if((Math.abs(y + height/2 - HopperActivity.demo.state_vector[2]) > (HopperActivity.demo.hopper_height/2 + height/2)) || (Math.abs(x + width/2 - HopperActivity.demo.state_vector[0]) > (HopperActivity.demo.hopper_width/2 + width/2))){
	  		  this.counter++;
	  		  //update position of image
	  		  x = x + dir;
	  		  y = (int) (y + 1.0/2*g*Math.pow(counter*1.0/HopperActivity.demo.TPS,2));
	  		  visible = isVisible();
	  	  } else {
	  		  visible = false;
	  		  HopperActivity.demo.counter = (int) (HopperActivity.demo.counter + 5000f/HopperActivity.demo.TPS);//increase score
	  		  HopperActivity.demo.update_counter();
	  	  }
	    }

	}
 
 class HotDog extends Projectiles {
		
	    public HotDog(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.hotdog);
	        bmp = Bitmap.createScaledBitmap(bmp,(int) (1.5*HopperActivity.demo.hopper_width), (int) (0.75*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    //Methods to override
	    public void update() {
	  	  //Check for collision first
	  	  if((Math.abs(y + height/2 - HopperActivity.demo.state_vector[2]) > (HopperActivity.demo.hopper_height/2 + height/2)) || (Math.abs(x + width/2 - HopperActivity.demo.state_vector[0]) > (HopperActivity.demo.hopper_width/2 + width/2))){
	  		  this.counter++;
	  		  //update position of image
	  		  x = x + dir;
	  		  y = (int) (y + 1.0/2*g*Math.pow(counter*1.0/HopperActivity.demo.TPS,2));
	  		  visible = isVisible();
	  	  } else {
	  		  visible = false;
	  		  HopperActivity.demo.counter = (int) (HopperActivity.demo.counter + 5000f/HopperActivity.demo.TPS);//increase score
	  		  HopperActivity.demo.update_counter();
	  	  }
	    }

	}
 
 class Life extends Projectiles {
		
	    public Life(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.pogoface2);
	        bmp = Bitmap.createScaledBitmap(bmp, HopperActivity.demo.hopper_width, (int) (1.5*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    //Methods to override
	    public void update() {
	  	  //Check for collision first
	  	  if((Math.abs(y + height/2 - HopperActivity.demo.state_vector[2]) > (HopperActivity.demo.hopper_height/2 + height/2)) || (Math.abs(x + width/2 - HopperActivity.demo.state_vector[0]) > (HopperActivity.demo.hopper_width/2 + width/2))){
	  		  this.counter++;
	  		  //update position of image
	  		  x = x + dir;
	  		  y = (int) (y + 1.0/2*g*Math.pow(counter*1.0/HopperActivity.demo.TPS,2));
	  		  visible = isVisible();
	  	  } else {
	  		  visible = false;
	  		  //increase no of lives
			  HopperActivity.demo.no_lives++;
			  //post change
		        mHandler.post(new Runnable() {
		            @Override
		            public void run() {
		                // This gets executed on the UI thread so it can safely modify Views
		                HopperActivity.livestext.setText("X " + String.valueOf(HopperActivity.demo.no_lives));
		            }
		        });//reduce no of lives
	  	  }
	    }

	}

 
 
 class Darwin extends Projectiles {
		
	    public Darwin(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.darwin);
	        bmp = Bitmap.createScaledBitmap(bmp,(int) (2.25*HopperActivity.demo.hopper_width),(int) (1.5*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 class Ghost extends Projectiles {
		
	    public Ghost(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.ghost);
	    	bmp = Bitmap.createScaledBitmap(bmp,(int) (1.5*HopperActivity.demo.hopper_width), (int) (1.5*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }

	}
 
 
 class Sauron extends Projectiles {
		
	    public Sauron(float g) {
	        this.g = g;
	        
	    	bmp = BitmapFactory.decodeResource(HopperActivity.demo.getResources(), R.drawable.sauron);
	    	bmp = Bitmap.createScaledBitmap(bmp,(int) (1.5*HopperActivity.demo.hopper_width), (int) (0.75*HopperActivity.demo.hopper_width), true);

	        this.width = bmp.getWidth();//get dimensions of projectile
	        this.height = bmp.getHeight();

	  }
	    
	    protected void onDraw(Canvas canvas) {
	        Rotate_Draw_Bitmap(5*x, x, y, canvas);
	    }

	}

 
 

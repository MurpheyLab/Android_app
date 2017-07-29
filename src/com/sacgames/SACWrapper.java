package com.sacgames;

public class SACWrapper {
	
	//Those are for the pendulum part
	public static native void initialize(double h, double ts, double xdes);
	
	public static native double[] sac_stepper(double[] xinit, double tinit);
	
	public static native double[] input_stepper(double[] xinit, double tinit, double u_controller, double user_input);

    // Load library
    static {
        System.loadLibrary("SAC_pendulum");
    }
    
    
	//Those are for the hopper part
	public static native void initialize2(double ts, double middle, int difficulty);
	
	public static native double[] sac_stepper2(double[] xinit, double tinit, double direction);
	
    // Load library
    static {
        System.loadLibrary("SAC_hopper");
    }

}

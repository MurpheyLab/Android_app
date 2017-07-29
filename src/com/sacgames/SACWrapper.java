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

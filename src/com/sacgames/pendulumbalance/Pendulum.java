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



public class Pendulum implements Vector_field {
	
	protected final float m;//mass
	protected float l;//pendulum length
	protected final float b;//viscous damping coefficient
	protected final float g;
	public static int numstates = 4;
	
	
    public Pendulum(float l, float m, float g, float b) {
        this.l = l;
        this.m = m;
        this.g = g;
        this.b = b;
  }


	 public float[] calculate_vector_field(float[] x, float u)   // t = time, x = array of states
	    {
		 float[] vector_field = new float[numstates];
		 
		 // w = theta'
	      float w = x[3];
	      
	     // m l^2 w' = m g l sin(theta) + m l u cos(theta) - b w
	     // u is lateral acceleration input
	     //u =  Main.K[0]*x[0] + Main.K[1]*x[1] + Main.K[2]*x[2] + Main.K[3]*x[3];//here i should apply take finite differences twice
	      
	     float w_dot = (float) ((m*g*l*Math.sin(x[2])+m*l*u*Math.cos(x[2])-b*x[3])/(m*l*l));
	     
	     vector_field[0] = x[1];//this is xdot
	     vector_field[1] = u; // this is xddot = u	     
	     vector_field[2] = w;
	     vector_field[3] = w_dot;
     
	     return vector_field;  
	    }

}

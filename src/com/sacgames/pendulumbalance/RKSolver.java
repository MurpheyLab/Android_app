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




public class RKSolver {
	 public static float[] states;  // array of variables

	 public static void solve(float h, int N, Vector_field vf, float u)//RK Solver
	    {
	      int i;
	      float[] inp = new float[N];
	      float[] k1 = new float[N];
	      float[] k2 = new float[N];
	      float[] k3 = new float[N];
	      float[] k4 = new float[N];	    

	      
	      //RK integration
	      k1 = vf.calculate_vector_field(states, u);     // evaluate at time t
	      for (i=0; i<N; i++)
	    	  inp[i] = states[i]+k1[i]*h/2; // set up input to diffeqs
	      
	      k2 = vf.calculate_vector_field(inp, u);  // evaluate at time t+h/2
	      for (i=0; i<N; i++)
	    	  inp[i] = states[i]+k2[i]*h/2; // set up input to diffeqs
	    
	      k3 = vf.calculate_vector_field(inp, u);  // evaluate at time t+h/2
	      for (i=0; i<N; i++)
	    	  inp[i] = states[i]+k3[i]*h; // set up input to diffeqs
	    
	      k4 = vf.calculate_vector_field(inp, u);    // evaluate at time t+h
	      for (i=0; i<N; i++)
	    	  states[i] = states[i]+(k1[i]+2*k2[i]+2*k3[i]+k4[i])*h/6;//state in next time step

	    }


}

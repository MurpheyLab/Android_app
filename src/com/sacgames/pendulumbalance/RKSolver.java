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

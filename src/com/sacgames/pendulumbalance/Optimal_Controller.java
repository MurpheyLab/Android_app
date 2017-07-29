package com.sacgames.pendulumbalance;


import Jama.Matrix;

public class Optimal_Controller extends Pendulum implements Vector_field {
	public int numstates;
	public float R;
	public double[][] Q;
	public Matrix Bm;

	public Optimal_Controller(float m, float l, float b, double[][] Q, float R, float g) {
		super(l, m, g, b);
		this.R = R;//LQR Cost weights
		this.Q = Q;
		this.numstates = Q.length * Q.length;//number of elements in the P matrix
	}
	
	
	 public float[] calculate_vector_field(float[] Pv, float u)   // t = time, x = array of states
	    {
		 double[] vector_field_temp = new double[numstates];
		 float[] vector_field = new float[numstates];//this is for the final result
		 
		 //Convert P back to matrix (it was column)
		 double[][] P = new double[(int) Math.sqrt(numstates)][(int) Math.sqrt(numstates)];
		 final int Plength = P.length;
		 for(int i=0; i<Plength; i++){
			 for(int j=0; j<Plength; j++){
				 P[j][i] = Pv[i*Plength+j];
			 }
		 }
		 Matrix Pm = new Matrix(P);		
		 
		 Matrix Qm = new Matrix(Q);//state weights	 
		 
		 //State space equations: X' = AX+Bu
	     Bm = new Matrix(Bm());
	      
	     Matrix Am = new Matrix(Am());
	      
	     //RICCATI EQUATION
	     Matrix Riccati1 = Pm.times(Am).timesEquals(-1).minus(Am.transpose().times(Pm));
	     Matrix Riccati2 = Pm.times(Bm).timesEquals(1/R).times(Bm.transpose()).times(Pm).minus(Qm);
	     Matrix P_dot = Riccati1.plus(Riccati2);
	      
	     //Convert back to column and make it float
	     vector_field_temp = P_dot.getColumnPackedCopy();

	     for (int i = 0 ; i < vector_field_temp.length; i++)
	         vector_field[i] = (float) vector_field_temp[i];
	     
	     return vector_field;  
	    }
	 
	   //Calculates B matrix from state space
	    public double[][] Bm(){
	    	//double[][] B = {{0},{1/M},{0},{1/(M*super.l)}};
	    	double[][] B = {{0},{1},{0},{1/(super.l)}}; //this is for the system with kinematic input (not with Force)
	    	return B;

	    }
	    
		//Calculates A matrix from state space
	    public double[][] Am(){
	    	//double[][] A = {{0, 1, 0, 0},{0, 0, super.m*super.g/M, -super.b/(M*super.l)},{0, 0, 0, 1},{0, 0, (M+super.m)*super.g/(M*super.l), -super.b*(1/(super.m*Math.pow(super.l,2))+1/(M*Math.pow(super.l,2)))}};
	    	double[][] A = {{0, 1, 0, 0},{0, 0, 0, 0},{0, 0, 0, 1},{0, 0, super.g/(super.l), -super.b*(1/(super.m*Math.pow(super.l,2)))}};//this is for the system with kinematic input (not with Force)
	    	return A;

	    }
	   

}

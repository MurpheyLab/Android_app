#include <iostream>
#include "SAC.h"
#include <android/log.h>
#include "setting.hpp"              // Pendulum length & SAC settings
#include <master.hpp>               // Master include file
#include <cmath>  /* for std::abs(double) */


#define TAG_DEBUG "myApp"
using namespace sac;

/* Required by lib fuctions */
inline void state_proj( state_type & x );
inline void get_DesTraj( const double t,
			 Eigen::Matrix< double, xlen, 1 > &m_mxdes );

inline bool isEqual(double x, double y);

/* Required if you want to control those from java */
double sac::h;// = 2;
double sac::ts;
double sac::T;
double sac::u_sat;
double xdes;



/* Initialization Stuff (ALSO SEE SETTINGS.HPP) */
//void initialize(double h, double ts, double usat);

namespace sac {
  namespace init {
    using namespace std;
    //[ initialization    
    /* Search for or just apply u2* at t0? */
    bool u2Search = true;
    state_type x(xlen), u_switch(ulen), t_curr(3), u_default(ulen);
    //
    sac_step SACit( u2Search, get_DesTraj ); 
    vector<state_type> &x_vec = SACit.x_vec, x_out, u2list, TiTappTf;
    vector<double> &times = SACit.times;
    state_intp x_intp(x_vec, times, xlen);
    state_type &x0noU = SACit.x0noU;   b_control &u = SACit.u;
    double &J0 = SACit.J0, &Jn = SACit.Jn, &t_app = SACit.t_app, 
      &t_i = SACit.t_i, &t_f = SACit.t_f;
  }
}


/* SAC STEPPER
  input:  initial state and time 
  return: vector [x1_new, x2_new, t_new, u_new, t_1, t_2]
  
  x1_new - intregrated theta component of cart pendulum at time t_new
  x2_new - intregrated theta_dot component of cart pendulum at time t_new
  x3_new - intregrated cart position of cart pendulum at time t_new
  x4_new - intregrated cart verlocity of cart pendulum at time t_new
  t_new - updated time = t0 + ts  (ts specified in settings)
  u_new - control applied from [t_1, t_2] which is a subset of [t0, t0+ts].
          If [t_1, t_2] is not equal to [t0, t0+ts] then the default control
          u_new=0 is applied over the remaining interval. 
  t_1 - initial time for application of the control.  t0 <= t_1 <= t0+ts
  t_2 - final time for control application.  t0 <= t_2 <= t0+ts

  WARNING: u_new is only applied when t_2-t_1 > 0, otherwise u_new=0.
  WARNING: If [t_1, t_2] is not equal to [t0, t0+ts] then u_new=0 is applied 
           over the remaining interval.
  NOTE: for speed return and input types should be changed and passed as
        references / pointers
*/


JNIEXPORT jdoubleArray JNICALL Java_com_sacgames_SACWrapper_sac_1stepper
  (JNIEnv * je, jclass jc, jdoubleArray xinit_s, jdouble tinit)
{



//std::vector<double> sac_step(state_type xinit, double tinit) {
    using namespace std;
    using namespace boost::numeric::odeint;
    using namespace sac::init;



    jsize size = je->GetArrayLength( xinit_s );//get size of xinit_s
    state_type xinit (size);

    je->GetDoubleArrayRegion( xinit_s, 0, size, &xinit[0] );//point xinit to xinit_s



    /* Perform SAC iteration - updates: J0, Jn, u, x_intp */
    SACit( tinit, xinit, u_default, 0, 0 );	 

    /* Get new u & switching times */
    u( t_i, u_switch );   
 
    if ( Jn > J0 ) { // cost increased so don't apply control
      xinit = x0noU;    // return x
      u_switch[0]=0; // return u
      t_curr[0]=tinit; t_curr[1]=tinit; 
      t_curr[2]=tinit+ts; // return time horizon
    }
    else { 
      x_intp( tinit+ts, xinit );
      t_curr[0]=t_i; t_curr[1]=t_app;  
      t_curr[2]=( t_f > tinit+ts ? tinit+ts : t_f ); // return time horizon
    }
    
    std::vector<double> rvec; // return vec
    rvec.push_back( xinit[0] );
    rvec.push_back( xinit[1] );
    rvec.push_back( xinit[2] );
    rvec.push_back( xinit[3] );
    //rvec.push_back( tinit+ts );
    rvec.push_back( u_switch[0] );
    //rvec.push_back( t_curr[0] );
    //rvec.push_back( t_curr[2] );



    jdoubleArray rvec2 = je->NewDoubleArray( rvec.size() );
    je->SetDoubleArrayRegion( rvec2, 0, rvec.size(), &rvec[0] );



    //__android_log_print(ANDROID_LOG_INFO, TAG_DEBUG, "malakia %f", rvec2[4]);


    return rvec2;
}



/*******************************************
   Alternative (not SAC) control input */

JNIEXPORT jdoubleArray JNICALL Java_com_sacgames_SACWrapper_input_1stepper
(JNIEnv * je, jclass jc, jdoubleArray xinit_s, jdouble tinit, jdouble u_controller, jdouble user_input) {
    using namespace std;
    using namespace boost::numeric::odeint;
    
    typedef runge_kutta_dopri5< state_type > stepper_type;
    double tf = tinit+ts; 
	double accepted;
    
	b_control u(ulen);//I should not create new instances every time but let it be for now
	sys_dynam xdot(u);
	xdot.SAC = false;//don't use SAC as input
	
	jsize size = je->GetArrayLength( xinit_s );//get size of xinit_s
    state_type xinit (size);

    je->GetDoubleArrayRegion( xinit_s, 0, size, &xinit[0] );//point xinit to xinit_s

	//saturate user's input to create a never-failing interface
	if(user_input > sac::u_sat)
		user_input = sac::u_sat;
	if(user_input < -sac::u_sat)
		user_input = -sac::u_sat;
	
	//Maxwell's demon check	
	if(((user_input*u_controller) >= 0)){//||((abs(u_controller)<1)&&(abs(user_input)<5))) {//if inner product is positive
		xdot.u_curr_[0] = user_input;
		accepted = 1;
	} else {
		xdot.u_curr_[0] = 0;//u_controller;
		accepted = 0;
	}
		

	//simulate system
    size_t steps = integrate_adaptive( 
				      make_controlled( 1E-4 , 1E-4 , 
						       stepper_type( ) ) , 
				      xdot , xinit , tinit , tf , 0.0005);	

    std::vector<double> rvec; // return vec
    rvec.push_back( xinit[0] );
    rvec.push_back( xinit[1] );
    rvec.push_back( xinit[2] );
    rvec.push_back( xinit[3] );
	rvec.push_back( accepted );
    //rvec.push_back( pot_input );
    //rvec.push_back( xdot.u_curr_[0] );
	
	jdoubleArray rvec2 = je->NewDoubleArray( rvec.size() );
    je->SetDoubleArrayRegion( rvec2, 0, rvec.size(), &rvec[0] );
    
    return rvec2;
}







//Initializations
JNIEXPORT void JNICALL Java_com_sacgames_SACWrapper_initialize
  (JNIEnv * je, jclass jc, jdouble h, jdouble ts, jdouble x_des)
{
  using namespace sac::init;
  double q3, q4, p1, p2, q2, p3;

  //different weights depending on the difficulty level
  if(isEqual(h, 1.0)){
	  q2 = 0;
	  q3 = 100;
	  q4 = 50;
	  p1 = 0;
	  p2 = 0;
	  p3 = 0;
	  sac::T = 1.2;
	  sac::u_sat = 35;
  }
  if(isEqual(h, 2.8)){
	  q2 = 0;
	  q3 = 25;//25
	  q4 = 20;//20
	  p1 = 12;//12
	  p2 = 12;//12
	  p3 = 0;
	  sac::T = 1.7;
	  sac::u_sat = 100;
  }
  if(isEqual(h, 6.0)){
	  q2 = 0;
	  q3 = 10;//8
	  q4 = 15;//18
	  p1 = 25;//20
	  p2 = 25;//20
	  p3 = 0;
	  sac::T = 2.5;//2.0
	  sac::u_sat = 150;
  }


  
  Q << 200, 0, 0, 0,  // 200            // Q weight matrix
    0, q2, 0, 0,       // 0
    0, 0, q3, 0,     // 100
    0, 0, 0, q4;      // 50
  P << p1, 0, 0, 0,    // 0              // P weight matrix
    0, p2, 0, 0,
    0, 0, p3, 0,
    0, 0, 0, 0;
  R << 0.3;           // 0.3             // R weight matrix

  u_default[0] = 0.0;

  /* Initialize pendulum length, sampling time (and maximum control duration) ts
  and also saturation (this depends on the screen size) */
  sac::h = h; //CHANGE THAT LATER!!
  sac::ts = ts;
  xdes = x_des;

	//__android_log_print(ANDROID_LOG_INFO, TAG_DEBUG, "malakia %f", q3);

}


inline bool isEqual(double x, double y)
{
  const double epsilon = 1e-3;
  return std::abs(x - y) <= epsilon * std::abs(x);

}





/*******************************************
   Projection for calculations involving x(t) */
inline void state_proj( state_type & x ) {
  AngleWrap( x[0] );
}


/*******************************************
   outputs a point in the desired trajectory at time t */
inline void get_DesTraj( const double /*t*/, 
			 Eigen::Matrix< double, xlen, 1 > &m_mxdes ) {
  m_mxdes << 0, 0, xdes, 0;
}

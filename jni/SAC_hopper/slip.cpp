/*
 2D SLIP Hopper

 State dimension is 5:
 X = [ x, x', z, z', x_toe ];
 
 Control dimension is 2 with saturation:
 U = [ add_toe_x_vel,   thrust_into_ground ];
 */

#include <iostream>
#include "setting.hpp"                     // Project globals & settings
#include "SAC2.h"
#include <master.hpp>                      // Master include file

using namespace sac;

double dir;//=1; /* Set the desired hopping direction */
double height = 1.6;

inline void get_DesTraj( const double t, 
			 Eigen::Matrix< double, xlen, 1 > &m_mxdes );
inline double PhiWrap( state_type &x );

double sac::ts;//global defined by java
double sac::screen_center;
int sac::difficulty;

/* Initialization Stuff (ALSO SEE SETTINGS.HPP) */
void initialize();
namespace sac {
  namespace init {
    using namespace std;
    //[ initialization    
    /* Search for or just apply u2* at t0? */
    bool u2Search = false;
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
  return: vector [x1_new, ... , x5_new, t_new, u_new, t_1, t_2]
  
  x1_new - intregrated mass x at time t_new
  x2_new - intregrated mass x velocity at time t_new
  x3_new - intregrated mass z at time t_new
  x4_new - intregrated mass z velocity at time t_new
  x5_new - intregrated toe x position, xt, at time t_new
  t_new - updated time = t0 + ts  (ts specified in settings)
  u1_new - toe velocity control applied from [t_1, t_2] which is a subset of 
           [t0, t0+ts]. If [t_1, t_2] is not equal to [t0, t0+ts] then 
           the default control u_new=0 is applied over the remaining interval. 
  u2_new - thrust applied from [t_1, t_2] which is a subset of [t0, t0+ts]. 
           If [t_1, t_2] is not equal to [t0, t0+ts] then the default control 
	   u2_new=0 is applied over the remaining interval. 
  t_1 - initial time for application of the control.  t0 <= t_1 <= t0+ts
  t_2 - final time for control application.  t0 <= t_2 <= t0+ts

  WARNING: u_new is only applied when t_2-t_1 > 0, otherwise u_new=0.
  WARNING: If [t_1, t_2] is not equal to [t0, t0+ts] then u_new=0 is applied 
           over the remaining interval.
  NOTE: for speed return and input types should be changed and passed as
        references / pointers
*/
JNIEXPORT jdoubleArray JNICALL Java_com_sacgames_SACWrapper_sac_1stepper2
  (JNIEnv * je, jclass jc, jdoubleArray xinit_s, jdouble tinit, jdouble direction)
{
    using namespace std;
    using namespace sac::init;
	
	dir = direction;
		
	jsize size = je->GetArrayLength( xinit_s );//get size of xinit_s
    state_type xinit (size);

    je->GetDoubleArrayRegion( xinit_s, 0, size, &xinit[0] );//point xinit to xinit_s


    /* Perform SAC iteration - updates: J0, Jn, u, x_intp */
    // SACit( tinit, xinit, u_default, 0, 0 );	  	
    SACit( tinit, xinit, u_default, tinit, tinit+calc_tm );

    /* Get new u & switching times */
    u( t_i, u_switch );   
 
    if ( Jn > J0 ) { // cost increased so don't apply control
      xinit = x0noU;    // return x
      u_switch[0]=0; // return u
      u_switch[1]=0; // return u
      t_curr[0]=tinit+calc_tm; t_curr[1]=t_curr[0]; 
      t_curr[2]=t_curr[0]+ts; // return time horizon
    }
    else { 
      x_intp( tinit+ts, xinit );
      t_curr[0]=t_i; t_curr[1]=t_app;  
      t_curr[2]=t_f;
    }
    
    std::vector<double> rvec; // return vec

    rvec.push_back( xinit[0] );
    rvec.push_back( xinit[1] );
    rvec.push_back( xinit[2] );
    rvec.push_back( xinit[3] );
    rvec.push_back( xinit[4] );
	//Calculate spring length
	if ( Phi( xinit ) > 0 ) { // flight dynamics
		l = L0;
	} else {             // stance dynamics
		l = pow( pow(xinit[0]-xinit[4],2)+pow(xinit[2]-zGrndToe(xinit[4]),2) ,0.5);
	}	
	rvec.push_back( sac::l );
	
	//set desired height according to terrain
	height = 1.6 + zGrndToe( xinit[0] );
	
	//See if the hopper is down
	if (xinit[2] > 0) 
		rvec.push_back( 0 );
	else
		rvec.push_back( 1 );
    //rvec.push_back( u_switch[0] );
    //rvec.push_back( u_switch[1] );
    //rvec.push_back( t_curr[0] );
    //rvec.push_back( t_curr[2] );
	
	jdoubleArray rvec2 = je->NewDoubleArray( rvec.size() );
    je->SetDoubleArrayRegion( rvec2, 0, rvec.size(), &rvec[0] );
    
    return rvec2;
}


//int main(int /* argc */ , char** /* argv */ )
//{
//    using namespace std;
//    using namespace boost::numeric::odeint;
    
    /* initialization */
//    using namespace sac::init;
 //   initialize();
    
 //   std::vector<double> rvec(8); 
 //   double t0=0.0;  state_type x0(xlen);
 //   x0[0]=0; x0[1]=1; x0[2]=1.75; x0[3]=0; x0[4]=0;

  //  rvec = sac_stepper( x0, t0 );

  //  for (t0 = 0; t0 < 105; t0 = t0 + ts)//receding horizon
  //    {
//	rvec = sac_stepper( x0, t0 );

	/* TEST IF HOPPER FALLS */
	//if (rvec[2] <= 0) { 
//	  cout << "The hopper fell at t = " << t0 << "!\n";
//	  break; 
//	}

	/* Print Return Values */
	// updates: x0, t_curr[1], t_curr[2], u_switch[0]
//	 cout << rvec[0] << ", " << rvec[1] << ", " << rvec[2]
//	      << ", " << rvec[3] << "\n";

	/* Update State */
//	x0[0] = rvec[0]; x0[1] = rvec[1]; x0[2] = rvec[2]; 
//	x0[3] = rvec[3]; x0[4] = rvec[4];//for receding horizon
  //    }
 //   	system("pause");
  //  return 0;
//}

/*******************************************
   Initialize */
JNIEXPORT void JNICALL Java_com_sacgames_SACWrapper_initialize2
  (JNIEnv * je, jclass jc, jdouble ts, jdouble middle, jint difficulty)
{
  using namespace sac::init;
  
    Q(1,1) = 95;                           // Q weight matrix
    Q(2,2) = 75;
    // R weight matrix
    R = Eigen::Matrix< double,ulen,ulen >::Identity(ulen,ulen);

    u_default[0] = 0.0;
    u_default[1] = 0.0;
	
	sac::ts = ts;
	sac::difficulty = difficulty;
	
	sac::screen_center = middle;
}



/*******************************************
   outputs a point in the desired trajectory at time t */
inline void get_DesTraj( const double t, 
			 Eigen::Matrix< double, xlen, 1 > &m_mxdes ) {
  m_mxdes << 0, dir, height, 0, 0;
}

/*!
    computes signed distance between state and the event surface. Event_detect
    looks for a zero crossing in this function
    \param[in] x state_type storing the current state.
    \return Signed distance between state and the event surface
  */
inline double PhiWrap( state_type &x ) { 
  return Phi(x); 
}

/*******************************************
   outputs times vector elements with the associated state vector elements */
void output( std::vector<state_type>& states, std::vector<double>& times ) {
  using namespace std;
  for( size_t i=0; i<times.size(); i++ ) {
    cout << times[i] << '\t';
    for ( size_t j=0; j<states.back().size(); j++ ) {
      cout << states[i][j] << '\t';
    }
    cout << '\n';
  }
}

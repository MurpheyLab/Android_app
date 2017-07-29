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
#ifndef SYS_DYNAM_HPP
#define SYS_DYNAM_HPP

namespace sac {

  inline double zGrndToe( const double& /*x*/ );
  inline void DzGrndToe( const state_type &/*x*/, state_type &Dgrnd );
  inline double Phi( const state_type &x );

  //[ The rhs of xdot = f(x) defined as a class
  // USER SPECIFIED:
  class sys_dynam {
    b_control & c_u_;
    state_type u_;

  public:
    sys_dynam( b_control & uu ) : c_u_(uu) , u_(ulen) {  }

    void operator() (const state_type &x, state_type &dxdt, const double t)
    {
      c_u_(t, u_);
      //
      if ( Phi( x ) > 0 ) { // flight dynamics
			dxdt[0] = x[1];
			dxdt[1] = 0;
			dxdt[2] = x[3];
			dxdt[3] = -9.81;
			dxdt[4] = x[1]+u_[0]; 
      } else {             // stance dynamics
			double L = pow( pow(x[0]-x[4],2)+pow(x[2]-zGrndToe(x[4]),2) ,0.5);
			dxdt[0] = x[1];
			dxdt[1] = (k/m*(L0 - L) + u_[1]/m)*(x[0]-x[4])/L;
			dxdt[2] = x[3];
			dxdt[3] = (k/m*(L0 - L) + u_[1]/m)*(x[2]-zGrndToe(x[4]))/L 
			  - 9.81;
			dxdt[4] = 0; 
      }
    }
  };
  //]


  /*!
    computes the ground z-height at a location x
    \param[in] x position in the x-dimension of the world frame.
  */
  inline double zGrndToe( const double& x ) {  

  	if(difficulty == 3){
		if((x < screen_center)&&(x > screen_center/3.0))
			return  0.2*cos(4.0*x - Pi)+0.2; // 0.2*cos(2.0*x); 
		else if((x < 4*screen_center/3.0)&&(x > screen_center))
			return  0.3*cos(4.0*x - Pi)+0.3; // 0.2*cos(2.0*x); 
		else
			return  0;//0.2*cos(4.0*x - Pi)+0.2; // 0.2*cos(2.0*x); 
	} else if(difficulty == 2){
		return 0.1*cos(2.0*x - Pi)+0.1;
	} else
		return 0;
		
  }

  /*!
    computes the spatial derivative of the ground function at a location x
    \param[in] x state_type storing the current state.
  */
  inline void DzGrndToe( const state_type &x, state_type &Dgrnd ) {
    for (size_t i = 0; i<xlen; i++) {
      Dgrnd[i]=0;
    }
	if(difficulty == 3){
		if((x[4] < screen_center)&&(x[4] > screen_center/3.0))
			Dgrnd[4]= -0.8*sin(4.0*x[4] - Pi);//-0.4*sin(2.0*x[4]);
		else if((x[4] < 4*screen_center/3.0)&&(x[4] > screen_center))
			Dgrnd[4]= -1.2*sin(4.0*x[4] - Pi);//-0.4*sin(2.0*x[4]);
	} else if(difficulty == 2){
		Dgrnd[4]= -0.2*sin(2.0*x[4] - Pi);//-0.4*sin(2.0*x[4]);
	}
  }

  /*!
    computes signed distance between state and the event surface. Event_detect
    looks for a zero crossing in this function
    \param[in] x state_type storing the current state.
    \return Signed distance between state and the event surface
  */
  inline double Phi( const state_type &x ) { 
    return x[2] 
      - L0*( x[2]-zGrndToe(x[4]) )
      / pow( pow(x[0]-x[4],2)+pow(x[2]-zGrndToe(x[4]),2) ,0.5)
      - zGrndToe(x[4]); 
  }

}

#endif  // SYS_DYNAM_HPP

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
#ifndef INC_COST_HPP
#define INC_COST_HPP

namespace sac {

  /*********************************************/
  //[ Optimized incremental trajectory tracking cost, l(x), for integration
  // USER SPECIFIED:
  class inc_cost {
    state_intp & x_intp_; // store current state
    state_type x_;
    Eigen::Matrix< double, xlen, 1 > mxdes_;
    void (*p_get_DesTraj_)( const double t,        // pointer to function
			    Eigen::Matrix< double, // to get desired trajectory
			    xlen, 1 > &m_mxdes );
  
  public:
    inc_cost( state_intp & x_intp, 
	      void (* xdesFnptr) ( const double t, 
				   Eigen::Matrix< double, xlen, 1 > &m_mxdes ) 
	      ) : x_intp_( x_intp ), x_( xlen ),		
		  mxdes_(Eigen::Matrix< double, 
			 xlen, 1 >::Zero(xlen,1) ),
		  p_get_DesTraj_( xdesFnptr ) { }
  
    void operator() (const state_type &/*J*/, state_type &dJdt, const double t)
    {
      x_intp_(t, x_); // store the current state in x
      AngleWrap( x_[0] ); // Only for angle wrapping
      //
      p_get_DesTraj_( t, mxdes_ ); // Store desired trajectory point in mxdes_
      //
      dJdt[0] = ( ( Q(0,0)*pow(x_[0]-mxdes_[0] , 2) 
		    + Q(1,1)*pow(x_[1]-mxdes_[1] , 2) 
		    + Q(2,2)*pow(x_[2]-mxdes_[2] , 2) 
		    + Q(3,3)*pow(x_[3]-mxdes_[3] , 2)  ) / 2.0 );
    }
  
    inline void dx( const double t, const Eigen::Matrix< double, xlen, 1 > &mx,
		    Eigen::Matrix< double, 1, xlen > &dldx ) { 
      p_get_DesTraj_( t, mxdes_ ); // Store desired trajectory point in mxdes_
      //
      dldx = (mx-mxdes_).transpose()*Q;
    }

    double begin( ) { return x_intp_.begin( ); }

    double end( ) { return x_intp_.end( ); }
  };
  //]

}

#endif  // INC_COST_HPP

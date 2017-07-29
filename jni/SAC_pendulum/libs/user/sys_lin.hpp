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
#ifndef SYS_LIN_HPP
#define SYS_LIN_HPP

namespace sac {

  //[ Linearizations of the system defined as a class
  // USER SPECIFIED:
  class sys_lin {
    double g_, m_, b_;
  
  public:
    sys_lin( ) :  g_(g) , m_(m), b_(b){  }

    void A( const state_type & x, const state_type & u, 
	    Eigen::Matrix< double, xlen, xlen > & Amat ) {
      Amat << 0, 1, 0, 0,
	( g_*cos(x[0]) - u[0]*sin(x[0]) ) / h ,  -b_/(m_*h*h), 0, 0,
	0, 0, 0, 1,
	0, 0, 0, 0;
    }

    void B( const state_type & x, const state_type & /*u*/, 
	    Eigen::Matrix< double, xlen, ulen > & Bmat ) {
      Bmat << 0, cos(x[0]) / h, 0, 1;
    }
  };
  //]

}

#endif  // SYS_LIN_HPP

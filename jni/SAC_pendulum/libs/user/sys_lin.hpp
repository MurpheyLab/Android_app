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

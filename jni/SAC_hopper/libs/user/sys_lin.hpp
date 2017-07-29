#ifndef SYS_LIN_HPP
#define SYS_LIN_HPP

namespace sac {

  //[ Linearizations of the system defined as a class
  // USER SPECIFIED:
  class sys_lin {
    state_type Dgrnd_;
    double zGrnd_;
    double L_;
    //
    double dzGrnddxt_;
    double dzGrnddyt_;
    double dLdzGrnd_;
    double dLdx_;
    double dLdy_;
    double dLdz_;
    double dLdxt_;
    double dLdyt_;
    double denom_;
    //
    
  public:
    sys_lin( ) : Dgrnd_(xlen) , zGrnd_(0) , L_(0) ,
                 dzGrnddxt_(0) , dzGrnddyt_(0) , dLdzGrnd_(0) ,
                 dLdx_(0), dLdy_(0), dLdz_(0), dLdxt_(0), 
		 dLdyt_(0), denom_(1) {  }

    void A( const state_type & x, const state_type & u, 
	    Eigen::Matrix< double, xlen, xlen > & Amat ) {
      zGrnd_ = zGrndToe( x[4] );
      L_ = pow( pow(x[0]-x[4],2)+pow(x[2]-zGrnd_,2) ,0.5);
      DzGrndToe( x, Dgrnd_ );
      //
      dzGrnddxt_ = Dgrnd_[4];
      dLdzGrnd_ = (zGrnd_-x[2])/L_;
      dLdx_ = (x[0]-x[4])/L_;
      dLdz_ = (x[2]-zGrnd_)/L_;
      dLdxt_ = -dLdx_ + dLdzGrnd_*dzGrnddxt_;
      //
      Amat = 0*Amat;
      if ( Phi( x ) > 0 ) { // flight linearization
	Amat(0,1) = 1;
	Amat(2,3) = 1;
	Amat(4,1) = 1;
      } else {             // stance linearization
	denom_ = m*pow(L_,2);
	//
	Amat(0,1) = 1;
	//
	Amat(1,0) = (-k*pow(L_,2) + L_*(k*L0 + u[1]) 
		     - (k*L0 + u[1])*(x[0] - x[4])*dLdx_ )
		     / (denom_);
	Amat(1,2) = -(k*L0 + u[1])*(x[0] - x[4])*dLdz_ / (denom_);
	Amat(1,4) = (k*pow(L_,2) - L_*(k*L0 + u[1]) 
		     - (k*L0 + u[1])*(x[0] - x[4])*dLdxt_) 
	            / (denom_);
	//
	Amat(2,3) = 1;
	//
	Amat(3,0) = (k*L0 + u[1])*(zGrnd_ - x[2])*dLdx_ / (denom_);
	Amat(3,2) = (-k*pow(L_,2) + L_*(k*L0 + u[1]) 
		     + (k*L0 + u[1])*(zGrnd_ - x[2])*dLdz_ )
		     / (denom_);
	Amat(3,4) = (k*pow(L_,2)*dzGrnddxt_ - L_*(k*L0 + u[1])*dzGrnddxt_
		     + (k*L0 + u[1])*(zGrnd_ - x[2])*dLdxt_) 
	            / (denom_);
      }
    }

    void B( const state_type & x, const state_type & /*u*/,
	    Eigen::Matrix< double, xlen, ulen > & Bmat ) {
      zGrnd_ = zGrndToe( x[4] );
      L_ = pow( pow(x[0]-x[4],2)+pow(x[2]-zGrnd_,2) ,0.5);
      Bmat = Bmat*0;
      if ( Phi( x ) > 0 ) { // flight linearization
	Bmat(4,0) = 1;
      } else {             // stance linearization
	Bmat(1,1) = (x[0]-x[4])/(m*L_);
	Bmat(3,1) = (x[2]-zGrnd_)/(m*L_);
      }
    }

  };
  //]

}

#endif  // SYS_LIN_HPP

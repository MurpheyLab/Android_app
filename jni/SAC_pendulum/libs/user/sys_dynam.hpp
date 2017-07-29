#ifndef SYS_DYNAM_HPP
#define SYS_DYNAM_HPP

namespace sac {

  //[ The rhs of x' = f(x) defined as a class
  // USER SPECIFIED:
  class sys_dynam {
    double g_, m_, b_;
    b_control & u_;

  public:
	//need to change these from outside for general control inputs
    bool SAC;
    state_type u_curr_;
    sys_dynam( b_control & u ) : g_(g) , m_(m), b_(b), u_(u), u_curr_(ulen), SAC(true) { }
  
  
    void operator() (const state_type &x, state_type &dxdt, const double t)
    {
      if(SAC)
      	u_(t, u_curr_);
		
      dxdt[0] = x[1];
      dxdt[1] = ( g_*sin(x[0]) + u_curr_[0]*cos(x[0]) )/h - b_*x[1]/(m_*h*h);//using the global value for the length l
      dxdt[2] = x[3];
      dxdt[3] = u_curr_[0];
    }
  };
  //]

}

#endif  // SYS_DYNAM_HPP

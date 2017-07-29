#ifndef SETTING_HPP
#define SETTING_HPP

namespace sac {

  /*********************************************/
  /* Parameters */
  double T = 0.55;       // prediction horizon
  double lam = -15;
  double maxdt = .2;
  extern double ts;// = 0.05;
  extern double screen_center;
  extern int difficulty;
  double l;//spring length
  double usat[3][2] = { {5, -5}, {30, -30} };
  double calc_tm = 0;  //ts;
  /**/
  const size_t xlen = 5;
  const size_t ulen = 2;
  /**/
  double  L0=1, m=1, k=90;
  
  const double Pi = 3.1415926535897;

}

/*********************************************/
/* Optional Class declarations */
// #define IMPACTS    // use bc_impact_step, event_detect, & event_exec classes


#endif  // SETTING_HPP

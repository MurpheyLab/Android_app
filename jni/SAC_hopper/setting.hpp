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

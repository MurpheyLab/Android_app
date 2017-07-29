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
  extern double T;  // prediction horizon
  extern double u_sat;//this is to satirate user's input when in maxwell's demon mode
  double lam = -5; 
  double maxdt = .2;
  extern double ts;// = 0.0167; // game frequency
  double usat[1][2] = { {15, -15} };
  double calc_tm = 0;
  /**/
  extern double h;// = 1.0;  // pendulum length set by java
  const double g = 9.81;  // pendulum length
  const double m = 0.2;  // pendulum length
  const double b = 0.01;  // pendulum length
  const size_t xlen = 4, ulen = 1;

}

#endif  // SETTING_HPP

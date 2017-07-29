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
package com.sacgames.hopper;

public class Score implements Comparable<Score> {
	private String scoreDate;
	public int scoreNum;
	
	public Score(String date, int num){
	    scoreDate=date;
	    scoreNum=num;
	}
	
	
	public int compareTo(Score sc){
	    //return 0 if equal
	    //1 if passed greater than this
	    //-1 if this greater than passed
	    return sc.scoreNum>scoreNum? 1 : sc.scoreNum<scoreNum? -1 : 0;
	}
	
	public String getScoreText()
	{
	    return scoreDate+" : "+scoreNum;
	}
}

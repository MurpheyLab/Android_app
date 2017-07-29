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
package com.sacgames;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.sacgames.R;
import com.sacgames.hopper.MainActivity2;
import com.sacgames.pendulumbalance.MainActivity;

public class ChoiceGameActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_choice_game);
	}
	

    /* Called when the user clicks the pendulum button */
    public void pendulumGame(View view) {
    	Intent intent = new Intent(this, MainActivity.class); //create explicit intent to start game

    	startActivity(intent);  
    }
    
    /* Called when the user clicks the hopper button */
    public void hopperGame(View view) {
    	Intent intent = new Intent(this, MainActivity2.class); //create explicit intent to start game

    	startActivity(intent);  
    }
}

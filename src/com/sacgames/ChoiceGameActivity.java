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

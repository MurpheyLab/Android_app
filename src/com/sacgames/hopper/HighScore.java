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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sacgames.R;
import com.sacgames.R.id;
import com.sacgames.R.layout;

public class HighScore extends Activity {
	private SharedPreferences[] gamePrefs = new SharedPreferences[3];
	public static final String[] GAME_PREFS = {"High_Score_Easy", "High_Score_Medium", "High_Score_Hard"};//name of files

	private TextView easy, high_scores, medium, hard;
	private TextView[] high_score_text = new TextView[3];
	private Button button;
	final Context context = this;
    private int[] text_names = new int[]{R.id.scores_easy, R.id.scores_medium, R.id.scores_hard};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_high_score);
		
		//Create fonts
		Typeface tf = Typeface.createFromAsset(getAssets(),"JUNEBUG_.ttf");
		
		//File creation mode: the default mode, where the created file can only be accessed by the calling application
		for(int i = 0;i<3;i++) {
			gamePrefs[i] = getSharedPreferences(GAME_PREFS[i], Context.MODE_PRIVATE);
			high_score_text[i] = (TextView) findViewById(text_names[i]);//get ID of textview  
			high_score_text[i].setTypeface(tf);
		}

		button = (Button) findViewById(R.id.buttonAlert); 
		high_scores = (TextView) findViewById(R.id.high_scores);//get ID of textview 
		easy = (TextView) findViewById(R.id.easy);//get ID of textview 
		medium = (TextView) findViewById(R.id.medium);//get ID of textview 
		hard = (TextView) findViewById(R.id.hard);//get ID of textview 
		
		//set fonts
	    button.setTypeface(tf);
	    easy.setTypeface(tf);
	    medium.setTypeface(tf);
	    hard.setTypeface(tf);
	    high_scores.setTypeface(tf);
		
	}

	
	@Override
	public void onResume(){
	    super.onResume();
	    Log.i("myApp", "Main Resuming");
	    
	    for(int i = 0;i<3;i++) {
		    //Split the string into an array of high scores:
		    String[] savedScores = gamePrefs[i].getString("highScores", "").split("\\|");
		    
		    //Iterate through the scores, appending them into a single string with new lines between them:
		    StringBuilder scoreBuild = new StringBuilder("");
		    for(String score : savedScores) {
		        scoreBuild.append(score+"\n");
		    }	    
	    
		    //Show scores
		    high_score_text[i].setText(scoreBuild.toString());
	    }
 
	}
	
	
	public static void setHighScore(int exScore, SharedPreferences gamePrefs){
		//set high score  

		if(!GameView2.high_score_checked){
			//we have a valid score  
			SharedPreferences.Editor scoreEdit = gamePrefs.edit();//open file to edit
			//get date
			SimpleDateFormat dateForm = new SimpleDateFormat("dd MMMM yyyy");
			String dateOutput = dateForm.format(new Date());
			//retrieve any existing scores from file
			String scores = gamePrefs.getString("highScores", "");

			if(scores.length()>0){
			    //we have existing scores
				List<Score> scoreStrings = new ArrayList<Score>();
				//We will be storing the high scores in the Shared Preferences as one pipe-delimited string, so split that now
				String[] exScores = scores.split("\\|");
				
				// creating a Score object for each high score by splitting each one into its date and number, then adding it to the list
				for(String eSc : exScores){
				    String[] parts = eSc.split(" : ");
				    scoreStrings.add(new Score(parts[0], Integer.parseInt(parts[1])));
				}
				
				if(exScore > scoreStrings.get(scoreStrings.size()-1).scoreNum)//if this score is bigger than the last entry
					GameView2.high_score = true;//Log.i("myApp", String.valueOf(scoreStrings.get(scoreStrings.size()-1).scoreNum));

				
				//create a Score object for the current score and add it to the list
				Score newScore = new Score(dateOutput+" by "+MainActivity2.current_user, exScore);
				scoreStrings.add(newScore);
				
				//This will sort the Score objects according to the compareTo method we defined in the class declaration
				Collections.sort(scoreStrings);
				
				
				//Now we can simply add the first ten to a pipe-delimited string and write it to the Shared Preferences:
				StringBuilder scoreBuild = new StringBuilder("");
				for(int s=0; s<scoreStrings.size(); s++){
				    if(s>=5) 
				    	break;//only want five
				    if(s>0) 
				    	scoreBuild.append("|");//pipe separate the score strings
				    scoreBuild.append(scoreStrings.get(s).getScoreText());
				}
				//write to prefs
				scoreEdit.putString("highScores", scoreBuild.toString());
				scoreEdit.commit();
				
			}
			else{
			    //no existing scores
				scoreEdit.putString("highScores", ""+dateOutput+" by "+MainActivity2.current_user+" : "+exScore);
				scoreEdit.commit();
				GameView2.high_score = true;//new high score!
			}
			GameView2.high_score_checked = true;
			}
		
		}
	
	
	@Override
	public void onDestroy(){
	    super.onDestroy();
	    
	    unbindDrawables(findViewById(R.id.RootView3));//release resources (bitmaps etc) (otherwise I get out of memory error!!)
	    System.gc();
	    
	    Log.i("myApp", "Destroying");
	}
	
    
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
        view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
        ((ViewGroup) view).removeAllViews();
        }
    }
    
    
    public void erase_scores(View view) {

    	
 
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
		// set title
		//alertDialogBuilder.setTitle("Your Title");
 
			// set dialog message
		alertDialogBuilder
			.setMessage("Are you sure you want to delete the high scores?")
			.setCancelable(false)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity and delete scores
				    for(int i = 0;i<3;i++) {
						SharedPreferences.Editor scoreEdit = gamePrefs[i].edit();//open file to edit
						scoreEdit.clear(); //clear file 
						scoreEdit.commit();
				    }
					HighScore.this.finish();
				}
			  })
			.setNegativeButton("No",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
			alertDialog.show();
		    		
    }
    
}

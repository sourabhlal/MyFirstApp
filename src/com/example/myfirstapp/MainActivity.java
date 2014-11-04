package com.example.myfirstapp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;



public class MainActivity extends Activity {

   TextToSpeech ttobj;
   Boolean questionsRemaining;
   DBHelper mydb;
   Outside_Interactions oi;
   Map<String,String> answers = new HashMap<String,String>();
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      mydb = new DBHelper(this);
      
      questionsRemaining = Boolean.TRUE;
      txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
      ttobj=new TextToSpeech(getApplicationContext(), 
      new TextToSpeech.OnInitListener() {    
      @Override
      public void onInit(int status) {
         if(status != TextToSpeech.ERROR){
             ttobj.setLanguage(Locale.UK);
            }				
         }
      });
      
      oi = new Outside_Interactions("John");
      
      // hide the action bar
      getActionBar().hide();
   }
   
   @Override
   public void onPause(){
      if(ttobj !=null){
         ttobj.stop();
      }
      super.onPause();
   }
   
   static final int REQUEST_IMAGE_CAPTURE = 1;

   private void dispatchTakePictureIntent() {
       Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
           startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
       }
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
   
   public void speakText(View view){
	   String toSpeak;
	   String currentStateKey = oi.getStateText(oi.getCurrentState());
	   toSpeak = oi.questions.get(currentStateKey);
	   Log.d(((Integer) oi.getCurrentState()).toString(),toSpeak);
	   ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

	   try {
		    Thread.sleep(oi.getTimeDelay());
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    return;
		}
	   if(oi.getCurrentState()<90){
		   if (oi.getCurrentState()==24){
			   dispatchTakePictureIntent();
			   oi.updateCurrentState("");
               oi.setTimeDelay();
		   }
		   else{
			   promptSpeechInput();
		   }
	   }
	   else{
		   displayResults();
		   if (answers.get("intro").equalsIgnoreCase("yes") || answers.get("intro").equalsIgnoreCase("yes please") || answers.get("intro").equalsIgnoreCase("sure")){
			   mydb.insertUser(answers);   
		   }
		   oi.updateCurrentState("");
	   }
   }

   private void displayResults(){
	   for(Entry<String, String> e : answers.entrySet()) {
	        Log.d(e.getKey(),e.getValue());
	    }
   }
   
   private TextView txtSpeechInput;
   private final int REQ_CODE_SPEECH_INPUT = 100;
   
   /**
    * Showing google speech input dialog
    * */
   private void promptSpeechInput() {
       Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
       intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
               RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
       intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
       intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
               getString(R.string.speech_prompt));
       try {
           startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
       } catch (ActivityNotFoundException a) {
           Toast.makeText(getApplicationContext(),
                   getString(R.string.speech_not_supported),
                   Toast.LENGTH_SHORT).show();
       }
   }

   /**
    * Receiving speech input
    * */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);

       switch (requestCode) {
       case REQ_CODE_SPEECH_INPUT: {
           if (resultCode == RESULT_OK && null != data) {

               ArrayList<String> result = data
                       .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
               answers.put(oi.getStateText(oi.getCurrentState()),result.get(0));
               oi.updateCurrentState(result.get(0));
               oi.setTimeDelay();
               txtSpeechInput.setText(result.get(0));
           }
           break;
       }

       }
   }
}



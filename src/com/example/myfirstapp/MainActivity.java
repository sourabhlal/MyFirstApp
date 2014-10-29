package com.example.myfirstapp;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
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
   int wordsSpoken;
   ArrayList<String> questions = new ArrayList<String>();
   ArrayList<String> answers = new ArrayList<String>();
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      questionsRemaining = Boolean.TRUE;
      txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
      //write.setVisibility(View.GONE);
      ttobj=new TextToSpeech(getApplicationContext(), 
      new TextToSpeech.OnInitListener() {    
      @Override
      public void onInit(int status) {
         if(status != TextToSpeech.ERROR){
             ttobj.setLanguage(Locale.UK);
            }				
         }
      });
      wordsSpoken = 0;
      questions.add("Hi. You are at sawrub's residence ");
      questions.add("Who are you ");
      questions.add("What are you doing here ");
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
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
   
   public void speakText(View view){
	   String toSpeak;
	   if (questionsRemaining){
		  toSpeak = questions.get(wordsSpoken);
		  if (wordsSpoken == questions.size()-1){
			  questionsRemaining = Boolean.FALSE;  
		  }
		  else{
			  wordsSpoken++;  
		  }
		  ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);


	   try {
		    // to sleep 10 seconds
		    Thread.sleep(2000);
		} catch (InterruptedException e) {
		    // recommended because catching InterruptedException clears interrupt flag
		    Thread.currentThread().interrupt();
		    // you probably want to quit if the thread is interrupted
		    return;
		}
	   promptSpeechInput();
	   try {
		    // to sleep 10 seconds
		    Thread.sleep(2000);
		} catch (InterruptedException e) {
		    // recommended because catching InterruptedException clears interrupt flag
		    Thread.currentThread().interrupt();
		    // you probably want to quit if the thread is interrupted
		    return;
		}
	   }
	   else{
		   displayResults();
	   }
	   
   }

   private void displayResults(){
	  for(int i = 0; i< answers.size(); i++){
		  Log.d("result",answers.get(i));
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
               answers.add(wordsSpoken-1,result.get(0));
               Log.d("WORKING",answers.get(wordsSpoken-1));
               txtSpeechInput.setText(result.get(0));
           }
           break;
       }

       }
   }
}



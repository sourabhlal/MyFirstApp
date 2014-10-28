package com.example.myfirstapp;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;



public class MainActivity extends Activity {

   TextToSpeech ttobj;
   private Boolean answerIncoming;
   Boolean questionsRemaining;
   ArrayList<String> questions = new ArrayList<String>();
   ArrayList<String> answers = new ArrayList<String>();
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      answerIncoming = Boolean.FALSE;
      questionsRemaining = Boolean.TRUE;
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
      collectData();
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
	  for (int i = 0; i<questions.size(); i++){
		  if (!answerIncoming){
			  String toSpeak = questions.get(i);
			  ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
		  }
	  }
	  questionsRemaining = Boolean.FALSE;
   }
 
   private void collectData(){
	   while (questionsRemaining){      
		   if (!ttobj.isSpeaking()){
			// get response
			   promptSpeechInput();   
		   }
	   }
   }
   
   private final int REQ_CODE_SPEECH_INPUT = 100;

   /**
    * Showing google speech input dialog
    * */
   private void promptSpeechInput() {
	   answerIncoming = Boolean.TRUE;
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
               answers.add(result.get(0));
               answerIncoming = Boolean.FALSE;
           }
           break;
       }

       }
   }
}



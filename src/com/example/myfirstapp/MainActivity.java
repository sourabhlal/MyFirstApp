package com.example.myfirstapp;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import android.speech.RecognizerIntent;

import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends Activity {

   TextToSpeech ttobj;
   private EditText write;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      write = (EditText)findViewById(R.id.editText1);
      ttobj=new TextToSpeech(getApplicationContext(), 
      new TextToSpeech.OnInitListener() {    
      @Override
      public void onInit(int status) {
         if(status != TextToSpeech.ERROR){
             ttobj.setLanguage(Locale.UK);
            }				
         }
      });
      txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
      btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

      // hide the action bar
      getActionBar().hide();

      btnSpeak.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
              promptSpeechInput();
          }
      });

   }
   
   @Override
   public void onPause(){
      if(ttobj !=null){
         ttobj.stop();
         ttobj.shutdown();
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
      String toSpeak = write.getText().toString();
      Toast.makeText(getApplicationContext(), toSpeak, 
      Toast.LENGTH_SHORT).show();
      ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

   }
   
   private TextView txtSpeechInput;
   private ImageButton btnSpeak;
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
               txtSpeechInput.setText(result.get(0));
           }
           break;
       }

       }
   }
}



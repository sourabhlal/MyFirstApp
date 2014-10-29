package com.example.myfirstapp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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
   int currentState;
   int timeDelay;
   DBHelper mydb;
   Map<String,String> questions = new HashMap<String,String>();
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
      currentState = 1;
      setTimeDelay();
      String residentName = "John";

      questions.put("intro", "Hi! "+residentName+" is not home at the moment. Would you like for me to inform him that you were here?");
      questions.put("instruction", "I will ask you a couple of questions so that I can note down a reminder for  "+residentName+". Please wait for the beep before you reply. Please keep your responses as brief as possible. Say “OK” for me to continue.");
      questions.put("name", "What is your name?");
      questions.put("urgent", "Is your visit urgent? Or can it wait till  "+residentName+" arrives back home?");
      questions.put("purpose", "So XYZ123, What is the purpose of your visit?");
      questions.put("contactType", "How would you prefer  "+residentName+" to contact you? By email or by phone?");
      questions.put("phoneNumber", "What is your mobile phone number?");
      questions.put("emailAddress", "What is your email address. Please spell it out.");
      questions.put("verifyConact", "Your bla is bla bla bla. Is that correct?");
      questions.put("sorry", "Sorry, Your information has not been recorded. Do you want to try again.");
      questions.put("byeGood", "Great! Thank you for visiting "+residentName+"’s residence. He will get in touch with you at his earliest convenience. Have a good day!");
      questions.put("byeBad", "Thank you for visiting "+residentName+"’s residence. I'm sorry I could not be of more assistance. Have a good day!");
      
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
   
   public void setTimeDelay(){
	   switch (currentState){
	   	case 1:
	   		timeDelay = 5000;
	   		break;
	   	case 21:
	   		timeDelay = 12000;
	   		break;
	   	case 22:
	   		timeDelay = 12000;
	   		break;
	   	case 23:
	   		timeDelay = 12000;
	   		break;
	   	case 3:
	   		timeDelay = 2000;
	   		break;
	   	case 4:
	   		timeDelay = 6000;
	   		break;
	   	case 5:
	   		timeDelay = 3000;
	   		break;
	   	case 6:
	   		timeDelay = 5000;
	   		break;
	   	case 71:
	   		timeDelay = 3000;
	   		break;
	   	case 72:
	   		timeDelay = 3000;
	   		break;
	   	case 81:
	   		timeDelay = 7000;
	   		break;
	   	case 82:
	   		timeDelay = 5000;
	   		break;
	   	case 91:
	   		timeDelay = 8000;
	   		break;
	   	case 92:
	   		timeDelay = 8000;
	   		break;
	   }
	   timeDelay += 500;
   }
   
   public String getStateText(int state){
	   switch (state){
	   	case 1:
	   		return "intro";
	   	case 21:
	   		return "instruction";
	   	case 22:
	   		return "instruction";
	   	case 23:
	   		return "instruction";
	   	case 3:
	   		return "name";
	   	case 4:
	   		return "purpose";
	   	case 5:
	   		return "urgent";
	   	case 6:
	   		return "contactType";
	   	case 71:
	   		return "phoneNumber";
	   	case 72:
	   		return "emailAddress";
	   	case 81:
	   		return "verifyConact";
	   	case 82:
	   		return "sorry";
	   	case 91:
	   		return "byeGood";
	   	case 92:
	   		return "byeBad";
	   }
	return "intro";
   }
   
   public void updateCurrentState(String userResponse){
	   String str1 = "";
	   String str2 = "";
	   switch (currentState){
	   	case 1:
	   		if (userResponse.equalsIgnoreCase("yes") || userResponse.equalsIgnoreCase("yes please") || userResponse.equalsIgnoreCase("sure")){
	   			currentState = 21;
	   		}
	   		else{
	   			currentState = 92;
	   		}
	   		break;
	   	case 21:
	   		if (userResponse.equalsIgnoreCase("ok") || userResponse.equalsIgnoreCase("okay")){
	   			currentState = 3;
	   		}
	   		else{
	   			currentState = 22;
	   		}
	   		break;
	   	case 22:
	   		if (userResponse.equalsIgnoreCase("ok") || userResponse.equalsIgnoreCase("okay")){
	   			currentState = 3;
	   		}
	   		else{
	   			currentState = 23;
	   		}
	   		break;
	   	case 23:
	   		if (userResponse.equalsIgnoreCase("ok") || userResponse.equalsIgnoreCase("okay")){
	   			currentState = 3;
	   		}
	   		else{
	   			currentState = 1;
	   		}
	   		break;
	   	case 3:
	   		str1 = questions.get("purpose");
	   		str2 = str1.replaceFirst("XYZ123", userResponse);
	   		questions.put("purpose", str2);
	   		currentState = 4;
	   		break;
	   	case 4:
	   		currentState = 5;
	   		break;
	   	case 5:
	   		currentState = 6;
	   		break;
	   	case 6:	   		
	   		if (userResponse.toLowerCase().contains("phone")||userResponse.toLowerCase().contains("fone")){
	   			questions.put("verifyConact", "Your phone number is XYZ123. Is that correct?");
	   			currentState = 71;
	   		}
	   		else if (userResponse.toLowerCase().contains("mail")||userResponse.toLowerCase().contains("fone")){
	   			questions.put("verifyConact", "Your email address is XYZ123. Is that correct?");
	   			currentState = 72;
	   		}
	   		break;
	   	case 71:
	   		str1 = questions.get("verifyConact");
	   		str2 = str1.replaceFirst("XYZ123", userResponse);
	   		questions.put("verifyConact", str2);
	   		currentState = 81;
	   		break;
	   	case 72:
	   		str1 = questions.get("verifyConact");
	   		str2 = str1.replaceFirst("XYZ123", userResponse);
	   		questions.put("verifyConact", str2);
	   		currentState = 81;
	   		break;
	   	case 81:
	   		if (userResponse.toLowerCase().contains("yes") || userResponse.equalsIgnoreCase("yup") || userResponse.equalsIgnoreCase("correct")){
	   			currentState = 91;
	   		}
	   		else{
	   			currentState = 82;
	   		}
	   		break;
	   	case 82:
	   		if (userResponse.toLowerCase().contains("yes") || userResponse.equalsIgnoreCase("sure")){
	   			currentState = 6;
	   		}
	   		else{
	   			currentState = 92;
	   		}
	   		break;
	   	case 91:
	   		currentState = 1;
	   		break;
	   	case 92:
	   		currentState = 1;
	   		break;
	   }
	   str1 = "";
	   str2 = "";
   }
   
   public void speakText(View view){
	   String toSpeak;
	   String currentStateKey = getStateText(currentState);
	   toSpeak = questions.get(currentStateKey);
	   Log.d(((Integer) currentState).toString(),toSpeak);
	   ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

	   try {
		    Thread.sleep(timeDelay);
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    return;
		}
	   if(currentState<90){
		   promptSpeechInput();   
	   }
	   else{
		   displayResults();
		   if (answers.get("intro").equalsIgnoreCase("yes") || answers.get("intro").equalsIgnoreCase("yes please") || answers.get("intro").equalsIgnoreCase("sure")){
			   mydb.insertUser(answers);   
		   }
		   updateCurrentState("");
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
               answers.put(getStateText(currentState),result.get(0));
               updateCurrentState(result.get(0));
               setTimeDelay();
               txtSpeechInput.setText(result.get(0));
           }
           break;
       }

       }
   }
}



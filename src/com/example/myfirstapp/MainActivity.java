package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FEATURE_LIST;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;



public class MainActivity extends Activity {

   private TextToSpeech ttobj;
   Boolean questionsRemaining;
   DBHelper mydb;
   Outside_Interactions oi;
   Map<String,String> answers = new HashMap<String,String>();
   
   public final String TAG = "FacialRecognitionActivity";
   public static FacialProcessing faceObj;
   public final int confidence_value = 58;
   public static boolean activityStartedOnce = false;
   public static final String ALBUM_NAME = "serialize_deserialize";
   public static final String HASH_NAME = "HashMap";
   HashMap<String, String> hash;
   
   private boolean identifiedPerson = false;
   private String person_id = "Not_Identified"; 
   private int hashPosition;
   
   private TextView txtSpeechInput;
   private final int REQ_CODE_SPEECH_INPUT = 100;
   private final int REQ_IMAGE_CAPTURE= 101;
   private final int REQ_PHOTO_ID = 102;
   //combine to make a single activity
   
   int count = 0;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      Log.d("onCreate", "started");
      
      hash = retrieveHash(getApplicationContext());
      
      Bundle extras = getIntent().getExtras();
      oi = new Outside_Interactions("John");
      
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
      
      if (!activityStartedOnce) // Check to make sure FacialProcessing object is not created multiple times. 
      {
		activityStartedOnce = true;
      boolean isSupported = FacialProcessing.isFeatureSupported(FEATURE_LIST.FEATURE_FACIAL_RECOGNITION);
      
      if (isSupported) {
			Log.d(TAG, "Feature Facial Recognition is supported");
			faceObj = (FacialProcessing) FacialProcessing.getInstance();
			loadAlbum(); // De-serialize a previously stored album.
			if (faceObj != null) {
				faceObj.setRecognitionConfidence(confidence_value);
				faceObj.setProcessingMode(FP_MODES.FP_MODE_STILL);
			}
		} else // If Facial recognition feature is not supported then
				// display an alert box.
		{
			Log.e(TAG, "Feature Facial Recognition is NOT supported");
			new AlertDialog.Builder(this)
					.setMessage(
							"Your device does NOT support Qualcomm's Facial Recognition feature. ")
					.setCancelable(false)
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							}).show();
		}
      }
      
      displayResults();
      
      if(extras != null){
    	  identifiedPerson = extras.getBoolean("ID_Successful");
          person_id = extras.getString("ID_Person");
          hashPosition = extras.getInt("hashPos");
          
          Log.d("id person", person_id);
          
          if(identifiedPerson) {
        	  Log.d("id success", "true");
        	  oi.setCategory(1);
        	  oi.setCurrentState(3);
        	  oi.setVisitorName(person_id);
        	  oi.setQuestions();
          }
          else {
        	  Log.d("id success", "false");
        	  oi.setCurrentState(21);
        	  oi.setCategory(0);
        	  oi.setVisitorName("Unidentified_Person");
        	  oi.setQuestions();
          }

      }
            
      // hide the action bar
      getActionBar().hide();
   }
   
   @Override
   public void onPause(){
      if(ttobj !=null){
         ttobj.stop();
         //ttobj.shutdown();
      }
      super.onPause();
   }

   private void dispatchTakePictureIntent() {
	   Intent intent = new Intent(this, FacialRecogActivity.class);
	   intent.putExtra("Username", "Not Identified");
	   intent.putExtra("PersonId", -1);
	   intent.putExtra("IdentifyPerson", true);
	   startActivity(intent);
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
	   Log.d("current state", Integer.toString(oi.getCurrentState()));
	   Log.d("state key", currentStateKey);

	   toSpeak = oi.getQuestion();
	   
	   Log.d(((Integer) oi.getCurrentState()).toString(),toSpeak);
	   ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

	   try {
		    Thread.sleep(oi.getTimeDelay());
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    Log.e("oops!", e.toString());
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
		   if (answers.get("intro").equalsIgnoreCase("yes") || answers.get("intro").equalsIgnoreCase("yes please") || answers.get("intro").equalsIgnoreCase("sure")){
			   if(!identifiedPerson){
				   mydb.insertUser(answers);
			   }		   
		   }
		   
		  new SendMail().execute(answers);
	    	  
		   displayResults();
		   oi.updateCurrentState("");
		   answers.clear();
	   }
   }

   private void displayResults(){
	   for(Entry<String, String> e : answers.entrySet()) {
	        Log.d(e.getKey(),e.getValue());
	    }
	   
	   ArrayList<String> array_list = new ArrayList<String>();
	   array_list = mydb.getAllContacts();
	   for(int i=0; i < array_list.size(); i++) {
		   Log.d("User"+i, array_list.get(i));
	   }
	   
   }
   
   public void speakText(){
	   String toSpeak;
	   String currentStateKey = oi.getStateText(oi.getCurrentState());
	   
	   Log.d("state key", currentStateKey);
	   toSpeak = oi.getQuestion();
	   
	   Log.d(((Integer) oi.getCurrentState()).toString(),toSpeak);
	   ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

	   try {
		    Thread.sleep(oi.getTimeDelay());
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    Log.e("oops!", e.toString());
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
		   if (answers.get("intro").equalsIgnoreCase("yes") || answers.get("intro").equalsIgnoreCase("yes please") || answers.get("intro").equalsIgnoreCase("sure")){
			   displayAnswers();
			   if(!identifiedPerson) {
				   mydb.insertUser(answers);
			   }
		   }
		   Map<String, String> answersIn = new HashMap<String, String>();
		   
		   for(Entry<String, String> e: answers.entrySet()) {
				answersIn.put(e.getKey(), e.getValue());
			}
		   
		   new SendMail().execute(answersIn);
	    	  
		   //displayResults();
		   oi.updateCurrentState("");
		   answers.clear();
	   }
   }
   
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
	
	               ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	               if (oi.getStateText(oi.getCurrentState()).equalsIgnoreCase("contactType") 
	            		   && (result.get(0).toLowerCase().contains("phone")
	            			||result.get(0).toLowerCase().contains("fone")
	            			||result.get(0).toLowerCase().contains("call"))) 
	               {
	            	   answers.put(oi.getStateText(oi.getCurrentState()),"phone");
	               }
	               else if(oi.getStateText(oi.getCurrentState()).equalsIgnoreCase("contactType") 
	            		   && (result.get(0).toLowerCase().contains("write")
	            			||result.get(0).toLowerCase().contains("mail"))) 
	               {
	            	   answers.put(oi.getStateText(oi.getCurrentState()),"email");
	               }
	               else if(oi.getStateText(oi.getCurrentState()).equalsIgnoreCase("confirmIdentity") 
	            		   && result.get(0).toLowerCase().contains("ye")) {
	            	   answers.put("name", person_id);
	            	   getContact(person_id);
	               }
	               else {
	            	   answers.put(oi.getStateText(oi.getCurrentState()),result.get(0));
	               }
	               if(oi.getCurrentState() == 3) {
	            	   answers.put("intro", "yes");
	            	   Log.d("answer", result.get(0));
	            	   Log.d("Hash pos", Integer.toString(hashPosition));
	            	   String personName = result.get(0);
	            	   this.hash.put(personName, Integer.toString(hashPosition));
	            	   saveHash(hash, getApplicationContext());
	               }
	               oi.updateCurrentState(result.get(0));
	               oi.setTimeDelay();
	               txtSpeechInput.setText(result.get(0));
	               speakText();
	           }
	           break;
	       }

       }
   }
   
   public void getContact(String name) {
	   Cursor cur = mydb.getData(name);
	   cur.moveToNext();
	   String contType = cur.getString(cur.getColumnIndex(DBHelper.KEY_CONTACTTYPE));
	   String cont = cur.getString(cur.getColumnIndex(DBHelper.KEY_CONTACT));
	   answers.put("contactType", contType);
	   answers.put("contact", cont);  
	   cur.close();
   }
   
   public void displayAnswers() {
	   for(Entry<String, String> e : answers.entrySet()) {
	        Log.d(e.getKey(),e.getValue());
	    }
   }
   
   public void resetAlbum(View view) {
		// Alert box to confirm before reseting the album
		new AlertDialog.Builder(this)
				.setMessage(
						"Are you sure you want to RESET the album? All the photos saved will be LOST")
				.setCancelable(true)
				.setNegativeButton("No", null)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								boolean result = faceObj.resetAlbum();
								if (result) {
									HashMap<String, String> hashMap = retrieveHash(getApplicationContext());
									hashMap.clear();
									saveHash(hashMap, getApplicationContext());
									saveAlbum();
									Toast.makeText(getApplicationContext(),
											"Album Reset Successful.",
											Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(
											getApplicationContext(),
											"Internal Error. Reset album failed",
											Toast.LENGTH_LONG).show();
								}
							}
						}).show();
		this.deleteDatabase(mydb.DATABASE_NAME);
		mydb = new DBHelper(this);
	}
   
   public void loadAlbum() {
		SharedPreferences settings = getSharedPreferences(ALBUM_NAME, 0);
		String arrayOfString = settings.getString("albumArray", null);
		byte[] albumArray = null;
		if (arrayOfString != null) {
			String[] splitStringArray = arrayOfString.substring(1,
					arrayOfString.length() - 1).split(", ");
			albumArray = new byte[splitStringArray.length];
			for (int i = 0; i < splitStringArray.length; i++) {
				albumArray[i] = Byte.parseByte(splitStringArray[i]);
			}
			faceObj.deserializeRecognitionAlbum(albumArray);
			Log.e("TAG", "De-Serialized my album");
		}
		Log.d("end of loadalbum", "reached");
	}
   
   /*
	 * Method to save the recognition album to a permanent device memory
	 */
	public void saveAlbum() {
		byte[] albumBuffer = faceObj.serializeRecogntionAlbum();
		SharedPreferences settings = getSharedPreferences(ALBUM_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("albumArray", Arrays.toString(albumBuffer));
		editor.commit();
	}
   
   protected HashMap<String, String> retrieveHash(Context context) {
		SharedPreferences settings = context.getSharedPreferences(HASH_NAME, 0);
		HashMap<String, String> hash = new HashMap<String, String>();
		hash.putAll((Map<? extends String, ? extends String>) settings.getAll());
		return hash;
	}
	
	/*
	 * Function to store a HashMap to shared preferences.
	 * @param hash
	 */
	protected void saveHash(HashMap<String, String> hashMap, Context context) {
		SharedPreferences settings = context.getSharedPreferences(HASH_NAME, 0);
		
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		Log.e(TAG, "Hash Save Size = " + hashMap.size());
		for (String s : hashMap.keySet()) {
			editor.putString(s, hashMap.get(s));
		}
		editor.commit();
	}
	
	private class SendMail extends AsyncTask <Map<String,String>, Void, Void> {

		
		@Override
		protected Void doInBackground(Map<String, String>... params) {
			Map<String, String> answersIn = new HashMap<String, String>(params[0]);
			
			Mail m = new Mail("remindoor@gmail.com", "remindoor291"); 
			String[] toArr = {"freppy1213@hotmail.com", "remindoor@gmail.com"}; 
			String contactInfo;
			
			for(Entry<String, String> e: params[0].entrySet()) {
				answersIn.put(e.getKey(), e.getValue());
			}
			
			
			if(identifiedPerson) {
				contactInfo = answersIn.get("contact");
			}
			else {
				if(answersIn.get("contactType").equalsIgnoreCase("email")){
					Log.d("in email", "reached");
					contactInfo = answersIn.get("emailAddress");
				}
				else {
					Log.d("in phone", "reached");
					contactInfo = answersIn.get("phoneNumber");
				}
			}
			
		    m.setTo(toArr); 
		    m.setFrom("remindoor@gmail.com"); 
		    m.setSubject(answersIn.get("name") + " visited!!"); 
		    m.setBody(answersIn.get("name") + " visited your residence for the purpose of " + answersIn.get("purpose")
		    		+ ". You may contact him/her by " + answersIn.get("contactType") + ": " + contactInfo
		    		+ "\n\n Yours truly,\n Remindoor");
		    try {
				m.send();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		    
			return null;
		}
		   
	   }
}





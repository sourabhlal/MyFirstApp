package com.example.myfirstapp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.util.Log;

public class Outside_Interactions {

   int currentState;
   int timeDelay;
   int category;
   String residentName;
   Map<String,String> questions = new HashMap<String,String>();
   String visitorName;
   String[] funFacts = new String[9];
   int factNum;
   boolean wantToTalk = true;

   public Outside_Interactions(String owner){
	   this.residentName = owner;
	   this.currentState = 1;
	   this.category = -1;
	   this.setTimeDelay();
	   questions.put("intro", "Hi! "+owner+" is not home at the moment. Would you like for me to inform him that you were here?");
	   questions.put("picture", "Ok. Please look into the camera, and smile");
	   questions.put("instruction", "I will ask you a couple of questions so that I can note down a reminder for  "+residentName+". Okay?");

	   funFacts[0] = "Did you know that a donkey will sink in quicksand but a mule won�t.";
	   funFacts[1] = "Did you know that A rhinoceros horn is made of compacted hair.";
	   funFacts[2] = "Did you know that It�s illegal to spit on the sidewalk in Norfolk, Virginia.";
	   funFacts[3] = "Did you know that India has a Bill of Rights for cows.";
	   funFacts[4] = "Did you know that A giraffe can clean its ears with its 21-inch tongue!";
	   funFacts[5] = "Did you know that It was discovered on a space mission that a frog can throw up.";
	   funFacts[6] = "Did you know that Certain frogs can be frozen solid then thawed, and continue living.";
	   funFacts[7] = "Did you know that A duck�s quack doesn�t echo, and no one knows why.";
	   funFacts[8] = "Did you know that If you feed a seagull Alka-Seltzer, its stomach will explode.";
   }

   public void setQuestions(){
	   if (category == 0){
	      questions.put("name", "What is your name?");
	      questions.put("urgent", "Is your visit urgent? Or can it wait till  "+residentName+" arrives back home?");
	      questions.put("purpose", "So XYZ123, What is the purpose of your visit?");
	      questions.put("contactType", "How would you prefer  "+residentName+" to contact you? By email or by phone?");
	      questions.put("phoneNumber", "What is your mobile phone number?");
	      questions.put("emailAddress", "What is your email address. Please spell it out.");
	      questions.put("verifyContact", "Your bla is bla bla bla. Is that correct?");
	      questions.put("sorry", "Sorry, Your information has not been recorded. Do you want to try again.");
	      questions.put("byeGood", "Great! Thank you for visiting "+residentName+"�s residence. He will get in touch with you at his earliest convenience. Have a good day!");
	      questions.put("byeBad", "Thank you for visiting "+residentName+"'s residence. I'm sorry I could not be of more assistance. Have a good day!");
	   }
	   if (category==1){
		   Log.d("cat 1", "reached");
		   Random rand = new Random();
		   factNum = rand.nextInt(funFacts.length);
		   questions.put("confirmIdentity", "are you " + visitorName + "?");
		   questions.put("urgent", "Hey " + visitorName.split(" ")[0] + ", are you in a hurry?");
		   questions.put("purpose", "So " + visitorName.split(" ")[0] + ", what are you here for?");
		   questions.put("byeGood", "Ok I just notified " + residentName + ". Great seeing you again!");
		   questions.put("byeBad", "I sent the message, now get the hell out you unsocialble twit.");
		   questions.put("funFact", funFacts[factNum]);

	   }
	   if (category==2){

	   }
   }
   
   public String getQuestion() {
	   return this.questions.get(getStateText(this.currentState));
   }
   
   public String getStateText(int state){
	   if (category == -1){
		   switch (state){
		   	case 1:
		   		return "intro";
		   	case 24:
		   		return "picture";
		   }
	   }
	   if (category == 0){
		   switch (state){
		   	case 1:
		   		return "intro";
		   	case 21:
		   		return "instruction";
		   	case 22:
		   		return "instruction";
		   	case 23:
		   		return "instruction";
		   	case 24:
		   		return "picture";
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
		   		return "verifyContact";
		   	case 82:
		   		return "sorry";
		   	case 91:
		   		return "byeGood";
		   	case 92:
		   		return "byeBad";
		   }
		return "intro";
	   }
	   else if (category==1){
		   switch (state){
		   case 3:
			   return "confirmIdentity";
		   case 4:
			   return "urgent";
		   case 5:
			   return "purpose";
		   case 90:
			   if(wantToTalk)
				   return "byeGood";
			   else
				   return "byeBad";
		   case 7:
			   return "funFact";
		   		
		   }
	   }
	   else if (category==2){
		   switch (state){
		   
		   }
	   }
	   return "intro";   
   }
   
   public void updateCurrentState(String userResponse){
	   String str1 = "";
	   String str2 = "";
	   if (category == -1){   
		   switch (currentState){
		   	case 1:
		   		if (userResponse.equalsIgnoreCase("yes") || userResponse.equalsIgnoreCase("yes please") || userResponse.equalsIgnoreCase("sure")){
		   			currentState = 24;
		   		}
		   		else{
		   			currentState = 92;
		   		}
		   		break;
		   	case 24:
		   		category = 0;
		   		currentState = 21;
		   		setQuestions();
		   		break;
		   }
	   }
	   else if (category == 0){
		   switch (currentState){
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
		   		if (userResponse.toLowerCase().contains("phone")||userResponse.toLowerCase().contains("fone")||userResponse.toLowerCase().contains("call")){
		   			questions.put("verifyContact", "Your phone number is XYZ123. Is that correct?");
		   			currentState = 71;
		   		}
		   		else if (userResponse.toLowerCase().contains("mail")||userResponse.toLowerCase().contains("write")){
		   			questions.put("verifyContact", "Your email address is XYZ123. Is that correct?");
		   			currentState = 72;
		   		}
		   		break;
		   	case 71:
		   		str1 = questions.get("verifyContact");
		   		str2 = str1.replaceFirst("XYZ123", userResponse);
		   		questions.put("verifyContact", str2);
		   		currentState = 81;
		   		break;
		   	case 72:
		   		str1 = questions.get("verifyContact");
		   		str2 = str1.replaceFirst("XYZ123", userResponse);
		   		questions.put("verifyContact", str2);
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
	   }
	   if (category==1){
		   switch (currentState){
		   	case 3:
		   		if (userResponse.toLowerCase().contains("ye") || userResponse.equalsIgnoreCase("sure") || userResponse.equalsIgnoreCase("ok")){
		   			currentState = 4;
		   		}
		   		else {
		   			category = 0;
		   			currentState = 21;
		   			setQuestions();
		   		}
		   		break;
		   	case 4:
		   		if (userResponse.toLowerCase().contains("ye") || userResponse.equalsIgnoreCase("sure") || userResponse.equalsIgnoreCase("ok")) {
		   			currentState = 5;
		   			wantToTalk = false;
		   		}
		   		else {
		   			currentState = 7;
		   		}
		   		break;
		   	case 5:
		   		currentState = 90;
		   		break;
		   	case 90:
		   		currentState = 1;
		   		break;
		   	case 7:
		   		currentState = 5;
		   		break;
		   }
	   }
	   if (category==2){
		   switch (currentState){
		   
		   }
	   }
	   str1 = "";
	   str2 = "";
   }
   
	public int getCategory() {
		return category;
	}
	public int getCurrentState() {
		return currentState;
	}
	public int getTimeDelay() {
		return timeDelay;
	}
	
	public void setCategory(int category) {
		this.category = category;
	}
	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
	public void setTimeDelay(){
	   if (category == -1){
			switch (currentState){
		   	case 1:
		   		timeDelay = 6000;
		   		break;
		   	case 24:
		   		timeDelay = 5000;
		   		break;
			}
		}
	   if (category == 0){
		   switch (currentState){
		   	case 1:
		   		timeDelay = 5000;
		   		break;
		   	case 21:
		   		timeDelay = 8000;
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
	   }
	   else if (category==1){
		   switch (currentState){
		   case 3:
			   timeDelay = 1500;
			   break;
		   case 4:
			   timeDelay = 2000;
			   break;
		   case 5:
			   timeDelay = 3000;
			   break;
		   case 7:
			   timeDelay = 5000;
			   break;
		   }
	   }
	   else if (category==2){
		   switch (currentState){
		   
		   }
	   }
	   timeDelay += 500;
   }
	
	public void setVisitorName(String s) {
		this.visitorName = s;
	}
}

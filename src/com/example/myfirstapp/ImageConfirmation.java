package com.example.myfirstapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

public class ImageConfirmation extends ActionBarActivity {

	private static Bitmap storedBitmap;
	private ImageView confirmationView; // ImageView to display the selected image
	private ImageView confirmButton; // ImageView to confirm the presently loaded image
	private Bitmap mutableBitmap; // Temporary mutable bitmap
	private static FacialProcessing faceObj;
	private static Rect[] arrayOfRects; // A temporary array that will store the
										// face rects.
	private static HashMap<String, String> hash;
	private FaceData[] faceDataArray;
	public MainActivity mainAct;
	
	private int arrayPosition;
	private int personId;
	private boolean identifyPerson = true;
	private boolean inputNameFlag = true;
	private static boolean faceFoundFlag = false;
	private String userName;
	private final String TAG = "ImageConfirmation.java";
	private static final String ALBUM_NAME = "serialize_deserialize";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_confirmation);
		
		Bundle extras = getIntent().getExtras();
		byte[] data = getIntent().getByteArrayExtra(
				"com.example.myfirstapp.ImageConfirmation");
		int angle = extras
				.getInt("com.example.myfirstapp.ImageConfirmation.orientation");
		boolean cameraFacingFront = extras
				.getBoolean("com.example.myfirstapp.ImageConfirmation.switchCamera");
		boolean throughGallery = extras
		// If the image is coming through the gallery
				.getBoolean("com.example.myfirstapp.ImageConfirmation.through.gallery");
		personId = extras.getInt("PersonId");
		userName = extras.getString("Username");
		identifyPerson = extras.getBoolean("IdentifyPerson");
		confirmationView = (ImageView) findViewById(R.id.confirmationView);
		faceObj = MainActivity.faceObj;
		
		storedBitmap = BitmapFactory
				.decodeByteArray(data, 0, data.length, null);
		
		mainAct = new MainActivity();
		hash = mainAct.retrieveHash(getApplicationContext());
		
		Options bitmapOptions = new Options();
		bitmapOptions.inMutable = true;
		Matrix mat = new Matrix();
		if (cameraFacingFront) // Rotate the bitmap image based on the device
								// orientation
		{
			if (throughGallery) {
				mat.postRotate(angle == 90 ? 90 : (angle == 180 ? 180 : 0));
				storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0,
						storedBitmap.getWidth(), storedBitmap.getHeight(), mat,
						true);
			} else {
				mat.postRotate(angle == 0 ? 270 : angle == 270 ? 180
						: (angle == 180 ? 180 : 0));
				mat.postScale(-1, 1);
				storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0,
						storedBitmap.getWidth(), storedBitmap.getHeight(), mat,
						true);
			}
		} else {
			if (throughGallery) {
				mat.postRotate(angle == 90 ? 90 : (angle == 180 ? 180 : 0));
			} else {
				mat.postRotate(angle == 0 ? 90 : angle == 270 ? 180
						: (angle == 180 ? 180 : 0));
			}
			storedBitmap = Bitmap.createBitmap(storedBitmap, 0, 0,
					storedBitmap.getWidth(), storedBitmap.getHeight(), mat,
					true);
		}
		Bitmap tempBitmap = Bitmap.createScaledBitmap(storedBitmap,
				(storedBitmap.getWidth() / 2), (storedBitmap.getHeight() / 2),
				false);
		confirmationView.setImageBitmap(tempBitmap); // Setting the view with
														// the bitmap image that
														// came in.
		// If selected image is landscape then change the display view to
		// landscape or else change to portrait.
		if (storedBitmap.getWidth() > storedBitmap.getHeight()) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		confirmButtonOnClickListener();
		
		
	//end onCreate	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_confirmation, menu);
		return true;
	}
	
	/*
	 * Method to pop - up an alert box when a face is clicked to be added
	 */
	private boolean createAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ImageConfirmation.this);
		builder.setMessage("Enter Person Name");
		final EditText input = new EditText(ImageConfirmation.this);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String inputName = input.getText().toString();
				if (inputName != null && inputName.trim().length() != 0) {
					if (!hash.containsKey(inputName)) {
						int result = faceObj.addPerson(arrayPosition);
						hash.put(inputName, Integer.toString(result));
						mainAct.saveHash(hash, getApplicationContext());
						saveAlbum();
						Toast.makeText(
								getApplicationContext(),
								input.getText().toString()
										+ " added successfully",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(),
								"Username '" + inputName + "' already exist",
								Toast.LENGTH_SHORT).show();
						createAlert();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"User name cannot be empty", Toast.LENGTH_SHORT)
							.show();
					createAlert();
				}
			}
		});
		builder.show();
		return inputNameFlag;
	}
	
	private void confirmButtonOnClickListener() {
		confirmButton = (ImageView) findViewById(R.id.approve);
		confirmButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				int imageViewSurfaceWidth = confirmationView.getWidth();
				int imageViewSurfaceHeight = confirmationView.getHeight();
				
				Bitmap workingBitmap = Bitmap.createScaledBitmap(storedBitmap,
						imageViewSurfaceWidth, imageViewSurfaceHeight, false);
				mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888,
						true);
				
				String user_id = "Not identified";
				Boolean success = false;
				int hashPos = -11;
				
				boolean result = faceObj.setBitmap(storedBitmap);
				// Normalize the face data coordinates based on the image that
				// is fed in.
				faceObj.normalizeCoordinates(imageViewSurfaceWidth,
						imageViewSurfaceHeight);				
				
				if (result) // If setBitmap was successful
				{
					faceDataArray = faceObj.getFaceData();
					// If one or more face is detected
					if (faceDataArray != null) {
						// Creating a temporary rect to store the faceRects
						// returned from the faceData array
						arrayOfRects = new Rect[faceDataArray.length];
						Canvas canvas = null;
						for (int i = 0; i < faceDataArray.length; i++) {
							Rect rect = faceDataArray[i].rect;
							// Extra padding around the faeRects
							rect.set(rect.left -= 20, rect.top -= 20,
									rect.right += 20, rect.bottom += 20);
							canvas = new Canvas(mutableBitmap);
							Paint paintForRectFill = new Paint(); // Draw rect
																	// fill
							paintForRectFill.setStyle(Paint.Style.FILL);
							paintForRectFill.setColor(Color.WHITE);
							paintForRectFill.setAlpha(80);
							// Draw rect strokes
							Paint paintForRectStroke = new Paint();
							paintForRectStroke.setStyle(Paint.Style.STROKE);
							paintForRectStroke.setColor(Color.GREEN);
							paintForRectStroke.setStrokeWidth(5);
							canvas.drawRect(rect, paintForRectFill);
							canvas.drawRect(rect, paintForRectStroke);
							
							// Update the temporary rect array with the given
							// face rect
							// so that we can use this in the future to get the
							// corresponsing
							// faceIndex of the faces.
							arrayOfRects[i] = rect;
							if (identifyPerson) {
								String selectedPersonId = Integer.toString(faceDataArray[i].getPersonId());
								String personName = null;
								Iterator<HashMap.Entry<String, String>> iter = hash.entrySet().iterator();
								while (iter.hasNext()) {
									HashMap.Entry<String, String> entry = iter.next();
									if (entry.getValue().equals(selectedPersonId)) {
										personName = entry.getKey();
									}
								}
								float pixelDensity = getResources()
										.getDisplayMetrics().density;
								int textSize = (int) (rect.width() / 25 * pixelDensity);
								Paint paintForText = new Paint();
								paintForText.setColor(Color.WHITE);
								paintForText.setTextSize(textSize);
								Typeface tp = Typeface.SERIF;
								Rect backgroundRect = new Rect(rect.left,
										rect.bottom, rect.right,
										(rect.bottom + textSize));
								Paint paintForTextBackground = new Paint();
								paintForTextBackground
										.setStyle(Paint.Style.FILL);
								paintForTextBackground.setColor(Color.BLACK);
								;
								paintForText.setTypeface(tp);
								paintForTextBackground.setAlpha(80);
								
								//if found person
								if (personName != null) {
									canvas.drawRect(backgroundRect,
											paintForTextBackground);
									canvas.drawText(personName, rect.left,
											rect.bottom + (textSize),
											paintForText);
									user_id = personName;
									success = true;
									hashPos = -1;
								} else {
									canvas.drawRect(backgroundRect,
											paintForTextBackground);
									canvas.drawText("Not identified",
											rect.left,
											rect.bottom + (textSize),
											paintForText);
									user_id = "Not identified";
									success = false;
								}
								
							}
							if (faceDataArray[i].getPersonId() < 0) {
								// Check the array position
								// corresponding the rect and add that
								// index
								arrayPosition = i;
							}
							if(!success) {
								hashPos = faceObj.addPerson(arrayPosition);
								saveAlbum();
								Log.d("Success save album", "reached");
							}
						}
						confirmButton.setVisibility(View.GONE);
						// Setting the view with the bitmap image that came in
						confirmationView.setImageBitmap(mutableBitmap);
						
					} else {
						Toast.makeText(getApplicationContext(),
								"No Faces detected", Toast.LENGTH_SHORT).show();
						takeAnotherPicture();
						
					}
					
				} else {
					Log.e(TAG, "Set Bitmap failed");
				}
				
				returnToMain(user_id, success, hashPos);
			}
		});
		
		confirmButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					confirmButton
							.setImageResource(R.drawable.confirm_highlighted);
				} else if (arg1.getAction() == MotionEvent.ACTION_UP) {
					confirmButton.setImageResource(R.drawable.confirm);
				}
				
				return false;
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.e(TAG, "Serializing");
	}
	
	/*
	 * Function to retrieve the byte array from the Shared Preferences.
	 */
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
	}
	
	/*
	 * Method to save the recognition album to a permanent device memory
	 */
	public void saveAlbum() {
		byte[] albumBuffer = faceObj.serializeRecogntionAlbum();
		Log.e(TAG, "Size of byte Array =" + albumBuffer.length);
		SharedPreferences settings = getSharedPreferences(ALBUM_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("albumArray", Arrays.toString(albumBuffer));
		editor.commit();
	}
	
	
	
	private void returnToMain(String user, Boolean id_success, int hashPos) {
		Intent in1 = new Intent(this, MainActivity.class);
		in1.putExtra("ID_Successful", id_success);
		in1.putExtra("ID_Person", user);
		in1.putExtra("hashPos", hashPos);
		startActivity(in1);
		finish();
	}
	
	private void takeAnotherPicture() {
		Intent in1 = new Intent(this, FacialRecogActivity.class);
		startActivity(in1);
		finish();
	}
	
}

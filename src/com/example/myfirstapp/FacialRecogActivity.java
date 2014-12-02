package com.example.myfirstapp;

import java.io.ByteArrayOutputStream;

import android.support.v7.app.ActionBarActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.widget.FrameLayout;

public class FacialRecogActivity extends ActionBarActivity implements Camera.PreviewCallback{

	private Bitmap bitmap;
	private Camera cameraObj; // Accessing the Android native Camera.
	private FrameLayout preview; // Layout on which camera surface is displayed
	private CameraSurfacePreview mPreview;
	private OrientationEventListener orientationListener; // Accessing device
	
	private int FRONT_CAMERA_INDEX = 1;
	private int lastAngle = 0;
	private int personId;
	private static int displayAngle;
	private static boolean cameraFacingFront = true;
	private static boolean activityStartedOnce = false;
	private boolean identifyPerson = true;
	private final String PROJECTION_PATH = MediaStore.Images.Media.DATA;
	private final String TAG = "Facial Recognition";
	private String userName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facial_recog);
		
		Bundle extras = getIntent().getExtras();
		
		personId = extras.getInt("PersonId");
		userName = extras.getString("Username");
		identifyPerson = extras.getBoolean("IdentifyPerson");
		
		orientationListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int arg0) {
				int prevAngle = lastAngle * 90;
				if (arg0 - prevAngle < 0) {
					arg0 += 360;
				}
				
				// Only shift if > 60 degree deviance
				if (Math.abs(arg0 - prevAngle) < 60)
					return;
				
				int angle = ((arg0 + 45) % 360) / 90;
				
				if (lastAngle == angle) {
					return;
				}
				lastAngle = angle;
				
				switch (angle) {
				case 0: // portrait
					displayAngle = 0;
					break;
				case 1: // landscape right
					displayAngle = 270;
					break;
				case 2: // upside-down
					displayAngle = 180;
					break;
				case 3: // landscape left
					displayAngle = 90;
					break;
				}
			}
		};
		
		if (!activityStartedOnce) {
			startCamera();
		}
	}
	
	protected void onPause() {
		super.onPause();
		stopCamera();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		if (orientationListener != null)
			orientationListener.disable();
	}
	
	protected void onResume() {
		super.onResume();
		if (cameraObj != null) {
			stopCamera();
		}
		startCamera();
	}
	
private void stopCamera() {
		
		if (cameraObj != null) {
			cameraObj.stopPreview();
			cameraObj.setPreviewCallback(null);
			preview.removeView(mPreview);
			cameraObj.release();
		}
		cameraObj = null;
	}
	
	/*
	 * Method that handles initialization and starting of camera.
	 */
	private void startCamera() {
		
		cameraObj = Camera.open(FRONT_CAMERA_INDEX); // Open the Front

		mPreview = new CameraSurfacePreview(FacialRecogActivity.this, cameraObj,
				orientationListener); // Create a new surface on which Camera
										// will be displayed.

		preview = (FrameLayout) findViewById(R.id.cameraPreview);
		preview.addView(mPreview);
		cameraObj.setPreviewCallback(FacialRecogActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.facial_recog, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		
	}
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d("TAG", "onShutter'd");
		}
	};
	
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("TAG", "onPictureTaken - raw");
		}
	};
	
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			usePicture(data);
		}
		
	};
	
	private void usePicture(byte[] data) {
		Intent intent = new Intent(this, ImageConfirmation.class);
		if (data != null) {
			intent.putExtra(
					"com.example.myfirstapp.ImageConfirmation",
					data);
		}
		intent.putExtra(
				"com.example.myfirstapp.ImageConfirmation.switchCamera",
				cameraFacingFront);
		intent.putExtra(
				"com.example.myfirstapp.ImageConfirmation.orientation",
				displayAngle);
		intent.putExtra("Username", userName);
		intent.putExtra("PersonId", personId);
		intent.putExtra("IdentifyPerson", identifyPerson);
		startActivityForResult(intent, 1);
		finish();
	}
	
	protected void onActivityResult(int requestCode, int finalResultCode,
			Intent returnedImage) {
		super.onActivityResult(requestCode, finalResultCode, returnedImage);
		
		switch (requestCode) {
		case 0:
			if (finalResultCode == RESULT_OK) {
				ContentResolver resolver = getContentResolver();
				Uri userSelectedImage = returnedImage.getData();
				String[] projection = { PROJECTION_PATH };
				Cursor csr = resolver.query(userSelectedImage, projection,
						null, null, null);
				csr.moveToFirst();
				int selectedIndex = 0;
				String path = csr.getString(selectedIndex);
				csr.close();
				bitmap = BitmapFactory.decodeFile(path);
				
				// Convert to byte array
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				if (bitmap != null) {
					Log.e(TAG, "Bitmap is not NULL");
					bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream);
					byte[] byteArray = stream.toByteArray();
					Intent in1 = new Intent(FacialRecogActivity.this,
							ImageConfirmation.class);
					in1.putExtra(
							"com.example.myfirstapp.ImageConfirmation",
							byteArray);
					in1.putExtra(
							"com.example.myfirstapp.ImageConfirmation.switchCamera",
							true);
					in1.putExtra(
							"com.example.myfirstapp.ImageConfirmation.through.gallery",
							true);
					in1.putExtra(
							"com.example.myfirstapp.ImageConfirmation.orientation",
							0);
					in1.putExtra("Username", userName);
					in1.putExtra("PersonId", personId);
					in1.putExtra("IdentifyPerson", identifyPerson);
					startActivityForResult(in1, 1);
					finish();
				} else {
					Log.e(TAG, "Bitmap is NULL");
				}
			}
		}
	}
}

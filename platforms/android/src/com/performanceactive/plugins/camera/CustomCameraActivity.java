package com.performanceactive.plugins.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.venkykowshik.squarecamera.CameraActivity;

public class CustomCameraActivity extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;
//	private PictureCallback mPicture;
	private ImageButton capture, switchCamera, flash, gallery;
	private Context myContext;
//	private LinearLayout cameraPreview;
	private boolean cameraFront = false;
	private boolean isLighOn = false;
	private ImageView backButton;

	int numberOfCameras;
	int cameraCurrentlyLocked;

	// The first rear facing camera
	int defaultCameraId;

	// private RelativeLayout layout;

	public static String FILENAME = "Filename";
	public static String QUALITY = "Quality";
	public static String IMAGE_URI = "ImageUri";
	public static String ERROR_MESSAGE = "ErrorMessage";
	public static String TARGET_WIDTH = "TargetWidth";
	public static String TARGET_HEIGHT = "TargetHeight";

	private static int RESULT_LOAD_IMG = 1;
	private static int PIC_CROP = 2;
	private static final int REQUEST_CAMERA = 0;

	private FakeR fakeR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		fakeR = new FakeR(this);
		setContentView(fakeR.getId("layout", "activity_main"));

		backButton = (ImageView) findViewById(fakeR.getId("id", "backArrow"));
		backButton.setImageResource(getDrawable("close"));
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				 Intent startCustomCameraIntent = new Intent(CustomCameraActivity.this, CameraActivity.class);
			        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
			}
		});

		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}

		initialize();

	}

	public void initialize() {
	
		mPreview = new CameraPreview(this, (SurfaceView) findViewById(fakeR.getId("id", "surfaceView")));		
		mPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((FrameLayout) findViewById(fakeR.getId("id",
				"camera_perview"))).addView(mPreview);
		mPreview.setKeepScreenOn(true);

		createCaptureButton();
		createRotateButton();
		createFlashButton();
		createGalleryButton();

	}

	private void createCaptureButton() {
		capture = (ImageButton) findViewById(fakeR.getId("id", "capture"));
		setBitmap(capture, "capture_button");
		capture.setOnClickListener(captrureListener);
	}

	private void createRotateButton() {

		// roatate camera button...
		switchCamera = (ImageButton) findViewById(fakeR.getId("id",
				"switch_camera"));
		setBitmap(switchCamera, "switch_camera");
	//	switchCamera.setOnClickListener(switchCameraListener);
	}

	private void createFlashButton() {
		// flash button....
		flash = (ImageButton) findViewById(fakeR.getId("id", "flash"));
		setBitmap(flash, "flash");
	//	flash.setOnClickListener(flashCameraListener);
	}

	private void createGalleryButton() {

		// roatate camera button...
		gallery = (ImageButton) findViewById(fakeR.getId("id", "gallery"));
		setBitmap(gallery, "gallery");
		gallery.setOnClickListener(galleryListner);
	}

	private void setBitmap(ImageButton imageView, String imageName) {
		try {
			imageView.setImageResource(getDrawable(imageName));
		} catch (Exception e) {
			Log.e("dsnjnsdnjk", "Could load image", e);
		}
	}

	public int getDrawable(String name) {

		return fakeR.getId("drawable", name);
	}

//	private int findFrontFacingCamera() {
//		int cameraId = -1;
//		// Search for the front facing camera
//		int numberOfCameras = Camera.getNumberOfCameras();
//		for (int i = 0; i < numberOfCameras; i++) {
//			CameraInfo info = new CameraInfo();
//			Camera.getCameraInfo(i, info);
//			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//				cameraId = i;
//				cameraFront = true;
//				break;
//			}
//		}
//		return cameraId;
//	}

	private int findBackFacingCamera() {
		int cameraId = -1;
		// Search for the back facing camera
		// get the number of cameras
		int numberOfCameras = Camera.getNumberOfCameras();
		// for every camera check
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				cameraFront = false;
				break;
			}
		}
		return cameraId;
	}

	@Override
	protected void onResume() {
		super.onResume();

		int numCams = Camera.getNumberOfCameras();
		if(numCams > 0){
			try{
				mCamera = Camera.open(0);
				mCamera.startPreview();
				mPreview.setCamera(mCamera);
			} catch (RuntimeException ex){
				Toast.makeText(this, "Camera Not Found", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if(mCamera != null) {
			mCamera.stopPreview();
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		super.onPause();
	}
	
	private void resetCam() {
		mCamera.startPreview();
		mPreview.setCamera(mCamera);
	}

	@Override
	public void onBackPressed() {
		// code here to show dialog
		finishWithError("Camera View Closed");
		super.onBackPressed(); // optional depending on your needs
	}

	OnClickListener flashCameraListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (cameraFront) {
				Toast toast = Toast
						.makeText(
								myContext,
								"Sorry, your phone doesn't have Flash For Front Camera",
								Toast.LENGTH_LONG);
				toast.show();
				return;
			}

			if (isLighOn) {
				releaseCamera();
				mCamera = Camera.open();
				Parameters p = mCamera.getParameters();
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
				setBitmap(flash, "flash");
				mCamera.setParameters(p);
				//mPreview.refreshCamera(mCamera);
				setCameraDisplayOrientation((Activity) myContext,
						findBackFacingCamera(), mCamera);
				isLighOn = false;
			} else {
				releaseCamera();
				mCamera = Camera.open();
				Parameters p = mCamera.getParameters();
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				setBitmap(flash, "flash");
				mCamera.setParameters(p);
				//mPreview.refreshCamera(mCamera);
				setCameraDisplayOrientation((Activity) myContext,
						findBackFacingCamera(), mCamera);
				isLighOn = true;
			}

		}
	};

	OnClickListener switchCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			 // check for availability of multiple cameras
            if (numberOfCameras == 1) {
            	Toast toast = Toast.makeText(myContext,
						"Sorry, your phone has only one camera!",
						Toast.LENGTH_LONG);
				toast.show();
            }

            // OK, we have multiple cameras.
            // Release this camera -> cameraCurrentlyLocked
            if (mCamera != null) {
                mCamera.stopPreview();
                mPreview.setCamera(null);
                mCamera.release();
                mCamera = null;
            }

            // Acquire the next camera and request Preview to reconfigure
            // parameters.
            mCamera = Camera
                    .open((cameraCurrentlyLocked + 1) % numberOfCameras);
            cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
                    % numberOfCameras;
           // mPreview.switchCamera(mCamera);

            // Start the preview
            mCamera.startPreview();
		}
	};

	OnClickListener galleryListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create intent to Open Image applications like Gallery, Google
			// Photos
			Intent galleryIntent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			// Start the Intent
			startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
		}
	};

//	private void chooseCamera() {
//		// if the camera preview is the front
//
//		if (cameraFront) {
//			int cameraId = findBackFacingCamera();
//			if (cameraId >= 0) {
//				// open the backFacingCamera
//				// set a picture callback
//				// refresh the preview
//				mCamera = Camera.open(cameraId);
//				setCameraDisplayOrientation(this, cameraId, mCamera);
//				mPicture = getPictureCallback();
//				//mPreview.refreshCamera(mCamera);
//			}
//		} else {
//			int cameraId = findFrontFacingCamera();
//			if (cameraId >= 0) {
//				// open the backFacingCamera
//				// set a picture callback
//				// refresh the preview
//				mCamera = Camera.open(cameraId);
//				setCameraDisplayOrientation(this, cameraId, mCamera);
//				mPicture = getPictureCallback();
//			//	mPreview.refreshCamera(mCamera);
//			}
//		}
//	}

	private PictureCallback getPictureCallback() {
		PictureCallback picture = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] jpegData, Camera camera) {
				try {
					String filename = getIntent().getStringExtra(FILENAME);
					int quality = getIntent().getIntExtra(QUALITY, 80);
					File capturedImageFile = new File(getCacheDir(), filename);
					Bitmap capturedImage = getScaledBitmap(jpegData);
					capturedImage = correctCaptureImageOrientation(capturedImage);
					capturedImage.compress(CompressFormat.JPEG, quality,
							new FileOutputStream(capturedImageFile));
					Intent data = new Intent();
					data.putExtra(IMAGE_URI, Uri.fromFile(capturedImageFile)
							.toString());
					setResult(RESULT_OK, data);
					finish();
				} catch (Exception e) {
					finishWithError("Failed to save image");
				}

			}
		};
		return picture;
	}

	OnClickListener captrureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mCamera.takePicture(null, null, getPictureCallback());
		}
	};

	private Bitmap getScaledBitmap(byte[] jpegData) {
		int targetWidth = getIntent().getIntExtra(TARGET_WIDTH, -1);
		int targetHeight = getIntent().getIntExtra(TARGET_HEIGHT, -1);
		if (targetWidth <= 0 && targetHeight <= 0) {
			return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
		}

		// get dimensions of image without scaling
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);

		// decode image as close to requested scale as possible
		options.inJustDecodeBounds = false;
		options.inSampleSize = calculateInSampleSize(options, targetWidth,
				targetHeight);
		Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0,
				jpegData.length, options);

		// set missing width/height based on aspect ratio
		float aspectRatio = ((float) options.outHeight) / options.outWidth;
		if (targetWidth > 0 && targetHeight <= 0) {
			targetHeight = Math.round(targetWidth * aspectRatio);
		} else if (targetWidth <= 0 && targetHeight > 0) {
			targetWidth = Math.round(targetHeight / aspectRatio);
		}

		// make sure we also
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight,
				true);
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int requestedWidth, int requestedHeight) {
		int originalHeight = options.outHeight;
		int originalWidth = options.outWidth;
		int inSampleSize = 1;
		if (originalHeight > requestedHeight || originalWidth > requestedWidth) {
			int halfHeight = originalHeight / 2;
			int halfWidth = originalWidth / 2;
			while ((halfHeight / inSampleSize) > requestedHeight
					&& (halfWidth / inSampleSize) > requestedWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	private Bitmap correctCaptureImageOrientation(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
	}

	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	private void finishWithError(String message) {
		Intent data = new Intent().putExtra(ERROR_MESSAGE, message);
		setResult(RESULT_CANCELED, data);
		finish();
	}

	public void setCameraDisplayOrientation(Activity activity, int cameraId,
			android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			
			if (requestCode == REQUEST_CAMERA) {

	            if (data.getExtras() != null) {
	                Toast.makeText(this,"Camera View Closed",Toast.LENGTH_LONG).show();
	            } else {
	                Uri photoUri = data.getData();
	                // Get the bitmap in according to the width of the device
	                Intent intent = new Intent();
					intent.putExtra(IMAGE_URI, photoUri.toString());
					setResult(RESULT_OK, intent);
					finish();
	            }
	        }
			
			// When an Image is picked
			if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
					&& null != data) {
				// Get the Image from data

				Uri selectedImage = data.getData();
				// String[] filePathColumn = { MediaStore.Images.Media.DATA };

				try {
					// call the standard crop action intent (the user device may
					// not support it)
					Intent cropIntent = new Intent(
							"com.android.camera.action.CROP");
					// indicate image type and Uri
					cropIntent.setDataAndType(selectedImage, "image/*");
					// set crop properties
					cropIntent.putExtra("crop", "true");
					// indicate aspect of desired crop
					cropIntent.putExtra("aspectX", 1);
					cropIntent.putExtra("aspectY", 1);
					// indicate output X and Y
					cropIntent.putExtra("outputX", 256);
					cropIntent.putExtra("outputY", 256);
					// retrieve data on return
					cropIntent.putExtra("return-data", true);
					// start the activity - we handle returning in
					// onActivityResult
					startActivityForResult(cropIntent, PIC_CROP);
				} catch (ActivityNotFoundException anfe) {
					// display an error message
					String errorMessage = "Whoops - your device doesn't support the crop action!";
					Toast toast = Toast.makeText(this, errorMessage,
							Toast.LENGTH_SHORT);
					toast.show();

					InputStream iStream = getContentResolver().openInputStream(
							selectedImage);
					byte[] jpegData = getBytes(iStream);

					String filename = getIntent().getStringExtra(FILENAME);
					int quality = getIntent().getIntExtra(QUALITY, 80);
					File capturedImageFile = new File(getCacheDir(), filename);
					Bitmap capturedImage = getScaledBitmap(jpegData);
					capturedImage = correctCaptureImageOrientation(capturedImage);
					capturedImage.compress(CompressFormat.JPEG, quality,
							new FileOutputStream(capturedImageFile));
					Intent intent = new Intent();
					intent.putExtra(IMAGE_URI, Uri.fromFile(capturedImageFile)
							.toString());
					setResult(RESULT_OK, intent);
					finish();

				}

			} else if (requestCode == PIC_CROP && resultCode == RESULT_OK
					&& null != data) {
				// get the returned data
				Bundle extras = data.getExtras();
				// get the cropped bitmap
				Bitmap thePic = extras.getParcelable("data");

				InputStream iStream = getContentResolver().openInputStream(
						getImageUri(this, thePic));
				byte[] jpegData = getBytes(iStream);

				String filename = getIntent().getStringExtra(FILENAME);
				int quality = getIntent().getIntExtra(QUALITY, 80);
				File capturedImageFile = new File(getCacheDir(), filename);
				Bitmap capturedImage = getScaledBitmap(jpegData);
				capturedImage = correctCaptureImageOrientation(capturedImage);
				capturedImage.compress(CompressFormat.JPEG, quality,
						new FileOutputStream(capturedImageFile));
				Intent intent = new Intent();
				intent.putExtra(IMAGE_URI, Uri.fromFile(capturedImageFile)
						.toString());
				setResult(RESULT_OK, intent);
				finish();

			} else {
				Toast.makeText(this, "You haven't picked Image",
						Toast.LENGTH_LONG).show();
				finishWithError("Failed to save image");
			}
		} catch (Exception e) {
			Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
					.show();
			finishWithError("Failed to save image");
		}

	}

	public Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = Images.Media.insertImage(inContext.getContentResolver(),
				inImage, "Title", null);
		return Uri.parse(path);
	}

	public byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		return byteBuffer.toByteArray();
	}
}

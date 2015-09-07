package com.performanceactive.plugins.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class CustomCameraActivity extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;
	private ImageButton capture, switchCamera, flash, gallery;
	private Context myContext;
	private LinearLayout cameraPreview;
	private boolean cameraFront = false;
	private boolean isLighOn = false;

	private RelativeLayout layout;

	public static String FILENAME = "Filename";
	public static String QUALITY = "Quality";
	public static String IMAGE_URI = "ImageUri";
	public static String ERROR_MESSAGE = "ErrorMessage";
	public static String TARGET_WIDTH = "TargetWidth";
	public static String TARGET_HEIGHT = "TargetHeight";
	
	 private static int RESULT_LOAD_IMG = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;

		layout = new RelativeLayout(this);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(layoutParams);

		initialize();
		// setContentView(R.layout.activity_main);
		setContentView(layout);
	}

	public void initialize() {
		cameraPreview = new LinearLayout(myContext);

		cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		cameraPreview.setOrientation(LinearLayout.VERTICAL);

		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);
		layout.addView(cameraPreview);

		createCaptureButton();
		createRotateButton();
		createFlashButton();
		createGalleryButton();

		// capture = (Button) findViewById(R.id.button_capture);
		// capture.setOnClickListener(captrureListener);
		//
		// switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
		// switchCamera.setOnClickListener(switchCameraListener);
		//
		// flash = (Button) findViewById(R.id.button_flash);
		// flash.setOnClickListener(flashCameraListener);

	}

	private void createCaptureButton() {
		capture = new ImageButton(getApplicationContext());
		setBitmap(capture, "capture_button.png");
		capture.setBackgroundColor(Color.parseColor("#90226a"));
		capture.setScaleType(ScaleType.FIT_CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				dpToPixels(75), dpToPixels(75));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

		// layoutParams.bottomMargin = dpToPixels(10);
		capture.setLayoutParams(layoutParams);
		capture.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				setCaptureButtonImageForEvent(capture, event);
				return false;
			}
		});
		capture.setOnClickListener(captrureListener);
		layout.addView(capture);
	}

	private void createRotateButton() {

		// roatate camera button...
		switchCamera = new ImageButton(getApplicationContext());
		setBitmap(switchCamera, "capture_button.png");
		switchCamera.setBackgroundColor(Color.parseColor("#567678"));
		switchCamera.setScaleType(ScaleType.FIT_CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				dpToPixels(75), dpToPixels(75));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		// layoutParams.bottomMargin = dpToPixels(10);
		switchCamera.setLayoutParams(layoutParams);
		switchCamera.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				setCaptureButtonImageForEvent(switchCamera, event);
				return false;
			}
		});
		switchCamera.setOnClickListener(switchCameraListener);
		layout.addView(switchCamera);
	}

	private void createFlashButton() {
		// flash button....
		flash = new ImageButton(getApplicationContext());
		setBitmap(flash, "capture_button.png");
		flash.setBackgroundColor(Color.parseColor("#567678"));
		flash.setScaleType(ScaleType.FIT_CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				dpToPixels(75), dpToPixels(75));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// layoutParams.bottomMargin = dpToPixels(10);
		flash.setLayoutParams(layoutParams);
		flash.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				setCaptureButtonImageForEvent(flash, event);
				return false;
			}
		});
		flash.setOnClickListener(flashCameraListener);
		layout.addView(flash);
	}
	
	private void createGalleryButton() {

		// roatate camera button...
		gallery = new ImageButton(getApplicationContext());
		setBitmap(gallery, "capture_button.png");
		gallery.setBackgroundColor(Color.parseColor("#567678"));
		gallery.setScaleType(ScaleType.FIT_CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				dpToPixels(75), dpToPixels(75));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		// layoutParams.bottomMargin = dpToPixels(10);
		gallery.setLayoutParams(layoutParams);
		gallery.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				setCaptureButtonImageForEvent(gallery, event);
				return false;
			}
		});
		gallery.setOnClickListener(galleryListner);
		layout.addView(gallery);
	}

	private int dpToPixels(int dp) {
		float density = getResources().getDisplayMetrics().density;
		return Math.round(dp * density);
	}

	private void setCaptureButtonImageForEvent(ImageButton image,
			MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			setBitmap(image, "capture_button_pressed.png");
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			setBitmap(image, "capture_button.png");
		}
	}

	private void setBitmap(ImageView imageView, String imageName) {
		try {
			InputStream imageStream = this.getAssets().open(
					"www/img/cameraoverlay/" + imageName);
			Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
			imageView.setImageBitmap(bitmap);
			imageStream.close();
		} catch (Exception e) {
			Log.e("dsnjnsdnjk", "Could load image", e);
		}
	}

	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				cameraFront = true;
				break;
			}
		}
		return cameraId;
	}

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

	public void onResume() {
		super.onResume();
		if (!hasCamera(myContext)) {
			Toast toast = Toast.makeText(myContext,
					"Sorry, your phone does not have a camera!",
					Toast.LENGTH_LONG);
			toast.show();
			finishWithError("Could not display camera preview");
			finish();
		}
		if (mCamera == null) {
			// if the front facing camera does not exist
			if (findFrontFacingCamera() < 0) {
				Toast.makeText(this, "No front facing camera found.",
						Toast.LENGTH_LONG).show();
				switchCamera.setVisibility(View.GONE);
			}
			mCamera = Camera.open(findBackFacingCamera());
			setCameraDisplayOrientation(this, findBackFacingCamera(), mCamera);
			mPicture = getPictureCallback();
			mPreview.refreshCamera(mCamera);
		}
	}
	
	@Override
public void onBackPressed()
{
     // code here to show dialog
     finishWithError("Camera View Closed");
     super.onBackPressed();  // optional depending on your needs
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
				mCamera.setParameters(p);
				mPreview.refreshCamera(mCamera);
				setCameraDisplayOrientation((Activity) myContext,
						findBackFacingCamera(), mCamera);
				isLighOn = false;
			} else {
				releaseCamera();
				mCamera = Camera.open();
				Parameters p = mCamera.getParameters();
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mCamera.setParameters(p);
				mPreview.refreshCamera(mCamera);
				setCameraDisplayOrientation((Activity) myContext,
						findBackFacingCamera(), mCamera);
				isLighOn = true;
			}

		}
	};

	OnClickListener switchCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get the number of cameras
			int camerasNumber = Camera.getNumberOfCameras();
			if (camerasNumber > 1) {
				// release the old camera instance
				// switch camera, from the front and the back and vice versa
				releaseCamera();
				chooseCamera();
			} else {
				Toast toast = Toast.makeText(myContext,
						"Sorry, your phone has only one camera!",
						Toast.LENGTH_LONG);
				toast.show();
			}
		}
	};
	
	OnClickListener galleryListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
		// Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
		}
	};

	public void chooseCamera() {
		// if the camera preview is the front

		if (cameraFront) {
			int cameraId = findBackFacingCamera();
			if (cameraId >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview
				mCamera = Camera.open(cameraId);
				setCameraDisplayOrientation(this, cameraId, mCamera);
				mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		} else {
			int cameraId = findFrontFacingCamera();
			if (cameraId >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview
				mCamera = Camera.open(cameraId);
				setCameraDisplayOrientation(this, cameraId, mCamera);
				mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// when on Pause, release camera in order to be used from other
		// applications
		releaseCamera();
	}

	private boolean hasCamera(Context context) {
		// check if the device has camera
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

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
		                capturedImage.compress(CompressFormat.JPEG, quality, new FileOutputStream(capturedImageFile));
		                Intent data = new Intent();
		                data.putExtra(IMAGE_URI, Uri.fromFile(capturedImageFile).toString());
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
			mCamera.takePicture(null, null, mPicture);
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
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
 
                Uri selectedImage = data.getData();
              //  String[] filePathColumn = { MediaStore.Images.Media.DATA };
                InputStream iStream =   getContentResolver().openInputStream(selectedImage);
                byte[] jpegData = getBytes(iStream);
 
                String filename = getIntent().getStringExtra(FILENAME);
                int quality = getIntent().getIntExtra(QUALITY, 80);
                File capturedImageFile = new File(getCacheDir(), filename);
                Bitmap capturedImage = getScaledBitmap(jpegData);
                capturedImage = correctCaptureImageOrientation(capturedImage);
                capturedImage.compress(CompressFormat.JPEG, quality, new FileOutputStream(capturedImageFile));
                Intent intent = new Intent();
                intent.putExtra(IMAGE_URI, Uri.fromFile(capturedImageFile).toString());
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

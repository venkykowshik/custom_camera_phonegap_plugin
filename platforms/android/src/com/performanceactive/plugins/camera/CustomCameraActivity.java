package com.performanceactive.plugins.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CustomCameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PictureCallback {

	public static final String TAG = CustomCameraActivity.class.getSimpleName();
	public static final String CAMERA_ID_KEY = "camera_id";
	public static final String CAMERA_FLASH_KEY = "flash_mode";
	public static final String IMAGE_INFO = "image_info";
	public static String IMAGE_URI = "ImageUri";
	public static String ERROR_MESSAGE = "ErrorMessage";

	private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

	private int mCameraID;
	private String mFlashMode;
	private Camera mCamera;
	private SquareCameraPreview mPreviewView;
	private SurfaceHolder mSurfaceHolder;

	private ImageParameters mImageParameters;

	private static int RESULT_LOAD_IMG = 2;
	private static int PIC_CROP = 1;
	private static int PIC_FILTERS = 3;

	private CameraOrientationListener mOrientationListener;

	private FakeR fakeR;

	private ImageView backButton;
	
	private TextView header;
	private TextView gallery_text;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			mCameraID = getBackCameraID();
			mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
			mImageParameters = new ImageParameters();
		} else {
			mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
			mFlashMode = savedInstanceState.getString(CAMERA_FLASH_KEY);
			mImageParameters = savedInstanceState.getParcelable(IMAGE_INFO);
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		fakeR = new FakeR(this);
		mOrientationListener = new CameraOrientationListener(this);
		setContentView(fakeR.getId("layout", "activity_main"));
		
		header = (TextView) findViewById(fakeR.getId("id", "titletxt"));
		 Typeface face= Typeface.createFromAsset(this.getAssets(), "Fonts/ARIALUNI.TTF");
		 header.setTypeface(face);
		 
		 gallery_text = (TextView) findViewById(fakeR.getId("id", "gallery_text"));
		 gallery_text.setTypeface(face);
		 
		mOrientationListener = new CameraOrientationListener(this);

		backButton = (ImageView) findViewById(fakeR.getId("id", "backArrow"));
		backButton.setImageResource(getDrawable("exist"));
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finishWithError("Camera View Closed...");
			}
		});

		mOrientationListener.enable();

		mPreviewView = (SquareCameraPreview) findViewById(fakeR.getId("id", "camera_preview_view"));
		mPreviewView.getHolder().addCallback(this);

		final View topCoverView = findViewById(fakeR.getId("id", "cover_top_view"));
		final View btnCoverView = findViewById(fakeR.getId("id", "cover_bottom_view"));

		final ImageView flashIconBtn = (ImageView) findViewById(fakeR.getId("id", "flash_icon"));
		flashIconBtn.setImageResource(getDrawable("flash"));

		mImageParameters.mIsPortrait = getResources()
				.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		Log.d(TAG, "onViewCreated");
		if (savedInstanceState == null) {
			ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
			observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					mImageParameters.mPreviewWidth = mPreviewView.getWidth();
					mImageParameters.mPreviewHeight = mPreviewView.getHeight();

					mImageParameters.mCoverWidth = mImageParameters.mCoverHeight = mImageParameters
							.calculateCoverWidthHeight();

					// Log.d(TAG, "parameters: " + mImageParameters.getStringValues());
					// Log.d(TAG, "cover height " + topCoverView.getHeight());
					resizeTopAndBtmCover(topCoverView, btnCoverView);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			});
		} else {
			 if (mImageParameters.isPortrait()) {
	                topCoverView.getLayoutParams().height = mImageParameters.mCoverHeight;
	                btnCoverView.getLayoutParams().height = mImageParameters.mCoverHeight;
	            } else {
	                topCoverView.getLayoutParams().width = mImageParameters.mCoverWidth;
	                btnCoverView.getLayoutParams().width = mImageParameters.mCoverWidth;
	            }
		}

		final ImageView swapCameraBtn = (ImageView) findViewById(fakeR.getId("id", "change_camera"));
		swapCameraBtn.setImageResource(getDrawable("switch_camera"));
		swapCameraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
					mCameraID = getBackCameraID();
				} else {
					mCameraID = getFrontCameraID();
				}
				restartPreview();
			}
		});

		final View changeCameraFlashModeBtn = findViewById(fakeR.getId("id", "flash"));
		changeCameraFlashModeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
					mFlashMode = Camera.Parameters.FLASH_MODE_ON;
				} else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
					mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
				} else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
					mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
				}

				setupFlashMode();
				setupCamera();
			}
		});
		setupFlashMode();

		final ImageView takePhotoBtn = (ImageView) findViewById(fakeR.getId("id", "capture_image_button"));
		takePhotoBtn.setImageResource(getDrawable("capture_button"));
		takePhotoBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePicture();
			}
		});

		final ImageButton gallery = (ImageButton) findViewById(fakeR.getId("id", "gallery"));
		setBitmap(gallery, "gallery");

		final LinearLayout gallery_view = (LinearLayout) findViewById(fakeR.getId("id", "gallery_view"));
		gallery_view.setOnClickListener(galleryListner);

	}

	OnClickListener galleryListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create intent to Open Image applications like Gallery, Google
			// Photos
			Intent galleryIntent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			// Start the Intent
			startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
		}
	};

	public int getDrawable(String name) {

		return fakeR.getId("drawable", name);
	}

	private void setupFlashMode() {

		final TextView autoFlashIcon = (TextView) findViewById(fakeR.getId("id", "auto_flash_icon"));
		 Typeface face= Typeface.createFromAsset(this.getAssets(), "Fonts/ARIALUNI.TTF");
		 autoFlashIcon.setTypeface(face);
		if (Camera.Parameters.FLASH_MODE_AUTO.equalsIgnoreCase(mFlashMode)) {
			autoFlashIcon.setText("تلقائي");
		} else if (Camera.Parameters.FLASH_MODE_ON.equalsIgnoreCase(mFlashMode)) {
			autoFlashIcon.setText("على");
		} else if (Camera.Parameters.FLASH_MODE_OFF.equalsIgnoreCase(mFlashMode)) {
			autoFlashIcon.setText("بعيدا");
		}
	}
	
	@Override
	protected void onPause() {
		if (mCamera != null) {
			stopCameraPreview();
		}
		super.onPause();
	}
	
	@Override
	protected void onRestart() {
		startCameraPreview();
		super.onRestart();
	}
	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Log.d(TAG, "onSaveInstanceState");
		outState.putInt(CAMERA_ID_KEY, mCameraID);
		outState.putString(CAMERA_FLASH_KEY, mFlashMode);
		outState.putParcelable(IMAGE_INFO, mImageParameters);
		super.onSaveInstanceState(outState);
	}

	private void resizeTopAndBtmCover(final View topCover, final View bottomCover) {
		ResizeAnimation resizeTopAnimation = new ResizeAnimation(topCover, mImageParameters);
		resizeTopAnimation.setDuration(800);
		resizeTopAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		topCover.startAnimation(resizeTopAnimation);

		ResizeAnimation resizeBtmAnimation = new ResizeAnimation(bottomCover, mImageParameters);
		resizeBtmAnimation.setDuration(800);
		resizeBtmAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
		bottomCover.startAnimation(resizeBtmAnimation);
	 }

	private void getCamera(int cameraID) {
		try {
			mCamera = Camera.open(cameraID);
			mPreviewView.setCamera(mCamera);
		} catch (Exception e) {
			Log.d(TAG, "Can't open camera with id " + cameraID);
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Start the camera preview
	 */
	private void startCameraPreview() {
		determineDisplayOrientation();
		setupCamera();

		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d(TAG, "Can't start camera preview due to IOException " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Stop the camera preview
	 */
	private void stopCameraPreview() {
		// Nulls out callbacks, stops face detection
		mCamera.stopPreview();
		mPreviewView.setCamera(null);
	}

	/**
	 * Determine the current display orientation and rotate the camera preview
	 * accordingly
	 */
	private void determineDisplayOrientation() {
		CameraInfo cameraInfo = new CameraInfo();
		Camera.getCameraInfo(mCameraID, cameraInfo);

		// Clockwise rotation needed to align the window display to the natural
		// position
		int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;

		switch (rotation) {
		case Surface.ROTATION_0: {
			degrees = 0;
			break;
		}
		case Surface.ROTATION_90: {
			degrees = 90;
			break;
		}
		case Surface.ROTATION_180: {
			degrees = 180;
			break;
		}
		case Surface.ROTATION_270: {
			degrees = 270;
			break;
		}
		}

		int displayOrientation;

		// CameraInfo.Orientation is the angle relative to the natural position
		// of the device
		// in clockwise rotation (angle that is rotated clockwise from the
		// natural position)
		if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
			// Orientation is angle of rotation when facing the camera for
			// the camera image to match the natural orientation of the device
			displayOrientation = (cameraInfo.orientation + degrees) % 360;
			displayOrientation = (360 - displayOrientation) % 360;
		} else {
			displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
		}

		mImageParameters.mDisplayOrientation = displayOrientation;
		mImageParameters.mLayoutOrientation = degrees;

		mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);
	}

	/**
	 * Setup the camera parameters
	 */
	private void setupCamera() {
		// Never keep a global parameters
		Camera.Parameters parameters = mCamera.getParameters();

		Size bestPreviewSize = determineBestPreviewSize(parameters);
		Size bestPictureSize = determineBestPictureSize(parameters);

		parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
		parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

		// Set continuous picture focus, if it's supported
		if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}

		final View changeCameraFlashModeBtn = findViewById(fakeR.getId("id", "flash"));
		List<String> flashModes = parameters.getSupportedFlashModes();
		if (flashModes != null && flashModes.contains(mFlashMode)) {
			parameters.setFlashMode(mFlashMode);
			changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
		} else {
			changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
		}

		// Lock in the changes
		mCamera.setParameters(parameters);
	}

	private Size determineBestPreviewSize(Camera.Parameters parameters) {
		return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
	}

	private Size determineBestPictureSize(Camera.Parameters parameters) {
		return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
	}

	private Size determineBestSize(List<Size> sizes, int widthThreshold) {
		Size bestSize = null;
		Size size;
		int numOfSizes = sizes.size();
		for (int i = 0; i < numOfSizes; i++) {
			size = sizes.get(i);
			boolean isDesireRatio = (size.width / 4) == (size.height / 3);
			boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

			if (isDesireRatio && isBetterSize) {
				bestSize = size;
			}
		}

		if (bestSize == null) {
			Log.d(TAG, "cannot find the best camera size");
			return sizes.get(sizes.size() - 1);
		}

		return bestSize;
	}

	private void restartPreview() {
		if (mCamera != null) {
			stopCameraPreview();
			mCamera.release();
			mCamera = null;
		}

		getCamera(mCameraID);
		startCameraPreview();
	}

	private int getFrontCameraID() {
		PackageManager pm = this.getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
			return CameraInfo.CAMERA_FACING_FRONT;
		}

		return getBackCameraID();
	}

	private int getBackCameraID() {
		return CameraInfo.CAMERA_FACING_BACK;
	}

	/**
	 * Take a picture
	 */
	private void takePicture() {
		mOrientationListener.rememberOrientation();

		// Shutter callback occurs after the image is captured. This can
		// be used to trigger a sound to let the user know that image is taken
		Camera.ShutterCallback shutterCallback = null;

		// Raw callback occurs when the raw image data is available
		Camera.PictureCallback raw = null;

		// postView callback occurs when a scaled, fully processed
		// postView image is available.
		Camera.PictureCallback postView = null;

		// jpeg callback occurs when the compressed image is available
		mCamera.takePicture(shutterCallback, raw, postView, this);
	}

	@Override
	public void onStop() {
		mOrientationListener.disable();

		// stop the preview
		if (mCamera != null) {
			stopCameraPreview();
			mCamera.release();
			mCamera = null;
		}

		super.onStop();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder;

		getCamera(mCameraID);
		startCameraPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// The surface is destroyed with the visibility of the SurfaceView is
		// set to View.Invisible
	}

	/**
	 * When orientation changes, onOrientationChanged(int) of the listener will
	 * be called
	 */
	private static class CameraOrientationListener extends OrientationEventListener {

		private int mCurrentNormalizedOrientation;
		private int mRememberedNormalOrientation;

		public CameraOrientationListener(Context context) {
			super(context, SensorManager.SENSOR_DELAY_NORMAL);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			if (orientation != ORIENTATION_UNKNOWN) {
				mCurrentNormalizedOrientation = normalize(orientation);
			}
		}

		/**
		 * @param degrees
		 *            Amount of clockwise rotation from the device's natural
		 *            position
		 * @return Normalized degrees to just 0, 90, 180, 270
		 */
		private int normalize(int degrees) {
			if (degrees > 315 || degrees <= 45) {
				return 0;
			}

			if (degrees > 45 && degrees <= 135) {
				return 90;
			}

			if (degrees > 135 && degrees <= 225) {
				return 180;
			}

			if (degrees > 225 && degrees <= 315) {
				return 270;
			}

			throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
		}

		public void rememberOrientation() {
			mRememberedNormalOrientation = mCurrentNormalizedOrientation;
		}

		public int getRememberedNormalOrientation() {
			rememberOrientation();
			return mRememberedNormalOrientation;
		}
	}

	private void setBitmap(ImageButton imageView, String imageName) {
		try {
			imageView.setImageResource(getDrawable(imageName));
		} catch (Exception e) {
			Log.e("dsnjnsdnjk", "Could load image", e);
		}
	}

	/**
	 * A picture has been taken
	 * 
	 * @param data
	 * @param camera
	 */
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		int rotation = getPhotoRotation();

		Bitmap bitmap = ImageUtility.decodeSampledBitmapFromByte(this, data);
		// Log.d(TAG, "original bitmap width " + bitmap.getWidth() + " height "
		// + bitmap.getHeight());
		if (rotation != 0) {
			Bitmap oldBitmap = bitmap;

			Matrix matrix = new Matrix();
			matrix.postRotate(rotation);

			bitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, false);

			oldBitmap.recycle();
		}

		Uri photoUri = ImageUtility.savePicture(this, bitmap);
		// Intent intent = new Intent();
		// intent.putExtra(IMAGE_URI, photoUri.toString());
		// setResult(RESULT_OK, intent);
		// finish();

//		Intent intent = new Intent(this, ImageFiltersActivity.class);
//		intent.putExtra(IMAGE_URI, photoUri.toString());
//		startActivityForResult(intent, PIC_FILTERS);

		Intent intent = new Intent(this, CropActivity.class);
		intent.putExtra(IMAGE_URI, photoUri.toString());
		startActivityForResult(intent, PIC_CROP);
	}

	private int getPhotoRotation() {
		int rotation;
		int orientation = mOrientationListener.getRememberedNormalOrientation();
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(mCameraID, info);

		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			rotation = (info.orientation - orientation + 360) % 360;
		} else {
			rotation = (info.orientation + orientation) % 360;
		}

		return rotation;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {

			// When an Image is picked
			if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
				// Get the Image from data

				Uri selectedImage = data.getData();
				// String[] filePathColumn = { MediaStore.Images.Media.DATA };

				try {
					// call the standard crop action intent (the user device may
					// not support it)
//					Intent cropIntent = new Intent("com.android.camera.action.CROP");
//					// indicate image type and Uri
//					cropIntent.setDataAndType(selectedImage, "image/*");
//					// set crop properties
//					cropIntent.putExtra("crop", "true");
//					// indicate aspect of desired crop
//					cropIntent.putExtra("aspectX", 10);
//					cropIntent.putExtra("aspectY", 10);
//					// indicate output X and Y
//					cropIntent.putExtra("outputX", 1280);
//					cropIntent.putExtra("outputY", 640);
//					// retrieve data on return
//					cropIntent.putExtra("return-data", true);
//					// start the activity - we handle returning in
//					// onActivityResult
//					startActivityForResult(cropIntent, PIC_CROP);
					Intent intent = new Intent(this, CropActivity.class);
					intent.putExtra(IMAGE_URI, selectedImage.toString());
					startActivityForResult(intent, PIC_CROP);
				} catch (ActivityNotFoundException anfe) {
					// display an error message
					String errorMessage = "Whoops - your device doesn't support the crop action!";
					Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
					toast.show();

					InputStream iStream = getContentResolver().openInputStream(selectedImage);
					byte[] jpegData = getBytes(iStream);

					Bitmap bitmap = ImageUtility.decodeSampledBitmapFromByte(this, jpegData);
					Uri photoUri = ImageUtility.savePicture(this, bitmap);
					Intent intent = new Intent(this, ImageFiltersActivity.class);
					intent.putExtra(IMAGE_URI, photoUri.toString());
					startActivityForResult(intent, PIC_FILTERS);

				}

			} else if (requestCode == PIC_CROP && resultCode == RESULT_OK && null != data) {
				// get the returned data
				Bundle extras = data.getExtras();
				
				
				Uri photoUri = Uri.parse(extras.getString(CustomCameraActivity.IMAGE_URI));
				Intent intent = new Intent(this, ImageFiltersActivity.class);
				intent.putExtra(IMAGE_URI, photoUri.toString());
				startActivityForResult(intent, PIC_FILTERS);

			} else if (requestCode == PIC_FILTERS && resultCode == RESULT_OK && null != data) {
				// Toast.makeText(this, "Pic Filters Callback",
				// Toast.LENGTH_LONG).show();
				Bundle bundle = data.getExtras();
				String uriStr = bundle.getString(CustomCameraActivity.IMAGE_URI);
				Intent intent = new Intent();
				intent.putExtra(IMAGE_URI, uriStr);
				setResult(RESULT_OK, intent);
				finish();

			} else if (requestCode == PIC_FILTERS && resultCode == RESULT_FIRST_USER) {

			} else if (requestCode == PIC_FILTERS) {

				finishWithError("User Cancelled Closed Page.");
			} else {
				Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
				finishWithError("Failed to save image");
			}
		} catch (Exception e) {
			Log.e("Saving", e.getMessage());
			Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
			finishWithError("Failed to save image");
		}

	}

	private void finishWithError(String message) {
		Intent data = new Intent().putExtra(ERROR_MESSAGE, message);
		setResult(RESULT_CANCELED, data);
		finish();
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

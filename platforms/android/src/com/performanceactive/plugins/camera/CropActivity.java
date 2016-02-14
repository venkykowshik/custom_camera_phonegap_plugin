package com.performanceactive.plugins.camera;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class CropActivity extends Activity
		implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnGetCroppedImageCompleteListener {

	// region: Fields and Consts

	private static final int DEFAULT_ASPECT_RATIO_VALUES = 20;

	private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";

	private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";

	private CropImageView mCropImageView;

	private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;

	private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;

	Bitmap croppedImage;
	// endregion

	// Saves the state upon rotating the screen/restarting the activity
	@Override
	protected void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt(ASPECT_RATIO_X, mAspectRatioX);
		bundle.putInt(ASPECT_RATIO_Y, mAspectRatioY);
	}

	// Restores the state upon rotating the screen/restarting the activity
	@Override
	protected void onRestoreInstanceState(@SuppressWarnings("NullableProblems") Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		mAspectRatioX = bundle.getInt(ASPECT_RATIO_X);
		mAspectRatioY = bundle.getInt(ASPECT_RATIO_Y);
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_crop);

		// Initialize components of the app
		mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
		
		Bundle bundle = getIntent().getExtras();
		String uriStr = bundle.getString(CustomCameraActivity.IMAGE_URI);
		mCropImageView.setImageUri(Uri.parse(uriStr));

		// Sets initial aspect ratio to 10/10, for demonstration purposes
		mCropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);

		findViewById(R.id.titletxt).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mCropImageView.getCroppedImageAsync(mCropImageView.getCropShape(), 1280, 640);
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		mCropImageView.setOnSetImageUriCompleteListener(this);
		mCropImageView.setOnGetCroppedImageCompleteListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mCropImageView.setOnSetImageUriCompleteListener(null);
		mCropImageView.setOnGetCroppedImageCompleteListener(null);
	}

	@Override
	public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
		if (error == null) {
			Toast.makeText(mCropImageView.getContext(), "Image load successful", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mCropImageView.getContext(), "Image load failed: " + error.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {
		if (error == null) {
			croppedImage = bitmap;
			
			Uri photoUri = ImageUtility.savePicture(CropActivity.this, bitmap);
			Intent intent = new Intent();
			intent.putExtra(CustomCameraActivity.IMAGE_URI, photoUri.toString());
			setResult(RESULT_OK, intent);
			finish();

		} else {
			Toast.makeText(mCropImageView.getContext(), "Image crop failed: " + error.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

}

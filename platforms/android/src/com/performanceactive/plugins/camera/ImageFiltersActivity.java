package com.performanceactive.plugins.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageFiltersActivity extends Activity {

	private ImageView imgMain;
	private Bitmap src;
	private ImageView doneImg;
	
	private FakeR fakeR;

	private ImageView backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		fakeR = new FakeR(this);
		
		setContentView(fakeR.getId("layout", "activity_filters"));
		imgMain = (ImageView) findViewById(fakeR.getId("id", "effect_main"));
		Bundle bundle = getIntent().getExtras();
		String uriStr = bundle.getString(CustomCameraActivity.IMAGE_URI);
		imgMain.setImageURI(Uri.parse(uriStr));
		src = ((BitmapDrawable)imgMain.getDrawable()).getBitmap();
		
		
		
		backButton = (ImageView) findViewById(fakeR.getId("id", "backArrow"));
		backButton.setImageResource(getDrawable("close"));
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finishWithError("User Closed View...");
			}
		});
		
		doneImg = (ImageView) findViewById(fakeR.getId("id", "doneButton"));
		doneImg.setImageResource(getDrawable("close"));
		doneImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Bitmap bitmap = ((BitmapDrawable) imgMain.getDrawable()).getBitmap();
				Uri photoUri = ImageUtility.savePicture(ImageFiltersActivity.this, bitmap);
				Intent intent = new Intent();
				intent.putExtra(CustomCameraActivity.IMAGE_URI, photoUri.toString());
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	private void finishWithError(String message) {
		Intent data = new Intent().putExtra(CustomCameraActivity.ERROR_MESSAGE, message);
		setResult(RESULT_CANCELED, data);
		finish();
	}

	public void buttonClicked(View v) {

		Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT).show();
		ImageFilters imgFilter = new ImageFilters();
		if (v.getId() == R.id.effect_none) {
			
		}
		else if (v.getId() == fakeR.getId("id", "effect_brightness"))
			imgMain.setImageBitmap(imgFilter.applyBrightnessEffect(src, 80));
		else if (v.getId() == fakeR.getId("id", "effect_contrast"))
			imgMain.setImageBitmap(imgFilter.applyContrastEffect(src, 70));
		else if (v.getId() == fakeR.getId("id", "effect_flea"))
			imgMain.setImageBitmap(imgFilter.applyFleaEffect(src));
		else if (v.getId() == fakeR.getId("id", "effect_gaussian_blue"))
			imgMain.setImageBitmap(imgFilter.applyGaussianBlurEffect(src));
		else if (v.getId() == fakeR.getId("id", "effect_gamma"))
			imgMain.setImageBitmap(imgFilter.applyGammaEffect(src, 1.8, 1.8, 1.8));
		else if (v.getId() == fakeR.getId("id", "effect_grayscale"))
			imgMain.setImageBitmap(imgFilter.applyGreyscaleEffect(src));
		else if (v.getId() == fakeR.getId("id", "effect_mean_remove"))
			imgMain.setImageBitmap(imgFilter.applyMeanRemovalEffect(src));
		else if (v.getId() == fakeR.getId("id", "effect_sepia"))
			imgMain.setImageBitmap(imgFilter.applySepiaToningEffect(src, 10, 1.5, 0.6, 0.12));

	}
	
	public int getDrawable(String name) {

		return fakeR.getId("drawable", name);
	}
}

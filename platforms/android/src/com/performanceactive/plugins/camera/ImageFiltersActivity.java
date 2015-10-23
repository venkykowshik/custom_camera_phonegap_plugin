package com.performanceactive.plugins.camera;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageFiltersActivity extends Activity implements GLSurfaceView.Renderer {

	// private ImageView imgMain;
	private GLSurfaceView mEffectView;
	private Bitmap src;
	private ImageView doneImg;

	private FakeR fakeR;

	private ImageView backButton;

	int mCurrentEffect;
	
	private TextureRenderer mTexRenderer = new TextureRenderer();
	
	private int[] mTextures = new int[2];
	private EffectContext mEffectContext;
	private Effect mEffect;
	private int mImageWidth;
	private int mImageHeight;
	private boolean mInitialized = false;
	private volatile boolean saveFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		fakeR = new FakeR(this);

		setContentView(fakeR.getId("layout", "activity_filters"));
		// imgMain = (ImageView) findViewById(fakeR.getId("id", "effect_main"));
		/**
		 * Initialise the renderer and tell it to only render when Explicit
		 * requested with the RENDERMODE_WHEN_DIRTY option
		 */
		mEffectView = (GLSurfaceView) findViewById(fakeR.getId("id", "effect_main"));
		mEffectView.setEGLContextClientVersion(2);
		mEffectView.setRenderer(this);
		mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mCurrentEffect = fakeR.getId("id", "effect_none");
		Bundle bundle = getIntent().getExtras();
		String uriStr = bundle.getString(CustomCameraActivity.IMAGE_URI);
		// imgMain.setImageURI(Uri.parse(uriStr));
		Bitmap bitmap;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(uriStr));
			src = bitmap;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		backButton = (ImageView) findViewById(fakeR.getId("id", "backArrow"));
		backButton.setImageResource(getDrawable("close"));
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finishWithError("User Closed View...");
			}
		});

		doneImg = (ImageView) findViewById(fakeR.getId("id", "doneButton"));
		// doneImg.setImageResource(getDrawable("close"));
		doneImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Bitmap bitmap = src;
				Uri photoUri = ImageUtility.savePicture(ImageFiltersActivity.this, bitmap);
				Intent intent = new Intent();
				intent.putExtra(CustomCameraActivity.IMAGE_URI, photoUri.toString());
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		final LinearLayout back_view = (LinearLayout) findViewById(fakeR.getId("id", "bottom_view"));
		back_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent data = new Intent().putExtra(CustomCameraActivity.ERROR_MESSAGE, "User Closed View");
				setResult(RESULT_FIRST_USER, data);
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
		setCurrentEffect(v.getId());
		mEffectView.requestRender();
	}
	
	public void setCurrentEffect(int effect) {
		mCurrentEffect = effect;
	}

	public int getDrawable(String name) {
		return fakeR.getId("drawable", name);
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (mTexRenderer != null) {
			mTexRenderer.updateViewSize(width, height);
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	private void loadTextures() {
		// Generate textures
		GLES20.glGenTextures(2, mTextures, 0);

		// Load input bitmap
		Bitmap bitmap = src;
		mImageWidth = bitmap.getWidth();
		mImageHeight = bitmap.getHeight();
		mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);

		// Upload to texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// Set texture parameters
		GLToolbox.initTexParams();
	}

	private void initEffect() {
		EffectFactory effectFactory = mEffectContext.getFactory();
		if (mEffect != null) {
			mEffect.release();
		}
		/**
		 * Initialize the correct effect based on the selected menu/action item
		 */
		if (mCurrentEffect == fakeR.getId("id", "effect_none")) {
			
		} else if (mCurrentEffect == fakeR.getId("id", "effect_brightness")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BRIGHTNESS);
			mEffect.setParameter("brightness", 2.0f);
		} else if (mCurrentEffect == fakeR.getId("id", "effect_contrast")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_CONTRAST);
			mEffect.setParameter("contrast", 1.4f);
		} else if (mCurrentEffect == fakeR.getId("id", "effect_flea")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_CROSSPROCESS);
		} else if (mCurrentEffect == fakeR.getId("id", "effect_gaussian_blue")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DOCUMENTARY);
		} else if (mCurrentEffect == fakeR.getId("id", "effect_gamma")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DUOTONE);
			mEffect.setParameter("first_color", Color.YELLOW);
			mEffect.setParameter("second_color", Color.DKGRAY);
		} else if (mCurrentEffect == fakeR.getId("id", "effect_mean_remove")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FILLLIGHT);
			mEffect.setParameter("strength", .8f);
		}  else if (mCurrentEffect == fakeR.getId("id", "effect_grayscale")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAYSCALE);
		} else if (mCurrentEffect == fakeR.getId("id", "effect_sepia")) {
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SEPIA);
		} else {
			
		}
		
		
			
//			case R.id.autofix:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_AUTOFIX);
//			mEffect.setParameter("scale", 0.5f);
//			break;
//
//		case R.id.bw:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BLACKWHITE);
//			mEffect.setParameter("black", .1f);
//			mEffect.setParameter("white", .7f);
//			break;

//		case R.id.fisheye:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FISHEYE);
//			mEffect.setParameter("scale", .5f);
//			break;
//
//		case R.id.flipvert:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP);
//			mEffect.setParameter("vertical", true);
//			break;
//
//		case R.id.fliphor:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP);
//			mEffect.setParameter("horizontal", true);
//			break;
//
//		case R.id.grain:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAIN);
//			mEffect.setParameter("strength", 1.0f);
//			break;
//		case R.id.lomoish:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_LOMOISH);
//			break;
//
//		case R.id.negative:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_NEGATIVE);
//			break;
//
//		case R.id.posterize:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_POSTERIZE);
//			break;
//
//		case R.id.rotate:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_ROTATE);
//			mEffect.setParameter("angle", 180);
//			break;
//
//		case R.id.saturate:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SATURATE);
//			mEffect.setParameter("scale", .5f);
//			break;
//		case R.id.sharpen:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SHARPEN);
//			break;
//
//		case R.id.temperature:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TEMPERATURE);
//			mEffect.setParameter("scale", .9f);
//			break;
//
//		case R.id.tint:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TINT);
//			mEffect.setParameter("tint", Color.MAGENTA);
//			break;
//
//		case R.id.vignette:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_VIGNETTE);
//			mEffect.setParameter("scale", .5f);
//			break;

	}

	private void applyEffect() {
		mEffect.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
	}

	private void renderResult() {
		if (mCurrentEffect != fakeR.getId("id", "effect_none")) {
			// if no effect is chosen, just render the original bitmap
			mTexRenderer.renderTexture(mTextures[1]);
		} else {
			saveFrame=true;
			// render the result of applyEffect()
			mTexRenderer.renderTexture(mTextures[0]);
		}
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		if (!mInitialized) {
			// Only need to do this once
			mEffectContext = EffectContext.createWithCurrentGlContext();
			mTexRenderer.init();
			loadTextures();
			mInitialized = true;
		}
		if (mCurrentEffect != fakeR.getId("id", "effect_none")) {
			// if an effect is chosen initialize it and apply it to the texture
			initEffect();
			applyEffect();
		}
		renderResult();
		if (saveFrame) {
			saveBitmap(takeScreenshot(gl));
		}
	}
	
	private void saveBitmap(Bitmap bitmap) {
		src = bitmap;
	}

	public Bitmap takeScreenshot(GL10 mGL) {
		final int mWidth = mEffectView.getWidth();
		final int mHeight = mEffectView.getHeight();
		IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
		IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
		mGL.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

		// Convert upside down mirror-reversed image to right-side up normal
		// image.
		for (int i = 0; i < mHeight; i++) {
			for (int j = 0; j < mWidth; j++) {
				ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
			}
		}

		Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		mBitmap.copyPixelsFromBuffer(ibt);
		return mBitmap;
	}
}

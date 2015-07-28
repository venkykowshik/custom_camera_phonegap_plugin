package com.performanceactive.plugins.camera;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static android.hardware.Camera.Parameters.FOCUS_MODE_MACRO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class CustomCameraActivity extends Activity {

    private static final String TAG = CustomCameraActivity.class.getSimpleName();
//  private static final float ASPECT_RATIO = 126.0f / 86;

    public static String FILENAME = "Filename";
    public static String QUALITY = "Quality";
    public static String IMAGE_URI = "ImageUri";
    public static String ERROR_MESSAGE = "ErrorMessage";

    private Camera camera;
    private RelativeLayout layout;
    private FrameLayout cameraPreviewView;
    private ImageView borderTopLeft;
    private ImageView borderTopRight;
    private ImageView borderBottomLeft;
    private ImageView borderBottomRight;
    private ImageButton captureButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        
        createCameraPreview();
        createTopLeftBorder();
        createTopRightBorder();
        createBottomLeftBorder();
        createBottomRightBorder();
        //layoutBottomBorderImagesRespectingAspectRatio();
        createCaptureButton();
        setContentView(layout);
    }

    private void createCameraPreview() {
        cameraPreviewView = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        cameraPreviewView.setLayoutParams(layoutParams);
        layout.addView(cameraPreviewView);
    }

    private void createTopLeftBorder() {
		borderTopLeft = new ImageView(this);
		
		borderTopLeft.setAlpha(0.7f);
		borderTopLeft.setScaleX(2.9f);
		borderTopLeft.setScaleY(2.9f);

        setBitmap(borderTopLeft, "carre-camera-1500.png");

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        borderTopLeft.setLayoutParams(layoutParams);
        borderTopLeft.setTranslationY(dpToPixels(-(75/2))); 
        
        layout.addView(borderTopLeft);
    }

    private void createTopRightBorder() {
        borderTopRight = new ImageView(this);
        //setBitmap(borderTopRight, "border_top_right.png");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(50));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        if (isXLargeScreen()) {
//            layoutParams.topMargin = dpToPixels(100);
//            layoutParams.rightMargin = dpToPixels(100);
//        } else if (isLargeScreen()) {
//            layoutParams.topMargin = dpToPixels(50);
//            layoutParams.rightMargin = dpToPixels(50);
//        } else {
//            layoutParams.topMargin = dpToPixels(10);
//            layoutParams.rightMargin = dpToPixels(10);
//        }
        borderTopRight.setLayoutParams(layoutParams);
        layout.addView(borderTopRight);
    }

    private void createBottomLeftBorder() {
        borderBottomLeft = new ImageView(this);
        //setBitmap(borderBottomLeft, "border_bottom_left.png");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(50));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        if (isXLargeScreen()) {
//            layoutParams.leftMargin = dpToPixels(100);
//        } else if (isLargeScreen()) {
//            layoutParams.leftMargin = dpToPixels(50);
//        } else {
//            layoutParams.leftMargin = dpToPixels(10);
//        }
        borderBottomLeft.setLayoutParams(layoutParams);
        layout.addView(borderBottomLeft);
    }

    private void createBottomRightBorder() {
        borderBottomRight = new ImageView(this);
        //setBitmap(borderBottomRight, "border_bottom_right.png");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(50));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        if (isXLargeScreen()) {
//            layoutParams.rightMargin = dpToPixels(100);
//        } else if (isLargeScreen()) {
//            layoutParams.rightMargin = dpToPixels(50);
//        } else {
//            layoutParams.rightMargin = dpToPixels(10);
//        }
        borderBottomRight.setLayoutParams(layoutParams);
        layout.addView(borderBottomRight);
    }

//    private void layoutBottomBorderImagesRespectingAspectRatio() {
//        RelativeLayout.LayoutParams borderTopLeftLayoutParams = (RelativeLayout.LayoutParams)borderTopLeft.getLayoutParams();
//        RelativeLayout.LayoutParams borderTopRightLayoutParams = (RelativeLayout.LayoutParams)borderTopRight.getLayoutParams();
//        RelativeLayout.LayoutParams borderBottomLeftLayoutParams = (RelativeLayout.LayoutParams)borderBottomLeft.getLayoutParams();
//        RelativeLayout.LayoutParams borderBottomRightLayoutParams = (RelativeLayout.LayoutParams)borderBottomRight.getLayoutParams();
//        float height = (screenWidthInPixels() - borderTopRightLayoutParams.rightMargin - borderTopLeftLayoutParams.leftMargin) * ASPECT_RATIO;
//        borderBottomLeftLayoutParams.bottomMargin = screenHeightInPixels() - Math.round(height) - borderTopLeftLayoutParams.topMargin;
//        borderBottomLeft.setLayoutParams(borderBottomLeftLayoutParams);
//        borderBottomRightLayoutParams.bottomMargin = screenHeightInPixels() - Math.round(height) - borderTopRightLayoutParams.topMargin;
//        borderBottomRight.setLayoutParams(borderBottomRightLayoutParams);
//    }

//    private int screenWidthInPixels() {
//        Point size = new Point();
//        getWindowManager().getDefaultDisplay().getSize(size);
//        return size.x;
//    }
//
//    private int screenHeightInPixels() {
//        Point size = new Point();
//        getWindowManager().getDefaultDisplay().getSize(size);
//        return size.y;
//    }

    private void createCaptureButton() {
        captureButton = new ImageButton(getApplicationContext());
        setBitmap(captureButton, "capture_button.png");
        captureButton.setBackgroundColor(Color.parseColor("#90226a"));
        captureButton.setScaleType(ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpToPixels(75));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //layoutParams.bottomMargin = dpToPixels(10);
        captureButton.setLayoutParams(layoutParams);
        captureButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setCaptureButtonImageForEvent(event);
                return false;
            }
        });
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        layout.addView(captureButton);
    }

    private void setCaptureButtonImageForEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setBitmap(captureButton, "capture_button_pressed.png");
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            setBitmap(captureButton, "capture_button.png");
        }
    }

    private void takePicture() {
        String focusMode = camera.getParameters().getFocusMode();
        if (focusMode == FOCUS_MODE_AUTO || focusMode == FOCUS_MODE_MACRO) {
            camera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    camera.takePicture(null, null, pictureCallback);
                }
            });
        } else {
            camera.takePicture(null, null, pictureCallback);
        }
    }

    private final PictureCallback pictureCallback = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] jpegData, Camera camera) {
            try {
                String filename = getIntent().getStringExtra(FILENAME);
                int quality = getIntent().getIntExtra(QUALITY, 80);
                File capturedImageFile = new File(getCacheDir(), filename);
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
                bitmap.compress(CompressFormat.JPEG, quality, new FileOutputStream(capturedImageFile));
                Intent data = new Intent();
                data.putExtra(IMAGE_URI, Uri.fromFile(capturedImageFile).toString());
                setResult(RESULT_OK, data);
                finish();
            } catch (Exception e) {
                finishWithError("Failed to save image");
            }
        }
    };

    private void finishWithError(String message) {
        Intent data = new Intent().putExtra(ERROR_MESSAGE, message);
        setResult(RESULT_CANCELED, data);
        finish();
    }

    private int dpToPixels(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

//    private boolean isXLargeScreen() {
//        int screenLayout = getResources().getConfiguration().screenLayout;
//        return (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE;
//    }
//
//    private boolean isLargeScreen() {
//        int screenLayout = getResources().getConfiguration().screenLayout;
//        return (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
//    }

    private void setBitmap(ImageView imageView, String imageName) {
        try {
            InputStream imageStream = getAssets().open("www/img/cameraoverlay/" + imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(bitmap);
            imageStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Could load image", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            camera = Camera.open();
        } catch (Exception e) {
            finishWithError("Camera is not accessible");
        }
        if (camera != null) {
            displayCameraPreview();
        } else {
            finishWithError("Could not display camera preview");
        }
    }

    private void displayCameraPreview() {
        cameraPreviewView.addView(new CustomCameraPreview(this, camera));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (camera != null) {
            camera.release();
        }
    }

}

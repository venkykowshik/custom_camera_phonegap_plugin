package com.performanceactive.plugins.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProductScreenActivity extends Activity {

	private FakeR fakeR;

	private ImageView backButton;
	private ImageView imgMain;
	private ImageView doneImg;

	private EditText title;
	private EditText price;
	private EditText desc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 

		fakeR = new FakeR(this);

		setContentView(fakeR.getId("layout", "activity_add_product"));

		imgMain = (ImageView) findViewById(fakeR.getId("id", "picture"));

		Bundle bundle = getIntent().getExtras();
		final String uriStr = bundle.getString(CustomCameraActivity.IMAGE_URI);
		imgMain.setImageURI(Uri.parse(uriStr));

		final LinearLayout back_view = (LinearLayout) findViewById(fakeR.getId("id", "bottom_view"));
		back_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent data = new Intent().putExtra(CustomCameraActivity.ERROR_MESSAGE, "User Closed View");
				setResult(RESULT_FIRST_USER, data);
				finish();
			}
		});

		backButton = (ImageView) findViewById(fakeR.getId("id", "backArrow"));
		backButton.setImageResource(getDrawable("exist"));
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finishWithError("User Closed View...");
			}
		});

		final ProgressDialog loading = new ProgressDialog(this);
		loading.setCancelable(false);
		loading.setMessage("Loading...");
		loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		title = (EditText) findViewById(fakeR.getId("id", "title"));
		price = (EditText) findViewById(fakeR.getId("id", "price"));
		desc = (EditText) findViewById(fakeR.getId("id", "desc"));

		doneImg = (ImageView) findViewById(fakeR.getId("id", "doneButton"));
		doneImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isConnectingToInternet()) {
					
					 loading.show();
					// WebServer Request URL
					String serverURL = "http://uaegwu.com/create.php?title=" + title.getText().toString() + "&description="
							+ desc.getText().toString() + "&price=" + price.getText().toString();
					
					
					byte[] inputData = null;
					try {
						inputData = readBytes(Uri.parse(uriStr));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String encodedImage = Base64.encodeToString(inputData, Base64.DEFAULT);
					
					try {
				        HttpClient client = new DefaultHttpClient();  
				        HttpPost post = new HttpPost(serverURL); 
				            List<NameValuePair> params = new ArrayList<NameValuePair>();
				            params.add(new BasicNameValuePair("title", title.getText().toString()));
				            params.add(new BasicNameValuePair("description", desc.getText().toString()));
				            params.add(new BasicNameValuePair("price", price.getText().toString()));
				            params.add(new BasicNameValuePair("img", encodedImage));
				            
				            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
				            post.setEntity(ent);
				            HttpResponse responsePOST = client.execute(post);  
				            HttpEntity resEntity = responsePOST.getEntity();  
				            if (resEntity != null) {    
				            	String response = EntityUtils.toString(resEntity);
				                Log.i("RESPONSE",response);
				                loading.hide();
				                Toast.makeText(ProductScreenActivity.this, response, Toast.LENGTH_LONG).show();
								Intent intent = new Intent();
								intent.putExtra(CustomCameraActivity.IMAGE_URI, uriStr);
								setResult(RESULT_OK, intent);
								finish();
				            } else {
				            	String response = EntityUtils.toString(resEntity);
				            	Log.i("RESPONSE",response);
				                loading.hide();
				            }
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
					
				} else {
					Toast.makeText(ProductScreenActivity.this, "No Internet", Toast.LENGTH_LONG).show();
				}
			}
		});

		TextView header = (TextView) findViewById(fakeR.getId("id", "titletxt"));
		Typeface face = Typeface.createFromAsset(this.getAssets(), "Fonts/ARIALUNI.TTF");
		header.setTypeface(face);

		TextView next = (TextView) findViewById(fakeR.getId("id", "next_text"));
		TextView back = (TextView) findViewById(fakeR.getId("id", "back_text"));

		next.setTypeface(face);
		back.setTypeface(face);

		

		title.setTypeface(face);
		price.setTypeface(face);
		desc.setTypeface(face);

	}

	public int getDrawable(String name) {
		return fakeR.getId("drawable", name);
	}

	private void finishWithError(String message) {
		Intent data = new Intent().putExtra(CustomCameraActivity.ERROR_MESSAGE, message);
		setResult(RESULT_CANCELED, data);
		finish();
	}

	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}

	

	public byte[] readBytes(Uri uri) throws IOException {
		// this dynamically extends to take the bytes you read
		InputStream inputStream = getContentResolver().openInputStream(uri);
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// we need to know how may bytes were read to write them to the
		// byteBuffer
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// and then we can return your byte array.
		return byteBuffer.toByteArray();
	}

}

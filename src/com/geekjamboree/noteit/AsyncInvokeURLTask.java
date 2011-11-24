/**
 * 
 */
package com.geekjamboree.noteit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author mgupta
 *
 */
public class AsyncInvokeURLTask extends AsyncTask<Void, Void, String> {
	private final String 				mNoteItWebUrl = "http://10.0.2.2/noteit.web/controller/AppController.php";
	private ArrayList<NameValuePair> 	mParams;
	private	OnPostExecuteListener		mPostExecuteListener = null;
	
	public static interface OnPostExecuteListener{
		void onPostExecute(JSONObject result);
	}

	AsyncInvokeURLTask(
					ArrayList<NameValuePair> nameValuePairs, 
					OnPostExecuteListener postExecuteListener) throws Exception {
		mParams = nameValuePairs;
		mPostExecuteListener = postExecuteListener;
		if (mPostExecuteListener == null)
			throw new Exception("Param postExecuteListener cannot be null.");
	}
	
	@Override
	protected String doInBackground(Void... params) {
	    String result = "";
	    
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(mNoteItWebUrl);
	    try {
	        // Add parameters
	        httppost.setEntity(new UrlEncodedFormEntity(mParams));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        if (entity != null){
	        	InputStream inStream = entity.getContent();
	        	result = convertStreamToString(inStream);
	    		Log.i("AsyncInvokeUTRTask", "Got back response: " + result);
	        }
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    	Log.e("AsyncInvokeURLTask.doInBackground", e.getMessage());
			e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    	Log.e("AsyncInvokeURLTask.doInBackground", e.getMessage());
			e.printStackTrace();
	    } catch (Exception e) {
	    	Log.e("AsyncInvokeURLTask.doInBackground", "Unknown exception: " + e.getMessage());
	    	e.printStackTrace();
	    }
		
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		if (mPostExecuteListener != null){
			try {
	        	JSONObject json = new JSONObject(result);
				mPostExecuteListener.onPostExecute(json);
			} catch (JSONException e){
				Log.e("AsyncInvokeURLTask.onPostExecute", e.getMessage());
				JSONObject json = new JSONObject();
				try {
					json.put("JSONRetVal", -1);
					json.put("JSONRetMessage", e.getMessage());
				} catch (JSONException newE) {
					
				}
				mPostExecuteListener.onPostExecute(json);
			}
		}
	}

	private static String convertStreamToString(InputStream is){
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
} // AsyncInvokeURLTask
/**
 * 
 */
package com.geekjamboree.noteit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 	@author mgupta
 *	
 * 	This code is not very secure when using https as we're ignoring all certificates, 
 *  suffices for the current purpose of using google api for search, but we should definitely revisit
 */
class AsyncInvokeURLTask extends AsyncTask<Void, Void, String> {

	private String 						mNoteItWebUrl;
	private ArrayList<NameValuePair> 	mParams;
	private	OnPostExecuteListener		mPostExecuteListener = null;
	private RequestMethod				mRequestMethod = RequestMethod.POST;
	
	public static interface OnPostExecuteListener{
		void onPostExecute(JSONObject result);
	}

	enum RequestMethod {
		POST,
		GET
	}
	
	AsyncInvokeURLTask(
		ArrayList<NameValuePair> nameValuePairs, 
		OnPostExecuteListener postExecuteListener) throws Exception {
		
		mParams = nameValuePairs;
		mPostExecuteListener = postExecuteListener;
		mRequestMethod = RequestMethod.POST;
		mNoteItWebUrl = getNoteItURL(); 
		if (mPostExecuteListener == null)
			throw new Exception("Param postExecuteListener cannot be null.");
	}
	
	AsyncInvokeURLTask(
			RequestMethod method,
			String url,
			ArrayList<NameValuePair> nameValuePairs, 
			OnPostExecuteListener postExecuteListener) throws Exception {

		mRequestMethod = method;
		mParams = nameValuePairs;
		mPostExecuteListener = postExecuteListener;
		if (mPostExecuteListener == null)
			throw new Exception("Param postExecuteListener cannot be null.");
		mNoteItWebUrl = url.equals("") ? getNoteItURL() : url;
	}

	@SuppressWarnings("unused")
	private String getNoteItURL() {
		final String URL_HOST_LOOPBACK 	= "http://10.0.2.2/noteit.web/controller/appcontroller.php";
		final String URL_GEEKJAM 		= "http://geekjamboree.com/controller/appcontroller.php"; 
		final String URL_IMAC 			= "http://192.168.1.20/noteit.web/controller/appcontroller.php";
		String url 						= URL_IMAC;
		if(("sdk".equals(Build.PRODUCT)) || ("google_sdk".equals(Build.PRODUCT)))
			// We're running in the emulator connect with host loopback
			url = URL_HOST_LOOPBACK;
		else
			url = URL_GEEKJAM; //URL_GEEKJAM;
		return url;
	}
	protected String doGetInBackground(Void...params) {
	    
		String 				result = "";
		HttpURLConnection 	http = null;
		URL 				url;
	  
		try {
			String data = URLEncodedUtils.format(mParams, HTTP.UTF_8);
			url = new URL(mNoteItWebUrl + "?" + data);
	        
			if (url.getProtocol().toLowerCase().equals("https")) {
			    trustAllHosts();
			    HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			    https.setHostnameVerifier(DO_NOT_VERIFY);
			    http = https;
	        } else {
	        	http = (HttpURLConnection) url.openConnection();
	        }
		
			http.setRequestMethod(HttpGet.METHOD_NAME);
		    int rc = http.getResponseCode();
		    if (rc == HttpURLConnection.HTTP_OK) {
		    	result = convertStreamToString(http.getInputStream());
		    } else {
		    	result = getJSONFormattedErrorString(rc, http.getResponseMessage());
		    }
		} 
		catch (MalformedURLException e1) {
			e1.printStackTrace();
	    	result = getJSONFormattedErrorString(-1, e1.getMessage());
		} catch (IOException e2) {
			e2.printStackTrace();
	    	result = getJSONFormattedErrorString(-1, e2.getMessage());
		}

//		The code below works beautifully in Android 2.2 and above for both
//		http and https. Unfortunately, at this time, we need to support 2.1 also
		
//		AndroidHttpClient 	httpclient = AndroidHttpClient.newInstance("Mozilla/4.0");
//    	String 				urlParams = URLEncodedUtils.format(mParams, HTTP.UTF_8);
//      HttpGet 			method = new HttpGet(mNoteItWebUrl + "?" + urlParams);
//        
//	    try {
//	        // Add parameters
//	        HttpResponse 	response = httpclient.execute(method);
//	        HttpEntity 		entity = response.getEntity();
//	        
//	        if (entity != null){
//	        	InputStream inStream = entity.getContent();
//	        	result = convertStreamToString(inStream);
//	    		Log.i("AsyncInvokeUTRTask", "Got back response: " + result);
//	        }
//	        
//	    } catch (ClientProtocolException e) {
//	    	Log.e("AsyncInvokeURLTask.doInBackground", e.getMessage());
//			e.printStackTrace();
//	    } catch (IOException e) {
//	    	Log.e("AsyncInvokeURLTask.doInBackground", e.getMessage());
//			e.printStackTrace();
//	    } catch (Exception e) {
//	    	Log.e("AsyncInvokeURLTask.doInBackground", "Unknown exception: " + e.getMessage());
//	    	e.printStackTrace();
//	    }
//		
//	    httpclient.close();
		return result;
	}
	
	protected String doPostInBackground(Void...params) {
        
		String 				result = "";
//		HttpURLConnection 	http = null;
//		URL 				url;
//		  
//		try {
//			url = new URL(mNoteItWebUrl);
//			if (url.getProtocol().toLowerCase().equals("https")) {
//				trustAllHosts();
//				HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
//				https.setHostnameVerifier(DO_NOT_VERIFY);
//				http = https;
//			} else {
//			    http = (HttpURLConnection) url.openConnection();
//			}
//		
//			String data = URLEncodedUtils.format(mParams, HTTP.UTF_8);
//			http.setDoOutput(true);
//			http.setRequestMethod(HttpPost.METHOD_NAME);
//			http.setRequestProperty("User-Agent", "Mozilla/4.0");
//			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			http.setRequestProperty("Content-Length", String.valueOf(data.length()));
//		    OutputStreamWriter wr = new OutputStreamWriter(http.getOutputStream());
//		    wr.write(data);
//		    wr.flush();
//		
////		    int rc = http.getResponseCode();
////		    if (rc == HttpURLConnection.HTTP_OK) {
//		    	result = convertStreamToString(http.getInputStream());
////		    } else {
////		    	Log.e("AysncInvokeURLTask.doPostInBackground", "POST Failed with return code: " + rc);
////		    	result = getJSONFormattedErrorString(rc, http.getResponseMessage());
////		    }
//		} 
//		catch (MalformedURLException e1) {
//			e1.printStackTrace();
//	    	result = getJSONFormattedErrorString(-1, e1.getMessage());
//		} catch (IOException e2) {
//			e2.printStackTrace();
//	    	result = getJSONFormattedErrorString(-1, e2.getMessage());
//		}
		
//		AndroidHttpClient	httpclient = new DefaultHttpClient();
		HttpClient			httpclient = new DefaultHttpClient();
		HttpPost 			method = new HttpPost(mNoteItWebUrl);
        
	    try {
	        // Add parameters
	    	method.setEntity(new UrlEncodedFormEntity(mParams, HTTP.UTF_8));

	        // Execute HTTP Post Request
	        HttpResponse 	response = httpclient.execute(method);
	        HttpEntity 		entity = response.getEntity();
	        if (entity != null){
	        	InputStream inStream = entity.getContent();
	        	result = convertStreamToString(inStream);
	    		Log.i("AsyncInvokeUTRTask", "Got back response: " + result);
	        }
	    } catch (ClientProtocolException e) {
	    	Log.e("AsyncInvokeURLTask.doInBackground", e.getMessage());
			e.printStackTrace();
	    } catch (IOException e) {
	    	Log.e("AsyncInvokeURLTask.doInBackground", e.getMessage());
			e.printStackTrace();
	    } catch (Exception e) {
	    	Log.e("AsyncInvokeURLTask.doInBackground", "Unknown exception: " + e.getMessage());
	    	e.printStackTrace();
	    }
		
		return result;
	}
	
	@Override
	protected String doInBackground(Void... params) {

		return mRequestMethod == RequestMethod.POST ? 
				doPostInBackground(params) : 
				doGetInBackground(params);
	}

	@Override
	protected void onPostExecute(String result) {
		
		if (mPostExecuteListener != null){
			try {
	        	JSONObject json = new JSONObject(result);
				mPostExecuteListener.onPostExecute(json);
			} catch (JSONException e){
				// If we have a json exception here, most likely
				// means there's a huge error on the server side.
				// send back an appropriate message
				JSONObject json = new JSONObject();
				try {
					json.put("JSONRetVal", -1);
					json.put("JSONRetMessage", "There was an error connecting to the server. Please try later");
				} catch (JSONException newE) {
					
				}
				mPostExecuteListener.onPostExecute(json);
			}
		}
	}

	private static String convertStreamToString(InputStream is){
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(is, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e1) {
			Log.e("AsyncInvokeURLTask.convertStreamToString", "UnsupportedEncodingException");
			return "";
		}
		
		StringBuilder sb = new StringBuilder();

		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			Log.e("AsyncInvokeURLTask.convertStreamToString", "IOException");
			return "";
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				Log.e("AsyncInvokeURLTask.convertStreamToString", "IOException");
				return "";
			}
		}
		return sb.toString();
	}
	
	// Https related stuff
	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - don't check for any certificate
	 */
	private static void trustAllHosts() {

		// Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            	return new java.security.cert.X509Certificate[] {};
            }

			public void checkClientTrusted(X509Certificate[] chain,
				String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
				String authType) throws CertificateException {
			}
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private String getJSONFormattedErrorString(long result, String message) {
    	
		JSONObject errorObj = new JSONObject();
    	try {
			errorObj.put("JSONRetVal", result);
	    	errorObj.put("JSONRetMessage", message != null ? message : "Error description unavailable.");
	    	return errorObj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return "";
	}
	
} // AsyncInvokeURLTask
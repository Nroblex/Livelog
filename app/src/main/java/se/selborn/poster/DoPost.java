package se.selborn.poster;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by anders on 1/21/16.
 */
public class DoPost extends AsyncTask<String, Void, String>{


    private static final String TAG = "DOPOST";




    public static void PostMe(String aJsonString){
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost("http://localhost:8080/pos/position.php");
        try {
            StringEntity params = new StringEntity("json=" + aJsonString);

            request.addHeader("Content-Type", "application/json");
            request.setEntity(params);
            HttpResponse response=httpClient.execute(request);

            System.out.println(org.apache.http.util.EntityUtils.toString(response.getEntity()));



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void PostMe(){




    }

    @Override
    protected String doInBackground(String... params) {

        JSONObject jsonObject = new JSONObject();
        JSONArray arr = new JSONArray();


        try {
            jsonObject.put("latitude", "57.09002");
            jsonObject.put("longitude", "11.29002");
            jsonObject.put("speed", "47");
            jsonObject.put("altitude", "101");

            arr.put(jsonObject);

            HttpParams httpParams = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 5000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost("http://localhost:8080/pos/saveme.php");
            httpPost.setHeader("json",arr.toString());
            httpPost.getParams().setParameter("jsonpost",arr);

            try {
                HttpResponse response = client.execute(httpPost);
                String resFromServer = org.apache.http.util.EntityUtils.toString(response.getEntity());


                Log.e(TAG, "ReplyFromServer: " + resFromServer);


            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }


        return "";
    }
}

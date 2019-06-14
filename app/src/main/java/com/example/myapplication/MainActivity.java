package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import ai.api.android.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceContextBuilder;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    ImageButton btnspeak;
    TextView txtSpeechInput, outputSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnspeak = (ImageButton) findViewById(R.id.btnSpeak);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        outputSpeech = (TextView) findViewById(R.id.outputText);

        btnspeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                promptSpeechInput(); //form implicit intent
            }
        });
    }

    //showing google speech input dialog box

    private void promptSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Your device doesn't support input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userQuery = result.get(0);
                    txtSpeechInput.setText(userQuery);
                    RetrieverFeedTask task= new RetrieverFeedTask();
                    task.execute(userQuery);
                }
                break;
            }
        }
    }

    //Send GET data request
    //create getText Method

    public String GetText(String query) throws UnsupportedEncodingException {

        String text = "";
        BufferedReader reader = null;

        //send data

        try {
            //define URL

            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");

            //send POST data request
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestProperty("Authorization", "Bearer 155c80f6875945fe8e6a91e6e1d5ba25");
            conn.setRequestProperty("Content-Type", "application/json");

            //Create JSON objct here

            JSONObject jsonParam = new JSONObject();
            JSONArray queryArray = new JSONArray();
            queryArray.put(query);
            jsonParam.put("query", queryArray);
            jsonParam.put("lang", "en");
            jsonParam.put("sessionId", "1234567890");

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            Log.d("karma", "after conversion is " + jsonParam.toString());
            wr.write(jsonParam.toString());
            wr.flush();
            Log.d("karma", "json is " + jsonParam);

            //get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            //read the server  response
            while ((line = reader.readLine()) != null) {
                //append server response in string

                sb.append(line + "\n"); //data returned by our agent
            }

            text = sb.toString();
            JSONObject object1= new JSONObject(text);
            JSONObject object= object1.getJSONObject("result");
            JSONObject fulfillment = null;
            String speech= null;
  //          if (object.has("fulfillment")) {
                fulfillment = object.getJSONObject("fulfillment");
//                if (fulfillment.has("speech")) {
                speech = fulfillment.optString("speech");
//                }
//            }

            Log.d("karma ", "response is " + text);
            return speech;

        }

        catch (Exception ex) {

            Log.d("Karma", "exception at last"+ ex);
        }

        finally {
            try {
                reader.close();
            }
            catch (Exception ex){

            }
        }

        return null;
    }

    class RetrieverFeedTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... voids) {
            String s= null;

            try{
                s= GetText(voids[0]);
            }
            catch (UnsupportedEncodingException e){
                e.printStackTrace();
                Log.d("Karma", "Exception Occured " + e);
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            outputSpeech.setText(s);
        }
    }
}

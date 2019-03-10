package com.example.equationsolver;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetEquationTask extends AsyncTask<String, Void, Equation> {

    Context context;
    String imgUrl;
    public static final String TAG = "Equation Task";

    public GetEquationTask(Context context, String imgUrl) {
        this.context = context;
        this.imgUrl = imgUrl;
    }

    @Override
    protected Equation doInBackground(String... strings) {
        URL url = null;
        Equation equation = null;
        try {
            url = new URL(strings[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection;
        try {
            // Send a POST request
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(imgUrl);
            dataOutputStream.flush();
            dataOutputStream.close();
            Log.d(TAG, "Request: " + imgUrl);

            // Get the server response
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            String line;
            do {
                line = bufferedReader.readLine();
                builder.append(line);
            } while (line != null);

            JSONObject jsonObject = new JSONObject(builder.toString());
            equation = new Equation(
                    jsonObject.getString("equation"),
                    jsonObject.getInt("result")
            );

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return equation;
    }

    @Override
    protected void onPostExecute(Equation equation) {
        super.onPostExecute(equation);
        if(equation != null) {
            Toast.makeText(context, "Equation: " + equation.getEquation(), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Equation is null", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.equationsolver;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
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

    private Context context;
    private String imgUrl;
    private TextView tvToChange;
    private ProgressDialog dialog;

    public GetEquationTask(Context context, String imgUrl, TextView tvToChange) {
        this.context = context;
        this.imgUrl = imgUrl;
        this.tvToChange = tvToChange;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Recognising Equation...");
        dialog.show();
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
            connection.setRequestProperty("Content-Type", "application/json; UTF-8");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(imgUrl);
            dataOutputStream.flush();
            dataOutputStream.close();

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
            tvToChange.setText(equation.getEquation());
        }
        else {
            Toast.makeText(context, "Please Try Again", Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }
}

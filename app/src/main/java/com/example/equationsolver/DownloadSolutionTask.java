package com.example.equationsolver;

import android.app.ProgressDialog;
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

public class DownloadSolutionTask extends AsyncTask<String, Void, Solution> {

    Context context;
    String equationUrl;
    private ProgressDialog dialog;
    public static final String TAG = "TASK";

    public interface OnDownloadSolution {
        void getSolution(Solution result);
    }

    OnDownloadSolution onDownloadSolution;

    public DownloadSolutionTask(Context context, String equationUrl, OnDownloadSolution onDownloadSolution) {
        this.context = context;
        this.equationUrl = equationUrl;
        this.onDownloadSolution = onDownloadSolution;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Calculating...");
        dialog.show();
    }

    @Override
    protected Solution doInBackground(String... strings) {
        URL url = null;
        Solution solution = null;
        try {
            url = new URL(strings[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection;
        try {
            // Send the POST request
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            Log.d(TAG, "Task Url: " + equationUrl);
            dataOutputStream.writeBytes(equationUrl);
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
            if(jsonObject.has("graph")) {
                solution = new Solution(
                        jsonObject.getBoolean("success"),
                        jsonObject.getString("solution"),
                        jsonObject.getString("graph")
                );
            }
            else {
                solution = new Solution(
                        jsonObject.getBoolean("success"),
                        jsonObject.getString("solution"),
                        null
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return solution;
    }

    @Override
    protected void onPostExecute(Solution solution) {
        super.onPostExecute(solution);
        if(solution != null && solution.isSuccess() && onDownloadSolution != null) {
            onDownloadSolution.getSolution(solution);
        }
        else {
            Toast.makeText(context, "Problem occurred while calculating", Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }
}

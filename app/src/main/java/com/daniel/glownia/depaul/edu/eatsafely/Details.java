package com.daniel.glownia.depaul.edu.eatsafely;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.net.Socket;


public class Details extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String latlong;
        Serializable file = getIntent().getSerializableExtra("loc");
        latlong = (String) file;

        Log.d("Details onCreate ", file.toString());
        Log.d("Details onCreate ", "Executing the Async");

        new GetDetails().execute(latlong);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetDetails extends AsyncTask<String,String,String>{
        TextView restaurant, address, result, date, comments, result_label, comments_label;
        @Override
        protected String doInBackground(String... strings) {
            try{
                Log.d("Details doInBackground() ", "started");
                int count = strings.length;
                String coordinates = strings[0];
                String input;
                Socket client_socket;

                Log.d("Details doInBackground() ", "about to connect to socket");
                client_socket = new Socket("207.181.245.105", 7000);

                //write coordinates
                Log.d("MapsActivity doInBackground ", "About to write data");
                DataOutputStream dataOutput = new DataOutputStream(client_socket.getOutputStream());
                dataOutput.writeUTF("-r");
                dataOutput.writeUTF(coordinates);
                //read coordinates
                Log.d("MapsActivity doInBackground ", "About to read data");
                DataInputStream dataInput = new DataInputStream(client_socket.getInputStream());
                input = dataInput.readUTF();
                client_socket.close();
                //Log.d("MapsActivity doInBackground ", input);

                return input;
            }
            catch (Exception e) {
                return "Exception Returned";
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d("Details onPreExecute() ", "started");
            restaurant = (TextView) findViewById(R.id.res_name);
            address = (TextView) findViewById(R.id.address);
            result = (TextView) findViewById(R.id.results);
            result_label = (TextView) findViewById(R.id.results_label);
            date = (TextView) findViewById(R.id.date);
            comments = (TextView) findViewById(R.id.comments_view);
            comments_label = (TextView) findViewById(R.id.comments);

            Context context = getApplicationContext();
            CharSequence text = "Loading...";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("Details onPostExecute() ", s);
            String splitBy = ",";

            String[] restaurantInfo = s.split(splitBy);

            restaurant.setText(restaurantInfo[0]);
            address.setText(restaurantInfo[1]);
            result.setText(restaurantInfo[2].toUpperCase());
            result_label.setText("Results");
            date.setText(restaurantInfo[3]);
            comments.setText(restaurantInfo[4]);
            comments_label.setText("Comments");

            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}

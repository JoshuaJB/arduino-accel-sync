package me.jbakita.arduinodatalogging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataLogReceiver;
import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanManager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.ArrayList;

/**
 * MainActivity class
 * Implements a PebbleDataLogReceiver to process received log data, 
 * as well as a finished session.
 */
public class MainActivity extends Activity implements BeanDiscovererListener {

    private BeanDiscoverer discoverer;

    private String[] features = {
        "DOMINANT_WRIST",
        "NON_DOMINATE_WRIST",
        "WAIST",
        "RIGHT_ANKLE",
        "LEFT_ANKLE",
        "UPPER_DOMINATE_ARM",
        "UPPER_NON_DOMINATE_ARM",
        "RIGHT_THIGH",
        "LEFT_THIGH",
        "CHEST",
        "NECK"
    };
    private String[] activityStrings = {"Pushups", "Situps", "Jumping Jacks", "Stretching", "Running", "Walking"};

    private final ArrayList<Sensor> sensors = new ArrayList<Sensor>();
    private final ArrayList<MotionActivity> activities = new ArrayList<MotionActivity>();
    private ArrayAdapter<Sensor> adapter;
    private Button startStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startDiscovery();
        // Get listview
        ListView sensorsView = (ListView)findViewById(R.id.listView);
        // Setup progress bar
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        sensorsView.setEmptyView(progressBar);

        // Setup data adapter
        adapter = new ArrayAdapter<Sensor>(this, android.R.layout.simple_list_item_2, android.R.id.text1, sensors) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(sensors.get(position).getTitle());
                text2.setText(sensors.get(position).getInfo());
                return view;
            }
        };

        // Add listview display adapter
        sensorsView.setAdapter(adapter);

        // Setup start/stop button
        startStopButton = (Button)findViewById(R.id.startstopbutton);
        startStopButton.setOnClickListener(new startStopListener());
        startStopButton.setText("Start");

        // Setup save button
        Button saveButton = (Button)findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new saveListener());
        saveButton.setText("Save");

        startDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDiscovery();
    }
    private void startDiscovery(){
        discoverer = new BeanDiscoverer();
        discoverer.addListenter(this);

        if(!BeanManager.getInstance().startDiscovery(discoverer)){
            Log.w("MainActivity", "Bluetooth stack was unable to start scanning.");
            displayDialog("Bluetooth Stack Error", "Unable to search for Beans.");
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getMotionActivity(final MotionActivity act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What activity did you complete?")
                .setItems(activityStrings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        act.name = activityStrings[which];
                    }
                });
        builder.create().show();

    }

    private void finishAndSaveReading() {
        for (int i = 0; i < activities.size(); i++) {
            for (int j = 0; j < sensors.size(); j++) {
                ArrayList<AccelerometerReading> readings = sensors.get(j).getReadings();
                // TODO: Handle missing/unavailable external storage
                try {
                    long lastReading = 0;
                    long firstReading = 0;
                    // Get/create our application's save folder
                    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/PebbleDataLogging/");
                    dir.mkdir();
                    // Create the file in the <activity name>-<sensor name>-<system time>.csv format
                    File file = new File(dir, activities.get(i).name + " " + features[j] + " " + DateFormat.getDateTimeInstance().format(new Date()) + ".csv");
                    FileOutputStream outputStream = new FileOutputStream(file);
                    // Write the colunm headers
                    outputStream.write("X(mG),Y(mG),Z(mG),Time(ms)\n".getBytes());
                    // Write all the readings which correlate to our current activity
                    for (int k = 0; k < readings.size(); k++) {
                        if (readings.get(k).getTimestamp() >= activities.get(i).startTime && readings.get(k).getTimestamp() < activities.get(i).endTime) {
                            if (firstReading == 0)
                                firstReading = readings.get(k).getTimestamp();
                            outputStream.write(String.format(Locale.US, "%+5d,%+5d,%+5d,%14d\n", readings.get(k).getX(), readings.get(k).getY(), readings.get(k).getZ(), readings.get(k).getTimestamp()).getBytes());
                            lastReading = readings.get(k).getTimestamp();
                        }
                    }
                    // Do some validation on the dataset
                    if (lastReading + 1000 < activities.get(i).endTime) {
                        displayDialog("Warning!", "It seems like the dataset you just saved stopped sooner than expected. Make sure that you have all your sensor data.");
                    }
                    else if (firstReading - 1000 > activities.get(i).startTime) {
                        displayDialog("Warning!", "It seems like the dataset you just saved started later than expected. Make sure that you have all your sensor data.");
                    }
                    outputStream.close();
                    // Workaround for Android bug #38282
                    MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
                } catch (Exception e) {e.printStackTrace();}
            }
        }
        Log.w("MainActivity", sensors.toString());
    }

    private AlertDialog displayDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title);
        AlertDialog dia = builder.create();
        dia.show();
        return dia;
    }

    private class Sensor {
        private String name;
        private long lastTimestamp = 0;
        private ArrayList<AccelerometerReading> readings = new ArrayList<AccelerometerReading>();
        private ArrayList<AccelerometerReading> readingBuffer = new ArrayList<AccelerometerReading>();
        /* Initialize the sensor with a name. Setting the sample rate, and start time are required before adding readings.
         * @param name The sensor name, used only for display
         * @param timestamp ms since POSIX epoch at which this sensor started
         */
        public Sensor(String name, long timestamp) {
            this.name = name;
            this.lastTimestamp = timestamp;
        }
        /* Add a sequential accelerometer reading. The time is automatically calculated.
         * @param r the reading to add
         * @throws UnsupportedOperationException if the sample rate and beginning timestamp
         *                                       have yet to be set.
         */
        public void addReading(AccelerometerReading r) {
            // Check that everything is setup
            if (lastTimestamp == 0)
                throw new UnsupportedOperationException("No starting timestamp set on sensor");
            // Log the reading (we add timestamps later)
            readingBuffer.add(r);
        }
        public String getTitle() {
            return name;
        }
        public String getInfo() {
            return readings.size() + " readings taken over " + getDuration() / 60000 + "m " + getDuration() % 60000 / 1000 + "s " + getDuration() % 60000 % 1000 + "ms";
        }
        /* Get the duration of time that this sensor has been monitoring in ms
         */
        public long getDuration() {
            if (readings.isEmpty())
                return 0;
            return readings.get(readings.size() - 1).getTimestamp() - readings.get(0).getTimestamp();
        }
        public void addTimestamp(long t) {
            if (readingBuffer.isEmpty())
                throw new UnsupportedOperationException("No readings in buffer. Cannot add timestamp.");
            long dur = t - lastTimestamp;
            double readingSize = dur / (double)(readingBuffer.size() + 1);
            AccelerometerReading reading0 = readingBuffer.remove(0);
            reading0.setTimestamp(lastTimestamp);
            readings.add(reading0);
            for (AccelerometerReading r : readingBuffer) {
                r.setTimestamp(lastTimestamp += readingSize);
                readings.add(r);
            }
            lastTimestamp = t;
            readingBuffer.clear();
        }
        public ArrayList<AccelerometerReading> getReadings() {
            return readings;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String)
                return name.equals(obj);
            else if (obj instanceof Sensor)
                if (name.equals(((Sensor) obj).getTitle()))
                    return true;
            return false;
        }
        @Override
        public String toString() {
            return readings.toString();
        }
    }

    private class MotionActivity {
        public long startTime = -1;
        public long endTime = -1;
        public String name = "";
        public MotionActivity(long startTime) {
            this.startTime = startTime;
        }
        public boolean isFinished() {
            return startTime != -1 && endTime != -1;
        }

    }
    private class startStopListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (activities.isEmpty() || activities.get(activities.size() - 1).isFinished()) {
                // Start recording
                startStopButton.setText("Stop");
                activities.add(new MotionActivity(System.currentTimeMillis() + TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings()));
            }
            else {
                // End recording
                startStopButton.setText("Start");
                activities.get(activities.size() - 1).endTime = System.currentTimeMillis() + TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
                getMotionActivity(activities.get(activities.size() - 1));
            }
        }
    }
    private class saveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finishAndSaveReading();
        }
    }

    /**
     * Called when discoverer finishes scanning for Beans
     */
    public void onDiscoveryComplete() {
        Bean[] beans = discoverer.getDiscovered();
        if (beans.length == 0) {
            Log.i("MainActivity", "No Arduino devices discovered.");
            displayDialog("Warning", "No Arduino devices discovered.");
        }
        else {
            String[] beansString = new String[beans.length];
            for (int i = 0; i < beans.length; i++) {
                beansString[i] = beans[i].getDevice().getName() + " (" + beans[i].getDevice().getAddress() + ")";
            }
            chooseBean(beansString, beans, this);
        }
    }

    /**
     * Displays the passed list of Beans to the user and hes them choose which one to connect to
     * @param beansString A stringified list of Beans
     * @param beans Array of Bean objects corresponding to parallel indices in the beansString array
     * @param context What page we're displaying the dialog over
     */
    private void chooseBean(final String[] beansString, final Bean[] beans, final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Which bean do you want to connect to?")
                .setItems(beansString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("MainActivity", "Connecting...");
                        beans[which].connect(context, new SerialBytestreamListener());
                    }
                });
        builder.create().show();
    }
}

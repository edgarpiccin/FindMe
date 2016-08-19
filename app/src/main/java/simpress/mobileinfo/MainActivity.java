package simpress.mobileinfo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Time;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {

    private static String imeiString;
    private DatabaseHelper helper;
    public TextView provider, latitude, longitude;
    private String latitudeStr, longitudeStr, providerStr;
    public Location mLastLocation;
    private Boolean isGPSFix;
    private long mLastLocationMillis = 5000;
    int i = 0;
    boolean hasLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "APP oculto onCreate.", Toast.LENGTH_SHORT).show();


        minimizeApp();

/*
            PackageManager p = getPackageManager();
            ComponentName componentName = new ComponentName(this, MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
            if (p.getComponentEnabledSetting(componentName) == 0)
                p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);


*/

/*
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        Calendar c = Calendar.getInstance();
        int a = c.get(c.HOUR);

        provider = (TextView) findViewById(R.id.provedor);
        provider.setText(String.valueOf(a));
        */

        //getLocation();
        //hiddenApp();
/*
        //reapresenta o icone
        p = getPackageManager();
        componentName = new ComponentName(this, com.example.edgarpiccin.silentmode.MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
*/
    }

    public static class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }

    private void minimizeApp() {
        //minimiza o app
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
/*
        startMain.setClassName(getBaseContext(), "MainActivity");

        //esconde o icone

        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, startMain.getClass()); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        if (p.getComponentEnabledSetting(componentName) == 0)
            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        Toast.makeText(this, String.valueOf(p.getComponentEnabledSetting(componentName)), Toast.LENGTH_SHORT).show();
        */
    }

    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "APP oculto onStart.", Toast.LENGTH_SHORT).show();
        //runThread();
    }

    protected void onResume() {
        super.onResume();
        getLocation();
        Toast.makeText(this, "APP oculto onResume.", Toast.LENGTH_SHORT).show();
        //runThread();
    }

    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
        //runThread();
    }

    private void requestAllPermissions() {
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE))
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void getLocation() {
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        provider = (TextView) findViewById(R.id.provedor);

        long tempoAtualizacao = 0;
        float distancia = 0;

        LocationManager locationManager;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Listener listener = new Listener();

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tempoAtualizacao, distancia, listener);
        } else
            requestAllPermissions();
    }

    private void getIMEI() {
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tel.getPhoneCount() > 1)
            imeiString = tel.getDeviceId(0);
        else
            imeiString = tel.getDeviceId();
    }

    private void saveLocation() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
        String dataHora = sdf.format(new Date());

        values.put("imei", imeiString);
        values.put("latitude", latitudeStr);
        values.put("longitude", longitudeStr);
        values.put("data", dataHora);//"%Y-%m-%d %H:%M:%S"));

        db.insert("ImeiLocationHistory", null, values);
    }

    private void dropLocation() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("ImeiLocationHistory", "", null);
    }

    private String generateJson(ImeiLocation imei) {
        String json = "{" + "\"" + "IMEI" + "\"" + ":" + "\"" + imei._imei + "\"" + "," + "\"" + "Latitude" + "\"" + ":" + "\"" + imei._latitude + "\"" + "," + "\"" + "Longitude" + "\"" + ":" + "\"" + imei._longitude + "\"" + "," + "\"" + "Data_Localizacao" + "\"" + ":" + "\"" + imei._data.toString() + "\"}";
        return json;
    }

    private ArrayList<ImeiLocation> getSavedLocation() {
        SQLiteDatabase db1 = helper.getReadableDatabase();

        ArrayList<ImeiLocation> itensImei = new ArrayList<ImeiLocation>();

        Cursor cursor = db1.rawQuery("select _id, imei, latitude, longitude, data from ImeiLocationHistory", null);
        cursor.moveToFirst();

        if (cursor.getCount() >= 1) {
            Toast.makeText(this, "IMEI: " + cursor.getString(0), Toast.LENGTH_SHORT).show();

            for (int i = 0; i < cursor.getCount(); i++) {
                ImeiLocation item = new ImeiLocation(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));

                itensImei.add(item);

                cursor.moveToNext();
            }
        }

        cursor.close();
        return itensImei;
    }

    public static boolean Conectado(Context context) {

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        String LogSync = null;
        String LogToUserTitle = null;

        Network[] a = cm.getAllNetworks();
        NetworkInfo networkInfo;
        boolean x = false;

        for (Network myNet : a) {
            networkInfo = cm.getNetworkInfo(myNet);
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                x = true;
            } else {
                x = false;
            }
        }
        return x;

    }

    public boolean checkPermission(String permission) {
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getIMEI();
                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class Listener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            //Toast.makeText(getBaseContext(), "onLocationChanged", Toast.LENGTH_SHORT).show();

/*
            PackageManager p = getApplicationContext().getPackageManager();
            p.setComponentEnabledSetting(getComponentName(),PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
*/

            Calendar c = Calendar.getInstance();
            int hora = c.get(c.HOUR);
            int diaSemana = c.get(c.DAY_OF_WEEK);
            int minuto = c.get(c.MINUTE);
            int segundo = c.get(c.SECOND);

            i++;

            if (hora >= 7 && hora <= 18 && (minuto % 2 == 0) && segundo == 0)
            //(hora >= 7 && hora <= 18 && diaSemana > 1 && diaSemana < 7 && (minuto == 0 || minuto == 30))
            {
                try {
                    latitudeStr = String.valueOf(location.getLatitude());
                    longitudeStr = String.valueOf(location.getLongitude());
                    providerStr = String.valueOf(location.getProvider());
                    hasLocation = true;

                    //if (location == null) return;
                    mLastLocationMillis = SystemClock.elapsedRealtime();
                    mLastLocation = location;
                    isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 3000;

                    provider.setText(providerStr);
                    latitude.setText(latitudeStr);
                    longitude.setText(longitudeStr);

                    if (hasLocation) {
                        if (checkPermission("android.permission.READ_PHONE_STATE")) {
                            getIMEI();
                            TextView txtImei = (TextView) findViewById(R.id.imei);
                            txtImei.setText(imeiString);

                            helper = new DatabaseHelper(getBaseContext());
                            saveLocation();

                            if (Conectado(getBaseContext())) {
                                ArrayList<ImeiLocation> itensImei = getSavedLocation();

                                if (itensImei.size() > 0) {
                                    String jsonItem = "";
                                    for (ImeiLocation item : itensImei) {
                                        jsonItem = jsonItem + generateJson(item) + ",";
                                    }
                                    jsonItem = "[" + jsonItem + "]";
                                    new HttpAsyncTask().execute("https://apiexterno-hom.simpress.com.br/camadadeservico/infraestrutura/v1/mobileinfo/localizacao/list/", jsonItem);
                                }
                                dropLocation();
                                hasLocation = false;

                            } else {
                                saveLocation();
                            }
                        } else {
                            if (i == 1)
                                if (!(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)))
                                    requestAllPermissions();
                        }
                    }
                    //modificar para 1800000
                    //Thread.sleep(1800000); // a cada 30 minutos
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //runThread();
            //hiddenApp();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(getBaseContext(), "GPS sei lá", Toast.LENGTH_SHORT);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getBaseContext(), "GPS habilitado", Toast.LENGTH_SHORT);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getBaseContext(), "GPS desabilitado", Toast.LENGTH_SHORT);
        }
    }

    public class ImeiLocation extends ArrayList {
        private int _id;
        private String _imei;
        private String _latitude;
        private String _longitude;
        private String _data;


        public ImeiLocation(int id, String imei, String latitude, String longitude, String data) {
            _id = id;
            _imei = imei;
            _latitude = latitude;
            _longitude = longitude;
            _data = data;
        }
    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("app_token", "h7l6cCeFM79t");
                conn.addRequestProperty("client_id", "zbA3yQmjcC8f");
                conn.addRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(300000);

                // para activar el metodo post
                conn.setDoOutput(true);
                conn.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                InputStream is = conn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();
            } catch (Exception e) {
                Log.e("Erro API", e.toString());

            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //this method will be running on UI thread

        }
    }



    //descontinuado
    private void runThread() {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Time a = new Time("Brazil/East");
                    a.setToNow();

                    Toast.makeText(getBaseContext(), "horario: " + a.toString(), Toast.LENGTH_SHORT).show();

                    i++;

                    if (a.hour >= 7 && a.hour <= 18) { //rodará apenas no horário comercial
                        try {
                            getLocation();
                            if (hasLocation) {
                                if (checkPermission("android.permission.READ_PHONE_STATE")) {
                                    getIMEI();
                                    TextView txtImei = (TextView) findViewById(R.id.imei);
                                    txtImei.setText(imeiString);

                                    helper = new DatabaseHelper(getBaseContext());

                                    getLocation();
                                    dropLocation();
                                    saveLocation();


                                    if (Conectado(getBaseContext())) {
                                        ArrayList<ImeiLocation> itensImei = getSavedLocation();

                                        if (itensImei.size() > 0) {
                                            String jsonItem = "";
                                            for (ImeiLocation item : itensImei) {
                                                jsonItem = generateJson(item);
                                                //putLocationWS();
                                            }
                                            String imei = "{\"IMEI\": \"edgaredgar\",\"Latitude\": \"2541\",\"Longitude\": \"-2541\",\"Data_Localizacao\": \"2016-08-05T17:29:44.6136985-03:00\"}";
                                            new HttpAsyncTask().execute("https://apiexterno-hom.simpress.com.br/camadadeservico/infraestrutura/v1/mobileinfo/localizacao/", imei);
                                        }
                                        //dropLocation();

                                        //    txtImei.setText(String.valueOf(itensImei.size()));
                                    } else {
                                        saveLocation();
                                    }
                                } else {
                                    if (i == 1) {
                                        if (!(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)))
                                            requestAllPermissions();
                                    }
                                }
                            }
                            //modificar para 1800000
                            Thread.sleep(1000); // a cada 30 minutos
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (i == 10) break;
                }
            }
        }));
    }
}
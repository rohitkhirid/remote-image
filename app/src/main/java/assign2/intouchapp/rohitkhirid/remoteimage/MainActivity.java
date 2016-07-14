package assign2.intouchapp.rohitkhirid.remoteimage;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity
{
    String TAG = "InTouchApp";

    ImageView imageView;
    Button downloadImageButton;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);
        downloadImageButton = (Button)findViewById(R.id.downloadImageButton);

        downloadImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
                Boolean isInternetPresent = cd.isConnectingToInternet();
                if(!isInternetPresent)
                {
                    Log.d(TAG, "Internet connection Unavailable");
                    Toast.makeText(getApplicationContext(), "Internet Connection Unavailable", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if (Build.VERSION.SDK_INT >= 23)
                    {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                ||
                            ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            )
                        {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    }
                    else
                    {
                        Log.d(TAG, "Permissions taken from manifest");
                    }
                    Log.d(TAG, "Starting Actual Download");
                    Toast.makeText(getApplicationContext(), "Download has begun.\n You can check the progress in notification tray", Toast.LENGTH_LONG).show();
                    DownloadImage downloadImage = new DownloadImage();
                    downloadImage.execute();
                }
            }
        });
    }

    private class DownloadImage extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... params)
        {
            try
            {
                Log.d(TAG, "In AsyncTask");
                int count;
                NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                mBuilder.setContentTitle("Picture Download")
                        .setContentText("Download in progress")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true);
                mNotifyManager.notify(1, mBuilder.build());
                URL url = new URL("http://www.planwallpaper.com/static/images/wallpapers-hd-8000-8331-hd-wallpapers.jpg");
                URLConnection connection = url.openConnection();
                connection.connect();
                int lenghtOfFile = connection.getContentLength();
                Log.d(TAG, "Length Of File : " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/sample.jpg");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1)
                {
                    total += count;
                    mBuilder.setProgress(lenghtOfFile, (int)total, false);
                    mBuilder.setContentText("Completed : " + total + "/" + lenghtOfFile);
                    mNotifyManager.notify(1, mBuilder.build());
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                Log.d(TAG, "download completed");
                mBuilder.setOngoing(false);
                mNotifyManager.notify(1, mBuilder.build());
            }
            catch (Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
            Log.d(TAG, "Path : " + Environment.getExternalStorageDirectory() + "/sample.png");
            return Environment.getExternalStorageDirectory() + "/sample.png";
        }

        protected void onPostExecute(String result)
        {
            Log.d(TAG, "In onPostExecute");
            imageView.setImageURI(Uri.parse(result));
        }
    }
}
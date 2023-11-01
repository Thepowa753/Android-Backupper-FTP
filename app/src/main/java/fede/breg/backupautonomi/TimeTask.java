package fede.breg.backupautonomi;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class TimeTask extends AppCompatActivity {
    public Boolean[] status = new Boolean[]{false, false, false, false}; //medias,phone,whatsapp,apps
    public static ArrayList<String> FTP_path = new ArrayList<String>();
    public static SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        for(int i = 0; i<4; i++)
        {
            status[i] = prefs.getBoolean(String.format("%s", i), false);
            if(status[i])
            {
                switch (i){
                    case 0:
                        FTP_path.add("Foto");
                        FTP_path.add("Video");
                        break;
                    case 1:
                        FTP_path.add("SMS");
                        FTP_path.add("calls");
                        FTP_path.add("Rubrica");
                        break;
                    case 2:
                        FTP_path.add("WhatsApp");
                        break;
                    case 3:
                        FTP_path.add("Apps");
                        break;
                    default:
                        break;
                }
            }
        }
        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(prefs.getLong("LastBackupCompleted", 0));
        DoBackup(this, status, cur_cal);
        super.onCreate(savedInstanceState);
    }

    private static void createNotificationChannel(Context ctx) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        CharSequence name = "channel_backup";
        String description = "channel_backup";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("channel_backup", name, importance);
        channel.setDescription(description);
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static void DoBackup(Context ctx, Boolean[] status, Calendar lastUpload) {
        System.out.println("Start backup sequence");
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
//        createNotificationChannel(ctx);
//        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(ctx, "channel_backup")
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setOngoing(false)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentTitle("Notification!") // title for notification
//                .setContentText("Hello word"); // message for notification; // clear notification after click
//        Intent intenti = new Intent(ctx, TimeTask.class);
//        PendingIntent pi = PendingIntent.getActivity(ctx,0,intenti, Intent.FILL_IN_ACTION | PendingIntent.FLAG_IMMUTABLE);
//        mBuilder.setContentIntent(pi);
//        NotificationManager mNotificationManager =
//                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(1, mBuilder.build());

        //Book next backup time

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        cur_cal.set(Calendar.HOUR_OF_DAY, 2);
        cur_cal.set(Calendar.MINUTE, 0);
        if (cur_cal.get(Calendar.DAY_OF_YEAR) < 365)
            cur_cal.add(Calendar.DAY_OF_YEAR, 1);
        else {
            cur_cal.add(Calendar.YEAR, 1);
            cur_cal.set(Calendar.DAY_OF_YEAR, 1);
        }
        Intent intent = new Intent(ctx, TimeTask.class);
        PendingIntent pintent = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(), 480000, pintent);

        //Real backup logic


        //Save backup date
        ArrayList<File> files = new ArrayList<>();
        String prefix = "/Backups/Leila/";
        if (Build.DEVICE.equals("zeus"))
            prefix = "/Backups/Fede/";
        for(int i = 0; i<4; i++)
        {
            if(status[i])
            {
                switch (i){ //medias,phone,whatsapp,apps
                    case 0:
                        files.addAll(GetFileListPerPath("/sdcard/DCIM/Camera", lastUpload.getTimeInMillis()));
                        files.addAll(GetFileListPerPath("/sdcard/DCIM/Screenshots", lastUpload.getTimeInMillis()));
                        files.addAll(GetFileListPerPath("/sdcard/DCIM/ScreenRecorder", lastUpload.getTimeInMillis()));
                        files.addAll(GetFileListPerPath("/sdcard/Pictures", lastUpload.getTimeInMillis()));
                        files.addAll(GetFileListPerPath("/sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images", lastUpload.getTimeInMillis()));
                        files.addAll(GetFileListPerPath("/sdcard/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video", lastUpload.getTimeInMillis()));
                        break;
                    case 2:
                        files.addAll(GetFileListPerPath("/sdcard/Android/media/com.whatsapp/WhatsApp/Backups", lastUpload.getTimeInMillis()));
                        files.addAll(GetFileListPerPath("/sdcard/Android/media/com.whatsapp/WhatsApp/Databases ", lastUpload.getTimeInMillis()));
                        break;
                    default:
                        break;
                }
            }
        }
        String ip = prefs.getString("IP", "localhost");
        String user = prefs.getString("USER", "user");
        String pass = prefs.getString("PASS", "pass");
        int port = 21;
        if(ip.contains(":"))
        {
            String tmp = ip.split(":")[1];
            if(tmp.length()>0)
            {
                try{
                    port = Integer.parseInt(tmp);
                }
                catch (Exception ignored)
                {
                }
            }
        }
        UploadFileList(files, user, 0, ip, port, user, pass);
        for (File f:
             files) {
            System.out.println(f.getName());
            
        }
        System.out.println(prefix);
        Calendar cur_cala = new GregorianCalendar();
        cur_cala.setTimeInMillis(System.currentTimeMillis());
        cur_cala.add(Calendar.DAY_OF_YEAR, -1);
        SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editPrefs.putLong("LastBackupCompleted", cur_cala.getTimeInMillis());
        editPrefs.apply();
    }

    private static ArrayList<File> GetFileListPerPath(String basePath, long lastUpload)
    {
        File dir = new File(basePath);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.lastModified() > lastUpload && file.isFile();
            }
        };
        System.out.println("Scanning: "+basePath);
        File[] files = dir.listFiles(filter);
        if(files != null)
            return new ArrayList<File>(Arrays.asList(files));
        return new ArrayList<File>();
    }
    private static Boolean UploadFileList(ArrayList<File> files_paths, String prefix, int index, String ip, int port, String user, String pass)
    {
        FTPClient con;

        try
        {
            con = new FTPClient();
            con.connect(ip, port);

            if (con.login(user, pass))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);
                for (File local_file: files_paths
                     ) {

                    FileInputStream in = new FileInputStream(local_file);
                    boolean result = con.storeFile(prefix+"/"+FTP_path.get(index)+
                            local_file.getAbsolutePath().split("/")
                                    [local_file.getAbsolutePath().split("/").length-1], in);
                    in.close();
                    if (result) Log.v("upload result", "succeeded");
                }

                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
}

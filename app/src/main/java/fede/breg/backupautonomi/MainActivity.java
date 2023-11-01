package fede.breg.backupautonomi;

import static android.os.Environment.getExternalStoragePublicDirectory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Boolean[] status = new Boolean[]{false, false, false, false};
    public Switch[] switches;
    private SharedPreferences prefs;
    private Calendar cur_cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getExternalStoragePublicDirectory("all");
        System.out.println(Build.DEVICE); //2201122G
        switches = new Switch[]{(Switch)findViewById(R.id.medias), (Switch)findViewById(R.id.phone),(Switch)findViewById(R.id.whatsapp),(Switch)findViewById(R.id.apps)};
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        for(int i = 0; i<4; i++)
        {
            status[i] = prefs.getBoolean(String.format("%s", i), false);
            switches[i].setChecked(status[i]);
        }
        long lastBackup = prefs.getLong("LastBackupCompleted", 0);
        cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(lastBackup);
        if(lastBackup != 0) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
            ((TextView) findViewById(R.id.editTextDate)).setText(format.format(cur_cal.getTime()));
        }
    }

    public void onSettingsClicked(View v){
        Intent intent = new Intent(getApplicationContext(), settings.class);
        startActivity(intent);
    }

    public void onClickBackup(View v)
    {
        TimeTask.DoBackup(this, status, cur_cal);
    }

    public void changeSettings(View v)
    {
        SharedPreferences.Editor editPrefs = prefs.edit();
        for(int i = 0; i<4; i++)
        {
            status[i] = switches[i].isChecked();
            editPrefs.putBoolean(String.format("%s", i), status[i]);
        }
        editPrefs.apply();
    }
}
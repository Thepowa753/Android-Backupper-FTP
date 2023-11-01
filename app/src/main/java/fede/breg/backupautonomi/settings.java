package fede.breg.backupautonomi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity {
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ((EditText)findViewById(R.id.ip)).setText(prefs.getString("IP", "localhost:21"));
        ((EditText)findViewById(R.id.user)).setText(prefs.getString("USER", "USERNAME"));
        ((EditText)findViewById(R.id.pass)).setText(prefs.getString("PASS", "PASSWORD"));

    }

    public void onBackBTNPressed(View v){
        onBackPressed();
    }

    public void onSaveBtnPressed(View v)
    {
        SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editPrefs.putString("IP", ((EditText)findViewById(R.id.ip)).getText().toString());
        editPrefs.putString("USER", ((EditText)findViewById(R.id.user)).getText().toString());
        editPrefs.putString("PASS", ((EditText)findViewById(R.id.pass)).getText().toString());
        editPrefs.apply();
    }
}

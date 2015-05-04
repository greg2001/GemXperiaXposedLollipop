package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Constants.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;

import com.gem.util.GemActivity;
import com.gem.util.GemPreferenceFragment;

public class MainActivity extends GemActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    getFragmentManager()
      .beginTransaction()
      .replace(android.R.id.content, new SettingsFragment())
      .commit();
  }

public static class SettingsFragment extends GemPreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);

    try
    {
      findPreference("key_about_app").setTitle(getActivity().getTitle() + " " +
        getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);

      findPreference("key_restart_launcher").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
      {
        @Override
        public boolean onPreferenceClick(Preference preference)
        {
          try
          {
            Runtime.getRuntime().exec("su -c pkill " + Conditionals.SE_HOME_PACKAGE).waitFor();
          }
          catch(Exception ex)
          {
            ex.printStackTrace();
          }
          return true;
        }
      });
  
      findPreference("key_about_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
      {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
          getActivity().recreate();
          return true;
        }
      });
    }
    catch(NameNotFoundException ex)
    {
      Log.w(TAG, ex);
    }
  }
}

}

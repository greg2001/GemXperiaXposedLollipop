package com.gem.xperiaxposed;

import static com.gem.xperiaxposed.Constants.*;

import java.lang.reflect.Field;

import android.content.Context;
import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public class Conditionals
{
  public static String SE_HOME_PACKAGE;
  public static String SE_HOME_VERSION;
  
  private static boolean inited = false;
  
  public static void init(Context context)
  {
    if(inited)
      return;
    inited = true;
    
    try
    {
      for(String pkg: SE_HOME)
      {
        try
        {
          SE_HOME_VERSION = context.getPackageManager().getPackageInfo(pkg, 0).versionName;
          SE_HOME_PACKAGE = pkg;
          break;
        }
        catch(Throwable ex)
        {
        }
      }
      Log.i(TAG, "Xperia launcher package: " + SE_HOME_PACKAGE);
      Log.i(TAG, "Xperia launcher version: " + SE_HOME_VERSION);
    }
    catch(Throwable ex)
    {
      Log.w(TAG, "Unable to retrieve Xperia launcher version", ex);
    }
  }

  public static void initLauncher()
  {
  }
}

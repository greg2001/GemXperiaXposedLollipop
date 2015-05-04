package com.gem.xperiaxposed;

import android.content.res.XResources;

import com.gem.xperiaxposed.home.HomeResources;
import com.gem.xposed.ModuleResources;
import com.gem.xposed.ReflectionUtils;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.gem.xperiaxposed.Constants.SE_HOME;
import static de.robv.android.xposed.XposedBridge.log;

////////////////////////////////////////////////////////////

public class XposedMain implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources
{

////////////////////////////////////////////////////////////
  
  public static String MODULE_PATH;
  public static String SE_HOME_PACKAGE;
  public static XSharedPreferences prefs;

////////////////////////////////////////////////////////////
  
  public void setupClassLoader(XC_LoadPackage.LoadPackageParam param)
  {
    try
    {
      ClassLoader moduleClassLoader = getClass().getClassLoader();
      ClassLoader xposedClassLoader = moduleClassLoader.getParent();
      ClassLoader packageClassLoader = param.classLoader;
      ReflectionUtils.setParentClassLoader(moduleClassLoader, packageClassLoader, xposedClassLoader);
    }
    catch(Throwable ex)
    {
      log(ex);
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void initZygote(IXposedHookZygoteInit.StartupParam param) throws Throwable
  {
    MODULE_PATH = param.modulePath;
    prefs = new XSharedPreferences(XposedMain.class.getPackage().getName());
    prefs.makeWorldReadable();
  }

////////////////////////////////////////////////////////////

  @Override
  public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam param) throws Throwable
  {
    if(Arrays.asList(SE_HOME).contains(param.packageName))
    {
      SE_HOME_PACKAGE = param.packageName;
      prefs.reload();
      
      ModuleResources res = ModuleResources.createInstance(MODULE_PATH, param.res);
      HomeResources.updateResources(param.res, res);
    }
  }

////////////////////////////////////////////////////////////

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable
  {
    if(Arrays.asList(SE_HOME).contains(param.packageName))
    {
      SE_HOME_PACKAGE = param.packageName;
      prefs.reload();

      setupClassLoader(param);
      Conditionals.initLauncher();
      com.gem.xperiaxposed.home.HomeHooks.hookInitial(param);
      com.gem.xperiaxposed.home.HomeHooks.hookTransparency(param);
      com.gem.xperiaxposed.home.HomeHooks.hookFont(param);
      com.gem.xperiaxposed.home.HomeHooks.hookLayout(param);
      com.gem.xperiaxposed.home.HomeHooks.hookDesktop(param);
      com.gem.xperiaxposed.home.HomeHooks.hookDock(param);
      com.gem.xperiaxposed.home.HomeHooks.hookDrawer(param);
      com.gem.xperiaxposed.home.HomeHooks.hookFolders(param);
      com.gem.xperiaxposed.home.HomeHooks.hookWidgets(param);
      com.gem.xperiaxposed.home.HomeHooks.hookExperimental(param);
    }
  }
  
////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////

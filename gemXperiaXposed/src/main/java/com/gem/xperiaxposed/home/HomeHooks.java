package com.gem.xperiaxposed.home;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.view.View;
import com.gem.xperiaxposed.Constants;
import com.gem.xposed.AutoHook;
import com.sonymobile.flix.components.*;
import com.sonymobile.home.MainView;
import com.sonymobile.home.apptray.AppTray;
import com.sonymobile.home.apptray.AppTrayDrawerView;
import com.sonymobile.home.apptray.AppTrayPresenter;
import com.sonymobile.home.apptray.AppTrayView;
import com.sonymobile.home.data.Item;
import com.sonymobile.home.desktop.Desktop;
import com.sonymobile.home.desktop.DesktopPageIndicatorView;
import com.sonymobile.home.desktop.DesktopPresenter;
import com.sonymobile.home.desktop.DesktopView;
import com.sonymobile.home.folder.FolderOpener;
import com.sonymobile.home.folder.OpenFolderAdapter;
import com.sonymobile.home.presenter.view.*;
import com.sonymobile.home.ui.pageview.PageView;
import com.sonymobile.home.ui.pageview.PageViewInteractionListener;
import com.sonymobile.home.ui.widget.AdvWidgetProviderHelper;
import com.sonymobile.home.ui.widget.HomeAppWidgetManager;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.gem.xperiaxposed.XposedMain.prefs;
import static com.gem.xposed.ReflectionUtils.getField;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.*;

////////////////////////////////////////////////////////////

public class HomeHooks implements Constants
{

////////////////////////////////////////////////////////////
  
  public static Desktop desktop;
  public static AppTray appTray;
  public static DesktopPresenter desktopPresenter;
  public static AppTrayPresenter appTrayPresenter;
  
////////////////////////////////////////////////////////////
  
  @SuppressWarnings("unused")
  public static void hookInitial(XC_LoadPackage.LoadPackageParam param)
  {
    new AutoHook()
    {
      public void after_all_constructors(Desktop thiz)
      {
        desktop = thiz;
      }
      public void after_all_constructors(AppTray thiz)
      {
        appTray = thiz;
      }
      public void after_all_constructors(DesktopPresenter thiz)
      {
        desktopPresenter = thiz;
      }
      public void after_all_constructors(AppTrayPresenter thiz)
      {
        appTrayPresenter = thiz;
      }
    };
  }  
  
////////////////////////////////////////////////////////////

  @SuppressWarnings("unused")
  public static void hookTransparency(XC_LoadPackage.LoadPackageParam param)
  {
    final int system_ui_transparent_background = prefs.getInt("key_systemui_translucent_background", 0x99000000);
    new AutoHook()
    {
      public Object before_getSystemUiBackgroundColor(com.sonymobile.home.util.SystemUiExtensions thiz, Context c)
      {
        return system_ui_transparent_background;
      }
    };
  }
  
////////////////////////////////////////////////////////////

  @SuppressWarnings("unused")
  public static void hookFont(XC_LoadPackage.LoadPackageParam param)
  {
  }
  
////////////////////////////////////////////////////////////

  @SuppressWarnings("unused")
  public static void hookLayout(XC_LoadPackage.LoadPackageParam param)
  {
    final boolean desktop_disable_pagination = prefs.getBoolean("key_desktop_disable_pagination", false);
    final boolean drawer_disable_pagination = prefs.getBoolean("key_drawer_disable_pagination", false);
    if(desktop_disable_pagination || drawer_disable_pagination)
    {
      new AutoHook()
      {
        public void after_onSceneCreated(MainView thiz, Scene scene, int i1, int i2)
        {
          if(desktop_disable_pagination)
          {
            DesktopView view = getField(desktop, "mDesktopView");
            DesktopPageIndicatorView pageIndicator = getField(desktopPresenter, "mPageIndicatorView");
            view.removeChild(pageIndicator);
          }
          if(drawer_disable_pagination)
          {
            AppTrayView view = getField(appTray, "mAppTrayView");
            PageIndicatorView pageIndicator = getField(appTrayPresenter, "mPageIndicatorView");
            view.removeChild(pageIndicator);
          }
        }
      };
    }

    final boolean desktop_disable_labels = prefs.getBoolean("key_desktop_disable_labels", false);
    final boolean folder_disable_labels = prefs.getBoolean("key_folder_disable_labels", false);
    final boolean drawer_disable_labels = prefs.getBoolean("key_drawer_disable_labels", false);
    if(desktop_disable_labels || folder_disable_labels || drawer_disable_labels)
    {
      new AutoHook()
      {
        public Object after_includedLabel(ItemViewCreatorBase thiz, Item item)
        {
          String name = item.getPageViewName();
          if(desktop_disable_labels && "desktop".equals(name))
            return false;
          if(folder_disable_labels && "folder".equals(name))
            return false;
          if(drawer_disable_labels && "apptray".equals(name))
            return false;
          else
            return NONE;
        }

        public Object before_setMaxTextSize(IconLabelView thiz, int size)
        {
          if(! getBooleanField(thiz, "mIncludedLabel"))
            return VOID;
          else
            return NONE;
        }
      };
    }
    
    final int iconSize = prefs.getInt("key_launcher_icon_size", 100);
    if(iconSize != 100)
    {
      new AutoHook()
      {
        public void after_scaleIconToMaxSize(IconLabelView thiz)
        {
          Image image = thiz.getImage();
          if(image != null && image.getBitmap() != null)
            image.setScaling(image.getScalingX() * iconSize / 100.0f);
        }
      };
    }
  }
  
////////////////////////////////////////////////////////////
  
  @SuppressWarnings("unused")
  public static void hookDesktop(XC_LoadPackage.LoadPackageParam param)
  {
    final int desktop_animation = Integer.valueOf(prefs.getString("key_desktop_animation", Integer.toString(Animations.DESKTOP_DEFAULT)));
    if(desktop_animation != Animations.DESKTOP_DEFAULT)
    {
      new AutoHook()
      {
        private Animations animations = null;
        
        public void after_all_constructors(DesktopView thiz)
        {
          animations = new Animations(thiz);
        }
        
        public Object before_updateFromTouch(DesktopView thiz, boolean b)
        {
          if(animations != null)
          {
            animations.animate(b, desktop_animation);
            return VOID;
          }
          return NONE;
        }
      };
    }

    final int drawer_animation = Integer.valueOf(prefs.getString("key_drawer_animation", Integer.toString(Animations.DRAWER_DEFAULT)));
    if(drawer_animation != Animations.DRAWER_DEFAULT)
    {
      new AutoHook()
      {
        private Animations animations = null;
        
        public void after_all_constructors(AppTrayView thiz)
        {
          animations = new Animations(thiz);
        }
        
        public Object before_updateFromTouch(AppTrayView thiz, boolean b)
        {
          if(animations != null)
          {
            animations.animate(b, drawer_animation);
            return VOID;
          }
          return NONE;
        }
      };
    }
  }
  
////////////////////////////////////////////////////////////

  @SuppressWarnings("unused")
  public static void hookDock(XC_LoadPackage.LoadPackageParam param)
  {
  }
  
////////////////////////////////////////////////////////////

  @SuppressWarnings("unused")
  public static void hookDrawer(XC_LoadPackage.LoadPackageParam param)
  {
    final boolean enableDrawerBackground = prefs.getBoolean("key_enable_drawer_background", false);
    if(enableDrawerBackground)
    {
      final int backgroundColor = prefs.getInt("key_drawer_background", 0); 
      new AutoHook()
      {
        public void after_all_constructors(AppTrayView thiz)
        {
          setIntField(thiz, "mBackgroundColor", backgroundColor);
        }
      };
    }

    if(prefs.getBoolean("key_remember_drawer_page", false))
    {
      new AutoHook()
      {
        public Object before_gotoDefaultPage(AppTrayView thiz)
        {
          return VOID;
        }
      };
    }

    final int drawer_menu_opacity = prefs.getInt("key_drawer_menu_opacity", 100);
    if(drawer_menu_opacity != 100)
    {
      new AutoHook()
      {
        public void after_initialize(AppTrayDrawerView thiz, float f1, float f2, float f3)
        {
          ((View)getObjectField(thiz, "mListView")).getBackground().setAlpha((int)(2.55 * drawer_menu_opacity));
        }
      };
    }

    if(prefs.getBoolean("key_drawer_autohide_pagination", false))
    {
      new AutoHook()
      {
        public Object before_setVisible(Component thiz, boolean visible)
        {
          return "AppTrayPageIndicatorView".equals(thiz.getName()) ? VOID : null;
        }
        
        public void after_createPageIndicatorView(final AppTrayPresenter thiz)
        {
          thiz.getPageIndicatorView().setAutoHide(true);
          thiz.getPageIndicatorView().getListeners().addListener(new ComponentListeners.VisibilityChangeListener()
          {
            @Override
            public void onVisibilityChanged(Component component, boolean visible)
            {
              if(visible)
              {
                thiz.getPageIndicatorView().onInteractionStart();
                thiz.getPageIndicatorView().onInteractionEnd();
              }
            }

            @Override
            public void onCullingChanged(Component component, boolean culling)
            {
            }
          });
        }

        public void after_setView(final AppTrayPresenter thiz, AppTrayView view)
        {
          view.addInteractionListener(new PageViewInteractionListener()
          {
            @Override
            public void onInteractionStart()
            {
              thiz.getPageIndicatorView().onInteractionStart();
            }

            @Override
            public void onInteractionEnd()
            {
              thiz.getPageIndicatorView().onInteractionEnd();
            }
          });
        }
      };
    }
  }

////////////////////////////////////////////////////////////

  @SuppressWarnings("unused")
  public static void hookFolders(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_folder_auto_close", false))
    {
      new AutoHook()
      {
        public void after_doHandleClick(ActivityItemView thiz, Context context)
        {
          closeFolder(thiz);
        }
        
        public void after_doHandleClick(ShortcutItemView thiz, Context context)
        {
          closeFolder(thiz);
        }
        
        private void closeFolder(Component view)
        {
          MainView mainView = (MainView)view.getScene().getView();
          FolderOpener folderOpener = getField(mainView, "mFolderOpener");
          if(folderOpener != null)
            folderOpener.close(true);
        }
      };
    }
    
    if(prefs.getBoolean("key_folder_multiline_labels", false))
    {
      new AutoHook()
      {
        public void after_getItemView(OpenFolderAdapter thiz, int i, MethodHookParam param)
        {
          try
          {
            setBooleanField(param.getResult(), "mCenterVertically", false);
          }
          catch(Throwable ex)
          {
          }
        }
        
        public Object after_getItemViewTextLines(ItemViewCreatorBase thiz, String s)
        {
          if("folder".equals(s))
            return 2;
          else
            return NONE;
        }
      };
    }
    
    if(prefs.getBoolean("key_folder_disable_background_dim", false))
    {
      try {
      findAndHookMethod("com.sonymobile.home.folder.OpenFolderView$DimAnimation", param.classLoader, "onUpdate", float.class, float.class, new XC_MethodReplacement() 
      {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
        {
          return null;
        }
      });
      } catch(Throwable ex) { log(ex); }
    }
  }
  
////////////////////////////////////////////////////////////
  
  @SuppressWarnings("unused")
  public static void hookWidgets(XC_LoadPackage.LoadPackageParam param)
  {
    if(prefs.getBoolean("key_all_widgets_resizable", false))
    {
      new AutoHook()
      {
        public Object after_getResizeMode(HomeAppWidgetManager thiz, int i)
        {
          return AppWidgetProviderInfo.RESIZE_BOTH;
        }

        public int before_getResizeMode(AdvWidgetProviderHelper thiz, ActivityInfo activityInfo, Resources resources)
        {
          return AppWidgetProviderInfo.RESIZE_BOTH;
        }
      };
    }
  }
 
////////////////////////////////////////////////////////////
  
  public static void hookExperimental(XC_LoadPackage.LoadPackageParam param) throws Exception
  {
    if(prefs.getBoolean("key_enable_experimental", false))
      new com.gem.xperiaxposed.home.ExperimentalHooks();
  } 
  
////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////

package net.cylons.ioio.gimbal.app;

import android.content.Context;
import net.cylons.ioio.gimbal.ws.IOIOGimbalServices;

import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.app.Smarted;
import com.smartnsoft.droid4me.download.BitmapDownloader;
import com.smartnsoft.droid4me.ext.app.ConnectivityListener;

/**
 * Responsible for propagating connectivity events to all application components.
 * 
 * @author Jocelyn Girard
 * @since 2013.09.26
 */
public final class IOIOGimbalConnectivityListener
    extends ConnectivityListener
{

  public IOIOGimbalConnectivityListener(Context context)
  {
    super(context);
  }

  @Override
  protected void notifyServices(boolean hasConnectivity)
  {
    if (SmartApplication.isOnCreatedDone() == false)
    {
      return;
    }
    IOIOGimbalServices.getInstance().setConnected(hasConnectivity);
    for (int index = 0; index < BitmapDownloader.INSTANCES_COUNT; index++)
    {
      BitmapDownloader.getInstance(index).setConnected(hasConnectivity);
    }
  }

  @Override
  protected void updateActivity(Smarted<?> smartedActivity)
  {
  }

}
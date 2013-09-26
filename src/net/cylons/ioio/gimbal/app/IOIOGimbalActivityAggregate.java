package net.cylons.ioio.gimbal.app;

import android.app.Activity;
import net.cylons.ioio.gimbal.IOIOGimbalApplication;

/**
 * An aggregate which can be shared by all {@link Activity activities}.
 * 
 * @author Jocelyn Girard
 * @since 2013.09.26
 */
public final class IOIOGimbalActivityAggregate
{

  private final Activity activity;

  public IOIOGimbalActivityAggregate(Activity activity)
  {
    this.activity = activity;
  }

  public IOIOGimbalApplication getApplication()
  {
    return (IOIOGimbalApplication) activity.getApplication();
  }

}

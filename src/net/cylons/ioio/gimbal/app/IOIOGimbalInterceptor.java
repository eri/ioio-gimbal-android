package net.cylons.ioio.gimbal.app;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.Smartable;

/**
 * Is responsible for intercepting life-cycle events.
 * 
 * @author Jocelyn Girard
 * @since 2013.09.26
 */
public final class IOIOGimbalInterceptor
    implements ActivityController.Interceptor
{

  @SuppressWarnings("unchecked")
  @Override
  public void onLifeCycleEvent(Activity activity, Object component, InterceptorEvent interceptorEvent)
  {
    if (component != null)
    {
      // It's a Fragment
      final Smartable<IOIOGimbalFragmentAggregate> smartableFragment = (Smartable<IOIOGimbalFragmentAggregate>) component;
      if (interceptorEvent == InterceptorEvent.onSuperCreateBefore)
      {
        if (component instanceof Fragment)
        {
          smartableFragment.setAggregate(new IOIOGimbalFragmentAggregate((Fragment) smartableFragment));
        }
        else
        {
          smartableFragment.setAggregate(new IOIOGimbalFragmentAggregate((android.app.Fragment) smartableFragment));
        }
      }
    }
    else
    {
      // It's an Activity
      final Smartable<IOIOGimbalActivityAggregate> smartableActivity = (Smartable<IOIOGimbalActivityAggregate>) activity;
      if (interceptorEvent == InterceptorEvent.onSuperCreateBefore)
      {
        smartableActivity.setAggregate(new IOIOGimbalActivityAggregate(activity));
      }
    }
  }

}

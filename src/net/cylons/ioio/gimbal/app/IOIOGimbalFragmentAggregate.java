package net.cylons.ioio.gimbal.app;

import android.support.v4.app.Fragment;
import net.cylons.ioio.gimbal.IOIOGimbalApplication;

/**
 * An aggregate which can be shared by all {@link android.app.Fragment fragments}.
 * 
 * 
 * @author Jocelyn Girard
 * @since 2013.09.26
 */
public final class IOIOGimbalFragmentAggregate
{

  private final android.app.Fragment fragment;

  private final Fragment supportFragment;

  public IOIOGimbalFragmentAggregate(android.app.Fragment fragment)
  {
    this.fragment = fragment;
    this.supportFragment = null;
  }

  public IOIOGimbalFragmentAggregate(Fragment fragment)
  {
    this.fragment = null;
    this.supportFragment = fragment;
  }

  public IOIOGimbalApplication getApplication()
  {
    if (fragment != null)
    {
      return (IOIOGimbalApplication) fragment.getActivity().getApplication();
    }
    else
    {
      return (IOIOGimbalApplication) supportFragment.getActivity().getApplication();
    }
  }

}

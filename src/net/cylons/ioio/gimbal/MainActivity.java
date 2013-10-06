package net.cylons.ioio.gimbal;

import android.os.Looper;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.smartnsoft.droid4me.LifeCycle.BusinessObjectsRetrievalAsynchronousPolicyAnnotation;
import com.smartnsoft.droid4me.app.SmartIOIOActivity;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import net.cylons.ioio.gimbal.app.IOIOGimbalActivityAggregate;

/**
 * The starting screen of the application.
 *
 * @author Jocelyn Girard
 * @since 2013.09.26
 */
@BusinessObjectsRetrievalAsynchronousPolicyAnnotation
public final class MainActivity
    extends SmartIOIOActivity<IOIOGimbalActivityAggregate>
{

  private TextView textView_;

  private SeekBar seekBar_;

  private ToggleButton toggleButton_;

  private final class GimbalLooper
      extends BaseIOIOLooper
  {

    private AnalogInput input_;

    private PwmOutput pwmOutput_;

    private DigitalOutput led_;

    @Override
    public void setup()
        throws ConnectionLostException
    {
      led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
      input_ = ioio_.openAnalogInput(40);
      pwmOutput_ = ioio_.openPwmOutput(12, 100);
      enableUi(true);
    }

    @Override
    public void loop()
        throws ConnectionLostException, InterruptedException
    {
      final float reading = input_.read();
      setText(Float.toString(reading));
      pwmOutput_.setPulseWidth(500 + seekBar_.getProgress() * 2);
      led_.write(!toggleButton_.isChecked());
      Thread.sleep(10);
    }

    @Override
    public void disconnected()
    {
      enableUi(false);
    }
  }

  @Override
  protected IOIOLooper createIOIOLooper()
  {
    return new GimbalLooper();
  }


  public void onRetrieveDisplayObjects()
  {
    setContentView(R.layout.main);
    textView_ = (TextView) findViewById(R.id.TextView);
    seekBar_ = (SeekBar) findViewById(R.id.SeekBar);
    toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);
    enableUi(false);
  }

  public void onRetrieveBusinessObjects()
      throws BusinessObjectUnavailableException
  {

  }

  public void onFulfillDisplayObjects()
  {

  }

  public void onSynchronizeDisplayObjects()
  {

  }

//  @Override
//  public boolean onCreateOptionsMenu(Menu menu)
//  {
//    super.onCreateOptionsMenu(menu);
//
//    menu.add(Menu.NONE, R.string.Main_menu_settings, Menu.NONE, R.string.Main_menu_settings).setIcon(android.R.drawable.ic_menu_preferences).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
//    {
//      @Override
//      public boolean onMenuItemClick(MenuItem item)
//      {
//        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
//        return true;
//      }
//    });
//    menu.add(Menu.NONE, R.string.Main_menu_about, Menu.NONE, R.string.Main_menu_about).setIcon(android.R.drawable.ic_menu_info_details).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
//    {
//      @Override
//      public boolean onMenuItemClick(MenuItem item)
//      {
//        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
//        return true;
//      }
//    });
//
//    return true;
//  }

  private void enableUi(final boolean enable)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        seekBar_.setEnabled(enable);
        toggleButton_.setEnabled(enable);
      }
    });
  }

  private void setText(final String str)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        textView_.setText(str);
      }
    });
  }

}

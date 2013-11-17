package net.cylons.ioio.gimbal;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.smartnsoft.droid4me.LifeCycle.BusinessObjectsRetrievalAsynchronousPolicyAnnotation;
import com.smartnsoft.droid4me.app.SmartIOIOActivity;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
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
    implements SensorEventListener
{

  private final class GimbalLooper
      extends BaseIOIOLooper
  {

    private AnalogInput analogInput;

    private PwmOutput pwmOutputX;

    private PwmOutput pwmOutputY;

    private DigitalOutput led;

    @Override
    public void setup()
        throws ConnectionLostException
    {
      led = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
      analogInput = ioio_.openAnalogInput(40);
      pwmOutputX = ioio_.openPwmOutput(new DigitalOutput.Spec(12, Mode.NORMAL), 100);
      pwmOutputY = ioio_.openPwmOutput(new DigitalOutput.Spec(13, Mode.NORMAL), 100);
      enableUi(true);
    }

    @Override
    public void loop()
        throws ConnectionLostException, InterruptedException
    {
      final float reading = analogInput.read();
      setText(Float.toString(reading));
      int digitalOut = 700 + seekBar.getProgress();
      pwmOutputX.setPulseWidth(digitalOut);
      pwmOutputY.setPulseWidth(digitalOut);
      led.write(!toggleButton.isChecked());
      Thread.sleep(10);
    }

    @Override
    public void disconnected()
    {
      enableUi(false);
    }
  }

  private TextView textView;
  private TextView sensors;

  private SeekBar seekBar;

  private ToggleButton toggleButton;

  private SensorManager sensorManager;

  private float[] gravityValues;

  private float[] magneticValues;

  @Override
  protected IOIOLooper createIOIOLooper()
  {
    return new GimbalLooper();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
  }

  @Override
  public void onRetrieveBusinessObjects()
      throws BusinessObjectUnavailableException
  {
  }

  @Override
  public void onRetrieveDisplayObjects()
  {
    setContentView(R.layout.main);
    textView = (TextView) findViewById(R.id.TextView);
    sensors = (TextView) findViewById(R.id.sensors);
    seekBar = (SeekBar) findViewById(R.id.SeekBar);
    seekBar.setMax(1775);
    toggleButton = (ToggleButton) findViewById(R.id.ToggleButton);
    enableUi(false);
  }

  @Override
  public void onFulfillDisplayObjects()
  {

  }

  @Override
  public void onSynchronizeDisplayObjects()
  {

  }

  @Override
  protected void onResume()
  {
    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    sensorManager.unregisterListener(this);
    super.onPause();
  }

  @Override
  public void onSensorChanged(SensorEvent event)
  {
    switch (event.sensor.getType())
    {
    case Sensor.TYPE_ACCELEROMETER:
      gravityValues = event.values.clone();
      break;
    case Sensor.TYPE_MAGNETIC_FIELD:
      magneticValues = event.values.clone();
      break;
    default:
      return;
    }

    if (gravityValues != null && magneticValues != null)
    {
      final float[] temp = new float[9];
      final float[] R = new float[9];
      //Load rotation matrix into R
      SensorManager.getRotationMatrix(temp, null, gravityValues, magneticValues);

      //Remap to camera's point-of-view
      SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X, SensorManager.AXIS_Z, R);

      //Return the orientation orientationValues
      final float[] orientationValues = new float[3];
      SensorManager.getOrientation(R, orientationValues);

      //Convert to degrees
      for (int i = 0; i < orientationValues.length; i++)
      {
        final Double degrees = (orientationValues[i] * 180) / Math.PI;
        orientationValues[i] = degrees.floatValue();
      }

        sensors.setText("pitch=" + orientationValues[1] + " roll=" + orientationValues[2]);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i)
  {

  }

  private void enableUi(final boolean enable)
  {
    getHandler().post(new Runnable()
    {
      @Override
      public void run()
      {
        seekBar.setEnabled(enable);
        toggleButton.setEnabled(enable);
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
        textView.setText(str);
      }
    });
  }

}

package net.cylons.ioio.gimbal;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
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
import java.text.DecimalFormat;
import net.cylons.ioio.gimbal.app.IOIOGimbalActivityAggregate;
import org.codeandmagic.android.gauge.GaugeView;

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

    private PwmOutput pitchServoOut;

    private PwmOutput rollServoOut;

    private DigitalOutput led;

    @Override
    public void setup()
        throws ConnectionLostException
    {
      led = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
      analogInput = ioio_.openAnalogInput(39);
      pitchServoOut = ioio_.openPwmOutput(new DigitalOutput.Spec(12, Mode.NORMAL), 100);
      rollServoOut = ioio_.openPwmOutput(new DigitalOutput.Spec(13, Mode.NORMAL), 100);
    }

    @Override
    public void loop()
        throws ConnectionLostException, InterruptedException
    {
      final float normalizedPitchDegree = pitchDegree + 180f;
      final float normalizedRollDegree = rollDegree + 180f + 90f;

      final float pitchServoValue = (normalizedPitchDegree * MainActivity.SERVO_OUT_MAX_NORMALIZED / 360f) + MainActivity.SERVO_OUT_OFFSET;
      final float rollServoValue = (normalizedRollDegree * MainActivity.SERVO_OUT_MAX_NORMALIZED / 360f) + MainActivity.SERVO_OUT_OFFSET;

      pitchServoOut.setPulseWidth(pitchServoValue);
      rollServoOut.setPulseWidth(rollServoValue);

      getHandler().post(new Runnable()
      {
        @Override
        public void run()
        {
          pitchTextView.setText(getString(R.string.degrees, decimalFormat.format(normalizedPitchDegree - 180f)));
          rollTextView.setText(getString(R.string.degrees, decimalFormat.format(normalizedRollDegree - 180f)));
          try
          {
            final float alcoholValue = analogInput.read();
            gaugeView.setTargetValue(alcoholValue * 100);
          }
          catch (Exception exception)
          {
            if (log.isErrorEnabled() == true)
            {
              log.error("Unable to read the analogue input !", exception);
            }
          }
        }
      });

      Thread.sleep(10);
    }

    @Override
    public void disconnected()
    {
      analogInput.close();
    }
  }

  private final static float SERVO_OUT_MAX_NORMALIZED = 1775f;

  private final static float SERVO_OUT_OFFSET = 700f;

  private TextView pitchTextView;

  private TextView rollTextView;

  private GaugeView gaugeView;

  private SensorManager sensorManager;

  private Sensor magneticFieldSensor;

  private Sensor accelerometerSensor;

  private DecimalFormat decimalFormat;

  private float[] gravityValues;

  private float[] magneticValues;

  private float pitchDegree = 0f;

  private float rollDegree = 0f;

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
    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
  }

  @Override
  public void onRetrieveBusinessObjects()
      throws BusinessObjectUnavailableException
  {
    decimalFormat = new DecimalFormat("###.#");
  }

  @Override
  public void onRetrieveDisplayObjects()
  {
    setContentView(R.layout.main);
    pitchTextView = (TextView) findViewById(R.id.pitchTextView);
    rollTextView = (TextView) findViewById(R.id.rollTextView);
    gaugeView = (GaugeView) findViewById(R.id.gaugeView);
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
    sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    sensorManager.unregisterListener(this, accelerometerSensor);
    sensorManager.unregisterListener(this, magneticFieldSensor);
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
      final float[] rValue = new float[9];
      //Load rotation matrix into rValue
      SensorManager.getRotationMatrix(temp, null, gravityValues, magneticValues);

      //Remap to camera's point-of-view
      SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_X, SensorManager.AXIS_Z, rValue);

      //Return the orientation orientationValues
      final float[] orientationValues = new float[3];
      SensorManager.getOrientation(rValue, orientationValues);

      //Convert to degrees
      for (int i = 0; i < orientationValues.length; i++)
      {
        final Double degrees = (orientationValues[i] * 180) / Math.PI;
        orientationValues[i] = degrees.floatValue();
      }
      pitchDegree = Math.round(orientationValues[1]);
      rollDegree = Math.round(orientationValues[2]);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i)
  {

  }

}

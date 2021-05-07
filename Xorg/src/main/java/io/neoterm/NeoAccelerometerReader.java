package io.neoterm;

import android.content.Context;

/**
 * @author kiva
 */

public class NeoAccelerometerReader extends AccelerometerReader {
  public NeoAccelerometerReader(Context context) {
    super(context);
  }

  public static void setGyroInvertedOrientation(boolean invertedOrientation) {
    gyro.invertedOrientation = invertedOrientation;
  }
}

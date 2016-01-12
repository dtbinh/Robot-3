package Modules;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import main.Miner;

public class GyroSensor {

	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	
	GyroUpdateListener gyroUpdateListener;
	
	boolean hasReadingOpened = false;
	
	Timer timer = new Timer();
	TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if(gyroUpdateListener != null) {
				System.out.println("\tonGyroUpdate");
				gyroUpdateListener.onGyroUpdate(readGyro());
			}
		}
	};
	
	public static float readGyro() {
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();

		float[] sample = new float[sampleProvider.sampleSize()];
		sampleProvider.fetchSample(sample, 0);
		float angle = sample[0];

		return angle;
	}

	public void setListener(GyroUpdateListener listener) {
		System.out.println("\tsetGyroListener");
		gyroUpdateListener = listener;
	}

	public void startReading() {
		if (hasReadingOpened) return;
		if (Miner.isReset())
			return;

		gyroSensor.reset();

		timer.schedule(timerTask, 0, 50);
		hasReadingOpened = true;
	}


	public void removeListener() {
		System.out.println("\tremoveGyroListener");
		gyroUpdateListener = null;
	}
	
	public void reset() {
		gyroSensor.reset();
	}

	public interface GyroUpdateListener {
		void onGyroUpdate(float value);
	}

}

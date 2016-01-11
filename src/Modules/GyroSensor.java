package Modules;

import java.util.Timer;
import java.util.TimerTask;

import Utils.Commons;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import main.Miner;

public class GyroSensor {

	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	
	GyroUpdateListener gyroUpdateListener;
	
	Timer timer = new Timer();
	TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			System.out.println("\tonGyroUpdate");
			if(gyroUpdateListener != null) {
				System.out.println("\tonGyroUpdate accepted");
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
		gyroSensor.reset();
		gyroUpdateListener = listener;
	}

	public void startReading() {
		if (Miner.isReset())
			return;

		gyroSensor.reset();
		timer.schedule(timerTask, 0, 100);
	}

	public void removeListener() {
		System.out.println("\tremoveGyroListener");
		gyroUpdateListener = null;
	}

	public interface GyroUpdateListener {
		void onGyroUpdate(float value);
	}

}

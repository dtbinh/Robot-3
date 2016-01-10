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
	
	Timer timer;
	
	public static float readGyro() {
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		
		float [] sample = new float[sampleProvider.sampleSize()];
    	sampleProvider.fetchSample(sample, 0);
    	float angle = sample[0];
    	
    	return angle;
	}
	
	public void startReading(final UpdateListener listener) {
		if(Miner.isReset()) return;
		
		gyroSensor.reset();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				listener.onUpdate(readGyro());
				
			}
		}, 0, 50);
	}
	
	public void stopReading() {
		timer.cancel();
		timer.purge();
	}
	
	public interface UpdateListener {
		void onUpdate(float value);
	}

}

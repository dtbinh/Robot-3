package Modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Utils.Commons;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import main.Miner;
import main.Miner.Direction;

public class Radar {

	EV3MediumRegulatedMotor motor = new EV3MediumRegulatedMotor(MotorPort.C);

	EV3UltrasonicSensor backSensor = new EV3UltrasonicSensor(SensorPort.S4);
	EV3UltrasonicSensor baseSensor = new EV3UltrasonicSensor(SensorPort.S3);

	Direction baseSensorDirection = Direction.LEFT;

	List<UpdateListener> listeners = Collections.synchronizedList(new ArrayList<UpdateListener>());
	Object listenerSyncronizer = new Object();
	
	boolean shouldReadValues = false;
	Timer steadySampler = new Timer();

	boolean endSampling = false;

	public Radar() {
		motor.resetTachoCount();
		motor.rotateTo(0);
		motor.setSpeed(100);

		steadySampler.schedule(new TimerTask() {

			@Override
			public void run() {
				if (shouldReadValues) {
					synchronized (listenerSyncronizer) {
						float[] values = readValues();
						for (int i=0; i< listeners.size(); i++) {
							listeners.size();
							UpdateListener listener = listeners.get(i);
							listener.onUpdate(values[0], values[1]);
						}
					}
				}
			}
		}, 0, 300);
	}

	public void addUpdateListener(UpdateListener listener) {
		if(Miner.isReset()) return;
		
		synchronized (listenerSyncronizer) {
			shouldReadValues = true;
			listeners.add(listener);
		}
	}

	public void removeUpdateListener(UpdateListener listener) {
		synchronized (listenerSyncronizer) {
			listeners.remove(listener);
			if (listeners.size() == 0)
				shouldReadValues = false;
		}
	}

	public Direction getBaseDirection() {
		return baseSensorDirection;
	}

	public Direction getBackDirection() {
		return Direction.fromAngle((baseSensorDirection.getAngle() + 180) % 360);
	}

	public void rotate90() {
		rotate90(false);
	}

	public void rotate90(boolean reverse) {
		int angle = baseSensorDirection.getAngle();
		angle += reverse ? -90 : 90;

		rotateToDirection(Direction.fromAngle(angle));
	}

	public void rotateToDirection(Direction direction) {
		motor.rotateTo(direction.getAngle());
		baseSensorDirection = direction;
	}

	public void sweep() {
		int angle;
		switch (baseSensorDirection) {
		case LEFT:
			baseSensorDirection = Direction.RIGHT;
			angle = 180;
			break;
		case FORWARD:
			baseSensorDirection = Direction.BACKWARD;
			angle = 180;
			break;
		case RIGHT:
			baseSensorDirection = Direction.LEFT;
			angle = -180;
			break;
		default:
			baseSensorDirection = Direction.FORWARD;
			angle = -180;
			break;
		}
		motor.rotate(angle);
	}

	public void sweepAndSample() {
		Thread sampler = new Thread(new Runnable() {

			@Override
			public void run() {
				int sampleCount = 0;
				while (!endSampling) {
					float[] values = readValues();
					Commons.writeWithTitle(String.format("SampleCount: %d", ++sampleCount),
							String.format("%.2f&%.2f", values[0], values[1]));
				}
				endSampling = false;
			}
		});
		sampler.start();
		sweep();
		endSampling = true;
	}

	public synchronized float[] readValues() {
		float[] values = new float[2];
		SampleProvider sampleProvider;
		float[] sample;

		sampleProvider = baseSensor.getDistanceMode();
		sample = new float[sampleProvider.sampleSize()];
		sampleProvider.fetchSample(sample, 0);

		values[0] = sample[0] * 100 + 9;

		sampleProvider = backSensor.getDistanceMode();
		sample = new float[sampleProvider.sampleSize()];
		sampleProvider.fetchSample(sample, 0);

		values[1] = sample[0] * 100 - 2;

		return values;
	}

	public interface UpdateListener {
		public void onUpdate(float baseDist, float backDist);
	}
}

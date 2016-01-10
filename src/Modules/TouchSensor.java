package Modules;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import main.Miner;

public class TouchSensor {
	static EV3LargeRegulatedMotor motor = Miner.grabberMotor;

	Timer timer;

	public void startListening(final OnTouchListener listener) {
		if (Miner.isReset())
			return;

		reset();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (Math.abs(motor.getTachoCount()) > 2) {
					listener.onTouched();
				}
			}
		}, 0, 100);
	}

	public void stopListening() {
		timer.cancel();
		timer.purge();
	}

	private void reset() {
		motor.resetTachoCount();
		motor.rotateTo(0);
	}

	public interface OnTouchListener {
		public void onTouched();
	}

}

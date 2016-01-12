package Modules;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import main.Miner;

public class Grabber {
	
	static EV3LargeRegulatedMotor grabber = Miner.grabberMotor;
	
	public Grabber(int speed) {
		grabber.resetTachoCount();
		
		grabber.setSpeed(speed);
	}
	
	/**
	 * Sets the grabber state
	 * @param state 1 for locked and -1 for open
	 */
	public void setState(int state) {
		grabber.rotate(90 * state);
	}
}

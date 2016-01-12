package Modules;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import main.Miner;

public class Grabber {
	
	public enum State {
		PICK, DROP
	}
	static EV3LargeRegulatedMotor grabber = Miner.grabberMotor;
	
	public Grabber() {
		grabber.resetTachoCount();
	}
	
	/**
	 * Sets the grabber state
	 * @param state 1 for locked and -1 for open
	 */
	public void setState(State state) {
		if(state == State.PICK) {
			grabber.rotate(110);
		} else {
			grabber.rotate(-110);
		}
	}
}

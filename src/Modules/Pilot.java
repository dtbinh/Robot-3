package Modules;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.navigation.DifferentialPilot;
import main.Miner.Direction;

public class Pilot {
	private DifferentialPilot dPilot;
	private static final double WHEEL_DIAMETER = 5.6;
	private static final double TRACK_WIDTH = 11.5;//12.08;

	private EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	private EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);

	private float speedLeft, speedRight;

	public Pilot() {
		dPilot = new DifferentialPilot(WHEEL_DIAMETER, TRACK_WIDTH, leftMotor, rightMotor);
		dPilot.setRotateSpeed(60);
		stop();
	}

	public void stop() {
		leftMotor.stop(true);
		rightMotor.stop(true);
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
	}

	public void setSpeeds(float left, float right) {
		leftMotor.setSpeed(left);
		rightMotor.setSpeed(right);

		if (left >= 0) {
			leftMotor.forward();
		} else {
			leftMotor.backward();
		}

		if (right >= 0) {
			rightMotor.forward();
		} else {
			rightMotor.backward();
		}
	}

	/**
	 * Differential Pilot
	 */

	public void rotate(float angle, boolean immediateReturn) {
		dPilot.rotate(angle, immediateReturn);
	}

	public void travel(float distance) {
		dPilot.travel(distance);
	}

}

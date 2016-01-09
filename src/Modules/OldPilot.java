package Modules;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.navigation.DifferentialPilot;

public class OldPilot {
	private DifferentialPilot differentialPilot;
	
	private EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	private EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	private static final double WHEEL_DIAMETER = 5.6;
	private static final double TRACK_WIDTH = 12.08;
	
	public OldPilot() {
		leftMotor.resetTachoCount();
		leftMotor.rotateTo(0);

		rightMotor.resetTachoCount();
		rightMotor.rotateTo(0);

		leftMotor.setSpeed(400);
		rightMotor.setSpeed(400);
		
		differentialPilot = new DifferentialPilot(WHEEL_DIAMETER, TRACK_WIDTH, leftMotor, rightMotor);
	}

	public void travel(double distance) {
		differentialPilot.travel(distance);
	}
	
	public void travel() {
		differentialPilot.travel(1000, true);
	}
	
	public void stop() {
		differentialPilot.quickStop();
	}
	
	public void turnLeft() {
		differentialPilot.rotate(-90);
	}
	
	public void turnRight() {
		differentialPilot.rotate(90);
	}
	
	public void turn180() {
		differentialPilot.rotate(180);
	}
	
	public void arcForward(double radius) {
//		differentialPilot.arcForward(radius);
		differentialPilot.travelArc(radius, 10, true);
	}
}

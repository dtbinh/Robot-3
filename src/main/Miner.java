package main;

import Modules.Grabber;
import Modules.GyroSensor;
import Modules.Pilot;
import Modules.Radar;
import Modules.TouchSensor;
import Tasks.EntranceTask;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class Miner {
	
//	public static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	public static EV3LargeRegulatedMotor grabberMotor = new EV3LargeRegulatedMotor(MotorPort.B);
//	public static EV3LargeRegulatedMotor radarMotor = new EV3LargeRegulatedMotor(MotorPort.C);
//	public static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);


	public static void main(String[] args) throws Exception {

		Radar radar = new Radar();
		Pilot pilot = new Pilot();
		TouchSensor touchSensor = new TouchSensor();
		GyroSensor gyroSensor = new GyroSensor();
		

		EntranceTask task1 = new EntranceTask(radar, pilot, touchSensor, gyroSensor);
		task1.startTask();
	}
	
	public enum Direction {
		LEFT, FORWARD, RIGHT, BACKWARD;

		public int getAngle() {
			switch (this) {
				default:
				case LEFT:
					return 0;
				case FORWARD:
					return 90;
				case RIGHT:
					return 180;
				case BACKWARD:
					return 270;
			}
		}

		public static Direction fromAngle(int angle) {
			switch (angle) {
				default:
				case 0:
					return LEFT;
				case 90:
					return FORWARD;
				case 180:
					return RIGHT;
				case 270:
					return BACKWARD;
			}
		}
	}
}

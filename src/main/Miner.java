package main;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Modules.GyroSensor;
import Modules.Pilot;
import Modules.Radar;
import Modules.TouchSensor;
import Tasks.EntranceTask;
import Tasks.MappingTask;
import Tasks.Task;
import Utils.Commons;
import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class Miner {
	
	public static EV3LargeRegulatedMotor grabberMotor = new EV3LargeRegulatedMotor(MotorPort.B);
	
	public static int[] map = new int[36];

	static Timer buttonTimer = new Timer();
	static ArrayList<Task> tasks = new ArrayList<>();
	static int taskCounter = 0;
	
    static boolean isReset = false;
    
	public static void main(String[] args) throws Exception {
		
		Radar radar = new Radar();
		Pilot pilot = new Pilot();
		TouchSensor touchSensor = new TouchSensor();
		GyroSensor gyroSensor = new GyroSensor();


		tasks.add(new EntranceTask(radar, pilot, touchSensor, gyroSensor));
		tasks.add(new MappingTask(radar, pilot, gyroSensor));

		Commons.writeWithTitle("IDLE", "Select task...");
		
		buttonTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				switch (Button.readButtons()) {
				default:
					return;
				case Button.ID_ESCAPE:
					Commons.writeWithTitle("RESET COMPLETE", "Select task...");
					setReset(true);
					tasks.get(taskCounter).onResetTask();
					return;
				case Button.ID_UP:
					taskCounter = 0;
					Commons.writeWithTitle("RUNNING", "EntranceTask");
				case Button.ID_DOWN:
					 taskCounter = 1;
					break;
				case Button.ID_LEFT:
					// taskCounter = 2;
					break;
				}
				setReset(false);
				tasks.get(taskCounter).onStartTask();

			}
		}, 0, 100);
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
		
		public static int getCode(Direction direction) {
			switch (direction) {
			default:
			case LEFT:
				return 0;
			case FORWARD:
				return 1;
			case RIGHT:
				return 2;
			case BACKWARD:
				return 3;
			}
		}
	}
	
	public static boolean isReset() {
		return isReset;
	}
	
	public static void setReset(boolean reset) {
		isReset = reset;
	}
}

package Tasks;

import Modules.GyroSensor;
import Modules.GyroSensor.GyroUpdateListener;
import Modules.Pilot;
import Modules.Radar;
import Modules.TouchSensor;
import Utils.Commons;
import lejos.hardware.Sound;
import main.Miner.Direction;

public class EntranceTask implements Task, TouchSensor.OnTouchListener, Radar.RadarUpdateListener {

	public enum State {
		MOVE_ALONG, TURN_AROUND, TAKE_CORNER, FIND_WALL, UNKNOWN;
	}

	Radar radar;
	Pilot pilot;
	TouchSensor touchSensor;
	GyroSensor gyroSensor;

	Direction lastWallDirection;
	static final int SLOW = 450;
	static final int FAST = 500;

	private static final float DIST_THRESHOLD = 40;
	private State currentState;
	
	boolean isRadarActive = true;
	boolean isGyroActive = true;

	public EntranceTask(Radar radar, Pilot pilot, TouchSensor touchSensor, GyroSensor gyroSensor) {
		this.radar = radar;
		this.pilot = pilot;
		this.touchSensor = touchSensor;
		this.gyroSensor = gyroSensor;
	}

	@Override
	public void onStartTask() {
		gyroSensor.startReading();
		touchSensor.startListening(this);
		radar.setUpdateListener(this);
		gyroSensor.setListener(new GyroUpdateListener() {

			@Override
			public void onGyroUpdate(float value) {
				if (!isGyroActive) return;
				synchronized (currentState) {
					switch (currentState) {
					case MOVE_ALONG:
						System.out.println("moveAlongWallListener");
						if (value > 1.5) {
							pilot.setSpeeds(FAST, SLOW);
						} else if (value < -1.5) {
							pilot.setSpeeds(SLOW, FAST);
						} else {
							pilot.setSpeeds(FAST, FAST);
						}
						Commons.writeWithTitle("Moving along the wall", "Gyro: " + value);
						break;
					case TAKE_CORNER:
						System.out.println("takeCornerListener");
						Commons.writeWithTitle("onGyroUpdate", "Gyro: " + value);
						boolean isLeft = lastWallDirection == Direction.LEFT;

						if (isLeft && value >= 90) {
							isGyroActive = false;
							isRadarActive = false;
							currentState = State.FIND_WALL;
							pilot.stop();
							pilot.travel(20);
							isGyroActive = true;
							isRadarActive = true;

							System.out.println("Turned LEFT");
							Commons.writeWithTitle("Turning LEFT", "Gyro: " + value);
						}

						if (!isLeft && value <= -90) {
							isGyroActive = false;
							isRadarActive = false;
							currentState = State.FIND_WALL;
							pilot.stop();
							pilot.travel(20);
							isGyroActive = true;
							isRadarActive = true;

							System.out.println("Turned RIGHT");
							Commons.writeWithTitle("Turning RIGHT ", "Gyro: " + value);
						}
						break;
					case TURN_AROUND:
						System.out.println("turnRobotAroundListener");
						Commons.writeWithTitle("Gyro:", value + "");

						if (value >= 180) {
							pilot.stop();
							moveAlongWall();
						}
						break;
					default:
						break;
					}
				}
			}
		});
		moveAlongWall();
	}

	private void moveAlongWall() {
		currentState = State.MOVE_ALONG;
	}

	private void turnRobotAround() {
		currentState = State.TURN_AROUND;
		pilot.travel(-20);
		pilot.rotate(1000, true);
	}

	private void takeCorner() {
		currentState = State.TAKE_CORNER;
		if (lastWallDirection == Direction.LEFT) {
			pilot.rotate(1000, true);
		} else {
			pilot.rotate(-1000, true);
		}
		System.out.println("takeCorner: takeCornerListener");
	}

	@Override
	public void onRadarUpdate(float baseDist, float backDist) {
		if (!isRadarActive) return;
		// TODO wait for go a little bit until find wall again.. direct execution thinks it found another corner...
		if (currentState != State.FIND_WALL) {
			if(baseDist < DIST_THRESHOLD) {
				lastWallDirection = radar.getBaseDirection();
			} else if(backDist < DIST_THRESHOLD) {
				lastWallDirection = radar.getBackDirection();
			} else {
				System.out.println("onRadarUpdate: Corner Found!");
				//TODO takeCorner if not entered
				pilot.stop();
				takeCorner();
				
				return;
			}
		}
		else {
			if (backDist < DIST_THRESHOLD || baseDist < DIST_THRESHOLD)
				currentState = State.MOVE_ALONG;
			else
				onResetTask();
		}
	}

	@Override
	public void onTouched() {
		pilot.stop();
		touchSensor.stopListening();
		turnRobotAround();
	}

	@Override
	public void onResetTask() {
		currentState = State.UNKNOWN;
		Sound.beep();
		gyroSensor.removeListener();
		touchSensor.stopListening();
		pilot.stop();
	}

}

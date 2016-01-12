package Tasks;

import Modules.GyroSensor;
import Modules.GyroSensor.GyroUpdateListener;
import Modules.Pilot;
import Modules.Radar;
import Modules.TouchSensor;
import lejos.hardware.Sound;
import main.Miner.Direction;

public class EntranceTask implements Task, TouchSensor.OnTouchListener, Radar.RadarUpdateListener {

	public enum State {
		MOVING_ALONG, TAKING_CORNER, WAITING, CHECKING_WALL, FINISHED, TURNING_AROUND;
	}

	Radar radar;
	Pilot pilot;
	TouchSensor touchSensor;
	GyroSensor gyroSensor;

	Direction lastWallDirection;
	static final int SLOW = 430;
	static final int FAST = 500;

	private static final float DIST_THRESHOLD = 40;
	private State currentState = State.WAITING;

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

		pilot.setRotationSpeed(50);

		gyroSensor.startReading();
		touchSensor.startListening(this);
		radar.setUpdateListener(this);
		gyroSensor.setListener(new GyroUpdateListener() {

			@Override
			public void onGyroUpdate(float value) {
				switch (currentState) {
				case MOVING_ALONG:
					if (value > 1.5) {
						pilot.setSpeedsAndMove(FAST, SLOW);
					} else if (value < -1.5) {
						pilot.setSpeedsAndMove(SLOW, FAST);
					} else {
						pilot.setSpeedsAndMove(FAST, FAST);
					}
					break;
				case TAKING_CORNER:
					System.out.println("Taking corner: " + value);

					boolean isLeft = lastWallDirection == Direction.LEFT;

					if (isLeft && value >= 90) {
						System.out.println("Corner complete: " + value);
						currentState = State.WAITING;
						pilot.stop();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						gyroSensor.reset();
						pilot.travel(40);
						pilot.rotate(-gyroSensor.readGyro(), false);


						currentState = State.CHECKING_WALL;
						gyroSensor.reset();
					}

					if (!isLeft && value <= -90) {
						System.out.println("Corner complete: " + value);
						currentState = State.WAITING;
						pilot.stop();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						gyroSensor.reset();
						pilot.travel(40);
						pilot.rotate(-gyroSensor.readGyro(), false);

						currentState = State.CHECKING_WALL;
						gyroSensor.reset();
					}
					break;
				case TURNING_AROUND:
					System.out.println("TurnAround: " + value);
					if (value >= 180) {
						pilot.stop();
						currentState = State.MOVING_ALONG;
						gyroSensor.reset();
					}
					break;
				default:
					break;
				}
			}
		});
		moveAlongWall();
	}

	private void moveAlongWall() {
		currentState = State.MOVING_ALONG;
	}

	private void takeCorner() {
		gyroSensor.reset();
		if (lastWallDirection == Direction.LEFT) {
			pilot.rotate(2000, true);
		} else {
			pilot.rotate(-2000, true);
		}
		currentState = State.TAKING_CORNER;
	}

	@Override
	public void onRadarUpdate(float baseDist, float backDist) {
		if (!isRadarActive)
			return;

		Direction wallDirection;

		if (baseDist < DIST_THRESHOLD) {
			lastWallDirection = wallDirection = radar.getBaseDirection();
		} else if (backDist < DIST_THRESHOLD) {
			lastWallDirection = wallDirection = radar.getBackDirection();
		} else {
			wallDirection = null;
		}

		if (currentState == State.MOVING_ALONG && wallDirection == null) {
			currentState = State.WAITING;
			pilot.stop();
			takeCorner();
		}

		if (currentState == State.CHECKING_WALL) {
			if (wallDirection == null) {
				onResetTask();
			} else {
				currentState = State.MOVING_ALONG;
			}
		}

	}

	@Override
	public void onTouched() {
		currentState = State.WAITING;
		pilot.travel(-10);
		gyroSensor.reset();
		pilot.rotate(2000, true);
		currentState = State.TURNING_AROUND;

	}

	@Override
	public void onResetTask() {
		currentState = State.FINISHED;
		Sound.beep();
		gyroSensor.removeListener();
		touchSensor.stopListening();
		radar.removeUpdateListener();
		pilot.stop();
	}

}

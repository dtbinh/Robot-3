package Tasks;

import Modules.GyroSensor;
import Modules.GyroSensor.GyroUpdateListener;
import Tasks.EntranceTask.State;
import Modules.Pilot;
import Modules.Radar;
import Modules.TouchSensor;
import Utils.Commons;
import lejos.hardware.Sound;
import main.Miner.Direction;

public class EntranceTask2 implements Task, TouchSensor.OnTouchListener, Radar.RadarUpdateListener {

	public enum State {
		MOVING_ALONG, TAKING_CORNER, WAITING, CHECKING_WALL, FINISHED;
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

	public EntranceTask2(Radar radar, Pilot pilot, TouchSensor touchSensor, GyroSensor gyroSensor) {
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
				switch (currentState) {
				case MOVING_ALONG:
					if (value > 1.5) {
						pilot.setSpeeds(FAST, SLOW);
					} else if (value < -1.5) {
						pilot.setSpeeds(SLOW, FAST);
					} else {
						pilot.setSpeeds(FAST, FAST);
					}
					break;
				case TAKING_CORNER:
					boolean isLeft = lastWallDirection == Direction.LEFT;

					if (isLeft && value >= 90) {
						currentState = State.WAITING;
						pilot.stop();
						pilot.travel(40);
						currentState = State.CHECKING_WALL;
						gyroSensor.reset();
					}

					if (!isLeft && value <= -90) {
						currentState = State.WAITING;
						pilot.stop();
						pilot.travel(40);
						currentState = State.CHECKING_WALL;
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
		if (lastWallDirection == Direction.LEFT) {
			pilot.rotate(1000, true);
		} else {
			pilot.rotate(-1000, true);
		}
		currentState = State.TAKING_CORNER;
	}

	@Override
	public void onRadarUpdate(float baseDist, float backDist) {
		if (!isRadarActive)
			return;

		if (baseDist < DIST_THRESHOLD) {
			lastWallDirection = radar.getBaseDirection();
		} else if (backDist < DIST_THRESHOLD) {
			lastWallDirection = radar.getBackDirection();
		} else {
			lastWallDirection = null;
		}

		if (currentState == State.MOVING_ALONG && lastWallDirection == null) {
			currentState = State.WAITING;
			pilot.stop();
			takeCorner();
		}

		if (currentState == State.CHECKING_WALL) {
			if (lastWallDirection == null) {
				onResetTask();
			} else {
				currentState = State.MOVING_ALONG;
			}
		}

	}

	@Override
	public void onTouched() {

	}

	@Override
	public void onResetTask() {
		currentState = State.FINISHED;
		Sound.beep();
		gyroSensor.removeListener();
		touchSensor.stopListening();
		pilot.stop();
	}

}

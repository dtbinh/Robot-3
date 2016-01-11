package Tasks;

import Modules.GyroSensor;
import Modules.GyroSensor.GyroUpdateListener;
import Modules.Pilot;
import Modules.Radar;
import Modules.TouchSensor;
import Utils.Commons;
import lejos.internal.io.SocketOutputStream;
import main.Miner.Direction;

public class EntranceTask2 implements Task, TouchSensor.OnTouchListener, Radar.RadarUpdateListener {

	Radar radar;
	Pilot pilot;
	TouchSensor touchSensor;
	GyroSensor gyroSensor;

	Direction lastWallDirection;
	static final int SLOW = 450;
	static final int FAST = 500;

	private static final float DIST_THRESHOLD = 40;

	GyroUpdateListener moveAlongWallListener = new GyroUpdateListener() {

		@Override
		public void onGyroUpdate(float value) {
			System.out.println("\tmoveAlongWallListener");
			if (value > 1.5) {
				pilot.setSpeeds(FAST, SLOW);
			} else if (value < -1.5) {
				pilot.setSpeeds(SLOW, FAST);
			} else {
				pilot.setSpeeds(FAST, FAST);
			}
			Commons.writeWithTitle("Moving along the wall", "Gyro: " + value);
		}
	};

	GyroUpdateListener takeCornerListener = new GyroUpdateListener() {

		@Override
		public void onGyroUpdate(float value) {
			System.out.println("\ttakeCornerListener");
			Commons.writeWithTitle("onGyroUpdate", "Gyro: " + value);
			boolean isLeft = lastWallDirection == Direction.LEFT;

			if (isLeft && value >= 90) {
				gyroSensor.removeListener();
				pilot.stop();
				moveAlongWall();

				System.out.println("\tTurning LEFT ");
				Commons.writeWithTitle("Turning LEFT", "Gyro: " + value);
			}

			if (!isLeft && value <= -90) {
				gyroSensor.removeListener();
				pilot.stop();
				moveAlongWall();

				System.out.println("\tTurning RIGHT ");
				Commons.writeWithTitle("Turning RIGHT ", "Gyro: " + value);
			}
		}
	};

	GyroUpdateListener turnRobotAroundListener = new GyroUpdateListener() {
		@Override
		public void onGyroUpdate(float value) {
			System.out.println("\tturnRobotAroundListener");
			Commons.writeWithTitle("Gyro:", value + "");

			if (value >= 180) {
				gyroSensor.removeListener();
				pilot.stop();
				moveAlongWall();
			}
		}
	};
	
	
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
		moveAlongWall();
	}

	private void moveAlongWall() {
		radar.addUpdateListener(this);
		gyroSensor.setListener(moveAlongWallListener);
	}

	private void turnRobotAround() {
		pilot.travel(-20);
		pilot.rotate(1000, true);
		gyroSensor.setListener(turnRobotAroundListener);
	}

	private void takeCorner() {
		if (lastWallDirection == Direction.LEFT) {
			pilot.rotate(1000, true);
		} else {
			pilot.rotate(-1000, true);
		}
		System.out.println("\ttakeCorner(): takeCornerListener");
		gyroSensor.setListener(takeCornerListener);
	}

	@Override
	public void onRadarUpdate(float baseDist, float backDist) {
		if(baseDist < DIST_THRESHOLD) {
			lastWallDirection = radar.getBaseDirection();
		} else if(backDist < DIST_THRESHOLD) {
			lastWallDirection = radar.getBackDirection();
		} else {
			System.out.println("\tonRadarUpdate: Corner Found!");
			//TODO takeCorner if not entered
			gyroSensor.removeListener();
			radar.removeUpdateListener(this);
			pilot.stop();
			takeCorner();
			return;
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
		gyroSensor.removeListener();
		touchSensor.stopListening();
		pilot.stop();
	}

}

package Tasks;

import Modules.GyroSensor;
import Modules.GyroSensor.UpdateListener;
import Modules.Pilot;
import Modules.Radar;
import Modules.TouchSensor;
import Utils.Commons;
import main.Miner.Direction;

public class EntranceTask implements Task, Radar.UpdateListener, TouchSensor.OnTouchListener {

	private static final int WALL_DISTANCE = 20;
	private static final int THRESHOLD = 0;
	
	Radar radar;
	Pilot pilot;
	TouchSensor touchSensor;
	GyroSensor gyroSensor;

	public EntranceTask(Radar radar, Pilot pilot, TouchSensor touchSensor, GyroSensor gyroSensor) {
		this.radar = radar;
		this.pilot = pilot;
		this.touchSensor = touchSensor;
		this.gyroSensor = gyroSensor;
	}
	
	@Override
	public void startTask() {
		moveAlongWall();
	}
	
	private void moveAlongWall() {
		touchSensor.startListening(this);
		radar.addUpdateListener(this);
	}
	
	private void stop() {
		radar.removeUpdateListener(this);
		pilot.stop();
	}
	
	private void turnRobotAround() {
		pilot.travel(-20);
		
		pilot.rotate(1000, true);
		
		gyroSensor.startReading(new UpdateListener() {
			@Override
			public void onUpdate(float value) {
				Commons.writeWithTitle("Gyro:", value  + "");
				
				if(value >= 180) {
					gyroSensor.stopReading();
					pilot.stop();
					moveAlongWall();
				}
			}
		});
	}
	
	private void takeCorner(Direction d) {
		final boolean isLeft = d == Direction.LEFT;
		
		//pilot.travel(10);
		
		if(isLeft) {
			pilot.rotate(1000, true);
		} else {
			pilot.rotate(-1000, true);
		}
		
		
		gyroSensor.startReading(new UpdateListener() {
			@Override
			public void onUpdate(float value) {
				Commons.writeWithTitle("Gyro:", value  + "");
				
				if(isLeft && value >= 90) {
					gyroSensor.stopReading();
					pilot.stop();
					pilot.travel(50);
					moveAlongWall();
				}
				
				if(!isLeft && value <= -90) {
					gyroSensor.stopReading();
					pilot.stop();
					pilot.travel(50);
					moveAlongWall();
				}
			}
		});
	}
	
	
	Direction lastWallDirection;
	private static final float DIST_THRESHOLD = 40;
	
	@Override
	public void onUpdate(float baseDist, float backDist) {
		final int SLOW = 480;
		final int FAST = 500;
		
		if(baseDist > DIST_THRESHOLD && backDist > DIST_THRESHOLD) {
			stop();
			takeCorner(lastWallDirection);
			return;
		}
		
		Direction wall = backDist < baseDist ?
				radar.getBackDirection() : radar.getBaseDirection();
		float dist = Math.min(baseDist, backDist);
		if(dist == baseDist) lastWallDirection = radar.getBaseDirection();
		else lastWallDirection = radar.getBackDirection();

		if(wall == Direction.LEFT) {
			if(dist < WALL_DISTANCE - THRESHOLD) {
				pilot.setSpeeds(FAST, SLOW);
			} else if(dist > WALL_DISTANCE + THRESHOLD) {
				pilot.setSpeeds(SLOW, FAST);
			} else {
				pilot.setSpeeds(FAST, FAST);
			}
		} else {
			if(dist < WALL_DISTANCE - THRESHOLD) {
				pilot.setSpeeds(SLOW, FAST);
			} else if(dist > WALL_DISTANCE + THRESHOLD) {
				pilot.setSpeeds(FAST, SLOW);
			} else {
				pilot.setSpeeds(FAST, FAST);
			}
		}
	}

	@Override
	public void onTouched() {
		stop();
		touchSensor.stopListening();
		turnRobotAround();
	}

	@Override
	public void resetTask() {
		gyroSensor.stopReading();
		radar.removeUpdateListener(this);
		touchSensor.stopListening();
		pilot.stop();
	}
}

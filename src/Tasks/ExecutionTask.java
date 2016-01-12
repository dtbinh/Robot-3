package Tasks;

import Modules.Grabber;
import Modules.GyroSensor;
import Modules.Pilot;
import Modules.Grabber.State;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import main.Miner;
import main.Miner.Direction;

public class ExecutionTask implements Task, GyroSensor.GyroUpdateListener {

	private static final float GRID_LENGTH = 34f;

	private enum GyroState {
		ROTATE, WAIT
	}

	GyroState currentState = GyroState.WAIT;

	Grabber grabber;
	Pilot pilot;
	GyroSensor gyroSensor;

	float limitAngle;
	double heading;

	ActionFinishListener rotationFinishListener = null;

	public ExecutionTask(Grabber grabber, Pilot pilot, GyroSensor gyroSensor) {
		this.grabber = grabber;
		this.gyroSensor = gyroSensor;
		this.pilot = pilot;
	}


	int station = 0, target = 0, myPosition = 0;
	
	@Override
	public void onStartTask() {
	
		pilot.setRotationSpeed(60);

		gyroSensor.setListener(this);
		gyroSensor.startReading();
		
		
		for(int i=0; i< 36; i++) {
			if(Miner.map[i] == Miner.station) station = i;
			if(Miner.map[i] == Miner.target) target = i;
			if(Miner.map[i] == Miner.myPosition) myPosition = i;
		}

		heading = ((double) Direction.RIGHT.getAngle() - 180) % 360;

		heading = goTo(heading, 33, 10, new ActionFinishListener() {
			
			@Override
			public void onActionFinished() {
				Sound.twoBeeps();
				while (Button.getButtons() != Button.ID_ENTER) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				grabber.setState(State.PICK);
				heading = goTo(heading, 10, 25, new ActionFinishListener() {
					
					@Override
					public void onActionFinished() {
						grabber.setState(State.DROP);
						onResetTask();
						
					}
				});
				
				
			}
		});



		// heading = goTo(heading, Miner.station, Miner.target); //Go to target

		// grabber.setState(0);

		// onResetTask();

	}

	@Override
	public void onResetTask() {
		Sound.beep();
	}

	/**
	 * 
	 * @param heading
	 * @param from
	 * @param to
	 * @return new heading.
	 */
	public double goTo(double heading, int from, int to, final ActionFinishListener finishListener) {
		int fromX = from % 6;
		int fromY = -from / 6;
		int toX = to % 6;
		int toY = -to / 6;
		int diffX = toX - fromX;
		int diffY = toY - fromY;

		Double angle = Math.atan2(diffY, diffX) * (180 / Math.PI);
		final Double distance = Math.sqrt(diffX * diffX + diffY * diffY) * GRID_LENGTH;

		angle -= heading;

		if (angle < -180) {
			angle += 360;
		} else if (angle > 180) {
			angle -= 360;
		}

		gyroSensor.reset();
		startRotating(angle.floatValue(), new ActionFinishListener() {

			@Override
			public void onActionFinished() {
				startMoving(distance.floatValue(), new ActionFinishListener() {

					@Override
					public void onActionFinished() {
						finishListener.onActionFinished();

					}
				});

			}
		});

		return heading + angle;

	}

	@Override
	public void onGyroUpdate(float value) {
		System.out.println("onGyroUpdate");
		switch (currentState) {
		case ROTATE:
			System.out.println("Rotating: v: " + value + "l: " + limitAngle);
			if (value >= limitAngle) {
				currentState = GyroState.WAIT;
				pilot.stop();
				rotationFinishListener.onActionFinished();
			}
			break;

		default:
			break;
		}
	}

	private void startRotating(float angle, ActionFinishListener listener) {
		System.out.println("Start rotating to: " + angle);
		if (angle < 0.01f) {
			listener.onActionFinished();
		} else {
			limitAngle = angle;
			rotationFinishListener = listener;
			pilot.rotate(Math.signum(angle) * 1000, true);
			currentState = GyroState.ROTATE;
		}

	}

	int progress;

	private void startMoving(final float distance, final ActionFinishListener listener) {
		final float unit = 30;
		progress = 0;
		gyroSensor.reset();

		final ActionFinishListener recursiveListener = new ActionFinishListener() {

			@Override
			public void onActionFinished() {
				if (progress < distance - 0.001f) {
					pilot.travel(unit);
					progress += unit;
					startRotating(-gyroSensor.readGyro(), this);
				} else {
					listener.onActionFinished();
				}

			}
		};

		recursiveListener.onActionFinished();

	}

	private interface ActionFinishListener {
		void onActionFinished();
	}

}

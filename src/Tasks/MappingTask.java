package Tasks;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Modules.ColorSensor;
import Modules.GyroSensor;
import Modules.GyroSensor.GyroUpdateListener;
import Modules.Pilot;
import Modules.Radar;
import Modules.Radar.RadarUpdateListener;
import Utils.Commons;
import main.Miner;
import main.Miner.Direction;

public class MappingTask implements Task, GyroUpdateListener {
	
	public enum State {
		TURNING, MOVING;
	}
	
	static final int SLOW = 450;
	static final int FAST = 500;

	Radar radar;
	Pilot pilot;
	GyroSensor gyroSensor;

	ColorSensor colorSensor;

	ServerSocket serverSocket;
	DataOutputStream dataOutputStream;

	Direction[] path = new Direction[16];
	
	boolean activateCorrection = false;
	State currentState;
	Direction wallOnSide;
	
	int robotDirection = 0;
	// up, left, down, right
	int[] movement = new int[] { -6, -1, 6, 1 };

	public MappingTask(Radar radar, Pilot pilot, GyroSensor gyroSensor) {
		this.radar = radar;
		this.pilot = pilot;
		this.gyroSensor = gyroSensor;
	}

	@Override
	public void onStartTask() {
		try {
			pilot.setRotationSpeed(200);
			pilot.setSpeedsAndMove(100, 100);
			pilot.stop();
			gyroSensor.startReading();
//			gyroSensor.setListener(this);
			colorSensor = new ColorSensor();
			for (int r = 0; r < 6; r++)
				for (int c = 0; c < 6; c++) {
					int grid = r == 5 ? Miner.explored : Miner.unknown;
					Miner.map[r * 6 + c] = grid;
				}

			
			serverSocket = new ServerSocket(1029);
			Commons.writeWithTitle("Miner", "Waiting Master");
			
			Socket client = serverSocket.accept();

			OutputStream outputStream = client.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);

			
			findAndFeedReferanceLocation();
			updateMap();
			
			for (int i = 0;i < path.length;i++) {
				gyroSensor.reset();
				Direction direction = path[i];
				if (direction == Direction.LEFT) {
					int turnAmount = 90;
					
					currentState = State.TURNING;
					robotDirection = robotDirection == 3 ?
							0 : robotDirection + 1;
					pilot.rotate(turnAmount, false);
					
					float diff = turnAmount - gyroSensor.readGyro();
					while (Math.abs(diff) > 2) {
						pilot.rotate(diff, false);
						diff = turnAmount - gyroSensor.readGyro();
					}
				}
				else if (direction == Direction.RIGHT) {
					int turnAmount = -90;
					
					currentState = State.TURNING;
					robotDirection = robotDirection == 0 ?
							3 : robotDirection - 1;
					pilot.rotate(turnAmount, false);
					
					float diff = turnAmount - gyroSensor.readGyro();
					while (Math.abs(diff) > 2) {
						pilot.rotate(diff, false);
						diff = turnAmount - gyroSensor.readGyro();
					}
				}
				
				currentState = State.MOVING;
				pilot.travel(35);
				Miner.myPosition += movement[robotDirection];
				Miner.map[Miner.myPosition] = Miner.explored;
				
				sendMyLocation();
				findObstacle();
				updateMap();
			}
			
			if (robotDirection == 0)
				Miner.robotDirection = Direction.FORWARD;
			else if (robotDirection == 1)
				Miner.robotDirection = Direction.LEFT;
			else if (robotDirection == 2)
				Miner.robotDirection = Direction.BACKWARD;
			else Miner.robotDirection = Direction.RIGHT;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void updateMap() {
		System.out.println("updateMap");
		try {
			dataOutputStream.writeInt(-1);
			for (int i = 0; i < 36; i++)
				dataOutputStream.writeInt(Miner.map[i]);
			dataOutputStream.writeInt(-1);

			dataOutputStream.flush();
		} catch (IOException e) {
			System.out.println("updateMap: exception");
		}
	}
	
	private synchronized void findAndFeedReferanceLocation() {
		System.out.println("findAndFeedReferanceLocation");
		float[] values = radar.readValues();
		if(values[0] < values[1]) {
			wallOnSide = radar.getBaseDirection();
		} else {
			wallOnSide = radar.getBackDirection();
		}

		try {
			dataOutputStream.writeInt(-2);
			if (wallOnSide == Direction.LEFT) {
				Miner.myPosition = 32;
				path = new Direction[] {
						Direction.FORWARD, Direction.LEFT, Direction.RIGHT,
						Direction.FORWARD, Direction.FORWARD, Direction.RIGHT,
						Direction.FORWARD, Direction.FORWARD, Direction.RIGHT,
						Direction.FORWARD, Direction.FORWARD, Direction.RIGHT,
						Direction.RIGHT, Direction.FORWARD,
						Direction.LEFT, Direction.LEFT};
			}
			else {
				Miner.myPosition = 33;
				path = new Direction[] {
						Direction.FORWARD, Direction.RIGHT, Direction.LEFT,
						Direction.FORWARD, Direction.FORWARD, Direction.LEFT,
						Direction.FORWARD, Direction.FORWARD, Direction.LEFT,
						Direction.FORWARD, Direction.FORWARD, Direction.LEFT,
						Direction.LEFT, Direction.FORWARD,
						Direction.RIGHT, Direction.RIGHT};
			}
			dataOutputStream.writeInt(Miner.myPosition);
			dataOutputStream.writeInt(-2);

			dataOutputStream.flush();
		} catch (IOException e) {
			System.out.println("findAndFeedReferanceLocation: exception");
		}
	}
	
	private void findObstacle() {
		float[] values = radar.readValues();
		if (wallOnSide == radar.getBaseDirection()) {
			if (values[0] < 20)
				Miner.map[Miner.myPosition + movement[robotDirection]] = Miner.obstacle;
		}
		else {
			if (values[1] < 20)
				Miner.map[Miner.myPosition + movement[3 - robotDirection]] = Miner.obstacle;
		}
	}
	
	private synchronized void sendMyLocation() {
		try {
			dataOutputStream.writeInt(-2);
			dataOutputStream.writeInt(Miner.myPosition);
			dataOutputStream.writeInt(colorSensor.readColor());
			dataOutputStream.writeInt(-2);
		} catch (IOException e) {
			
		}
	}

	@Override
	public void onResetTask() {
		try {
			pilot.stop();
			pilot.setSpeeds(500, 500);
			
			gyroSensor.removeListener();
			radar.removeUpdateListener();
			colorSensor.close();
			colorSensor = null;
			dataOutputStream.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGyroUpdate(float value) {
		if (currentState == State.MOVING) {
			
		}
		else if (currentState == State.TURNING) {
			if (value > 90 || value < -90) {
				pilot.stop();
				gyroSensor.reset();
			}
		}
	}
}
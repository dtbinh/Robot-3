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
import main.Miner;
import main.Miner.Direction;

public class MappingTask implements Task, RadarUpdateListener, GyroUpdateListener {

	static final int SLOW = 450;
	static final int FAST = 500;

	Radar radar;
	Pilot pilot;
	GyroSensor gyroSensor;

	ColorSensor colorSensor;

	ServerSocket serverSocket;
	DataOutputStream dataOutputStream;

	Direction[] path = new Direction[16];

	public MappingTask(Radar radar, Pilot pilot, GyroSensor gyroSensor) {
		this.radar = radar;
		this.pilot = pilot;
		this.gyroSensor = gyroSensor;
	}

	@Override
	public void onStartTask() {
		try {
			gyroSensor.startReading();
			gyroSensor.setListener(this);
			radar.setUpdateListener(this);
			colorSensor = new ColorSensor();
			for (int r = 0; r < 6; r++)
				for (int c = 0; c < 6; c++) {
					int grid = r == 5 ? Miner.explored : Miner.unknown;
					Miner.map[r * 6 + c] = grid;
				}

			findAndFeedReferanceLocation();

			serverSocket = new ServerSocket(1234);
			Socket client = serverSocket.accept();

			OutputStream outputStream = client.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);

			updateMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void updateMap() {
		try {
			dataOutputStream.writeInt(-1);
			for (int i = 0; i < 36; i++)
				dataOutputStream.writeInt(Miner.map[i]);
			dataOutputStream.writeInt(-1);

			dataOutputStream.flush();
		} catch (IOException e) {

		}
	}

	private void findAndFeedReferanceLocation() {
		float[] values = radar.readValues();
		Direction closer;
		if (values[0] < values[1]) {
			closer = radar.getBaseDirection();
		} else {
			closer = radar.getBackDirection();
		}

		try {
			dataOutputStream.writeInt(-2);
			dataOutputStream.writeInt(Direction.getCode(closer));
			Miner.myPosition = closer == Direction.LEFT ? 32 : 33;
			dataOutputStream.writeInt(-2);

			dataOutputStream.flush();
		} catch (IOException e) {

		}
	}

	@Override
	public void onResetTask() {

		try {
			colorSensor.close();
			colorSensor = null;
			dataOutputStream.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onRadarUpdate(float baseDist, float backDist) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onGyroUpdate(float value) {
		System.out.println("moveAlongWallListener");
		if (value > 1.5) {
			pilot.setSpeeds(FAST, SLOW);
		} else if (value < -1.5) {
			pilot.setSpeeds(SLOW, FAST);
		} else {
			pilot.setSpeeds(FAST, FAST);
		}
	}
}
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

public class MappingTask implements Task, RadarUpdateListener, GyroUpdateListener {
	
	private final int unknown = 0;
	private final int explored = 1;
	private final int obstacle = 2;
	private final int station = 3;
	private final int target = 4;
	
	Radar radar;
	Pilot pilot;
	GyroSensor gyroSensor;
	
	ColorSensor colorSensor;
	
	ServerSocket serverSocket;
	DataOutputStream dataOutputStream;

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
			for (int r = 0;r < 6;r++)
				for (int c = 0;c < 6;c++) {
					int grid = r == 5 ? explored : unknown;
					Miner.map[r * 6 + c] = grid;
				}
			
			// TODO where am i within area
			
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
			for(int i = 0;i < 36;i++)
				dataOutputStream.writeInt(Miner.map[i]);
			dataOutputStream.writeInt(-1);
			
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
		// TODO Auto-generated method stub
	}
}
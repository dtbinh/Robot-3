package Tasks;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Modules.GyroSensor;
import Modules.Pilot;
import Modules.Radar;

public class MappingTask implements Task {
	
	Radar radar;
	Pilot pilot;
	GyroSensor gyroSensor;

	public MappingTask(Radar radar, Pilot pilot, GyroSensor gyroSensor) {
		this.radar = radar;
		this.pilot = pilot;
		this.gyroSensor = gyroSensor;
	}

	@Override
	public void onStartTask() {
		try {
			ServerSocket serverSocket = new ServerSocket(1234);
			Socket client = serverSocket.accept();
			
			OutputStream outputStream = client.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			
			dataOutputStream.writeInt(0);
			dataOutputStream.flush();

			dataOutputStream.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResetTask() {
		// TODO Auto-generated method stub
		
	}
}
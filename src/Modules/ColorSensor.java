package Modules;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.ColorAdapter;

public class ColorSensor {
	
	private static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
	private static ColorAdapter colorAdapter = new ColorAdapter(colorSensor);
	
	public int readColor() {
		return colorAdapter.getColorID();
	}
	
	public void close() {
		colorSensor.close();
	}
}

package Modules;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;

public class ColorSensor {
	
	private static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);
	private static ColorAdapter colorAdapter = new ColorAdapter(colorSensor);
	
	public int readColor() {
		Color color = colorAdapter.getColor();
		int res = color.getGreen() > 5 ? 1 :
			color.getRed() > 5 ? 7 : -1;
		return res;
	}
	
	public void close() {
		colorSensor.close();
	}
}

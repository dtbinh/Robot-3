package Utils;

import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;

public class Commons {
	
	private static EV3 ev3 = (EV3) BrickFinder.getDefault();

	public static void writeWithTitle(String title, String what) {
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString(title, graphicsLCD.getWidth()/2,
				graphicsLCD.getHeight()/4, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString(what, graphicsLCD.getWidth()/2,
				graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
	}
	
	public static void writeSubChoices(int position, String[] choices) {
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		int shift = graphicsLCD.getHeight() / choices.length;
		
		graphicsLCD.drawLine(graphicsLCD.getWidth() / 2, 0,
				graphicsLCD.getWidth() / 2, graphicsLCD.getHeight());
		for (int i = 0;i < choices.length;i++)
			graphicsLCD.drawString(((i == position) ? "->" : "  ") + choices[i],
					graphicsLCD.getWidth() / 2, shift * i, GraphicsLCD.LEFT);
	}

}

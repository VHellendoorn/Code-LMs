package space.earlygrey.shapedrawer.test;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1200;
		config.height = 800;
		config.resizable  = false;
		config.samples = 4;
		new LwjglApplication(new ShapeDrawerTest(), config);
	}
}

package lando.systems.platformy.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import lando.systems.platformy.Config;
import lando.systems.platformy.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
//		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		config.title = Config.title;
//		config.width = Config.windowWidth;
//		config.height = Config.windowHeight;
//		config.resizable = Config.resizable;
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(Config.title);
        config.setWindowedMode(Config.windowWidth, Config.windowHeight);
        config.setResizable(Config.resizable);
		new Lwjgl3Application(new Game(), config);
	}
}

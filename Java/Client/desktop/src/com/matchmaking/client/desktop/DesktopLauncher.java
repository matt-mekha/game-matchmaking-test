package com.matchmaking.client.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.matchmaking.client.Constants;
import com.matchmaking.client.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = Constants.SCENE_WIDTH;
		config.height = Constants.SCENE_HEIGHT;
		config.resizable = false;
		new LwjglApplication(new Game(), config);
	}
}

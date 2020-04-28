package com.example.matchmaking.client;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import com.example.matchmaking.util.Constants;
import com.example.matchmaking.util.Logger;

public class DesktopLauncher {
	public static void main (String[] args) {
		Logger.setName("Client");

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = Constants.SCENE_WIDTH;
		config.height = Constants.SCENE_HEIGHT;
		config.resizable = false;
		new LwjglApplication(new Game(args), config);
	}
}

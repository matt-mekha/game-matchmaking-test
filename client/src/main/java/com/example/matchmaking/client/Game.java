package com.example.matchmaking.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.matchmaking.util.ConnectionProtos;
import com.example.matchmaking.util.Constants;

import java.util.List;

public class Game extends ApplicationAdapter {

	private final String[] args;

	private BitmapFont font;
	private SpriteBatch textBatch;
	private ShapeRenderer shapeRenderer;

	private final Player myPlayer = new Player();

	private Connection connection;

	public Game(String[] args) {
		super();
		this.args = args;
	}

	@Override
	public void create () {
		this.font = new BitmapFont();
		this.textBatch = new SpriteBatch();
		this.shapeRenderer = new ShapeRenderer();

		font.setColor(0, 0, 0, 1);

		this.connection = new Connection(this, args);
	}

	@Override
	public void render () {
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1, 1, 1, 1);

		Connection.Status status = connection.getStatus();
		switch(status) {
			case CONNECTING:
			case QUEUED:
			case JOINING:

				textBatch.begin();
				font.draw(textBatch, status.toString() + "...", 100, 100);
				textBatch.end();

				break;
			case GAME:

				processInput();

				List<ConnectionProtos.Player> players = connection.getPlayers();
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

				for (ConnectionProtos.Player player : players) {
					ConnectionProtos.Player.Color color = player.getColor();
					ConnectionProtos.Player.Position position = player.getPosition();
					shapeRenderer.setColor(color.getR(), color.getG(), color.getB(), 1);
					shapeRenderer.rect(position.getX(), position.getY(), Constants.PLAYER_SIZE, Constants.PLAYER_SIZE);
				}

				shapeRenderer.end();

				break;
		}
	}

	private void processInput() {
		float dx = 0, dy = 0;
		float speed = Constants.PLAYER_SPEED * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.S)) {
			dy -= speed;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.W)) {
			dy += speed;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
			dx -= speed;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.D)) {
			dx += speed;
		}
		myPlayer.getPosition().update(dx, dy);
	}
	
	@Override
	public void dispose () {
		shapeRenderer.dispose();
		textBatch.dispose();
		font.dispose();
		connection.close();
	}

	public ConnectionProtos.Player getMyPlayer() {
		return myPlayer.getProto();
	}
}

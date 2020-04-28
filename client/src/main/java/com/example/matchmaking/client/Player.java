package com.example.matchmaking.client;

import com.badlogic.gdx.math.MathUtils;
import com.example.matchmaking.util.ConnectionProtos;
import com.example.matchmaking.util.Constants;

public class Player {

    private static final int X_MAX = Constants.SCENE_WIDTH - Constants.PLAYER_SIZE;
    private static final int Y_MAX = Constants.SCENE_HEIGHT - Constants.PLAYER_SIZE;

    private final Position position;
    private final Color color;

    public Player() {
        this.position = new Position();
        this.color = new Color();
    }

    public ConnectionProtos.Player getProto() {
        return ConnectionProtos.Player.newBuilder()
                .setColor(color.getProto())
                .setPosition(position.getProto())
                .build();
    }

    public Position getPosition() {
        return position;
    }

    public static class Position {
        private float x, y;
        private Position() {
            this.x = (float) Math.random() * (float)(X_MAX);
            this.y = (float) Math.random() * (float)(Y_MAX);
        }

        private ConnectionProtos.Player.Position getProto() {
            return ConnectionProtos.Player.Position.newBuilder()
                    .setX(x)
                    .setY(y)
                    .build();
        }

        public void update(float dx, float dy) {
            x = MathUtils.clamp(x + dx, 0, X_MAX);
            y = MathUtils.clamp(y + dy, 0, Y_MAX);
        }
    }

    public static class Color {
        private final float r, g, b;
        private Color() {
            this.r = (float) Math.random();
            this.g = (float) Math.random();
            this.b = (float) Math.random();
        }

        private ConnectionProtos.Player.Color getProto() {
            return ConnectionProtos.Player.Color.newBuilder()
                    .setR(r)
                    .setG(g)
                    .setB(b)
                    .build();
        }
    }
}
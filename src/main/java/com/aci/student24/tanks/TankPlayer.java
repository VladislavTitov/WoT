package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Base;
import com.aci.student24.api.tanks.objects.Indestructible;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class TankPlayer implements Algorithm {
    private int teamId;
    private Base enemyBase;
    private Base ourBase;
    private byte ourDislocation;
    private int count = 0;
    private List<Indestructible> indestructibles;

    private int i = 0;
    private Random rn;
    private Tank tank;

    @Override
    public void setMyId(final int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        if (count == 0) {
            initBaseParametrs(mapState);
            indestructibles = mapState.getIndestructibles();
            rn = new Random();
        }
        count++;
        return mapState.getTanks().stream().map(tank1 -> {
            tank = tank1;
            byte direction = move(tank1, getBestDirection(tank1, enemyBase));
            return new TankMove(tank1.getId(), direction, true);
        }).collect(Collectors.toList());
    }

    private Deltas calculateDeltas(Position tankPosition, Position enemyBasePosition) {
        int deltaX = enemyBasePosition.getX() - tankPosition.getX();
        int deltaY = enemyBasePosition.getY() - tankPosition.getY();
        return new Deltas(deltaX, deltaY);
    }

    public byte move(Position oldPosition, Position newPosition) {
        if (newPosition.getX() - oldPosition.getX() > 0) {
            return Direction.RIGHT;
        } else if (newPosition.getX() - oldPosition.getX() < 0){
            return Direction.LEFT;
        }else if (newPosition.getY() - oldPosition.getY() > 0) {
            return Direction.UP;
        } else if (newPosition.getY() - oldPosition.getY() < 0) {
            return Direction.DOWN;
        }
        return Direction.NO;
    }

    private boolean isWall(Position checkingPosition) {
        for (Indestructible indestructible : indestructibles) {
            if (checkingPosition.equals(indestructible)){
                return true;
            }
        }
        return false;
    }

    public Position getBestDirection (Position currentPosition, Position enemyBasePosition) {
        Deltas deltas = calculateDeltas(currentPosition, enemyBasePosition);
        byte possibleDirection = getDirectionByDeltas(deltas);
        Position checkingPosition = getNewPosition(currentPosition, possibleDirection);
        if (!isWall(checkingPosition)) {
            return checkingPosition;
        } else {
            i++;
            int randomDir = rn.nextInt(4) + 1;
            int dir = (i % 2 == 0) ? tank.getDir() : randomDir;
            return getNewPosition(currentPosition, (byte) dir);
        }
    }

    private byte getDirectionByDeltas(Deltas deltas) {
        int x = deltas.getDeltaX();
        int y = deltas.getDeltaY();
        if (abs(x) - abs(y) >= 0) {
            if (x > 0) {
                return Direction.RIGHT;
            } else {
                return Direction.LEFT;
            }
        } else if (abs(x) - abs(y) < 0){
            if (y > 0) {
                return Direction.UP;
            }else if (y < 0) {
                return Direction.DOWN;
            }
        }
        return Direction.NO;
    }

    private Position getNewPosition(Position oldPosition, byte direction) {
        switch (direction) {
            case Direction.UP:
                return new Position(oldPosition.getX(), oldPosition.getY() + 1);
            case Direction.DOWN:
                return new Position(oldPosition.getX(), oldPosition.getY() - 1);
            case Direction.LEFT:
                return new Position(oldPosition.getX() - 1, oldPosition.getY());
            case Direction.RIGHT:
                return new Position(oldPosition.getX() + 1, oldPosition.getY());
            default:
                return new Position(oldPosition.getX(), oldPosition.getY());
        }
    }

    private void initBaseParametrs(MapState mapState) {
        mapState.getBases().forEach(base -> {
            if (base.getTeamId() == teamId)
                ourBase = base;
            else
                enemyBase = base;
        });
        if (ourBase.getX() > enemyBase.getX())
            ourDislocation = Location.RIGHT_DISLOCATION;
        else
            ourDislocation = Location.LEFT_DISLOCATION;
    }

    class Deltas {
        private int deltaX;
        private int deltaY;

        public Deltas(int deltaX, int deltaY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        public int getDeltaX() {
            return deltaX;
        }

        public void setDeltaX(int deltaX) {
            this.deltaX = deltaX;
        }

        public int getDeltaY() {
            return deltaY;
        }

        public void setDeltaY(int deltaY) {
            this.deltaY = deltaY;
        }
    }


    class Location {
        public static final byte LEFT_DISLOCATION = 0;
        public static final byte RIGHT_DISLOCATION = 1;
    }

}


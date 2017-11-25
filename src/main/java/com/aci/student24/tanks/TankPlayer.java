package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Base;
import com.aci.student24.api.tanks.objects.Indestructible;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.ArrayList;
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

    private int width;
    private int height;

    private List<Tank> ourTanks = new ArrayList<>();

    @Override
    public void setMyId(final int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        if (count == 0) {
            initBaseParametrs(mapState);
        }
        count++;
        ourTanks = mapState.getTanks(teamId);

        return mapState.getTanks().stream().map(tank1 -> {
            byte direction = move(tank1, getBestDirection(tank1, enemyBase));
            return new TankMove(tank1.getId(), direction, isShootSave(tank1));
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
        } else if (newPosition.getX() - oldPosition.getX() < 0) {
            return Direction.LEFT;
        } else if (newPosition.getY() - oldPosition.getY() > 0) {
            return Direction.UP;
        } else if (newPosition.getY() - oldPosition.getY() < 0) {
            return Direction.DOWN;
        }
        return Direction.NO;
    }

    private boolean isWall(Position checkingPosition) {
        if (indestructibles == null) {
            return false;
        }
        int x = checkingPosition.getX();
        int y = checkingPosition.getY();
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1){
            return true;
        }
        for (Indestructible indestructible : indestructibles) {
            if (checkingPosition.equals(indestructible)) {
                return true;
            }
        }
        return false;
    }

    public boolean isShootSave(Tank tank) {
        List<Position> lineOfSight = getLineOfSight(tank);
        for (Position position : lineOfSight) {
            if (ourBase.getPosition().equals(position)) {
                return false;
            }
            for (Tank currentTank:ourTanks){
                if (currentTank.getPosition().equals(position)){
                    return false;
                }
            }
        }
        return true;
    }

    private List<Position> getLineOfSight(Tank tank) {
        List<Position> lineOfSight = new ArrayList<>();
        int xTank = tank.getX();
        int yTank = tank.getY();

        int count;
        switch (tank.getDir()) {
            case Direction.UP:
                count = height - yTank;
                for (int i = 1; i < count; i++)
                    lineOfSight.add(new Position(xTank, yTank + i));
                break;
            case Direction.DOWN:
                count = yTank;
                for (int i = 1; i < count; i++)
                    lineOfSight.add(new Position(xTank, yTank - i));
                break;
            case Direction.LEFT:
                count = xTank;
                for (int i = 1; i < count; i++)
                    lineOfSight.add(new Position(xTank - i, yTank));
                break;
            case Direction.RIGHT:
                count = width - xTank;
                for (int i = 1; i < count; i++)
                    lineOfSight.add(new Position(xTank + i, yTank));
                break;
        }
        return lineOfSight;
    }

    public Position getBestDirection(Position currentPosition, Position enemyBasePosition) {
        Deltas deltas = calculateDeltas(currentPosition, enemyBasePosition);
        byte possibleDirection = getDirectionByDeltas(deltas);
        Position checkingPosition = getNewPosition(currentPosition, possibleDirection);
        if (!isWall(checkingPosition)) {
            return checkingPosition;
        } else {
            return getNewPosition(checkingPosition, findDirectionToGateWay(checkingPosition, possibleDirection));
        }
    }

    private byte findDirectionToGateWay(Position wallPosition, byte direction) {
        Position firstPosition;
        Position secondPosition;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            firstPosition = new Position(wallPosition.getX() - 1, wallPosition.getY());
            secondPosition = new Position(wallPosition.getX() + 1, wallPosition.getY());
        } else/* if (direction != Direction.NO) */{
            firstPosition = new Position(wallPosition.getX(), wallPosition.getY() - 1);
            secondPosition = new Position(wallPosition.getX(), wallPosition.getY() + 1);
        }
        boolean firstPositionIsWall = isWall(firstPosition);
        boolean secondPositionIsWall = isWall(secondPosition);
        if (firstPositionIsWall & !secondPositionIsWall) {
            if (direction == Direction.UP || direction == Direction.DOWN) {
                return Direction.LEFT;
            } else {
                return Direction.UP;
            }
        }
        if (!firstPositionIsWall & secondPositionIsWall) {
            if (direction == Direction.UP || direction == Direction.DOWN) {
                return Direction.RIGHT;
            } else {
                return Direction.DOWN;
            }
        }
        if (firstPositionIsWall & secondPositionIsWall) {
            Random random = new Random();
            int randomVar = random.nextInt(2);
            if (direction == Direction.UP || direction == Direction.DOWN) {
                if (randomVar == 0){
                    return Direction.LEFT;
                }
                return Direction.RIGHT;
            } else {
                if (randomVar == 0){
                    return Direction.LEFT;
                }
                return Direction.DOWN;
            }
        }
        return Direction.NO; //TODO выяснить не мешает ли это нормальному функционированию
    }

    private byte getDirectionByDeltas(Deltas deltas) {
        int x = deltas.getDeltaX();
        int y = deltas.getDeltaY();
        if (abs(x) - abs(y) > 0) {
            if (x > 0) {
                return Direction.RIGHT;
            } else if (x < 0){
                return Direction.LEFT;
            }
        } else if (abs(x) - abs(y) < 0) {
            if (y > 0) {
                return Direction.DOWN;
            } else if (y < 0) {
                return Direction.UP;
            }
        } else if (abs(x) - abs(y) == 0) {
            if (x > 0) {
                return Direction.RIGHT;
            } else if (x < 0){
                return Direction.LEFT;
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
        width = mapState.getSize().getWidth();
        height = mapState.getSize().getHeight();

        indestructibles = mapState.getIndestructibles();
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


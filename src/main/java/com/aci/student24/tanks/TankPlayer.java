package com.aci.student24.tanks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Base;
import com.aci.student24.api.tanks.objects.Indestructible;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.Size;
import com.aci.student24.api.tanks.state.TankMove;

public class TankPlayer implements Algorithm {
    private int teamId;
    private Base enemyBase;
    private Base ourBase;
    private byte ourDislocation;
    private int count = 0;
    private Map<Position, Object> indestructibleMap = new HashMap<>();
    int INF = Integer.MAX_VALUE / 2;
    private int[][] graph;


    @Override
    public void setMyId(final int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        if (count == 0) {
            initBaseParametrs(mapState);
            mapIndestructibles(mapState.getIndestructibles());
            initAdjacency(mapState.getSize());
        }
        count++;
        return null;
    }

    private void initBaseParametrs(MapState mapState) {
        mapState.getBases().forEach(base -> {
            if (base.getId() == teamId)
                ourBase = base;
            else
                enemyBase = base;
        });
        if (ourBase.getX() > enemyBase.getX())
            ourDislocation = Location.RIGHT_DISLOCATION;
        else
            ourDislocation = Location.LEFT_DISLOCATION;
    }

    private void mapIndestructibles(List<Indestructible> indestructibles) {
        indestructibles.forEach(indestructible -> {
            this.indestructibleMap.put(indestructible.getPosition(), new Object());
        });
    }

    private void initAdjacency(Size size) {
        int width = size.getWidth();
        int height = size.getHeight();
        graph = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Position currentPosition = new Position(x, y);
                graph[x][y] = indestructibleMap.containsKey(currentPosition) ? INF : 1;
            }
        }
    }


    class Location {
        public static final byte LEFT_DISLOCATION = 0;
        public static final byte RIGHT_DISLOCATION = 1;
    }



}


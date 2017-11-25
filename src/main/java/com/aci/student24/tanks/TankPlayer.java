package com.aci.student24.tanks;

import java.util.List;
import java.util.stream.Collectors;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Base;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

public class TankPlayer implements Algorithm {
    private int teamId;
    private Base enemyBase;
    private Base ourBase;
    private byte ourDislocation;
    private int count = 0;


    @Override
    public void setMyId(final int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        if (count == 0)
            initBaseParametrs(mapState);
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
}

class Location {
    public static final byte LEFT_DISLOCATION = 0;
    public static final byte RIGHT_DISLOCATION = 1;
}
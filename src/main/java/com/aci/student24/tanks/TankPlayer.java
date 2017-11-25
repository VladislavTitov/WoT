package com.aci.student24.tanks;

import java.util.*;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Base;
import com.aci.student24.api.tanks.objects.Indestructible;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;
import javafx.geometry.Pos;

import java.util.stream.Collectors;


public class TankPlayer implements Algorithm {
    private int teamId;
    private Base enemyBase;
    private Base ourBase;

    private String targetTag;
    private String remboTag;

    private byte ourDislocation;
    private int count = 0;
    private Map<String, Position> mainMapRepresentation = new HashMap<>();
    private List<Indestructible> indestructibles;
    private List<String> remboPath;

    private Tank rembo;
    int width;
    int height;


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


        return mapState.getTanks(teamId).stream().map(tank -> {
            if (tank.equals(rembo)){
                Position desiredPos = mainMapRepresentation.get(remboPath.get(count));
                byte direction = move(rembo.getPosition(),desiredPos);
                return new TankMove(rembo.getId(),direction,true);
            }
            return new TankMove(tank.getId(),Direction.NO, false);
        }).collect(Collectors.toList());
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

    private void initBaseParametrs(MapState mapState) {
        indestructibles = mapState.getIndestructibles();
        width = mapState.getSize().getWidth();
        height = mapState.getSize().getHeight();

        rembo = mapState.getTanks(teamId).get(0);
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

        for (int x = 0; x < mapState.getSize().getWidth(); x++) {
            for (int y = 0; y < mapState.getSize().getHeight(); y++) {
                Position currentPosition = new Position(x, y);
                if (isWall(currentPosition))
                    continue;
                String key = String.valueOf(x) + String.valueOf(y);
                if (currentPosition.equals(enemyBase.getPosition()))
                    targetTag = key;
                if (currentPosition.equals(rembo.getPosition()))
                    remboTag = key;
                mainMapRepresentation.put(key, currentPosition);
            }
        }
        remboPath = new Dijsktra().getPath(remboTag, targetTag);
    }

    private boolean isWall(Position checkingPosition) {
        for (Indestructible indestructible : indestructibles) {
            if (checkingPosition.equals(indestructible)) {
                return true;
            }
        }
        return false;
    }


    class Location {
        public static final byte LEFT_DISLOCATION = 0;
        public static final byte RIGHT_DISLOCATION = 1;
    }


    class Dijsktra {

        private static final int MAIN_DISTANCE = 1;

        public List<String> getPath(String rembo, String base) {
            Graph g = new Graph();

            Position up;
            Position down;
            Position left;
            Position right;

            Position position;
            Position positionCompare;
            List<Vertex> vertices;
            for (String key : mainMapRepresentation.keySet()) {
                vertices = new ArrayList<>();
                position = mainMapRepresentation.get(key);

                for (String keyCompare : mainMapRepresentation.keySet()) {
                    positionCompare = mainMapRepresentation.get(keyCompare);
                    if (position.equals(positionCompare))
                        continue;
                    up = new Position(position.getX(), position.getY() + 1);
                    down = new Position(position.getX(), position.getY() - 1);
                    left = new Position(position.getX() - 1, position.getY());
                    right = new Position(position.getX() + 1, position.getY());

                    if (positionCompare.equals(up)
                            || positionCompare.equals(down)
                            || positionCompare.equals(left)
                            || positionCompare.equals(right)) {
                        vertices.add(new Vertex(keyCompare, MAIN_DISTANCE));
                    }
                }
                g.addVertex(key, vertices);
            }

            return g.getShortestPath(rembo, base);
        }

    }

    class Vertex implements Comparable<Vertex> {

        private String id;
        private Integer distance;

        public Vertex(String id, Integer distance) {
            super();
            this.id = id;
            this.distance = distance;
        }

        public String getId() {
            return id;
        }

        public Integer getDistance() {
            return distance;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setDistance(Integer distance) {
            this.distance = distance;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((distance == null) ? 0 : distance.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Vertex other = (Vertex) obj;
            if (distance == null) {
                if (other.distance != null)
                    return false;
            } else if (!distance.equals(other.distance))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Vertex [id=" + id + ", distance=" + distance + "]";
        }

        @Override
        public int compareTo(Vertex o) {
            if (this.distance < o.distance)
                return -1;
            else if (this.distance > o.distance)
                return 1;
            else
                return this.getId().compareTo(o.getId());
        }

    }

    class Graph {

        private final Map<String, List<Vertex>> vertices;

        public Graph() {
            this.vertices = new HashMap<String, List<Vertex>>();
        }

        public void addVertex(String String, List<Vertex> vertex) {
            this.vertices.put(String, vertex);
        }

        public List<String> getShortestPath(String start, String finish) {
            final Map<String, Integer> distances = new HashMap<String, Integer>();
            final Map<String, Vertex> previous = new HashMap<String, Vertex>();
            PriorityQueue<Vertex> nodes = new PriorityQueue<Vertex>();

            for (String vertex : vertices.keySet()) {
                if (vertex == start) {
                    distances.put(vertex, 0);
                    nodes.add(new Vertex(vertex, 0));
                } else {
                    distances.put(vertex, Integer.MAX_VALUE);
                    nodes.add(new Vertex(vertex, Integer.MAX_VALUE));
                }
                previous.put(vertex, null);
            }

            while (!nodes.isEmpty()) {
                Vertex smallest = nodes.poll();
                if (smallest.getId() == finish) {
                    final List<String> path = new ArrayList<String>();
                    while (previous.get(smallest.getId()) != null) {
                        path.add(smallest.getId());
                        smallest = previous.get(smallest.getId());
                    }
                    return path;
                }

                if (distances.get(smallest.getId()) == Integer.MAX_VALUE) {
                    break;
                }

                for (Vertex neighbor : vertices.get(smallest.getId())) {
                    Integer alt = distances.get(smallest.getId()) + neighbor.getDistance();
                    if (alt < distances.get(neighbor.getId())) {
                        distances.put(neighbor.getId(), alt);
                        previous.put(neighbor.getId(), smallest);

                        forloop:
                        for (Vertex n : nodes) {
                            if (n.getId() == neighbor.getId()) {
                                nodes.remove(n);
                                n.setDistance(alt);
                                nodes.add(n);
                                break forloop;
                            }
                        }
                    }
                }
            }

            return new ArrayList<String>(distances.keySet());
        }

    }
}






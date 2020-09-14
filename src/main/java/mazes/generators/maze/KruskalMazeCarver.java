package mazes.generators.maze;

import datastructures.concrete.Graph;
import datastructures.interfaces.ISet;
import mazes.entities.Maze;
import mazes.entities.Room;
import mazes.entities.Wall;

import java.util.Random;

/**
 * Carves out a maze based on Kruskal's algorithm.
 *
 * See the spec for more details.
 */
public class KruskalMazeCarver implements MazeCarver {
    @Override
    public ISet<Wall> returnWallsToRemove(Maze maze) {
        Random randy = new Random();
        ISet<Wall> walls = maze.getWalls();
        for (Wall wall : walls) {
            wall.setDistance(randy.nextDouble());
        }

        Graph<Room, Wall> graph = new Graph<>(maze.getRooms(), walls);
        ISet<Wall> wallsMst = graph.findMinimumSpanningTree();

        // Reset distances.
        for (Wall wall : walls) {
            wall.resetDistanceToOriginal();
        }

        return wallsMst;
    }
}

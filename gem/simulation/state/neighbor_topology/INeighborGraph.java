package gem.simulation.state.neighbor_topology;

import java.awt.Point;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

public interface INeighborGraph extends Graph<Point, DefaultWeightedEdge> {
	Set<Point> getAllLocations();
	Point[] getAllLocationsTouching(int x, int y);
	Point[] getAllLocationsTouching(Point p);
	Point[] getLocationsInfluencedBy(int x, int y);
	Point[] getLocationsThatInfluence(int x, int y);
	Set<DefaultWeightedEdge> getOutgoingInfluence(int x, int y);
	Set<DefaultWeightedEdge> getIncomingInfluence(int x, int y);
	Set<DefaultWeightedEdge> getOutgoingInfluence(Point p);
	Set<DefaultWeightedEdge> getIncomingInfluence(Point p);
	Point getOtherNodeInEdge(Point p, DefaultWeightedEdge e);
	boolean areNeighbors(Point p1, Point p2);
}
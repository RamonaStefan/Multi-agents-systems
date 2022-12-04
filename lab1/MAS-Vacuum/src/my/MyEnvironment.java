package my;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.List;
import base.Action;
import base.Perceptions;
import gridworld.AbstractGridEnvironment;
import gridworld.GridOrientation;
import gridworld.GridPosition;
import gridworld.GridRelativeOrientation;

/**
 * Your implementation of the environment in which cleaner agents work.
 *
 * @author Andrei Olaru
 */
public class MyEnvironment extends AbstractGridEnvironment
{
	/**
	 * Actions for the cleaner agent.
	 *
	 * @author Andrei Olaru
	 */
	public static enum MyAction implements Action {
		
		/**
		 * The robot must move forward.
		 */
		FORWARD,
		
		/**
		 * The robot must turn to the left.
		 */
		TURN_LEFT,
		
		/**
		 * The robot must turn to the right.
		 */
		TURN_RIGHT,
		
		/**
		 * The robot must clean the current tile.
		 */
		PICK,
		
	}
	
	/**
	 * The perceptions of an agent.
	 * 
	 * @author andreiolaru
	 */
	public static class MyAgentPerceptions implements Perceptions
	{
		/**
		 * Obstacles perceived. They are percieved as a relative orientation at which an obstacled is neighbor to the
		 * agent. The orientation is relative to the absolute orientation of the agent.
		 */
		protected Set<GridRelativeOrientation>	obstacles;
		/**
		 * <code>true</code> if there is trash in the current tile.
		 */
		protected boolean						isOverJtile;
		/**
		 * The indication of the agent's compass.
		 */
		protected GridOrientation				absoluteOrientation;
		
		/**
		 * @param obstacleOrientations
		 *            - Obstacles perceived. They are percieved as a relative orientation at which an obstacled is
		 *            neighbor to the agent. The orientation is relative to the absolute orientation of the agent.
		 * @param jTile
		 *            - <code>true</code> if there is trash in the current tile.
		 * @param orientation
		 *            - The indication of the agent's compass.
		 */
		public MyAgentPerceptions(Set<GridRelativeOrientation> obstacleOrientations, boolean jTile,
				GridOrientation orientation)
		{
			obstacles = new HashSet<>(obstacleOrientations);
			isOverJtile = jTile;
			absoluteOrientation = orientation;
		}
		
		/**
		 * @return the obstacles, each obstacle perceived as a relative orientation at which the obstacled is neighbor
		 *         to the agent's current position. The orientation is relative to the absolute orientation of the
		 *         agent.
		 */
		public Set<GridRelativeOrientation> getObstacles()
		{
			return obstacles;
		}
		
		/**
		 * @return <code>true</code> if there is trash at the agent's current position.
		 */
		public boolean isOverJtile()
		{
			return isOverJtile;
		}
		
		/**
		 * @return the indication of the agent's compass.
		 */
		public GridOrientation getAbsoluteOrientation()
		{
			return absoluteOrientation;
		}
		
	}
	
	/**
	 * Default constructor. This should call one of the {@link #initialize} methods offered by the super class.
	 */
	public MyEnvironment()
	{
		long seed = System.currentTimeMillis(); // new random experiment
		// long seed = 42L; // existing random experiment
		System.out.println("seed: [" + seed + "]");
		Random rand = new Random(seed);
		
		super.initialize(10, 10, 10, 5, rand);
	}
	
	@Override
	public void step()
	{
		// TODO Auto-generated method stub
		// this should iterate through all agents, provide them with perceptions, and apply the
		// action they return.
		List<GridAgentData> agents = getAgentsData();
		for (GridAgentData agent: agents) {
			Set<GridPosition> jTiles = getJtiles();
			Set<GridPosition> xTiles = getXtiles();
			// current position
			GridPosition position = agent.getPosition();
			// current orientation
			GridOrientation orientation = agent.getOrientation();
			// check if tile is dirty
			boolean isOverJtile = jTiles.contains(position);

			// obstacles orientations
			Set<GridRelativeOrientation> obstacleOrientations = new HashSet<>();

			for(GridRelativeOrientation orient : GridRelativeOrientation.values()) {
				GridPosition neighborPosition = position.getNeighborPosition(orientation, orient);
				// check if neighbor tile is obstacle
				if (xTiles.contains(neighborPosition)) {
					obstacleOrientations.add(orient);
				}
			}

			// build perceptions
			MyAgentPerceptions perceptions = new MyAgentPerceptions(obstacleOrientations, isOverJtile, orientation);
			// get action
			MyAction action = (MyAction) agent.getAgent().response(perceptions);
			// apply action
			switch(action) {
				case PICK:
					cleanTile(position);
					break;
				case FORWARD:
					GridPosition nextPosition = position.getNeighborPosition(orientation, GridRelativeOrientation.FRONT);
					agent.setPosition(nextPosition);
					break;
				case TURN_RIGHT:
					GridOrientation rightOrientation = orientation.computeRelativeOrientation(GridRelativeOrientation.RIGHT);
					agent.setOrientation(rightOrientation);
					break;
				case TURN_LEFT:
					GridOrientation leftOrientation = orientation.computeRelativeOrientation(GridRelativeOrientation.LEFT);
					agent.setOrientation(leftOrientation);
					break;
			}
		}
	}
}

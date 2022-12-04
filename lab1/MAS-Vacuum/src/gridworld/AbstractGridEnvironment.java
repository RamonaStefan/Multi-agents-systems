package gridworld;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import base.Agent;
import base.Environment;

/**
 * Abstract implementation of an environment.
 *
 * @author Andrei Olaru
 */
public abstract class AbstractGridEnvironment implements Environment
{
	/**
	 *
	 * @author Andrei Olaru
	 */
	public static class GridAgentData extends AgentData
	{
		/**
		 * he agent's position.
		 */
		protected GridPosition		position;
		/**
		 * The agent's orientation.
		 */
		protected GridOrientation	orientation;
		
		/**
		 * The number of points held by the agent, according to the point system.
		 */
		private float				points	= 0;
		
		/**
		 * Constructor.
		 *
		 * @param linkedAgent
		 *            - the agent.
		 * @param currentPosition
		 *            - the position.
		 * @param currentOrientation
		 *            - the orientation.
		 */
		public GridAgentData(Agent linkedAgent, GridPosition currentPosition, GridOrientation currentOrientation)
		{
			super(linkedAgent);
			position = currentPosition;
			orientation = currentOrientation;
		}
		
		/**
		 * @return the agent
		 */
		public Agent getAgent()
		{
			return agent;
		}
		
		/**
		 * @return the position
		 */
		public GridPosition getPosition()
		{
			return position;
		}
		
		/**
		 * @return the orientation
		 */
		public GridOrientation getOrientation()
		{
			return orientation;
		}
		
		/**
		 * @param position
		 *            the position to set
		 */
		public void setPosition(GridPosition position)
		{
			this.position = position;
		}
		
		/**
		 * @param orientation
		 *            the orientation to set
		 */
		public void setOrientation(GridOrientation orientation)
		{
			this.orientation = orientation;
		}
		
		/**
		 * @return the points
		 */
		public float getPoints()
		{
			return points;
		}
		
		/**
		 * @param delta
		 *            - number of points to add; may be negative.
		 */
		public void addPoints(float delta)
		{
			points += delta;
		}
	}
	
	/**
	 * Structure storing information about a nearby agent.
	 * 
	 * @author Andrei Olaru
	 */
	public static class NearbyAgent
	{
		/**
		 * The relative orientation of the position of the nearby agent, relative to the position and orientation of the
		 * perceiving agent.
		 */
		public GridRelativeOrientation	orientation;
		/**
		 * Flag indicating whether the nearby agent is cognitive.
		 */
		public boolean					isCognitive;
		/**
		 * The number of points of the nearby agent.
		 */
		public int						points;
		
		/**
		 * Creates a new structure indicating information about a nearby agent.
		 * 
		 * @param agentOrientation
		 *            - its orientation relative to this agent.
		 * @param isAgentCognitive
		 *            - is it cognitive?
		 * @param agentPoints
		 *            - its points.
		 */
		public NearbyAgent(GridRelativeOrientation agentOrientation, boolean isAgentCognitive, int agentPoints)
		{
			orientation = agentOrientation;
			isCognitive = isAgentCognitive;
			points = agentPoints;
		}
	}
	
	/**
	 * List of all the positions in the environment.
	 */
	protected Set<GridPosition>		positions;
	/**
	 * List of all the J-tiles in the environment. It must be dynamically updated when J-tiles are cleaned.
	 */
	protected Set<GridPosition>		Jtiles;
	/**
	 * List of all the X-tiles in the environment.
	 */
	protected Set<GridPosition>		Xtiles;
	/**
	 * List of all the agents in the environment.
	 */
	protected List<GridAgentData>	agents	= new ArrayList<>();
	
	/**
	 * @return <code>true</code> if there are no more JTiles.
	 */
	@Override
	public boolean goalsCompleted()
	{
		return Jtiles.isEmpty();
	}
	
	@Override
	public void addAgent(AgentData agentData)
	{
		agents.add((GridAgentData) agentData);
	}
	
	/**
	 * @return the agents
	 */
	protected List<GridAgentData> getAgentsData()
	{
		return agents;
	}
	
	/**
	 * Minimum x coordinate.
	 */
	protected int	x0;
	/**
	 * Maximum x coordinate.
	 */
	protected int	x1;
	/**
	 * Minimum y coordinate.
	 */
	protected int	y0;
	/**
	 * Maximum y coordinate.
	 */
	protected int	y1;
	/**
	 * Width of displayed cells.
	 */
	protected int	cellW	= 2;
	/**
	 * Height of displayed cells.
	 */
	protected int	cellH	= 2;
	
	/**
	 * Initializes the environment with the specified lists of positions, J-tiles, and X-tiles.
	 * <p>
	 * Adds all positions, computes boundaries.
	 *
	 * @param allPositions
	 *            - the set of all existing positions in the environment.
	 * @param environmentJtiles
	 *            - the set of positions (included in <code>allPositions</code>) that contain junk.
	 * @param environmentXtiles
	 *            - the set of positions (included in <code>allPositions</code>) that contain objects.
	 */
	protected void initialize(Set<GridPosition> allPositions, Set<GridPosition> environmentJtiles,
			Set<GridPosition> environmentXtiles)
	{
		positions = allPositions;
		Jtiles = environmentJtiles;
		Xtiles = environmentXtiles;
		
		GridPosition pos = allPositions.iterator().next();
		x0 = x1 = pos.positionX;
		y0 = y1 = pos.positionY;
		for(GridPosition p : allPositions)
		{
			GridPosition gp = p;
			if(gp.positionX < x0)
				x0 = gp.positionX;
			if(gp.positionX > x1)
				x1 = gp.positionX;
			if(gp.positionY < y0)
				y0 = gp.positionY;
			if(gp.positionY > y1)
				y1 = gp.positionY;
		}
	}
	
	/**
	 * Initializes the environment with the provided width, height and number of J- and X-tiles.
	 *
	 * @param w
	 *            - width
	 * @param h
	 *            - height
	 * @param nJtiles
	 *            - number of generated J-tiles.
	 * @param nXtiles
	 *            - number of generated X-tiles.
	 * @param rand
	 *            - random number generator to use.
	 */
	protected void initialize(int w, int h, int nJtiles, int nXtiles, Random rand)
	{
		Set<GridPosition> all = new HashSet<>();
		for(int i = 0; i <= w + 1; i++)
			for(int j = 0; j <= h + 1; j++)
				all.add(new GridPosition(i, j));
		Set<GridPosition> xs = new HashSet<>();
		for(int i = 0; i <= w + 1; i++)
		{
			xs.add(new GridPosition(i, 0));
			xs.add(new GridPosition(i, h + 1));
		}
		for(int j = 0; j <= h + 1; j++)
		{
			xs.add(new GridPosition(0, j));
			xs.add(new GridPosition(w + 1, j));
		}
		
		int attempts = nXtiles * nXtiles;
		int generated = 0;
		while(attempts > 0 && generated < nXtiles)
		{
			int x = rand.nextInt(w) + 1;
			int y = rand.nextInt(h) + 1;
			GridPosition pos = new GridPosition(x, y);
			
			boolean ok = true;
			for(GridPosition xtile : xs)
				if(pos.getDistanceTo(xtile) <= 2)
					ok = false;
			if(ok)
			{
				generated++;
				xs.add(pos);
			}
			
			attempts--;
		}
		if(generated < nXtiles)
			System.out.println("Failed to generate all required X-tiles");
		
		Set<GridPosition> js = new HashSet<>();
		attempts = nJtiles * nJtiles;
		generated = 0;
		while((attempts > 0) && (generated < nJtiles))
		{
			int x = rand.nextInt(w) + 1;
			int y = rand.nextInt(h) + 1;
			GridPosition pos = new GridPosition(x, y);
			
			if(!js.contains(pos) && !xs.contains(pos))
			{
				js.add(pos);
				generated++;
			}
			
			attempts--;
		}
		if(generated < nJtiles)
			System.out.println("Failed to generate all required J-tiles");
		
		initialize(all, js, xs);
	}
	
	@Override
	public String printToString()
	{
		// border top
		String res = "";
		res += "  |";
		for(int i = x0; i <= x1; i++)
		{
			for(int k = 0; k < cellW - (i >= 10 ? 2 : 1); k++)
				res += " ";
			res += i + "|";
		}
		res += "\n";
		res += "--+";
		for(int i = x0; i <= x1; i++)
		{
			for(int k = 0; k < cellW; k++)
				res += "-";
			res += "+";
		}
		res += "\n";
		// for each line
		for(int j = y1; j >= y0; j--)
		{
			// first cell row
			res += (j < 10 ? " " : "") + j + "|";
			for(int i = x0; i <= x1; i++)
			{
				GridPosition pos = new GridPosition(i, j);
				String agentString = "";
				for(GridAgentData agent : agents)
					if(agent.getPosition().equals(pos))
						agentString += agent.getOrientation().toString() + agent.getAgent().toString();
				int k = 0;
				if(Xtiles.contains(pos))
					for(; k < cellW; k++)
						res += "X";
				if((cellH < 2) && Jtiles.contains(pos))
				{
					res += "~";
					k++;
				}
				if(agentString.length() > 0)
				{
					if(cellW == 1)
					{
						if(agentString.length() > 1)
							res += ".";
						else
							res += agentString;
						k++;
					}
					else
					{
						res += agentString.substring(0, Math.min(agentString.length(), cellW - k));
						k += Math.min(agentString.length(), cellW - k);
					}
				}
				for(; k < cellW; k++)
					res += " ";
				
				res += "|";
			}
			res += "\n";
			// second cellrow
			res += "  |";
			for(int i = x0; i <= x1; i++)
			{
				GridPosition pos = new GridPosition(i, j);
				for(int k = 0; k < cellW; k++)
					if(Xtiles.contains(pos))
						res += "X";
					else if((k == 0) && Jtiles.contains(pos))
						res += "~";
					else
						res += " ";
				res += "|";
			}
			res += "\n";
			// other cell rows
			for(int ky = 0; ky < cellH - 2; ky++)
			{
				res += "|";
				for(int i = x0; i <= x1; i++)
				{
					for(int k = 0; k < cellW; k++)
						res += Xtiles.contains(new GridPosition(i, j)) ? "X" : " ";
					res += "|";
				}
				res += "\n";
			}
			res += "--+";
			for(int i = x0; i <= x1; i++)
			{
				for(int k = 0; k < cellW; k++)
					res += "-";
				res += "+";
			}
			res += "\n";
			
		}
		return res;
	}
	
	/**
	 * @return a {@link Set} of {@link GridPosition} instances indicating all positions in the environment.
	 */
	protected Set<GridPosition> getPositions()
	{
		return convertPositions(positions);
	}
	
	/**
	 * @return the bottom-left available position in the grid (minimum x and y)
	 */
	public GridPosition getBottomLeft()
	{
		return new GridPosition(x0 + 1, y0 + 1);
	}
	
	/**
	 * @return the top-left available position in the grid (minimum x, maximum y)
	 */
	public GridPosition getTopLeft()
	{
		return new GridPosition(x0 + 1, y1 - 1);
	}
	
	/**
	 * @return the bottom-right available position in the grid (maximum x, minimum y)
	 */
	public GridPosition getBottomRight()
	{
		return new GridPosition(x1 - 1, y0 + 1);
	}
	
	/**
	 * @return the top-right available position in the grid (maximum x and y)
	 */
	public GridPosition getTopRight()
	{
		return new GridPosition(x1 - 1, y1 - 1);
	}
	
	/**
	 * @return a {@link Set} of {@link GridPosition} instances indicating all positions of J-tiles in the environment.
	 */
	protected Set<GridPosition> getJtiles()
	{
		return convertPositions(Jtiles);
	}
	
	/**
	 * @return a {@link Set} of {@link GridPosition} instances indicating all positions of X-tiles in the environment.
	 */
	protected Set<GridPosition> getXtiles()
	{
		return convertPositions(Xtiles);
	}
	
	/**
	 * Converts a set of {@link GridPosition} instances to a set of {@link GridPosition} instances.
	 *
	 * @param toConvert
	 *            - the set of positions to convert.
	 * @return the result.
	 */
	protected static Set<GridPosition> convertPositions(Set<GridPosition> toConvert)
	{
		Set<GridPosition> ret = new HashSet<>();
		for(GridPosition pos : toConvert)
			ret.add(pos);
		return ret;
	}
	
	/**
	 * Removes a position from the list of dirty tiles.
	 * 
	 * @param position
	 *            the J-tile to remove.
	 */
	protected void cleanTile(GridPosition position)
	{
		if(!Jtiles.contains(position))
			throw new IllegalArgumentException("GridPosition was not dirty");
		Jtiles.remove(position);
	}
}

package hunting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import base.Agent;
import gridworld.AbstractGridEnvironment;
import gridworld.GridOrientation;
import gridworld.GridPosition;

/**
 * Implements the necessary functionality for a grid environment.
 *
 * @author Alexandru Sorici
 */
public abstract class AbstractHuntingEnvironment extends AbstractGridEnvironment
{
	
	/**
	 * List of all the predators in the environment.
	 */
	protected List<WildlifeAgentData>	predatorAgents	= new ArrayList<>();
	
	/**
	 * List of all the prey in the environment
	 * 
	 */
	protected List<WildlifeAgentData>	preyAgents		= new ArrayList<>();
	
	/**
	 *
	 * @author Alexandru Sorici
	 */
	public static class WildlifeAgentData extends GridAgentData
	{
		/**
		 * The type of agent (predator or prey).
		 */
		private WildlifeAgentType agentType;
		
		/**
		 * Constructor.
		 *
		 * @param linkedAgent
		 *            - the agent.
		 * @param type
		 *            - the agent type: predator or prey
		 * @param currentPosition
		 *            - the position.
		 */
		public WildlifeAgentData(Agent linkedAgent, WildlifeAgentType type, GridPosition currentPosition)
		{
			super(linkedAgent, currentPosition, GridOrientation.NORTH);
			this.agentType = type;
		}
		
		/**
		 * @return the agentType
		 */
		public WildlifeAgentType getAgentType()
		{
			return agentType;
		}
		
		/**
		 * @param agentType
		 *            the agentType to set
		 */
		public void setAgentType(WildlifeAgentType agentType)
		{
			this.agentType = agentType;
		}
	}
	
	@Override
	public void addAgent(AgentData agentData)
	{
		addWildlifeAgent((WildlifeAgentData) agentData);
	}
	
	/**
	 * @param agentData
	 *            - the data about the agent to add.
	 */
	public void addWildlifeAgent(WildlifeAgentData agentData)
	{
		switch(agentData.getAgentType())
		{
		case PREDATOR:
			predatorAgents.add(agentData);
			break;
		case PREY:
			preyAgents.add(agentData);
			break;
		default:
			throw new IllegalArgumentException("Wildlife agent type unrecognized: " + agentData.getAgentType());
		}
		
		// add to global list of agents as well
		agents.add(agentData);
	}
	
	/**
	 * Initializes the environment with the provided width, height and number of predator and prey agents.
	 *
	 * @param w
	 *            - width
	 * @param h
	 *            - height
	 * @param predators
	 *            - predator agents to place on the map.
	 * @param prey
	 *            - prey agents to place on the map.
	 * @param rand
	 *            - random number generator to use.
	 */
	protected void initialize(int w, int h, List<Agent> predators, List<Agent> prey, Random rand)
	{
		int numPredators = predators.size();
		int numPrey = prey.size();
		
		// generate all positions
		Set<GridPosition> all = new HashSet<>();
		for(int i = 0; i <= w + 1; i++)
			for(int j = 0; j <= h + 1; j++)
				all.add(new GridPosition(i, j));
			
		// generate all wall x-tiles
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
		
		// generate all predators
		int attempts = (predators.size() + 1) * (predators.size() + 1);
		int generated = 0;
		while(attempts > 0 && generated < numPredators)
		{
			int x = rand.nextInt(w) + 1;
			int y = rand.nextInt(h) + 1;
			GridPosition pos = new GridPosition(x, y);
			
			boolean ok = true;
			for(WildlifeAgentData ad : predatorAgents)
				if(pos.getDistanceTo(ad.getPosition()) <= 1)
					ok = false;
				
			if(ok)
			{
				generated++;
				addWildlifeAgent(new WildlifeAgentData(predators.get(0), WildlifeAgentType.PREDATOR, pos));
				predators.remove(0);
			}
			
			attempts--;
		}
		
		if(generated < numPredators)
			System.out.println("Failed to generate all required predator agents");
		
		// generate all prey
		attempts = (prey.size() + 1) * (prey.size() + 1);
		generated = 0;
		while(attempts > 0 && generated < numPrey)
		{
			int x = rand.nextInt(w) + 1;
			int y = rand.nextInt(h) + 1;
			GridPosition pos = new GridPosition(x, y);
			
			boolean ok = true;
			for(WildlifeAgentData ad : predatorAgents)
				if(pos.getDistanceTo(ad.getPosition()) <= 2)
					ok = false;
				
			for(WildlifeAgentData ad : preyAgents)
				if(pos.getDistanceTo(ad.getPosition()) <= 1)
					ok = false;
				
			if(ok)
			{
				generated++;
				addWildlifeAgent(new WildlifeAgentData(prey.get(0), WildlifeAgentType.PREY, pos));
				prey.remove(0);
			}
			
			attempts--;
		}
		if(generated < numPrey)
			System.out.println("Failed to generate all required prey agents");
		
		initialize(all, new HashSet<GridPosition>(), xs);
	}
	
	/**
	 * @return the predatorAgents
	 */
	public List<WildlifeAgentData> getPredatorAgents()
	{
		return predatorAgents;
	}
	
	/**
	 * @return the preyAgents
	 */
	public List<WildlifeAgentData> getPreyAgents()
	{
		return preyAgents;
	}
	
	/**
	 * Remove a prey agent.
	 * 
	 * @param agentData
	 *            - the agent to remove, as a reference to its corresponding {@link WildlifeAgentData} instance.
	 */
	protected void removePreyAgent(WildlifeAgentData agentData)
	{
		preyAgents.remove(agentData);
	}
	
	/**
	 * Returns the set of obstacles which are at a distance from a given position by at most <code>range</code>.
	 * 
	 * @param pos
	 *            - the position of the agent.
	 * @param range
	 *            - the range the agent can observe.
	 * @return The set of positions where obstacles are found
	 */
	public Set<GridPosition> getNearbyObstacles(GridPosition pos, int range)
	{
		Set<GridPosition> nearbyObstacles = new HashSet<>();
		for(GridPosition obs : Xtiles)
			if(pos.getDistanceTo(obs) <= range)
				nearbyObstacles.add(new GridPosition(obs));
		return nearbyObstacles;
	}
	
	/**
	 * Returns the set of predator agents which are at a distance from a given position by at most <code>range</code>.
	 * 
	 * @param pos
	 *            Position around which to determine the nearby agents
	 * @param range
	 *            - the range the agent can observe.
	 * @param exclude
	 *            - agent to exclude from the list (for instance, because this is used to generate perceptions for that
	 *            agent).
	 * @return The set of nearby agents given as {@link WildlifeAgentData} instances.
	 */
	public Set<WildlifeAgentData> getNearbyPredators(GridPosition pos, int range, WildlifeAgentData exclude)
	{
		Set<WildlifeAgentData> nearbyAgents = new HashSet<>();
		for(WildlifeAgentData predator : predatorAgents)
			if(pos.getDistanceTo(predator.getPosition()) <= range && predator != exclude)
				nearbyAgents.add(predator);
		return nearbyAgents;
	}
	
	/**
	 * Returns the set of prey agents which are at a distance from a given position by at most <code>range</code>.
	 * 
	 * @param pos
	 *            Position around which to determine the nearby agents
	 * @param range
	 *            - the range the agent can observe.
	 * @return The set of nearby agents given as {@link WildlifeAgentData} instances.
	 */
	public Set<WildlifeAgentData> getNearbyPrey(GridPosition pos, int range)
	{
		Set<WildlifeAgentData> nearbyAgents = new HashSet<>();
		for(WildlifeAgentData prey : preyAgents)
			if(pos.getDistanceTo(prey.getPosition()) <= range)
				nearbyAgents.add(prey);
		return nearbyAgents;
	}
	
	/**
	 * Remove dead prey. The condition for a prey being killed is that there be either one predator at a Manhattan
	 * distance of 1, or at least two predators, each at a Manhattan distance of 2 or less from the prey
	 */
	public void removeDeadPrey()
	{
		Iterator<WildlifeAgentData> preyIt = preyAgents.iterator();
		
		while(preyIt.hasNext())
		{
			WildlifeAgentData prey = preyIt.next();
			
			boolean hasNeighborPredator = false;
			int numClosePredators = 0;
			
			GridPosition preyGP = prey.getPosition();
			Set<String> predators = new HashSet<>();
			
			for(WildlifeAgentData predator : predatorAgents)
			{
				int predatorDist = preyGP.getDistanceTo(predator.getPosition());
				
				if(predatorDist <= 1)
				{
					hasNeighborPredator = true;
					predators.clear();
					predators.add(predator.getAgent().toString());
					break;
				}
				else if(predatorDist <= 2)
				{
					numClosePredators++;
					predators.add(predator.getAgent().toString());
					
					if(numClosePredators >= 2)
					{
						break;
					}
				}
			}
			
			if(hasNeighborPredator || numClosePredators >= 2)
			{
				System.out.println("Prey " + prey.getAgent().toString() + " is dead, killed by " + predators);
				preyIt.remove();
				agents.remove(prey);
			}
		}
	}
	
	@Override
	public String printToString()
	{
		// List<GridAgentData> agents = new ArrayList<>();
		// agents.addAll(predatorAgents);
		// agents.addAll(preyAgents);
		
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
						agentString += agent.getAgent().toString();
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
	
	// /**
	// * Return a grid position at a given dx and dy offset
	// *
	// * @param pos
	// * Position from which to compute the offsets
	// * @param dx
	// * X-axis offset
	// * @param dy
	// * Y-axis offset
	// * @return The new GridPosition
	// * @throws IllegalArgumentException
	// * if the new GridPosition is off the map
	// */
	// public GridPosition getPositionAtDelta(GridPosition pos, int dx, int dy) throws IllegalArgumentException {
	// int minX = getBottomLeft().getX();
	// int minY = getBottomLeft().getY();
	//
	// int maxX = getTopRight().getX();
	// int maxY = getTopRight().getY();
	//
	// if (pos.getX() + dx >= minX && pos.getX() + dx <= maxX) {
	// if (pos.getY() + dy >= minY && pos.getY() + dy <= maxY)
	// return new GridPosition(pos.getX() + dx, pos.getY() + dy);
	// throw new IllegalArgumentException(
	// "Agent position at (" + (pos.getX() + dx) + ", " + (pos.getY() + dy) + ") exceeds bounds ");
	// }
	// throw new IllegalArgumentException(
	// "Agent position at (" + (pos.getX() + dx) + ", " + (pos.getY() + dy) + ") exceeds bounds ");
	// }
	
	@Override
	public boolean goalsCompleted()
	{
		return preyAgents.isEmpty();
	}
}

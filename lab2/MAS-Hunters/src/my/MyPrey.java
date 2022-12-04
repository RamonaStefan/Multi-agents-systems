package my;

import base.Action;
import base.Perceptions;
import gridworld.GridPosition;
import gridworld.GridRelativeOrientation;
import gridworld.ProbabilityMap;
import hunting.AbstractWildlifeAgent;
import hunting.WildlifeAgentType;
import my.MyEnvironment.MyAction;
import my.MyEnvironment.MyPerceptions;

/**
 * Implementation for Pray agents.
 * 
 * @author Alexandru Sorici
 */
public class MyPrey extends AbstractWildlifeAgent
{
	/**
	 * Default probability for action.
	 */
	protected static final double	UP_PROB		= 0.25;
	/**
	 * Default probability for action.
	 */
	protected static final double	LEFT_PROB	= 0.25;
	/**
	 * Default probability for action.
	 */
	protected static final double	RIGHT_PROB	= 0.25;
	/**
	 * Default probability for action.
	 */
	protected static final double	DOWN_PROB	= 0.25;
	
	/**
	 * Default constructor.
	 */
	public MyPrey()
	{
		super(WildlifeAgentType.PREY);
	}
	
	@Override
	public Action response(Perceptions perceptions)
	{
		MyPerceptions wildlifePerceptions = (MyPerceptions) perceptions;
		GridPosition agentPos = wildlifePerceptions.getAgentPos();
		
		ProbabilityMap probMap = new ProbabilityMap();
		probMap.put(MyAction.NORTH, 0.25);
		probMap.put(MyAction.SOUTH, 0.25);
		probMap.put(MyAction.WEST, 0.25);
		probMap.put(MyAction.EAST, 0.25);
		
		// remove actions which are unavailable because of obstacles
		for(GridPosition obs : wildlifePerceptions.getObstacles())
		{
			if(agentPos.getDistanceTo(obs) > 1)
				continue;
			GridRelativeOrientation relativeOrientation = agentPos.getRelativeOrientation(obs);
			
			switch(relativeOrientation)
			{
			case FRONT:
				// don't go up
				probMap.removeAction(MyAction.NORTH);
				break;
			case BACK:
				// don't go down
				probMap.removeAction(MyAction.SOUTH);
				break;
			case LEFT:
				// don't go left
				probMap.removeAction(MyAction.WEST);
				break;
			case RIGHT:
				// don't go right
				probMap.removeAction(MyAction.EAST);
				break;
			default:
				break;
			}
		}
		
		// save available moves
		ProbabilityMap availableMoves = new ProbabilityMap(probMap);
		
		// examine actions which are unavailable because of predators
		for(GridPosition predatorPos : wildlifePerceptions.getNearbyPredators().values())
		{
			GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(predatorPos);
			
			switch(relativePos)
			{
			case FRONT:
				probMap.removeAction(MyAction.NORTH);
				break;
			case FRONT_LEFT:
				probMap.removeAction(MyAction.NORTH);
				probMap.removeAction(MyAction.WEST);
				break;
			case FRONT_RIGHT:
				probMap.removeAction(MyAction.NORTH);
				probMap.removeAction(MyAction.EAST);
				break;
			case BACK:
				probMap.removeAction(MyAction.SOUTH);
				break;
			case BACK_LEFT:
				probMap.removeAction(MyAction.SOUTH);
				probMap.removeAction(MyAction.WEST);
				break;
			case BACK_RIGHT:
				probMap.removeAction(MyAction.SOUTH);
				probMap.removeAction(MyAction.EAST);
				break;
			case LEFT:
				probMap.removeAction(MyAction.WEST);
				break;
			case RIGHT:
				probMap.removeAction(MyAction.EAST);
				break;
			default:
				break;
			}
		}
		
		if(!probMap.isEmpty())
			return probMap.choice();
		return availableMoves.choice();
	}
}

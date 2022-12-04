package my;

import base.Action;
import base.Perceptions;
import communication.AgentID;
import gridworld.GridPosition;
import gridworld.GridRelativeOrientation;
import hunting.AbstractWildlifeAgent;
import hunting.WildlifeAgentType;

import java.util.*;
import java.util.Random;
import java.util.Set;

/**
 * Implementation for predator agents.
 * 
 * @author Alexandru Sorici
 */
public class MyPredator extends AbstractWildlifeAgent
{
	Set<GridPosition> preysPositions;
	/**
	 * Default constructor.
	 */
	public MyPredator()
	{
		super(WildlifeAgentType.PREDATOR);
	}
	
	@Override
	public Action response(Perceptions perceptions)
	{
		// TODO Auto-generated method stub
		MyEnvironment.MyPerceptions wildlifePerceptions = (MyEnvironment.MyPerceptions) perceptions;
		GridPosition agentPos = wildlifePerceptions.getAgentPos();
		Set<GridPosition> nearbyPreys = wildlifePerceptions.getNearbyPrey();
		Map<AgentID, GridPosition> nearbyPredators = wildlifePerceptions.getNearbyPredators();

		Set<MyEnvironment.MyAction> map = new HashSet<>();
		map.add(MyEnvironment.MyAction.NORTH);
		map.add(MyEnvironment.MyAction.SOUTH);
		map.add(MyEnvironment.MyAction.WEST);
		map.add(MyEnvironment.MyAction.EAST);
		// remove directions with obstacles
		for(GridPosition obs : wildlifePerceptions.getObstacles())
		{
			if(agentPos.getDistanceTo(obs) > 1)
				continue;
			GridRelativeOrientation relativeOrientation = agentPos.getRelativeOrientation(obs);
			switch(relativeOrientation)
			{
				case FRONT:
					map.remove(MyEnvironment.MyAction.NORTH);
					break;
				case BACK:
					map.remove(MyEnvironment.MyAction.SOUTH);
					break;
				case LEFT:
					map.remove(MyEnvironment.MyAction.WEST);
					break;
				case RIGHT:
					map.remove(MyEnvironment.MyAction.EAST);
					break;
			}
		}

		// get nearest prey
		Set nearbyPositionsCopy = new HashSet<>(nearbyPreys);
		do {
			GridPosition desiredPosition = getNearestPrey(nearbyPreys, agentPos);

			if (desiredPosition != null) {
				//	go towards him
				GridRelativeOrientation relativeOrientation = agentPos.getRelativeOrientation(desiredPosition);
				MyEnvironment.MyAction actionTowardsPreyAvailable = goTowardsPrey(map, relativeOrientation);
				nearbyPreys.remove(desiredPosition);
				if (actionTowardsPreyAvailable != null) {
					// save preys' positions for this step
					nearbyPositionsCopy.remove(desiredPosition);
					preysPositions = new HashSet<>(nearbyPositionsCopy);
					return actionTowardsPreyAvailable;
				}
			}
		}while(nearbyPreys.size() > 0);

		// save available moves
		Set<MyEnvironment.MyAction> availableMoves = new HashSet<>(map);

		// examine actions which are unavailable because of predators
		for(GridPosition predatorPos : nearbyPredators.values())
		{
			GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(predatorPos);
			goAwayFromPredator(map, relativePos);
		}

		if (map.isEmpty()) {
			map = availableMoves;
		}

		// check prey positions from previous step
		do {
			if (preysPositions != null && preysPositions.size() > 0) {
				GridPosition preyPosition = getNearestPrey(preysPositions, agentPos);
				if(preyPosition != null) {
					GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(preyPosition);
					MyEnvironment.MyAction actionTowardsPossiblePrey = goTowardsPrey(map, relativePos);
					if (actionTowardsPossiblePrey != null) {
						preysPositions = new HashSet<>(nearbyPositionsCopy);
						return actionTowardsPossiblePrey;
					}
					preysPositions.remove(preyPosition);
				}
			}
		}while(preysPositions != null && preysPositions.size() > 0);

		int nr = new Random().nextInt(map.size());
		return (Action) map.toArray()[nr];
	}

	public GridPosition getNearestPrey(Set<GridPosition> nearbyPreys, GridPosition agentPos) {
		int minimumDistancePrey = Integer.MAX_VALUE;
		GridPosition desiredPosition = null;
		for (GridPosition preyPosition: nearbyPreys) {
			int manhattanDistance = agentPos.getDistanceTo(preyPosition);
			if(manhattanDistance < minimumDistancePrey) {
				minimumDistancePrey = manhattanDistance;
				desiredPosition = preyPosition;
			}
		}
		return desiredPosition;
	}

	public MyEnvironment.MyAction goTowardsPrey(Set<MyEnvironment.MyAction> map, GridRelativeOrientation relativePos) {
		switch(relativePos)
		{
			case FRONT_LEFT:
				if(map.contains(MyEnvironment.MyAction.NORTH)) {
					return MyEnvironment.MyAction.NORTH;
				}
				if(map.contains(MyEnvironment.MyAction.WEST)) {
					return MyEnvironment.MyAction.WEST;
				}
				break;
			case FRONT_RIGHT:
				if(map.contains(MyEnvironment.MyAction.NORTH)) {
					return MyEnvironment.MyAction.NORTH;
				}

				if(map.contains(MyEnvironment.MyAction.EAST)) {
					return MyEnvironment.MyAction.EAST;
				}
				break;
			case FRONT:
				if(map.contains(MyEnvironment.MyAction.NORTH)) {
					return MyEnvironment.MyAction.NORTH;
				}
				break;
			case BACK_LEFT:
				if(map.contains(MyEnvironment.MyAction.SOUTH)) {
					return MyEnvironment.MyAction.SOUTH;
				}
				if(map.contains(MyEnvironment.MyAction.WEST)) {
					return MyEnvironment.MyAction.WEST;
				}
				break;
			case BACK_RIGHT:
				if(map.contains(MyEnvironment.MyAction.SOUTH)) {
					return MyEnvironment.MyAction.SOUTH;
				}
				if(map.contains(MyEnvironment.MyAction.EAST)) {
					return MyEnvironment.MyAction.EAST;
				}
				break;
			case BACK:
				if(map.contains(MyEnvironment.MyAction.SOUTH)) {
					return MyEnvironment.MyAction.SOUTH;
				}
				break;
			case LEFT:
				if(map.contains(MyEnvironment.MyAction.WEST)) {
					return MyEnvironment.MyAction.WEST;
				}
				break;
			case RIGHT:
				if(map.contains(MyEnvironment.MyAction.EAST)) {
					return MyEnvironment.MyAction.EAST;
				}
				break;
		}
		return null;
	}

	public void goAwayFromPredator(Set<MyEnvironment.MyAction> map, GridRelativeOrientation relativePos) {
		switch(relativePos)
		{
			case FRONT_LEFT:
				map.remove(MyEnvironment.MyAction.NORTH);
				map.remove(MyEnvironment.MyAction.WEST);
				break;
			case FRONT_RIGHT:
				map.remove(MyEnvironment.MyAction.NORTH);
				map.remove(MyEnvironment.MyAction.EAST);
				break;
			case FRONT:
				map.remove(MyEnvironment.MyAction.NORTH);
				break;
			case BACK_LEFT:
				map.remove(MyEnvironment.MyAction.WEST);
				map.remove(MyEnvironment.MyAction.SOUTH);
				break;
			case BACK_RIGHT:
				map.remove(MyEnvironment.MyAction.EAST);
				map.remove(MyEnvironment.MyAction.SOUTH);
				break;
			case BACK:
				map.remove(MyEnvironment.MyAction.SOUTH);
				break;
			case LEFT:
				map.remove(MyEnvironment.MyAction.WEST);
				break;
			case RIGHT:
				map.remove(MyEnvironment.MyAction.EAST);
				break;
		}
	}
}

package my;

import base.Action;
import base.Agent;
import base.Perceptions;
import communication.AgentID;
import communication.AgentMessage;
import communication.SocialAction;
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
	private Set<GridPosition> preysPositionsFromPreviousStep = new HashSet<>();
	private Set<AgentID> predatorIDs = new HashSet<>();
	private Set<GridPosition> seenPositions = new HashSet<>();
	private int minHeight = Integer.MAX_VALUE;
	private int maxHeight = 0;
	private int minWidth = Integer.MAX_VALUE;
	private int maxWidth = 0;
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
		Set<AgentMessage> agentMessages = wildlifePerceptions.getMessages();

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

		Set<GridPosition> allSeenPositions = new HashSet<>(seenPositions);
		Set<AgentID> allPredatorIDs = new HashSet<>(nearbyPredators.keySet());
		Set nearbyPreyPositionsCopy = new HashSet<>(nearbyPreys);

		for(AgentMessage ag: agentMessages){
			MessageContent mc = (MessageContent) ag.getContent();
			allPredatorIDs.addAll(mc.getPredatorIds());
			preysPositionsFromPreviousStep.addAll(mc.getPreyPositions());
			allSeenPositions.addAll(mc.getPositions());
		}

		getMaximumGridSize(allSeenPositions);

		// get nearest prey
		do {
			GridPosition desiredPosition = getNearestPrey(nearbyPreys, agentPos);

			if (desiredPosition != null) {
				//	go towards him
				GridRelativeOrientation relativeOrientation = agentPos.getRelativeOrientation(desiredPosition);
				MyEnvironment.MyAction actionTowardsPreyAvailable = goTowardsPrey(map, relativeOrientation);
				if (actionTowardsPreyAvailable != null) {
					// save preys' positions for this step
					updateMemory(agentPos, allPredatorIDs, nearbyPreyPositionsCopy, agentMessages);
					SocialAction socialAction = new SocialAction(actionTowardsPreyAvailable);
					addMessageForPredators(socialAction);
					return socialAction;
				}
				nearbyPreys.remove(desiredPosition);
			}
		}while(nearbyPreys.size() > 0);

		// save available moves
		Set<MyEnvironment.MyAction> availableMoves = new HashSet<>(map);
		// examine actions which are unavailable because of predators
		for(GridPosition predatorPos : nearbyPredators.values())
		{
			if(agentPos.getDistanceTo(predatorPos) > 2) {
				continue;
			}
			GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(predatorPos);
			goAwayFromPredator(map, relativePos);
		}
		if (map.isEmpty()) {
			map = new HashSet<>(availableMoves);
		}

		// check prey positions from previous step
		do {
			if (preysPositionsFromPreviousStep != null && preysPositionsFromPreviousStep.size() > 0) {
				GridPosition preyPosition = getNearestPrey(preysPositionsFromPreviousStep, agentPos);
				if(preyPosition != null) {
					GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(preyPosition);
					MyEnvironment.MyAction actionTowardsPossiblePrey = goTowardsPrey(map, relativePos);
					if (actionTowardsPossiblePrey != null) {
						if (getNumberPredatorsCloser(new HashSet(nearbyPredators.values()), agentPos, preyPosition) > 2) {
							continue;
						}
						updateMemory(agentPos, allPredatorIDs, nearbyPreyPositionsCopy, agentMessages);
						SocialAction socialAction = new SocialAction(actionTowardsPossiblePrey);
						addMessageForPredators(socialAction);
						return socialAction;
					}
					preysPositionsFromPreviousStep.remove(preyPosition);
				}
			}
		}while(preysPositionsFromPreviousStep != null && preysPositionsFromPreviousStep.size() > 0);

		availableMoves = new HashSet<>(map);

		// visit a new position for all agents
		for (GridPosition pos: allSeenPositions) {
			if (pos.getDistanceTo(agentPos) > 1) {
				continue;
			}
			GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(pos);
			MyEnvironment.MyAction actionTowardsPossiblePrey = goTowardsPrey(map, relativePos);
			if (actionTowardsPossiblePrey != null && map.contains(actionTowardsPossiblePrey)) {
				map.remove(actionTowardsPossiblePrey);
			}
		}

		if (map.isEmpty()) {
			map = new HashSet<>(availableMoves);
			// visit a new position for the current agent
			for (GridPosition pos : seenPositions) {
				if (pos.getDistanceTo(agentPos) > 1) {
					continue;
				}
				GridRelativeOrientation relativePos = agentPos.getRelativeOrientation(pos);
				MyEnvironment.MyAction actionTowardsPossiblePrey = goTowardsPrey(map, relativePos);
				if (actionTowardsPossiblePrey != null && map.contains(actionTowardsPossiblePrey)) {
					map.remove(actionTowardsPossiblePrey);
				}
			}
		}

		if (map.isEmpty()) {
			map = new HashSet<>(availableMoves);
		}

		MyEnvironment.MyAction action = goFarthestCorner(agentPos, map);
		SocialAction socialAction;
		if (action == null) {
			int nr = new Random().nextInt(map.size());
			socialAction = new SocialAction((MyEnvironment.MyAction) map.toArray()[nr]);
		}
		else {
			socialAction = new SocialAction(action);
		}
		addMessageForPredators(socialAction);
		updateMemory(agentPos, allPredatorIDs, nearbyPreyPositionsCopy, agentMessages);
		return socialAction;
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

	public void addMessageForPredators(SocialAction action) {
		for(AgentID agent: predatorIDs) {
			if (agent.equals(AgentID.getAgentID(this))) {
				continue;
			}
			action.addOutgoingMessage(new AgentMessage(AgentID.getAgentID(this), agent, new MessageContent(seenPositions, preysPositionsFromPreviousStep, predatorIDs)));
		}
	}

	public void updateMemory(GridPosition agentPos, Set<AgentID> predators, Set<GridPosition> preys, Set<AgentMessage> agentMessages) {
		// add seen position
		seenPositions.add(agentPos);

		// add seen predators
		predatorIDs.addAll(predators);
		predatorIDs.add(AgentID.getAgentID(this));

		// add seen preys
		preysPositionsFromPreviousStep = new HashSet<>(preys);

		for(AgentMessage ag: agentMessages){
			MessageContent mc = (MessageContent) ag.getContent();
			predatorIDs.addAll(mc.getPredatorIds());
//			preysPositions.addAll(mc.getPreyPositions());
//			seenPositions.addAll(mc.getPositions());
		}
	}

	public int getNumberPredatorsCloser(Set<GridPosition> nearbyPredators, GridPosition agentPos, GridPosition preyPosition) {
		int numberPredators = 0;
		for (GridPosition predator: nearbyPredators) {
			if (predator.getDistanceTo(preyPosition) < agentPos.getDistanceTo(preyPosition)) {
				numberPredators++;
			}
		}
		return numberPredators;
	}

	public MyEnvironment.MyAction goFarthestCorner(GridPosition agentPos, Set<MyEnvironment.MyAction> possibleActions) {
		if (maxHeight - minHeight <= 1 || maxWidth - minWidth <= 1) {
			return null;
		}
		GridRelativeOrientation relativePos;
		GridPosition leftDown = new GridPosition(minWidth, minHeight);
		GridPosition leftUp = new GridPosition(minWidth, maxHeight);
		GridPosition rightDown = new GridPosition(maxWidth, minHeight);
		GridPosition rightUp = new GridPosition(maxWidth, maxHeight);
		int distanceLeftDown = agentPos.getDistanceTo(leftDown);
		int distanceLeftUp = agentPos.getDistanceTo(leftUp);
		int distanceRightDown = agentPos.getDistanceTo(rightDown);
		int distanceRightUp = agentPos.getDistanceTo(rightUp);
		List<Integer> distances = new ArrayList();
		distances.add(distanceLeftDown);
		distances.add(distanceLeftUp);
		distances.add(distanceRightUp);
		distances.add(distanceRightDown);
		Collections.sort(distances, Collections.reverseOrder());
		for(Integer i : distances) {
			if (i == distanceLeftDown) {
				relativePos = agentPos.getRelativeOrientation(leftDown);
			}
			else if (i == distanceLeftUp) {
				relativePos = agentPos.getRelativeOrientation(leftUp);
			}
			else if (i == distanceRightDown) {
				relativePos = agentPos.getRelativeOrientation(rightDown);
			}
			else {
				relativePos = agentPos.getRelativeOrientation(rightUp);
			}
			MyEnvironment.MyAction actionTowardsPossiblePrey = goTowardsPrey(possibleActions, relativePos);
			if(actionTowardsPossiblePrey != null) {
				return actionTowardsPossiblePrey;
			}
		}
		return null;
	}

	public void getMaximumGridSize(Set<GridPosition> allSeenPositions) {
		for(GridPosition position:allSeenPositions) {
			if(position.getX() > maxWidth) {
				maxWidth = position.getX();
			}
			if(position.getX() < minWidth) {
				minWidth = position.getX();
			}
			if(position.getY() > maxHeight) {
				maxHeight = position.getX();
			}
			if(position.getY() < minHeight) {
				minHeight = position.getX();
			}
		}

	}

	public class MessageContent {
		private Set<GridPosition> positions;
		private Set<GridPosition> preyPositions;
		private Set<AgentID> predatorIds;

		public MessageContent(Set<GridPosition> positions, Set<GridPosition> preyPositions, Set<AgentID> predatorIds) {
			this.positions = positions;
			this.preyPositions = preyPositions;
			this.predatorIds = predatorIds;
		}

		public Set<GridPosition> getPositions() {
			return positions;
		}

		public void setPositions(Set<GridPosition> positions) {
			this.positions = positions;
		}

		public Set<GridPosition> getPreyPositions() {
			return preyPositions;
		}

		public void setPreyPositions(Set<GridPosition> preyPositions) {
			this.preyPositions = preyPositions;
		}

		public Set<AgentID> getPredatorIds() {
			return predatorIds;
		}

		public void setPredatorIds(Set<AgentID> predatorIds) {
			this.predatorIds = predatorIds;
		}
	}
}

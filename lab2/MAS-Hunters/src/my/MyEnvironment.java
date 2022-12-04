package my;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import base.Action;
import base.Agent;
import base.Perceptions;
import communication.AgentID;
import communication.AgentMessage;
import gridworld.GridOrientation;
import gridworld.GridPosition;
import hunting.AbstractHuntingEnvironment;

/**
 * Your implementation of the environment in which cleaner agents work.
 *
 * @author Alexandru Sorici
 */
public class MyEnvironment extends AbstractHuntingEnvironment
{
	/**
	 * Actions for the hunter agent.
	 *
	 * @author Andrei Olaru
	 */
	public static enum MyAction implements Action {
		
	/**
	 * The agent must move forward.
	 */
	NORTH,
	
	/**
	 * The agent must move left.
	 */
	WEST,
	
	/**
	 * The agent must move right.
	 */
	EAST,
	
	/**
	 * The agent must move back.
	 */
	SOUTH
	}
	
	/**
	 * The perceptions of an agent.
	 * 
	 * @author Alexandru Sorici
	 */
	public static class MyPerceptions implements Perceptions
	{
		/**
		 * The agent position on the grid
		 */
		protected GridPosition					agentPos;
		
		/**
		 * The (possibly empty) set of obstacle positions (e.g. walls)
		 */
		protected Set<GridPosition>				obstacles;
		
		/**
		 * The set of nearby predator agents that can be observed by the agent
		 */
		protected Map<AgentID, GridPosition>	nearbyPredators;
		
		/**
		 * The set of nearby prey agents that can be observed by the agent
		 */
		protected Set<GridPosition>				nearbyPrey;
		
		/**
		 * Messages sent to this agent in the previous step.
		 */
		protected Set<AgentMessage>				messages;
		
		/**
		 * Default constructor.
		 * 
		 * @param agentPos
		 *            - agents's position.
		 * @param obstacles
		 *            - visible obstacles.
		 * @param nearbyPredators
		 *            - visible predators.
		 * @param nearbyPrey
		 *            - visible prey (if the agent is a predator.
		 */
		public MyPerceptions(GridPosition agentPos, Set<GridPosition> obstacles,
				Map<AgentID, GridPosition> nearbyPredators, Set<GridPosition> nearbyPrey)
		{
			this.agentPos = agentPos;
			this.obstacles = obstacles;
			this.nearbyPredators = nearbyPredators;
			this.nearbyPrey = nearbyPrey;
		}
		
		/**
		 * Constructor with messages.
		 * 
		 * @param agentPos
		 *            - agents's position.
		 * @param obstacles
		 *            - visible obstacles.
		 * @param nearbyPredators
		 *            - visible predators.
		 * @param nearbyPrey
		 *            - visible prey (if the agent is a predator.
		 * @param messages
		 *            - the messages sent to this agent at the previous step.
		 */
		public MyPerceptions(GridPosition agentPos, Set<GridPosition> obstacles,
				Map<AgentID, GridPosition> nearbyPredators, Set<GridPosition> nearbyPrey, Set<AgentMessage> messages)
		{
			this(agentPos, obstacles, nearbyPredators, nearbyPrey);
			this.messages = messages;
		}
		
		/**
		 * @return the nearbyPredators
		 */
		public Map<AgentID, GridPosition> getNearbyPredators()
		{
			return nearbyPredators;
		}
		
		/**
		 * @return the nearbyPrey
		 */
		public Set<GridPosition> getNearbyPrey()
		{
			return nearbyPrey;
		}
		
		/**
		 * @return the agentPos
		 */
		public GridPosition getAgentPos()
		{
			return agentPos;
		}
		
		/**
		 * @return the obstacles
		 */
		public Set<GridPosition> getObstacles()
		{
			return obstacles;
		}
		
		/**
		 * @return the messages sent at the previous step.
		 */
		public Set<AgentMessage> getMessages()
		{
			return messages;
		}
	}
	
	/**
	 * Messages sent in one step and delivered in the next.
	 */
	Set<AgentMessage> messageBox = new HashSet<>();
	
	/**
	 * Default constructor. This should call one of the {@link #initialize} methods offered by the super class.
	 * 
	 * @param w
	 *            - map width.
	 * @param h
	 *            - map height.
	 * @param numPrey
	 *            - number of initial Prey agents.
	 * @param numPredators
	 *            - number of Predator agents.
	 */
	public MyEnvironment(int w, int h, int numPredators, int numPrey)
	{
		long seed = System.currentTimeMillis(); // new random experiment
		// long seed = 42L; // existing random experiment
		System.out.println("seed: [" + seed + "]");
		Random rand = new Random(seed);
		
		List<Agent> predators = new ArrayList<>();
		List<Agent> prey = new ArrayList<>();
		
		for(int i = 0; i < numPredators; i++)
			predators.add(new MyPredator());
		for(int i = 0; i < numPrey; i++)
			prey.add(new MyPrey());
		
		super.initialize(w, h, predators, prey, rand);
	}
	
	@Override
	public void step()
	{
		// this should iterate through all agents, provide them with perceptions, and apply the
		// action they return.
		
		// STAGE 1: generate perceptions for all agents, based on the state of the environment at the beginning of this
		// step.
		
		Map<WildlifeAgentData, MyPerceptions> agentPerceptions = new HashMap<>();
		
		// Create perceptions for prey agents
		for(WildlifeAgentData preyAg : getPreyAgents())
		{
			Set<GridPosition> nearbyObstacles = getNearbyObstacles(preyAg.getPosition(), my.MyTester.PREY_RANGE);
			Map<AgentID, GridPosition> predatorPositions = new HashMap<>();
			for(WildlifeAgentData pred : getNearbyPredators(preyAg.getPosition(), my.MyTester.PREY_RANGE, null))
				predatorPositions.put(AgentID.getAgentID(pred.getAgent()), pred.getPosition());
			agentPerceptions.put(preyAg,
					new MyPerceptions(preyAg.getPosition(), nearbyObstacles, predatorPositions, null));
		}
		
		/*
		 * TODO: Create perceptions for predator agents.
		 */
		for (WildlifeAgentData predatorAg: getPredatorAgents()) {
			Set<GridPosition> nearbyObstacles = getNearbyObstacles(predatorAg.getPosition(), MyTester.PREDATOR_RANGE);
			Map<AgentID, GridPosition> predatorPositions = new HashMap<>();
			for(WildlifeAgentData pred : getNearbyPredators(predatorAg.getPosition(), MyTester.PREDATOR_RANGE, predatorAg))
				predatorPositions.put(AgentID.getAgentID(pred.getAgent()), pred.getPosition());

			Set<GridPosition> preyPositions = new HashSet<>();
			for(WildlifeAgentData prey : getNearbyPrey(predatorAg.getPosition(), MyTester.PREDATOR_RANGE))
				preyPositions.add(prey.getPosition());

			agentPerceptions.put(predatorAg,
					new MyPerceptions(predatorAg.getPosition(), nearbyObstacles, predatorPositions, preyPositions));
		}

		// STAGE 2: call response for each agent, in order to obtain desired actions
		
		Map<GridAgentData, MyAction> agentActions = new HashMap<>();

		/*
		 * TODO: Get actions for all agents.
		 */
		for(WildlifeAgentData preyAg : getPreyAgents()) {
			MyPerceptions percep = agentPerceptions.get(preyAg);
			MyAction preyAction = (MyAction) preyAg.getAgent().response(percep);
			agentActions.put(preyAg, preyAction);
//			 System.out.println(
//			 "Agent " + preyAg.getAgent().toString() + " at " + preyAg.getPosition() + " wants " + preyAction);
		}
		for (WildlifeAgentData predatorAg: getPredatorAgents()) {
			MyPerceptions perception = agentPerceptions.get(predatorAg);
			MyAction predAction = (MyAction) predatorAg.getAgent().response(perception);
			agentActions.put(predatorAg, predAction);
//			 System.out.println("Agent " + predatorAg.getAgent().toString() + " at " + predatorAg.getPosition() + " wants "
//			 + predAction + " detects prey " + perception.getNearbyPrey()+ " and predators " + perception.getNearbyPredators()
//			 + " and receives messages " + AgentMessage.filterMessagesFor(messageBox, predatorAg.getAgent()));
		}
		
		// STAGE 3: apply the agents' actions in the environment
		
		// all actions are applied
		for(GridAgentData agentData : agentActions.keySet())
			if(agentActions.get(agentData) == null)
				System.out.println("Agent " + agentData.getAgent().toString() + " did not opt for any action.");
			else
			{
				GridPosition newPosition = null;
				switch(agentActions.get(agentData))
				{
				case NORTH:
					newPosition = agentData.getPosition().getNeighborPosition(GridOrientation.NORTH);
					break;
				case SOUTH:
					newPosition = agentData.getPosition().getNeighborPosition(GridOrientation.SOUTH);
					break;
				case WEST:
					newPosition = agentData.getPosition().getNeighborPosition(GridOrientation.WEST);
					break;
				case EAST:
					newPosition = agentData.getPosition().getNeighborPosition(GridOrientation.EAST);
					break;
				default:
					break;
				}
				if(!getXtiles().contains(newPosition))
					agentData.setPosition(newPosition);
				else
					System.out.println("Agent " + agentData.getAgent().toString() + " tried to go through a wall.");
			}
		
		// see if any of the prey died by being cornered and remove it from the grid
		removeDeadPrey();
	}
}
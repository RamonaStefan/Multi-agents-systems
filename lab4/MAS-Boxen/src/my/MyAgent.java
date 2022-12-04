package my;

import base.Action;
import base.Agent;
import base.Perceptions;
import blocksworld.*;
import blocksworld.PlanningAction.PlanningActionType;
import blocksworld.Stack;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent to implement.
 */
public class MyAgent implements Agent
{
	/**
	 * Name of the agent.
	 */
	String agentName;
	BlocksWorld desiredState;
	List<Block> blockNotInPosition = new ArrayList<>();
	List<Block> seenBlock = new ArrayList<>();
	Map<BlocksWorldEnvironment.Station, List<Block>> stationsList = new HashMap<>();


	/**
	 * Constructor for the agent.
	 * 
	 * @param desiredState
	 *            - the desired state of the world.
	 * @param name
	 *            - the name of the agent.
	 */
	public MyAgent(BlocksWorld desiredState, String name)
	{
		// TODO
		agentName = name;
		this.desiredState = desiredState;
		for(Stack stack: desiredState.getStacks()) {
			blockNotInPosition.addAll(stack.getBlocks());
		}
	}
	
	@Override
	public Action response(Perceptions input)
	{
		@SuppressWarnings("unused")
		BlocksWorldPerceptions perceptions = (BlocksWorldPerceptions) input;
		BlocksWorldEnvironment.Station currentStation =  perceptions.getCurrentStation();
		List<BlocksWorldAction> remainingPlan = perceptions.getRemainingPlan();
		boolean previousActionSucceeded = perceptions.hasPreviousActionSucceeded();
		Stack currentStack = perceptions.getVisibleStack();

		PlanningAction plan = new PlanningAction(PlanningActionType.NEW_PLAN);

		// TODO: revise beliefs; if necessary, make a plan; return an action.

		// go to all stations
		if(!seenBlock.containsAll(currentStack.getBlocks())) {
			seenBlock.addAll(currentStack.getBlocks());
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.NEXT_STATION));
			stationsList.put(currentStation, currentStack.getBlocks());
			return plan;
	}

		// if plan with actions exists, continue
		if(remainingPlan != null && remainingPlan.size() > 0 && previousActionSucceeded) {
			plan = new PlanningAction(PlanningActionType.CONTINUE_PLAN);
			plan.addAll(remainingPlan);
			return plan;
		}

		// check if states from final state don't exist in initial state
		if(!seenBlock.containsAll(desiredState.allBlocks())) {
			plan = new PlanningAction(PlanningActionType.NEW_PLAN);
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.AGENT_COMPLETED));
			System.out.println("Scope can not be realised. Blocks are missing from initial state.");
			return plan;
		}

		// start creating a new plan by updating the status of current station
		stationsList.put(currentStation, currentStack.getBlocks());

		// check if is the desired state
		boolean sameState = true;
		for (Stack stack : desiredState.getStacks()) {
			if(!stationsList.containsValue(stack.getBlocks())) {
				sameState = false;
			}
		}
		if(sameState) {
			plan = new PlanningAction(PlanningActionType.NEW_PLAN);
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.AGENT_COMPLETED));
			return plan;
		}

		// unstack all blocks which are not in correct position
		List<List<Block>> newStacks = new ArrayList<>();
		for(List<Block> stack : stationsList.values()) {
			List<Block> desiredElements = stack.stream().filter(st -> desiredState.allBlocks().contains(st)).collect(Collectors.toList());
			if(desiredElements.size() > 0) {
				for(Block block : desiredElements) {
					if(!isBlockInPosition(block)) {
						unstackAllUntilBlock(block, plan, newStacks);
					}
					else {
						blockNotInPosition.remove(block);
					}
				}
			}

		}
		for (List<Block> newStack : newStacks) {
			stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);

		}

		// stack all desired blocks in correct position
		Collections.reverse(blockNotInPosition);
		for (Block block : blockNotInPosition) {
			stackBlockInDesiredPosition(block, plan);
		}
		plan.add(new BlocksWorldAction(BlocksWorldAction.Type.AGENT_COMPLETED));
		Collections.reverse(blockNotInPosition);
		return plan;
	}

	private void stackBlockInDesiredPosition(Block block, PlanningAction plan) {
		Stack desiredStack = desiredState.getStack(block);
		Block bellowBlock = desiredStack.getBelow(block);
		if (bellowBlock == null)
		{
			return;
		}
		BlocksWorldEnvironment.Station station = getStationOfBlock(block);
		plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, station));
		plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PICKUP, block));
		plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, getStationOfBlock(bellowBlock)));
		plan.add(new BlocksWorldAction(BlocksWorldAction.Type.STACK, block, bellowBlock));


		List<Block> currentStackWithBlock = getStackWithBlock(block);
		currentStackWithBlock.remove(block);
		List<Block> futureStackWithBlock = getStackWithBlock(bellowBlock);
		futureStackWithBlock.add(block);
	}

	private char getNextLabel() {
		char stationName = '0';
		while(stationsList.keySet().contains(new BlocksWorldEnvironment.Station(stationName))) {
			stationName++;
		}
		return stationName;
	}


	private List<Block> getStackWithBlock(Block block) {
		for(List<Block> stack : stationsList.values()){
			if (stack.contains(block)) {
				return stack;
			}
		}
		return null;
	}

	private boolean isBlockInPosition(Block block) {
		Stack desiredStackWithBlock = desiredState.getStack(block);
		List<Block> currentStackWithBlock = getStackWithBlock(block);

		Block blockDesired = desiredStackWithBlock.getBottomBlock();
		int length = currentStackWithBlock.size();
		Block blockCurrent = currentStackWithBlock.get(--length);

		do {
			if (!blockCurrent.equals(blockDesired)) {
				return false;
			}
			blockDesired = desiredStackWithBlock.getAbove(blockDesired);
			if(length >= 1) {
				blockCurrent = currentStackWithBlock.get(--length);
			}
			else {
				length--;
			}

		}while(length >= 0 && blockDesired != null);

		return length < 0 && blockDesired == null;
	}

	private void unstackAllUntilBlock(Block block, PlanningAction plan, List<List<Block>> newStacks ) {
		List<Block> currentStackWithBlock = getStackWithBlock(block);
		BlocksWorldEnvironment.Station currentStationWithBlock = getStationOfBlock(block);
		Block blockCurrent = currentStackWithBlock.get(0);
		do {
			if (isBlockInPosition(block) && blockCurrent.equals(block)) {
				blockNotInPosition.remove(block);
				break;
			}
			if(currentStackWithBlock.size() > 1) {
				Block belowBlock = currentStackWithBlock.get(1);
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, currentStationWithBlock));

				if (!plan.contains(new BlocksWorldAction(BlocksWorldAction.Type.UNSTACK, blockCurrent, belowBlock))) {
					plan.add(new BlocksWorldAction(BlocksWorldAction.Type.UNSTACK, blockCurrent, belowBlock));
				}
				if(!plan.contains(new BlocksWorldAction(BlocksWorldAction.Type.PUTDOWN, blockCurrent))) {
					plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PUTDOWN, blockCurrent));
				}

				currentStackWithBlock.remove(blockCurrent);

				List<Block> futureStackWithBlock = new ArrayList<>();
				futureStackWithBlock.add(blockCurrent);
				newStacks.add(futureStackWithBlock);

				if (blockCurrent.equals(block)) {
					break;
				}
				if(currentStackWithBlock.size() >= 1){
					blockCurrent = currentStackWithBlock.get(0);
				}

			}
		}while(currentStackWithBlock.size() > 1);
	}

	private BlocksWorldEnvironment.Station getStationOfBlock(Block block) {
		for(BlocksWorldEnvironment.Station station : stationsList.keySet()) {
			if (stationsList.get(station).contains(block)) {
				return station;
			}
		}
		return null;
	}


	@Override
	public String statusString()
	{
		// TODO: return information about the agent's current state and current plan.
		return toString() + ": All good.";
	}
	
	@Override
	public String toString()
	{
		return "" + agentName;
	}
}

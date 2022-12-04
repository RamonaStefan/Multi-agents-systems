package my;

import base.Action;
import base.Agent;
import base.Perceptions;
import blocksworld.*;
import blocksworld.PlanningAction.PlanningActionType;
import blocksworld.Stack;

import javax.jws.soap.SOAPBinding;
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
	private String agentName;
	private BlocksWorld desiredState;
	private List<Block> blockNotInPosition = new ArrayList<>();
	private List<Block> seenBlock = new ArrayList<>();
	private Map<BlocksWorldEnvironment.Station, List<Block>> stationsList = new HashMap<>();
	private Map<BlocksWorldEnvironment.Station, List<Block>> realStationsList = new HashMap<>();
	private Block searchedBlock = null;
	private int numberSteps = 0;

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
		agentName = name;
		this.desiredState = desiredState;
		for(Stack stack: desiredState.getStacks()) {
			blockNotInPosition.addAll(stack.getBlocks());
		}
		// TODO
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
		Block hold = perceptions.getHolding();

		PlanningAction plan = new PlanningAction(PlanningActionType.NEW_PLAN);

		// TODO: revise beliefs; if necessary, make a plan; return an action.
		numberSteps++;

		// go to all stations first
		if(!seenBlock.containsAll(currentStack.getBlocks())) {
			realStationsList.put(currentStation, currentStack.getBlocks());
			stationsList.put(currentStation, currentStack.getBlocks());
			PlanningAction planLocks = addLockActions(currentStack, realStationsList, remainingPlan);
			if(seenBlock.size() == 0 || remainingPlan.size() == 0) {
				plan =  planLocks != null ? planLocks: plan;
				addMultipleNextStations(plan);
			}
			else {
				if(planLocks != null) {
					plan =  planLocks;
				}
				else {
					plan = new PlanningAction(PlanningActionType.CONTINUE_PLAN);
					BlocksWorldAction nextAction = remainingPlan.get(0);
					checkNextActionIsStackAndLock(nextAction);
				}
			}
			seenBlock.addAll(currentStack.getBlocks());
			return plan;
		}

		realStationsList.put(currentStation, currentStack.getBlocks());
		stationsList.put(currentStation, currentStack.getBlocks());
		seenBlock.addAll(currentStack.getBlocks());

		// check if blocks can be locked
		List<Block> blocksToEliminate = new ArrayList<>();
		for(Block block: blockNotInPosition) {
			if(isBlockInPosition(block, realStationsList, "absolute")) {
				blocksToEliminate.add(block);
			}
			if(isBlockInPosition(block, realStationsList, "relative")) {
				plan = new PlanningAction(PlanningActionType.MODIFY_PLAN);
				if(!remainingPlan.contains(new BlocksWorldAction(BlocksWorldAction.Type.LOCK, block))) {
					plan.add(new BlocksWorldAction(BlocksWorldAction.Type.LOCK, block));
				}
			}
		}
		for(Block block: blocksToEliminate) {
			blockNotInPosition.remove(block);
		}

		// check if this is the desired state
		boolean same = true;
		for(Stack stack: desiredState.getStacks()) {
			if (!realStationsList.containsValue(stack.getBlocks())) {
				same = false;
				break;
			}
		}

		if (blockNotInPosition.size() == 0 || same) {
			plan = new PlanningAction(PlanningActionType.NEW_PLAN);
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.AGENT_COMPLETED));
			return plan;
		}

		// check if states from final state don't exist in initial state
		if((!seenBlock.containsAll(blockNotInPosition) && numberSteps > 100) || numberSteps > 500) {
			plan = new PlanningAction(PlanningActionType.NEW_PLAN);
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.AGENT_COMPLETED));
			System.out.println("Scope can not be realised. Blocks are missing from initial state.");
			return plan;
		}

		// if previous action failed
		if(remainingPlan != null && remainingPlan.size() > 0 && !previousActionSucceeded) {
			PlanningAction planLocks = addLockActions(currentStack, realStationsList, remainingPlan);
			plan =  planLocks != null ? planLocks: new PlanningAction(PlanningActionType.MODIFY_PLAN);
			continuePreviousFailedAction(remainingPlan.get(0), currentStack, currentStation, plan, realStationsList, hold, remainingPlan);

			return plan;
		}


		// if plan with actions exists, continue
		if(remainingPlan != null && remainingPlan.size() > 0 && previousActionSucceeded) {
			int nr_next_station = 0;
			// find number of  consecutive next_station actions
			for (BlocksWorldAction action: remainingPlan) {
				if (action.getType().equals(BlocksWorldAction.Type.NEXT_STATION)) {
					nr_next_station++;
				}
				else break;
			}
			
			// check if next action is valid or not => modify plan
			BlocksWorldAction nextAction = remainingPlan.get(0);
			if (isNextActionValid(currentStack, nextAction, hold)) {
				PlanningAction planLocks = addLockActions(currentStack, realStationsList, remainingPlan);
				if (nr_next_station <= 1) {
					plan = new PlanningAction(PlanningActionType.CONTINUE_PLAN);
					checkNextActionIsStackAndLock(nextAction);
					plan = planLocks != null ? planLocks : plan;
				} else {
					plan = new PlanningAction(PlanningActionType.MODIFY_PLAN);
					if(searchedBlock == null ) {
						plan = planLocks != null ? planLocks : plan;
						plan.removeFromOriginalPlan(nr_next_station);
						searchedBlock = null;
					}
					else {
						plan = new PlanningAction(PlanningActionType.CONTINUE_PLAN);
						checkNextActionIsStackAndLock(nextAction);
						if (searchedBlock!= null && currentStack.contains(searchedBlock)) {
							plan = planLocks != null ? planLocks : new PlanningAction(PlanningActionType.MODIFY_PLAN);
							plan.removeFromOriginalPlan(nr_next_station);
							searchedBlock = null;
						}
						else {
							plan = planLocks != null ? planLocks : plan;
						}
					}
				}
			} else {
				plan = new PlanningAction(PlanningActionType.MODIFY_PLAN);
				PlanningAction planLocks = addLockActions(currentStack, realStationsList, remainingPlan);
				plan = planLocks != null ? planLocks : plan;
				updatePlanForNextActionInvalid(plan, currentStack, nextAction, currentStation, remainingPlan, hold, realStationsList);
			}
			searchedBlock = null;
			return plan;
		}

		// create new plan
//		PlanningAction planLocks = addLockActions(currentStack, realStationsList, remainingPlan);
//		plan = planLocks != null ? planLocks : plan;
		stationsList = new HashMap<>(realStationsList);
		plan = new PlanningAction(PlanningActionType.NEW_PLAN);


		// unstack all blocks which are not in correct position
		List<List<Block>> newStacks = new ArrayList<>();
		for(List<Block> stack : realStationsList.values()) {
			List<Block> desiredElements = stack.stream().filter(st -> desiredState.allBlocks().contains(st)).collect(Collectors.toList());
			if(desiredElements.size() > 0) {
				for(Block block : desiredElements) {
					if(!isBlockInPosition(block, realStationsList, "absolute")) {
						unstackAllUntilBlock(block, plan, newStacks, stationsList);
					}
				}
			}

		}
		for (List<Block> newStack : newStacks) {
			stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);
			realStationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);
		}

		// stack all desired blocks in correct position
		if (plan.size() > 0) {
			Collections.reverse(blockNotInPosition);
			for (Block block : blockNotInPosition) {
				stackBlockInDesiredPosition(block, plan, stationsList);
			}
			Collections.reverse(blockNotInPosition);
		}

		// if no other action available -> add next_station until maximumNoSteps to wait for other blocks to show
		if(plan.size() == 0) {
			plan = new PlanningAction(PlanningActionType.NEW_PLAN);
			addMultipleNextStations(plan);
			searchedBlock = getSearchedBlock(remainingPlan, 1);
		}

		return plan;
	}

	private void stackBlockInDesiredPosition(Block block, PlanningAction plan, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList) {
		Stack desiredStack = desiredState.getStack(block);
		Block bellowBlock = desiredStack.getBelow(block);
		if (bellowBlock == null)
		{
			return;
		}
		BlocksWorldEnvironment.Station station = getStationOfBlock(block, stationsList);
		if (station != null) {
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, station));
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PICKUP, block));
			if (getStationOfBlock(bellowBlock, stationsList) != null) {
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, getStationOfBlock(bellowBlock, stationsList)));
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.STACK, block, bellowBlock));
				if (!plan.contains(new BlocksWorldAction(BlocksWorldAction.Type.LOCK, block))) {
					plan.add(new BlocksWorldAction(BlocksWorldAction.Type.LOCK, block));
				}
			} else {
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PUTDOWN, block));
			}

			List<Block> currentStackWithBlock = getStackWithBlock(block, stationsList);
			currentStackWithBlock.remove(block);
			List<Block> futureStackWithBlock = getStackWithBlock(bellowBlock, stationsList);
			if (futureStackWithBlock == null) {
				futureStackWithBlock = new ArrayList<>();
			}
			futureStackWithBlock.add(block);
			stationsList.put(new BlocksWorldEnvironment.Station(getNextLabel()), futureStackWithBlock);
		}
	}

	private char getNextLabel() {
		char stationName = '0';
		while(stationsList.keySet().contains(new BlocksWorldEnvironment.Station(stationName))) {
			stationName++;
		}
		return stationName;
	}
	
	private void checkNextActionIsStackAndLock(BlocksWorldAction nextAction) {
		if(nextAction.getType().equals(BlocksWorldAction.Type.STACK)) {
			blockNotInPosition.remove(nextAction.getFirstArgument());
		}
	}

	private List<Block> getStackWithBlock(Block block, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList) {
		for(List<Block> stack : stationsList.values()){
			if (stack.contains(block)) {
				return stack;
			}
		}
		return null;
	}

	private boolean isBlockInPosition(Block block, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList, String position) {
		Stack desiredStackWithBlock;
		try {
			desiredStackWithBlock = desiredState.getStack(block);
		}
		catch(Exception e) {
			return false;
		}
		List<Block> currentStackWithBlock = getStackWithBlock(block, stationsList);
		if(currentStackWithBlock == null) {
			return false;
		}

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

		if(position.equals("relative")) {
			return length < 0;
		}
		else {
			return length < 0 && blockDesired == null;
		}
	}

	private void unstackAllUntilBlock(Block block, PlanningAction plan, List<List<Block>> newStacks, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList ) {
		List<Block> currentStackWithBlock = getStackWithBlock(block, stationsList);
		BlocksWorldEnvironment.Station currentStationWithBlock = getStationOfBlock(block, stationsList);
		if(currentStackWithBlock == null) {
			return;
		}
		Block blockCurrent = currentStackWithBlock.get(0);
		do {
			if (isBlockInPosition(block, stationsList, "absolute") && blockCurrent.equals(block)) {
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

	private BlocksWorldEnvironment.Station getStationOfBlock(Block block, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList) {
		for(BlocksWorldEnvironment.Station station : stationsList.keySet()) {
			if (stationsList.get(station).contains(block)) {
				return station;
			}
		}
		return null;
	}

	private void addMultipleNextStations(PlanningAction plan) {
		for (int i = 0; i < 10; i++) {
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.NEXT_STATION));
		}
	}

	private PlanningAction addLockActions(Stack currentStack, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList, List<BlocksWorldAction> remainingPlan) {
		PlanningAction plan = new PlanningAction(PlanningActionType.MODIFY_PLAN);
		List<Block> desiredElements = currentStack.getBlocks().stream().filter(st -> desiredState.allBlocks().contains(st)).collect(Collectors.toList());
		if(desiredElements.size() > 0) {
			for(Block block : desiredElements) {
				if(isBlockInPosition(block, stationsList, "relative")) {
					checkLock(block, plan, currentStack, remainingPlan);
				}
			}
		}
		return plan.size() > 0 ? plan : null;
	}

	private void checkLock(Block block, PlanningAction plan, Stack currentStack, List<BlocksWorldAction> remainingPlan) {
		int index = currentStack.getBlocks().indexOf(block);
		if (index == -1) {
			return;
		}
		for(int i = currentStack.getBlocks().size()-1; i >= index ; i--) {
			Block b = currentStack.getBlocks().get(i);
			if (!currentStack.isLocked(b)
					&& !plan.contains(new BlocksWorldAction(BlocksWorldAction.Type.LOCK, b))
					&& !remainingPlan.contains(new BlocksWorldAction(BlocksWorldAction.Type.LOCK, b))) {
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.LOCK, b));
			}
		}
	}

	private void updatePlanForNextActionInvalid(PlanningAction plan, Stack currentStack, BlocksWorldAction action,
												BlocksWorldEnvironment.Station currentStation, List<BlocksWorldAction> remainingPlan,
												Block hold, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList) {
		int n = 1;
		switch(action.getType()) {
			case LOCK: {
				plan.removeFromOriginalPlan(n);
				break;
			}
			case PICKUP: {
				modifyPlanPickUp(action, plan, currentStack, currentStation, remainingPlan, n, stationsList);
				break;
			}
			case STACK: {
				modifyPlanStack(action, plan, currentStack, currentStation, remainingPlan, hold, stationsList);
				break;
			}
			case UNSTACK: {
				modifyPlanUnstack(action, plan, currentStack, currentStation, remainingPlan, n, stationsList);
				break;
			}
			case GO_TO_STATION:
			case NEXT_STATION:
			case PUTDOWN:
			default:
				break;
		}
	}

	private boolean isNextActionValid(Stack currentStack, BlocksWorldAction action, Block hold) {
		switch(action.getType()) {
			case PICKUP: {
				Block block = action.getFirstArgument();
				return currentStack.getBlocks().contains(block) && currentStack.isClear(block) && currentStack.isOnTable(block) && !currentStack.isLocked(block);
			}

			case STACK: {
				Block block = action.getFirstArgument();
				Block block2 = action.getSecondArgument();

				return currentStack.getBlocks().contains(block2) && hold != null && hold.equals(block) && currentStack.isClear(block2);
			}
			case UNSTACK: {
				Block block = action.getFirstArgument();
				Block block2 = action.getSecondArgument();
				return currentStack.getBlocks().contains(block) && currentStack.getBlocks().contains(block2)
						&& currentStack.isOn(block, block2) && currentStack.isClear(block) && !currentStack.isLocked(block);
			}
			case LOCK: {
				Block block = action.getFirstArgument();
				return currentStack.getBlocks().contains(block) && currentStack.isClear(block);
			}
			case GO_TO_STATION:
			case NEXT_STATION:
			case PUTDOWN:
			default:
				return true;
		}
	}

	private void continuePreviousFailedAction(BlocksWorldAction action, Stack currentStack, BlocksWorldEnvironment.Station currentStation,
											  PlanningAction plan, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList,
											  Block hold, List<BlocksWorldAction> remainingPlan) {
			int n = 1;
			switch(action.getType()) {
				case LOCK: {
					plan.removeFromOriginalPlan(n);
					break;
				}
				case PICKUP: {
					modifyPlanPickUp(action, plan, currentStack, currentStation, remainingPlan, n, stationsList);
					break;
				}
				case GO_TO_STATION: {
					plan.removeFromOriginalPlan(n);
					searchedBlock = getSearchedBlock(remainingPlan, 1);
					if(!currentStack.contains(searchedBlock)) {
						addMultipleNextStations(plan);
					}
					break;
				}
				case STACK: {
					modifyPlanStack(action, plan, currentStack, currentStation, remainingPlan, hold, stationsList);
					break;
				}
				case UNSTACK: {
					modifyPlanUnstack(action, plan, currentStack, currentStation, remainingPlan, n, stationsList);
					break;
				}
				case NEXT_STATION:
				case PUTDOWN:
				default:
					break;
			}
	}

	private void modifyPlanPickUp(BlocksWorldAction action, PlanningAction plan, Stack currentStack,
								  BlocksWorldEnvironment.Station currentStation, List<BlocksWorldAction> remainingPlan,
								  int n, Map<BlocksWorldEnvironment.Station, List<Block>> stationsList) {
		Block block = action.getFirstArgument();
		// block exists but is locked
		if(currentStack.getBlocks().contains(block) && currentStack.isLocked(block)) {
			for(int i = 1; i < remainingPlan.size(); i++) {
				if(remainingPlan.get(i).getType().equals(BlocksWorldAction.Type.PUTDOWN) ||
						remainingPlan.get(i).getType().equals(BlocksWorldAction.Type.LOCK)) {
					n++;
				}
				else break;
			}
			plan.removeFromOriginalPlan(n);
		}
		// block exists, is on the table, but have other blocks over -> unstack
		else if (currentStack.getBlocks().contains(block) && !currentStack.isClear(block) && currentStack.isOnTable(block)) {
			int index = currentStack.getBlocks().indexOf(block);
			List<List<Block>> newStacks = new ArrayList<>();

			System.out.println(index);
			for (int i = 0; i < index; i++) {
				unstackAllUntilBlock(currentStack.getBlocks().get(i), plan, newStacks, stationsList);
			}

			for (List<Block> newStack : newStacks) {
				stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);
			}
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, currentStation));
		}
		// block exists, is clear but is not on the table -> replace pickup with unstack
		else if (currentStack.getBlocks().contains(block) && !currentStack.isOnTable(block) && currentStack.isClear(block)) {
			plan.removeFromOriginalPlan(n);
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.UNSTACK, block, currentStack.getBelow(block)));
		}
		// block exists, is not on the table, is not clear -> unstack and replace pickup with unstack
		else if (currentStack.getBlocks().contains(block) && !currentStack.isOnTable(block) && !currentStack.isClear(block)) {
			int index = currentStack.getBlocks().indexOf(block);
			List<List<Block>> newStacks = new ArrayList<>();

			for (int i = 0; i < index; i++) {
				unstackAllUntilBlock(currentStack.getBlocks().get(i), plan, newStacks, stationsList);
			}

			for (List<Block> newStack : newStacks) {
				stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);
			}
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, currentStation));
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.UNSTACK, block, currentStack.getBelow(block)));
			plan.removeFromOriginalPlan(n);
		}
		// block don't exist in current stack -> search it
		else if (!currentStack.getBlocks().contains(block)) {
			addMultipleNextStations(plan);
			searchedBlock = getSearchedBlock(remainingPlan, 0);
		}
	}

	private void modifyPlanStack(BlocksWorldAction action, PlanningAction plan, Stack currentStack,
								 BlocksWorldEnvironment.Station currentStation, List<BlocksWorldAction> remainingPlan, Block hold,
								 Map<BlocksWorldEnvironment.Station, List<Block>> stationsList) {
		Block block1 = action.getFirstArgument();
		Block block2 = action.getSecondArgument();
		// not holding any block -> skip action
		if(hold == null) {
//					plan.add(new BlocksWorldAction(BlocksWorldAction.Type.NEXT_STATION));
			plan.removeFromOriginalPlan(1);
		}
		// target block exists, but is not clear -> putdown & unstack & pcikup
		else if(currentStack.getBlocks().contains(block2) && !currentStack.isClear(block2)) {
			int index = currentStack.getBlocks().indexOf(block2);
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PUTDOWN, block1));

			//create new stack for putdown
			List<Block> futureStackWithBlock = new ArrayList<>();
			futureStackWithBlock.add(block1);
			stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), futureStackWithBlock);

			// go back and unstack
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, currentStation));
			List<List<Block>> newStacks = new ArrayList<>();

			// unstack
			for (int i = 0; i < index; i++) {
				unstackAllUntilBlock(currentStack.getBlocks().get(i), plan, newStacks, stationsList);

			}
//			unstackAllUntilBlock(currentStack.getBlocks().get(index), plan, newStacks, stationsList);

			for (List<Block> newStack : newStacks) {
				stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);
			}

			BlocksWorldEnvironment.Station futureStation = getStationOfBlock(block1, stationsList);
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, futureStation));
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PICKUP, block1));
			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, currentStation));

		}
		else if (!currentStack.getBlocks().contains(block2)) {
			addMultipleNextStations(plan);
			searchedBlock = getSearchedBlock(remainingPlan, 0);
		}
	}

	private void modifyPlanUnstack(BlocksWorldAction action, PlanningAction plan, Stack currentStack,
								   BlocksWorldEnvironment.Station currentStation, List<BlocksWorldAction> remainingPlan, int n,
								   Map<BlocksWorldEnvironment.Station, List<Block>> stationsList) {
		Block block = action.getFirstArgument();
		Block block2 = action.getSecondArgument();

		// source block exists but is locked
		if(currentStack.getBlocks().contains(block) && currentStack.isLocked(block)) {
			for(int i = 1; i < remainingPlan.size(); i++) {
				if(remainingPlan.get(i).getType().equals(BlocksWorldAction.Type.PUTDOWN) ||
						remainingPlan.get(i).getType().equals(BlocksWorldAction.Type.LOCK)) {
					n++;
				}
				else break;
			}
			plan.removeFromOriginalPlan(n);
		}
		// both blocks exist (1 over 2), but is not clear -> unstack
		else if(currentStack.getBlocks().contains(block) && currentStack.getBlocks().contains(block2)
				&& currentStack.isOn(block, block2) && !currentStack.isClear(block)) {
			int index = currentStack.getBlocks().indexOf(block2);
			List<List<Block>> newStacks = new ArrayList<>();

			for (int i = 0; i < index; i++) {
				unstackAllUntilBlock(currentStack.getBlocks().get(i), plan, newStacks, stationsList);
			}

			for (List<Block> newStack : newStacks) {
				stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);
			}

			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, currentStation));
		}
		// source block exists, is clear, but not on target block -> modify Unstack command
		else if(currentStack.getBlocks().contains(block) && (currentStack.isOnTable(block) || !currentStack.contains(block2) || !currentStack.isOn(block, block2))
				&& currentStack.isClear(block)) {
			plan.removeFromOriginalPlan(n);
			if (!currentStack.isOnTable(block)) {
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.UNSTACK, block, currentStack.getBelow(block)));
			}
			else {
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PICKUP, block));
			}
		}
		// source block exists, but not on target block, is not clear -> unstack & modify unstack command
		else if(currentStack.getBlocks().contains(block) && currentStack.getBlocks().contains(block2)
				&& !currentStack.isOn(block, block2)) {

			int index = currentStack.getBlocks().indexOf(block2);
			List<List<Block>> newStacks = new ArrayList<>();

			for (int i = 0; i < index; i++) {
				unstackAllUntilBlock(currentStack.getBlocks().get(i), plan, newStacks, stationsList);
			}

			for (List<Block> newStack : newStacks) {
				stationsList.put( new BlocksWorldEnvironment.Station(getNextLabel()), newStack);
			}

			plan.add(new BlocksWorldAction(BlocksWorldAction.Type.GO_TO_STATION, currentStation));

			plan.removeFromOriginalPlan(n);
			if (!currentStack.isOnTable(block)) {
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.UNSTACK, block, currentStack.getBelow(block)));
			}
			else {
				plan.add(new BlocksWorldAction(BlocksWorldAction.Type.PICKUP, block));
			}
		}
		// source don't exist in this stack -> remove all actions related
		else if(!currentStack.getBlocks().contains(block)) {
			for(int i = 1; i < remainingPlan.size(); i++) {
				if(remainingPlan.get(i).getType().equals(BlocksWorldAction.Type.PUTDOWN) ||
						remainingPlan.get(i).getType().equals(BlocksWorldAction.Type.LOCK)) {
					n++;
				}
				else break;
			}
			plan.removeFromOriginalPlan(n);
		}
	}

	private Block getSearchedBlock(List<BlocksWorldAction> remainingPlan, int start) {
		for(int i = start; i < remainingPlan.size(); i++) {
			BlocksWorldAction action = remainingPlan.get(i);
			if(!action.getType().equals(BlocksWorldAction.Type.MARKER) && !action.getType().equals(BlocksWorldAction.Type.NONE)) {
				switch(action.getType()) {
					case STACK:
						return action.getSecondArgument();
					case UNSTACK:
					case PUTDOWN:
					case PICKUP:
					case LOCK:
						return action.getFirstArgument();
					case GO_TO_STATION:
					case NEXT_STATION:
					default:
						return null;
				}
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

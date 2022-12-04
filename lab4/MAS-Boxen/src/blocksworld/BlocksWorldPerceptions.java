package blocksworld;

import java.util.LinkedList;
import java.util.List;

import base.Perceptions;
import blocksworld.BlocksWorldEnvironment.Station;

/**
 * What an agent is able to perceive.
 * 
 * @author andreiolaru
 */
public class BlocksWorldPerceptions implements Perceptions
{
	/**
	 * The state of the current station.
	 */
	Stack	visibleStack;
	/**
	 * The name of the current station.
	 */
	Station	current;
	/**
	 * <code>true</code> if the previous action of the agent succeeded; <code>false</code> otherwise.
	 */
	boolean	previousActionSucceeded;
	
	/**
	 * This contains the actions that are left to perform by the environment.
	 */
	List<BlocksWorldAction> remainingPlan = null;
	
	/**
	 * Constructor.
	 * 
	 * @param stack
	 *            - the state of the current station.
	 * @param currentStation
	 *            - the current station.
	 * @param previousActionSucceeded
	 *            - whether the previous action was carried out correctly.
	 * @param planLeft 
	 *            - the actions that were not yet performed by the environment.
	 */
	public BlocksWorldPerceptions(Stack stack, Station currentStation, boolean previousActionSucceeded, List<BlocksWorldAction> planLeft)
	{
		this.visibleStack = stack;
		this.current = currentStation;
		this.previousActionSucceeded = previousActionSucceeded;
		this.remainingPlan = planLeft == null ? null : new LinkedList<>(planLeft);
	}
	
	/**
	 * @return the perceived state of the current station.
	 */
	public Stack getVisibleStack()
	{
		return visibleStack;
	}
	
	/**
	 * @return the current station.
	 */
	public Station getCurrentStation()
	{
		return current;
	}
	
	/**
	 * @return <code>true</code> if the previous action was carried out correctly.
	 */
	public boolean hasPreviousActionSucceeded()
	{
		return previousActionSucceeded;
	}
	
	public List<BlocksWorldAction> getRemainingPlan()
	{
		return remainingPlan;
	}
}

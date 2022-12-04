package gridworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import base.Action;

/**
 * Helper for the representation of a probability map.
 * 
 * @author andreiolaru
 */
public class ProbabilityMap extends HashMap<Action, Double>
{
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor.
	 */
	public ProbabilityMap()
	{
		super();
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param copySource
	 *            - the source for the copy.
	 */
	public ProbabilityMap(ProbabilityMap copySource)
	{
		super(copySource);
	}
	
	/**
	 * Implementation of {@link #put(Action, Double)} for <code>double</code> value.
	 * 
	 * @param key
	 *            - the key.
	 * @param value
	 *            - the value.
	 * @return the previous value, if any.
	 */
	public Double put(Action key, double value)
	{
		return super.put(key, new Double(value));
	}
	
	/**
	 * Updates a discrete action probability map by uniformly redistributing the probability of an action to remove over
	 * the remaining possible actions in the map.
	 * 
	 * @param actionToRemove
	 *            - The action to remove from the map.
	 */
	public void removeAction(Action actionToRemove)
	{
		Double val = remove(actionToRemove);
		if(val == null)
		{
			return;
		}
		
		List<Action> remainingActions = new ArrayList<>(keySet());
		int nrRemainingActions = remainingActions.size();
		
		if(nrRemainingActions == 0)
		{
			return;
		}
		
		double probSum = 0;
		for(int i = 0; i < nrRemainingActions - 1; i++)
		{
			double newActionProb = get(remainingActions.get(i)).doubleValue() + val.doubleValue() / nrRemainingActions;
			probSum += newActionProb;
			
			put(remainingActions.get(i), newActionProb);
		}
		
		put(remainingActions.get(nrRemainingActions - 1), 1 - probSum);
	}
	
	/**
	 * Return a random action from a discrete distribution over a set of possible actions.
	 * 
	 * @return an action chosen from the set of choices
	 */
	public Action choice()
	{
		double r = Math.random();
		double countProb = 0.0;
		for(Action act : keySet())
		{
			countProb += get(act).doubleValue();
			
			if(countProb >= r)
				return act;
		}
		throw new RuntimeException("Should never get here!");
	}
}

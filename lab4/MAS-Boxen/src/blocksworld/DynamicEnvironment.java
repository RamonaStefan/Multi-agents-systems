package blocksworld;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import my.MyTester;

/**
 * A {@link BlocksWorldEnvironment} in which the environment performs various dynamic actions, with a global probability
 * given in {@link my.MyTester#DYNAMICITY}.
 * <p>
 * Dynamic actions are always performed on stacks which are not being currently observed by any agents.
 * 
 * @author andreiolaru
 */
public class DynamicEnvironment extends BlocksWorldEnvironment
{
	
	/**
	 * Dynamic actions that can be performed. Each action contains its probability (given that a random action will
	 * certainly be executed)
	 */
	enum DynamicAction {
		/**
		 * Move a random block to the 'stash'.
		 */
		STASH(.15f),
		
		/**
		 * Get a random block from the 'stash' and place it in an existing stack.
		 */
		UNSTASH(.25f),
		
		/**
		 * Get a random block from a non-unitary stack and put it on the table.
		 */
		DROP(.3f),
		
		/**
		 * Get a random block and place it in an existing stack.
		 */
		TELEPORT(.3f),
		
		;
		
		/**
		 * The probability.
		 */
		float prob = 0;
		
		/**
		 * @param probability
		 *            - the probability.
		 */
		private DynamicAction(float probability)
		{
			prob = probability;
		}
		
		/**
		 * @return a randomly picked action, according to the probability distribution.
		 */
		static DynamicAction pick()
		{
			double r = Math.random();
			double countProb = 0.0;
			for(DynamicAction act : DynamicAction.values())
			{
				countProb += act.prob;
				if(countProb >= r)
					return act;
			}
			throw new RuntimeException("Should never get here; probabilities are broken!");
		}
	}
	
	/**
	 * For printing.
	 */
	protected static final String	HEAD	= "\t\t\t\t\t\t\t\t<DYNAMICS> ";
	
	/**
	 * The 'stash'.
	 */
	Set<Block>						stash	= new HashSet<>();
	
	/**
	 * @param world
	 *            - the initial state of the world.
	 */
	public DynamicEnvironment(BlocksWorld world)
	{
		super(world);
	}
	
	/**
	 * Evaluates whether a dynamic action should be performed, and if yes, an action is chosen and performed (if
	 * possible).
	 */
	protected void performDynamicAction()
	{
		if(Math.random() < MyTester.DYNAMICITY)
		{
			DynamicAction dyna = DynamicAction.pick();
			Set<Stack> observedStacks = new HashSet<>();
			for(AgentData ag : getAgents())
				observedStacks.add(worldstate.getStacks().get(stations.indexOf(ag.getStation())));
			switch(dyna)
			{
			case STASH:
			{
				Stack s = pickAStack(true, false, observedStacks);
				if(s == null)
					break;
				Block b = s.getTopBlock();
				if(s.isSingleBlock())
				{
					int pos = worldstate.getStacks().indexOf(s);
					worldstate.pickUp(b);
					stations.remove(pos);
				}
				else
					worldstate.unstack(b, s.getBelow(b));
				stash.add(b);
				System.out.println(HEAD + "[" + b + "] -> stash.");
				break;
			}
			case UNSTASH:
			{
				if(stash.isEmpty())
					break;
				Stack s = pickAStack(true, true, observedStacks);
				if(s == null)
					break;
				Block b = pickElement(stash);
				stash.remove(b);
				worldstate.stack(b, s.getTopBlock());
				System.out.println(HEAD + "[" + b + "] : stash -> " + s + ".");
				break;
			}
			case DROP:
			{
				Stack s = pickAStack(false, false, observedStacks);
				if(s == null)
					break;
				Block b = s.getTopBlock();
				worldstate.unstack(b, s.getBelow(b));
				worldstate.putDown(b, s);
				char stationName = '0';
				while(stations.contains(new Station(stationName)))
					stationName++;
				stations.add(worldstate.getStacks().indexOf(s) - 1, new Station(stationName));
				System.out.println(HEAD + "[" + b + "] -> ___.");
				break;
			}
			case TELEPORT:
			{
				Stack s = pickAStack(true, false, observedStacks);
				if(s == null)
					break;
				Stack s1 = pickAStack(true, true, observedStacks);
				if(s1 == null || s == s1)
					break;
				Block b = s.getTopBlock();
				if(s.isSingleBlock())
				{
					int pos = worldstate.getStacks().indexOf(s);
					worldstate.pickUp(b);
					stations.remove(pos);
				}
				else
					worldstate.unstack(b, s.getBelow(b));
				worldstate.stack(b, s1.getTopBlock());
				System.out.println(HEAD + "[" + b + "] : " + s + " -> " + s1 + ".");
				break;
			}
			default:
				throw new RuntimeException("enums are broken,");
			}
		}
		
		// pick an action
		// pick random stack where there are no agents.
		// have a stash of blocks that are 'hidden'
	}
	
	/**
	 * Picks a random stack which is not currently observed, according to conditions in the arguments.
	 * 
	 * @param canBeSingle
	 *            - can be a single-block stack.
	 * @param canBeLocked
	 *            - the top block can be locked (the stack will not be removed from).
	 * @param observedStacks
	 *            - the stacks where there are currently agents (the stack will not be picked from these).
	 * @return a stack with the given requirements, or <code>null</code> if no such stack can be found.
	 */
	protected Stack pickAStack(boolean canBeSingle, boolean canBeLocked, Set<Stack> observedStacks)
	{
		List<Stack> choiceStacks = new LinkedList<>();
		for(Stack s : worldstate.getStacks())
			if(!observedStacks.contains(s) && (canBeSingle || !s.isSingleBlock())
					&& (canBeLocked || !s.isLocked(s.getTopBlock())))
				choiceStacks.add(s);
		return choiceStacks.isEmpty() ? null : pickElement(choiceStacks);
	}
	
	/**
	 * Pick a random element from a non-empty collection.
	 * 
	 * @param c
	 *            - the collection.
	 * @return the chosen element.
	 */
	protected static <T> T pickElement(Collection<T> c)
	{
		int num = (int) (Math.random() * c.size());
		for(T t : c)
			if(--num < 0)
				return t;
		throw new RuntimeException("Probabilities are broken (or code).");
	}
	
	@Override
	public boolean step()
	{
		performDynamicAction();
		return super.step();
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "\nStash: " + stash + "\n";
	}
}

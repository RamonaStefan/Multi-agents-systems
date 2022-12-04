package blocksworld;

import java.util.HashSet;
import java.util.Set;

import base.Action;

/**
 * The class represents one action that can be performed on the world.
 * 
 * @author Andrei Olaru
 */
public class BlocksWorldAction extends Element<Block> implements Action
{
	/**
	 * The type of the action.
	 * 
	 * @author Andrei Olaru
	 */
	@SuppressWarnings("javadoc")
	public static enum Type implements ElementType {
		PICKUP(1),
		
		PUTDOWN(1),
		
		UNSTACK(2),
		
		STACK(2),
		
		LOCK(1),
		
		NEXT_STATION,
		
		GO_TO_STATION(1),
		
		NONE,
		
		/**
		 * Same as {@link #NONE}, but used only immediately after receiving a new plan.
		 */
		PLANNED,
		
		AGENT_COMPLETED,
		
		;
		
		int args;
		
		private Type()
		{
			args = 0;
		}
		
		private Type(int arguments)
		{
			args = arguments;
		}
		
		@Override
		public int getArgumentNumber()
		{
			return args;
		}
	}
	
	/**
	 * Constructor for actions with no arguments.
	 * 
	 * @param type
	 *            - the type of the action.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public BlocksWorldAction(Type type)
	{
		super(type);
	}
	
	/**
	 * Constructor for actions with one argument.
	 * 
	 * @param type
	 *            - type of the action.
	 * @param argument
	 *            - the argument of the action.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public BlocksWorldAction(Type type, Block argument)
	{
		super(type, argument);
	}
	
	/**
	 * Constructor for actions with two arguments.
	 * 
	 * @param type
	 *            - type of the action.
	 * @param firstArgument
	 *            - first argument of the action.
	 * @param secondArgument
	 *            - second argument of the action.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public BlocksWorldAction(Type type, Block firstArgument, Block secondArgument)
	{
		super(type, firstArgument, secondArgument);
	}
	
	/**
	 * @return the type of the action.
	 */
	public Type getType()
	{
		return (Type) elementType;
	}
	
	/**
	 * @param otherAction
	 *            - the action to test for conflicts with.
	 * @return <code>true</code> if any blocks are common to the two actions.
	 */
	@Deprecated // not used in DynamicBoxen
	public boolean isConflicting(BlocksWorldAction otherAction)
	{
		if(this.elementType == Type.NEXT_STATION)
			return false;
		Set<Block> intersection = new HashSet<>(arguments);
		intersection.retainAll(otherAction.arguments);
		return !intersection.isEmpty();
	}
}

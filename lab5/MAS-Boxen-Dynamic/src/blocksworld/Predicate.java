package blocksworld;

/**
 * The class represents a FOPL predicate that is part of the FOPL representation of the world.
 * 
 * @author Andrei Olaru
 */
public class Predicate extends Element<Block>
{
	/**
	 * Type of the predicate.
	 * 
	 * @author Andrei Olaru
	 */
	@SuppressWarnings("javadoc")
	public static enum Type implements ElementType{
		ARMEMPTY,
		
		HOLD(1),
		
		ON(2),
		
		ONTABLE(1),
		
		CLEAR(1),
		
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
		}	}
	
	
	/**
	 * Constructor for predicates with no arguments.
	 * 
	 * @param type
	 *            - the type of the predicate.
	 */
	public Predicate(Type type)
	{
		super(type);
	}
	
	/**
	 * Constructor for predicates with one argument.
	 * 
	 * @param type
	 *            - the type of the predicate.
	 * @param argument
	 *            - the argument of the predicate.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public Predicate(Type type, Block argument)
	{
		super(type, argument);
	}
	
	/**
	 * Constructor for predicates with one argument.
	 * 
	 * @param type
	 *            - the type of the predicate.
	 * @param firstArgument 
	 *            - first the argument of the predicate.
	 * @param secondArgument 
	 *            - second the argument of the predicate.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	public Predicate(Type type, Block firstArgument, Block secondArgument)
	{
		super(type, firstArgument, secondArgument);
	}
	
	/**
	 * @return the type of the predicate.
	 */
	public Type getType()
	{
		return (Type) elementType;
	}
}

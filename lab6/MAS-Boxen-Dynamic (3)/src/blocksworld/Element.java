package blocksworld;

import java.util.Vector;

/**
 * Abstract element with a type and a number of arguments that depends on the type.
 *
 * @param <T>
 *            the type of the arguments.
 * @author Andrei Olaru
 */
public abstract class Element<T>
{
	/**
	 * Represents the type of the element. Can be an enum implementing this interface.
	 *
	 * @author Andrei Olaru
	 */
	public interface ElementType
	{
		/**
		 * @return the number of arguments of the type.
		 */
		int getArgumentNumber();
	}

	/**
	 * The type (name).
	 */
	protected ElementType	elementType;

	/**
	 * First argument of the predicate.
	 */
	protected Vector<T>		arguments		= null;

	/**
	 * This member must be overwritten by extending classes with the name of the class.
	 */
	protected String		ELEMENT_CLASS	= "ERROR_HERE";

	/**
	 * Constructor.
	 *
	 * @param type
	 *            - the type of the element.
	 * @param argumentsPassed
	 *            - arguments actually passed to the constructor of the extending class.
	 * @throws IllegalArgumentException
	 *             if the given type has a different number of arguments.
	 */
	protected Element(ElementType type, @SuppressWarnings("unchecked") T... argumentsPassed)
	{
		ELEMENT_CLASS = this.getClass().getSimpleName();

		if(argumentsPassed.length != type.getArgumentNumber())
			throw new IllegalArgumentException(ELEMENT_CLASS + " " + type + " has "
					+ type.getArgumentNumber() + " arguments; " + argumentsPassed.length
					+ " given.");
		elementType = type;
		arguments = new Vector<>(type.getArgumentNumber());
		for(int i = 1; i <= type.getArgumentNumber(); i++)
		{
			if(argumentsPassed[i - 1] == null)
				throw new IllegalArgumentException(i
						+ ((i == 1) ? "st" : ((i == 2) ? "nd" : ((i == 3) ? "rd" : "th")))
						+ " argument is null.");
			arguments.add(argumentsPassed[i - 1]);
		}
	}

	/**
	 * @return <code>true</code> if the element has no arguments.
	 */
	public boolean hasNoArgs()
	{
		return elementType.getArgumentNumber() == 0;
	}

	/**
	 * @return <code>true</code> if the element has one argument.
	 */
	public boolean hasOneArg()
	{
		return elementType.getArgumentNumber() == 1;
	}

	/**
	 * @return <code>true</code> if the element has two arguments.
	 */
	public boolean hasTwoArgs()
	{
		return elementType.getArgumentNumber() == 2;
	}

	/**
	 * @return the argument of the predicate, for predicates with only one argument.
	 *
	 * @throws IllegalArgumentException
	 *             if the predicate has more or less than one argument.
	 */
	public T getArgument()
	{
		if(elementType.getArgumentNumber() != 1)
			throw new IllegalArgumentException(ELEMENT_CLASS + " " + elementType
					+ " has less than or more than one argument");
		return arguments.get(0);
	}

	/**
	 * @return the first argument of the predicate, for predicates with two arguments.
	 *
	 * @throws IllegalArgumentException
	 *             if the predicate has less than two arguments.
	 */
	public T getFirstArgument()
	{
		if(elementType.getArgumentNumber() == 0)
			throw new IllegalArgumentException(ELEMENT_CLASS + " " + elementType
					+ " has no arguments");
		return arguments.get(0);
	}

	/**
	 * @return the second argument of the predicate, for predicates with two arguments.
	 *
	 * @throws IllegalArgumentException
	 *             if the predicate has less than two arguments.
	 */
	public T getSecondArgument()
	{
		if(elementType.getArgumentNumber() <= 1)
			throw new IllegalArgumentException(ELEMENT_CLASS + " " + elementType
					+ " has less than two arguments");
		return arguments.get(1);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Element))
			return false;
		@SuppressWarnings("unchecked")
		Element<T> other = (Element<T>) obj;
		return (elementType == other.elementType) && (arguments.equals(other.arguments));
	}
	
	@Override
	public int hashCode()
	{
		return arguments.hashCode();
	}

	@Override
	public String toString()
	{
		String ret = elementType.toString();
		if(elementType.getArgumentNumber() > 0)
		{
			ret += "(";
			int i = 0;
			for(T arg : arguments)
				ret += ((i++ > 0) ? ", " : "") + arg.toString();
			ret += ")";
		}
		return ret;
	}
}

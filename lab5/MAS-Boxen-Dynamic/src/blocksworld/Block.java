package blocksworld;

/**
 * One block.
 * 
 * @author Andrei Olaru
 */
public class Block
{
	/**
	 * The label of the block (1 character).
	 */
	char label;
	
	/**
	 * Constructor.
	 * 
	 * @param label
	 *            - the label.
	 */
	public Block(char label)
	{
		this.label = label;
	}
	
	@Override
	public String toString()
	{
		return Character.valueOf(label).toString();
	}
	
	/**
	 * @return the label.
	 */
	public char getLabel()
	{
		return label;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Block)
			return ((Block) obj).getLabel() == label;
		return false;
	}
	
	/**
	 * Compares label to one character.
	 * 
	 * @param c
	 *            - comparison target.
	 * @return <code>true</code> if equal.
	 */
	public boolean equals(char c)
	{
		return label == c;
	}
	
	@Override
	public int hashCode()
	{
		return Character.valueOf(label).hashCode();
	}
}
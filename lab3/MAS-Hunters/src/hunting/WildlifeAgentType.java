package hunting;

/**
 * The type of an agent in the predator-prey scenario. Can be a predator or prey.
 * 
 * @author andreiolaru
 */
public enum WildlifeAgentType {
	/**
	 * The predator type.
	 */
	PREDATOR("H"),
	
	/**
	 * The prey type.
	 */
	PREY("F")
	
	;
	
	/**
	 * The representation of the agent on the map.
	 */
	String representation;
	
	/**
	 * @param representation
	 *            - the representation
	 */
	private WildlifeAgentType(String representation)
	{
		this.representation = representation;
	}
	
	@Override
	public String toString()
	{
		return representation;
	}
}

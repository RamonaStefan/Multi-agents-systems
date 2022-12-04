package gridworld;

/**
 * Implementation for orthogonal and diagonal relative orientations in a rectangular grid.
 * 
 * @author Andrei Olaru
 */
public enum GridRelativeOrientation {
	/**
	 * Same direction as the current orientation.
	 */
	FRONT(0),
	
	/**
	 * Direction clockwise from the previous value.
	 */
	FRONT_RIGHT(1),
	
	/**
	 * Direction clockwise from the previous value.
	 */
	RIGHT(2),
	
	/**
	 * Direction clockwise from the previous value.
	 */
	BACK_RIGHT(3),
	
	/**
	 * Direction clockwise from the previous value.
	 */
	BACK(4),
	
	/**
	 * Direction clockwise from the previous value.
	 */
	BACK_LEFT(5),
	
	/**
	 * Direction clockwise from the previous value.
	 */
	LEFT(6),
	
	/**
	 * Direction clockwise from the previous value.
	 */
	FRONT_LEFT(7),
	
	;
	
	/**
	 * 0 to 7
	 */
	int angle;
	
	/**
	 * @param relativeAngle
	 *            - angle relative to the front, clockwise, in increments such that 8 increments is a full circle.
	 */
	private GridRelativeOrientation(int relativeAngle)
	{
		angle = relativeAngle;
	}
	
	/**
	 * @return the angle (see constructor).
	 */
	int getAngle()
	{
		return angle;
	}
}
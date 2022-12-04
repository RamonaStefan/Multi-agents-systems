package base;

/**
 * Interface to be implemented by agent implementations. A reactive agent is only defined by its {@link #response} to
 * perceptions.
 * 
 * @author Andrei Olaru
 */
public interface Agent
{
	/**
	 * Computes the response of the agent to the perceptions. The perceptions are offered by the environment and the
	 * action is executed by the environment.
	 * 
	 * @param perceptions
	 *            - the perceptions that are given by the environment to the agent.
	 * 
	 * @return an {@link Action} to be executed by the agent on the environment.
	 */
	Action response(Perceptions perceptions);
	
	/**
	 * @return a 1-character string that is used to identify the agent.
	 */
	@Override
	public String toString();
}

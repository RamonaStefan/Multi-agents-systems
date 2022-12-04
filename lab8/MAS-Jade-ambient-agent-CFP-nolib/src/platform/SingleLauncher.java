package platform;

/**
 * Launches both containers and associated agents.
 * 
 * @author Andrei Olaru
 */
public class SingleLauncher
{
	
	/**
	 * Creates and launches containers.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args)
	{
		MainContainerLauncher main = new MainContainerLauncher();
		SlaveContainerLauncher slave = new SlaveContainerLauncher();
		
		main.setupPlatform();
		slave.setupPlatform();
		main.startAgents();
		slave.startAgents();
	}
	
}

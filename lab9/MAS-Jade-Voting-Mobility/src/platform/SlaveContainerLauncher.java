package platform;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

import agents.RegionRepAgent;

/**
 * Launches a slave container and associated agents.
 */
public class SlaveContainerLauncher
{
	/**
	 * The region rep agent containers.
	 */
	ArrayList<AgentContainer> regionContainers = new ArrayList<>();
	
	/**
	 * Configures and launches the central election container.
	 */
	void setupRegionContainers() {
		int portCt = 1;
		
		for (String regionContainer : Constants.REGION_CONTAINERS) {
			Properties containerProps = new ExtendedProperties();
			containerProps.setProperty(Profile.CONTAINER_NAME, regionContainer);

			containerProps.setProperty(Profile.LOCAL_HOST, Constants.HOST);
			containerProps.setProperty(Profile.LOCAL_PORT, "" + (Constants.PORT + portCt));
			containerProps.setProperty(Profile.PLATFORM_ID, Constants.PLATFORM_ID);
			
			containerProps.setProperty(Profile.MAIN_HOST, Constants.HOST);
			containerProps.setProperty(Profile.MAIN_PORT, "" + Constants.PORT);

			ProfileImpl containerProfile = new ProfileImpl(containerProps);
			AgentContainer agentContainer = Runtime.instance().createAgentContainer(containerProfile);
			
			regionContainers.add(agentContainer);
			
			portCt++;
		}
	}
	
	/**
	 * Starts the region representative agents
	 */
	void startRegionRepresentativeAgents() {
		int id = 1;
		
		for (AgentContainer agContainer : regionContainers) {
			String regionRepName = "region_rep_" + id;
			String regionVoteName = "region" + (id++);
			
			try {
				AgentController regionRepAgentCtrl = agContainer.createNewAgent(regionRepName, RegionRepAgent.class.getName(), new Object[] {regionVoteName});
				regionRepAgentCtrl.start();
				
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Launches the slave containers for .
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args)
	{
		SlaveContainerLauncher launcher = new SlaveContainerLauncher();
		launcher.setupRegionContainers();
		launcher.startRegionRepresentativeAgents();
	}
	
}

package platform;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import agents.BraceletAgent;
import agents.PhoneAlarmAgent;
import agents.PreferenceAgent;

/**
 * Launches a slave container and associated agents.
 */
public class SlaveContainerLauncher
{
	
	/**
	 * A reference to the launched container.
	 */
	AgentContainer secondaryContainer;
	
	/**
	 * Configures and launches a slave container.
	 */
	void setupPlatform()
	{
		Properties secondaryProps = new ExtendedProperties();
		secondaryProps.setProperty(Profile.CONTAINER_NAME, "AmI-Slave"); // change if multiple slaves.
		
		// TODO: replace with actual IP of the current machine
		secondaryProps.setProperty(Profile.LOCAL_HOST, "localhost");
		secondaryProps.setProperty(Profile.LOCAL_PORT, "1100");
		secondaryProps.setProperty(Profile.PLATFORM_ID, "ami-agents");
		
		// TODO: replace with actual IP of the machine running the main container.
		secondaryProps.setProperty(Profile.MAIN_HOST, "localhost");
		secondaryProps.setProperty(Profile.MAIN_PORT, "1099");
		
		ProfileImpl secondaryProfile = new ProfileImpl(secondaryProps);
		secondaryContainer = Runtime.instance().createAgentContainer(secondaryProfile);
	}
	
	/**
	 * Starts the agents assigned to this container.
	 */
	void startAgents()
	{
		try
		{
			AgentController braceletAgentCtrl = secondaryContainer.createNewAgent("bracelet",
					BraceletAgent.class.getName(), null);
			AgentController phoneAlarmAgentCtrl = secondaryContainer.createNewAgent("phone-alarm",
					PhoneAlarmAgent.class.getName(), null);
			
			AgentController preferenceAgentCtrl = secondaryContainer.createNewAgent("preference",
					PreferenceAgent.class.getName(), null);
			
			braceletAgentCtrl.start();
			phoneAlarmAgentCtrl.start();
			preferenceAgentCtrl.start();
		} catch(StaleProxyException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Launches a slave container.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args)
	{
		SlaveContainerLauncher launcher = new SlaveContainerLauncher();
		
		launcher.setupPlatform();
		launcher.startAgents();
	}
	
}

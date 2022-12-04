package platform;

import agents.PersonalAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Launches a main container and associated agents.
 */
public class MainContainerLauncher {

	/**
	 * The main container.
	 */
	AgentContainer mainContainer;

	/**
	 * Configures and launches the main container.
	 */
	void setupPlatform() {
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.GUI, "true"); // start the JADE GUI
		mainProps.setProperty(Profile.MAIN, "true"); // is main container
		mainProps.setProperty(Profile.CONTAINER_NAME, "AmI-Main"); // you can rename it
		// TODO: replace with actual IP of the current machine
		mainProps.setProperty(Profile.LOCAL_HOST, "localhost");
		mainProps.setProperty(Profile.LOCAL_PORT, "1099");
		mainProps.setProperty(Profile.PLATFORM_ID, "ami-agents");

		ProfileImpl mainProfile = new ProfileImpl(mainProps);
		mainContainer = Runtime.instance().createMainContainer(mainProfile);
	}

	/**
	 * Starts the agents assigned to the main container.
	 */
	void startAgents() {
		try {
			AgentController personalAgentCtrl = mainContainer.createNewAgent("personal", PersonalAgent.class.getName(), null);
			personalAgentCtrl.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launches the main container.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args) {
		MainContainerLauncher launcher = new MainContainerLauncher();

		launcher.setupPlatform();
		launcher.startAgents();
	}

}

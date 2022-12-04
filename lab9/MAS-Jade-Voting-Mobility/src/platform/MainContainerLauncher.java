package platform;

import java.util.ArrayList;
import java.util.List;

import agents.ElectionManagerAgent;
import agents.VoteCollectorAgent;
import jade.core.AID;
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
	 * The voting agent containers.
	 */
	AgentContainer containerCentralElection;

	/**
	 * Configures and launches the central election container.
	 */
	void setupCentralElectionContainer() {
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.GUI, "true"); // start the JADE GUI
		mainProps.setProperty(Profile.MAIN, "true"); // is main container
		mainProps.setProperty(Profile.CONTAINER_NAME, Constants.CENTRAL_ELECTION); // you can rename it
		mainProps.setProperty(Profile.LOCAL_HOST, Constants.HOST);
		mainProps.setProperty(Profile.LOCAL_PORT, "" + Constants.PORT);
		mainProps.setProperty(Profile.PLATFORM_ID, Constants.PLATFORM_ID);

		ProfileImpl mainProfile = new ProfileImpl(mainProps);
		containerCentralElection = Runtime.instance().createMainContainer(mainProfile);
	}

	/**
	 * Starts the agents assigned to the central election container.
	 */
	void startCentralElectionAgents() {
		String electionManagerName = "election_mgr";
		String voteCollectorName = "vote_collector";
		
		try {
			List<String> regionVoteNames = new ArrayList<>();
			for (String region : Constants.REGION_CONTAINERS) 
				regionVoteNames.add("region_" + region);
			
			AgentController electionMgrAgentCtrl = containerCentralElection.createNewAgent(electionManagerName, ElectionManagerAgent.class.getName(), 
					new Object[] {new AID(voteCollectorName, AID.ISLOCALNAME), regionVoteNames});
			electionMgrAgentCtrl.start();
			
			AgentController voteCollectorAgentCtrl = containerCentralElection.createNewAgent(voteCollectorName, VoteCollectorAgent.class.getName(), null);
			voteCollectorAgentCtrl.start();
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

		launcher.setupCentralElectionContainer();
		launcher.startCentralElectionAgents();
	}

}

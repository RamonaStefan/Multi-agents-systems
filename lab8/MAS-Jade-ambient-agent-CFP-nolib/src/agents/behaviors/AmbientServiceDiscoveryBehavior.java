package agents.behaviors;

import agents.PersonalAgent;
import agents.ServiceType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;
import platform.Log;

/**
 * The behavior for discovering other agents.
 */
public class AmbientServiceDiscoveryBehavior extends ParallelBehaviour
{
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = 7816281320324746190L;
	
	/**
	 * Describe a service need.
	 */
	class ServiceNeed
	{
		/**
		 * The type of the service.
		 */
		String	serviceType;
		/**
		 * The number of agents needed for this service.
		 */
		int		numberNeeded;
		/**
		 * The number of agents providing this service found so far.
		 */
		int		foundSoFar	= 0;
		
		/**
		 * The constructor.
		 * 
		 * @param type
		 *            - the type.
		 * @param limit
		 *            - the number needed.
		 */
		public ServiceNeed(String type, int limit)
		{
			serviceType = type;
			numberNeeded = limit;
		}
	}
	
	/**
	 * @param a
	 *            - the agent.
	 * @param endCondition
	 *            - end condition, as defined in {@link ParallelBehaviour}.
	 */
	public AmbientServiceDiscoveryBehavior(Agent a, int endCondition)
	{
		super(a, endCondition);
	}
	
	@Override
	public void onStart()
	{
		ServiceNeed[] needs = new ServiceNeed[] { new ServiceNeed(ServiceType.AMBIENT_AGENT, 2),
				new ServiceNeed(ServiceType.PREFERENCE_AGENT, 1) };
		
		AID dfAgent = myAgent.getDefaultDF();
		Log.log(myAgent, "Default DF Agent: " + dfAgent);
		
		for(ServiceNeed need : needs)
		{
			// Build the DFAgentDescription which holds the service descriptions for the the ambient-agent service
			// and the preference-agent description
			DFAgentDescription DFDesc = new DFAgentDescription();
			ServiceDescription serviceDesc = new ServiceDescription();
			serviceDesc.setType(need.serviceType);
			DFDesc.addServices(serviceDesc);
			
			SearchConstraints cons = new SearchConstraints();
			cons.setMaxResults(Long.valueOf(need.numberNeeded));
			
			// add sub behavior for ambient-agent service discovery
			this.addSubBehaviour(new SubscriptionInitiator(myAgent,
					DFService.createSubscriptionMessage(myAgent, dfAgent, DFDesc, cons)) {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void handleInform(ACLMessage inform)
				{
					Log.log(myAgent, "Notification received from DF");
					
					try
					{
						DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
						if(results.length > 0)
							for(DFAgentDescription dfd : results)
							{
								AID provider = dfd.getName();
								// The same agent may provide several services; we are interested
								// in the ambient-agent and preference-agent ones
								for(Iterator it = dfd.getAllServices(); it.hasNext();)
								{
									ServiceDescription sd = (ServiceDescription) it.next();
									if(sd.getType().equals(need.serviceType))
									{
										Log.log(myAgent, need.serviceType, "service found: Service \"", sd.getName(),
												"\" provided by agent ", provider.getName());
										((PersonalAgent) myAgent).addServiceAgent(need.serviceType, provider);
										need.foundSoFar++;
									}
								}
							}
						
						// the behavior can be removed if we have at least two providers for the ambient-agent service
						if(need.foundSoFar >= need.numberNeeded)
							cancel(inform.getSender(), true);
					} catch(FIPAException fe)
					{
						fe.printStackTrace();
					}
				}
			});
		}
		super.onStart();
	}
	
}

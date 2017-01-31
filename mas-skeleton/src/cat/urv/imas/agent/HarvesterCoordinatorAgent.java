/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.harvestercoordinator.CoalitionBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import static cat.urv.imas.agent.ImasAgent.OWNER;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import java.io.IOException;
/**
 *
 * @author Daniel, Dario, Pablo, Angel y Emanuel
 */
public class HarvesterCoordinatorAgent extends ImasAgent{
     /**
     * Builds the coordinator agent.
     */

    private GameSettings game = null;

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }

    
    public void addSettableBuildingCellList(SettableBuildingCell e) {
        this.SettableBuildingCellList.add(e);
    }

    
    
    public ArrayList<SettableBuildingCell> getSettableBuildingCellList() {
        return SettableBuildingCellList;
    }

    public void setSettableBuildingCellList(ArrayList<SettableBuildingCell> SettableBuildingCellList) {
        this.SettableBuildingCellList = SettableBuildingCellList;
    }

    public ArrayList<ServiceDescription> getServiceDescriptionList() {
        return ServiceDescriptionList;
    }

    public void setServiceDescriptionList(ArrayList<ServiceDescription> ServiceDescriptionList) {
        this.ServiceDescriptionList = ServiceDescriptionList;
    }

    public ArrayList<Integer> getAgentStatus() {
        return AgentStatus;
    }

    public void setAgentStatus(ArrayList<Integer> AgentStatus) {
        this.AgentStatus = AgentStatus;
    }

    public ArrayList<Cell> getRecyclingCenter() {
        return RecyclingCenter;
    }

    public void setRecyclingCenter(ArrayList<Cell> RecyclingCenter) {
        this.RecyclingCenter = RecyclingCenter;
    }
    
    private ArrayList<SettableBuildingCell> SettableBuildingCellList = new ArrayList<>();
    private ArrayList<ServiceDescription> ServiceDescriptionList = new ArrayList<>();
    private ArrayList<Integer> AgentStatus = new ArrayList<>();
    private ArrayList<Cell> RecyclingCenter = new ArrayList<>();
    
    
    public HarvesterCoordinatorAgent() {
        super(AgentType.HARVESTER_COORDINATOR);
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription hca = new ServiceDescription();
        hca.setType(AgentType.HARVESTER_COORDINATOR.toString());
        hca.setName(getLocalName());
        hca.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(hca);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }        
        log("Creatad new Harvester Coordinator: " + getLocalName());

        /* ********************************************************************/    
        // contract net system        
        System.out.println("Agent "+getLocalName()+" waiting for CFP...");
  	MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP) );
  		
	addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                SettableBuildingCell proposal = null;
                try {
                    // Call evaluateAction to convert cfp to SettableBuildingCell
                    proposal = evaluateAction(cfp);
                    System.out.println("2. "+getLocalName()+": contract "+cfp.getConversationId()+" received from "+cfp.getSender().getName());
                } catch (UnreadableException ex) {
                    Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("2. "+getLocalName()+": Refuse");
                    throw new RefuseException("evaluation-failed");                    
                }
                // Provide a proposal: HC always accepts proposal           
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                try {
                    propose.setContentObject(proposal);
                } catch (IOException ex) {
                    Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                }                
                propose.setContent(String.valueOf(proposal));                 
                return propose;              
            }
			
            @Override
            protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                System.out.println("5. "+getLocalName()+": proposal for contract "+accept.getConversationId()+" was accepted");
		if (performAction()) {
                    System.out.println("7. "+getLocalName()+": Action successfully performed on "+accept.getConversationId());
                    ACLMessage inform = accept.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent(accept.getContent());
                    return inform;
		}
		else {
                    System.out.println("7. "+getLocalName()+": action execution failed on "+accept.getConversationId());
                    throw new FailureException("unexpected-error");
		}	
            }
			
            protected void handleRejectProposal(ACLMessage reject) {
                System.out.println("5. "+getLocalName()+": Proposal:"+reject.getConversationId()+" rejected");
            }
        } );
    }
  
    private SettableBuildingCell evaluateAction(ACLMessage contract) throws UnreadableException {
  	// Convert ACLMessage to SettableBuildingCell
        SettableBuildingCell proposal = (SettableBuildingCell) contract.getContentObject();

  	return proposal;
    }
  
    private boolean performAction() { 
        System.out.println("6. "+getLocalName()+": formed a coalition");
        
        //INICIO DARIO
        // Register the service
        String serviceName = AgentType.HARVESTER_COORDINATOR.toString();
        String serviceType = AgentType.HARVESTER_COORDINATOR.toString();
        try {
                
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType(serviceType);
            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            dfd.addServices(sd);
  		
            DFService.register(this, dfd);
            System.out.println("Agent "+getLocalName()+" registering service \""+serviceName+"\" of type "+serviceType);
  	
        }
        catch (FIPAException e) {
            e.printStackTrace();
        }
        
        
        //Behaviours
        addBehaviour(new SearchHarvesterBehaviour(this));
        addBehaviour(new ReceiveInfoBehaviour(this));
        addBehaviour(new CoalitionBehaviour(this));
        
        //FIN DARIO
        
        
    }    
    
    
    //INICIO DARIO
    
    
    
    private class ReceiveInfoBehaviour extends CyclicBehaviour {

        public ReceiveInfoBehaviour(HarvesterCoordinatorAgent agent) {
            super(agent);
        }
        
        @Override
        public void action() {
            
            HarvesterCoordinatorAgent agent = (HarvesterCoordinatorAgent)this.getAgent();
            
            ACLMessage msg= receive();
            if (msg!=null){
                try {
                    if(msg.getContentObject().getClass().equals(MessageWrapper.class)){
                        MessageWrapper message = (MessageWrapper) msg.getContentObject();
                        switch(message.getType()){
                        case MessageContent.SEND_GAME:
                            agent.setGame((GameSettings) message.getObject());
                            System.out.println( " Message Object Received " + " <----------- " + message.getType() );
                            //Populate RecyclingCenter list
                            try{
                                for(Cell[] cc : agent.game.getMap()){
                                    for(Cell c : cc){
                                        if(c.getCellType().equals(CellType.RECYCLING_CENTER)){
                                            RecyclingCenter.add(c);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                            }
                            
                        case MessageContent.SETTABLE_BUILDING:
                            System.out.println( " Message Object Received " + " <----------- " + message.getType() );
                            //AQUI VA EL CONTRACT NET DE DANIEL
                            //agrego un elemento a la lista de SettableBuildingCellList
                        }
                    }
                    
                    
                } catch (UnreadableException ex) {
                    Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                //block();
            }
        }
  	// Call coalition to collect garbage
//        MessageWrapper messageCoalition = new MessageWrapper.setObject();
        // coalition(cellBuilding)
  	return true;
    }
    
    
    private class SearchHarvesterBehaviour extends OneShotBehaviour {

        private SearchHarvesterBehaviour(HarvesterCoordinatorAgent agent) {
            super(agent);
        }
    
        @Override
        public void action() {
            
            HarvesterCoordinatorAgent agent = (HarvesterCoordinatorAgent)this.getAgent();
            
            
            //Delay the search in DF
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // Search for services of type "HARVESTER"
            //String serviceType = "HARVESTER";
            String serviceType = AgentType.HARVESTER.toString();
            
            System.out.println("Agent "+getLocalName()+" searching for services of type "+serviceType);
            try {
                // Build the description used as template for the search
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription templateSd = new ServiceDescription();
                templateSd.setType(serviceType);
                template.addServices(templateSd);
  		
                SearchConstraints sc = new SearchConstraints();
                // We want to receive 20 results at most
                sc.setMaxResults(new Long(20));
  	
                DFAgentDescription[] results = DFService.search(HarvesterCoordinatorAgent.this, template, sc);
                
                if (results.length > 0) {
                    System.out.println("Agent "+getLocalName()+" found the following services:");
                    for (int i = 0; i < results.length; ++i) {
  			DFAgentDescription dfd = results[i];
  			AID provider = dfd.getName();
                        System.out.println("provider----> "+provider.toString());
                        Iterator it = dfd.getAllServices();
                        while (it.hasNext()) {
                            ServiceDescription sd = (ServiceDescription) it.next();
                            if (sd.getType().equals(AgentType.HARVESTER.toString())) {
                                agent.ServiceDescriptionList.add(sd);
                            }
                        }
                    }
                    //Add element to agentStatusList(one element per Harvester)
                    for (ServiceDescription e : agent.ServiceDescriptionList) {
                        agent.AgentStatus.add(0);
                    }
                }
                
                
                else {
                    System.out.println("Agent "+getLocalName()+" did not find any "+serviceType+ " service");
                }
            
            }
            catch (FIPAException fe) {
  		fe.printStackTrace();
            }
        }
    
         
    }
}    
            
    
//FIN DARIO
     
}

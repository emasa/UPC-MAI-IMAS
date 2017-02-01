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
import cat.urv.imas.behaviour.harvestercoordinator.PathFinderBehaviour;
import cat.urv.imas.behaviour.harvestercoordinator.StepsResponseBehaviour;
import cat.urv.imas.map.StreetCell;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Daniel, Dario, Pablo, Angel y Emanuel
 */
public class HarvesterCoordinatorAgent extends ImasAgent{

    //private GameSettings game;
     /**
     * Builds the coordinator agent.
     */

    private GameSettings game = null;
    
    private Map<AID,SettableBuildingCell> harvesterGarbageBuilding = new HashMap<>();
    private AID CoordinatorAgent;

       
    public Map<AID, SettableBuildingCell> getHarvesterGarbageBuilding() {
        return harvesterGarbageBuilding;
    }

    public void setHarvesterGarbageBuilding(Map<AID, SettableBuildingCell> harvesterGarbageBuilding) {
        this.harvesterGarbageBuilding = harvesterGarbageBuilding;
    }
    protected Map<AID, List<StreetCell>> harvesterGarbagePaths = new HashMap<>();
    protected Map<AID, List<StreetCell>> harvesterRecyclePaths = new HashMap<>();
    
    protected List<AID> AIDList = new ArrayList();

    public List<AID> getAIDList() {
        return AIDList;
    }

    public void setAIDList(List<AID> AIDList) {
        this.AIDList = AIDList;
    }
    
    public Map<AID, List<StreetCell>> getHarvesterGarbagePaths() {
        return harvesterGarbagePaths;
    }

    public void setHarvesterGarbagePaths(Map<AID, List<StreetCell>> harvesterGarbagePaths) {
        this.harvesterGarbagePaths = harvesterGarbagePaths;
    }

    public Map<AID, List<StreetCell>> getHarvesterRecyclePaths() {
        return harvesterRecyclePaths;
    }

    public void setHarvesterRecyclePaths(Map<AID, List<StreetCell>> harvesterRecyclePaths) {
        this.harvesterRecyclePaths = harvesterRecyclePaths;
    }
    
    
    
    private PathFinderBehaviour pfb = new PathFinderBehaviour();
    
    private double[][] distances;

    public double[][] getDistances() {
        return distances;
    }

    public void setDistances(double[][] distances) {
        this.distances = distances;
    }

    public PathFinderBehaviour getPfb() {
        return pfb;
    }

    public void setPfb(PathFinderBehaviour pfb) {
        this.pfb = pfb;
    }

    private int CoalitionNumber = 1;

    public int getCoalitionNumber() {
        return CoalitionNumber;
    }

    public void setCoalitionNumber(int CoalitionNumber) {
        this.CoalitionNumber = CoalitionNumber;
    }

    
    public void addSettableBuildingCellList(SettableBuildingCell e) {
        this.SettableBuildingCellList.add(e);
    }

    public AID getCoordinatorAgent() {
        return CoordinatorAgent;
    }

    public void setCoordinatorAgent(AID CoordinatorAgent) {
        this.CoordinatorAgent = CoordinatorAgent;
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
    
    public void addRecyclingCenter(Cell RecyclingCenter) {
        this.RecyclingCenter.add(RecyclingCenter);
    }
    public void clearRecyclingCenter() {
        this.RecyclingCenter.clear();
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
        
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.game = (GameSettings) args[0];
            if (getHarvesterGarbagePaths().isEmpty()) {
                log("Setup initial empty paths");

                Map<AID, List<StreetCell>> harvesterGarbagePaths = new HashMap<>();

                List<Cell> harvesterCells = game.getAgentList().get(AgentType.HARVESTER);
                for (int i = 0; i < harvesterCells.size(); i++) {
                    StreetCell harvesterCell = (StreetCell) harvesterCells.get(i);
                    AID harvester = harvesterCell.getAgent().getAID();
                    harvesterGarbagePaths.put(harvester, new ArrayList<StreetCell>());
                }
                setHarvesterGarbagePaths(harvesterGarbagePaths);
            }
        } else {
            // Make the agent terminate immediately
            doDelete();
        }
        
        log("Creatad new Harvester Coordinator: " + getLocalName());
        
        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.CoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.addBehaviour(new StepsResponseBehaviour(this, mt));
        addBehaviour(new SearchHarvesterBehaviour(this));
        addBehaviour(new ReceiveInfoBehaviour(this));
        addBehaviour(new CoalitionBehaviour(this));
        addBehaviour(new StartGraphBehaviour(this));
        addBehaviour(new CheckEmptyBuildingBehaviour(this));
                

        
        /* ********************************************************************/    
        // contract net system        
        System.out.println("Agent " + getLocalName() + " waiting for CFP...");
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
        addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                SettableBuildingCell proposal = null;
                try {
                    //Call evaluateAction to convert cfp to SettableBuildingCell
                    proposal = evaluateAction(cfp);
                    System.out.println("2. " + getLocalName() + ": contract " + cfp.getConversationId() + " received from " + cfp.getSender().getName());
                } catch (UnreadableException ex) {
                    Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("2. " + getLocalName() + ": Refuse");
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
                System.out.println("5. " + getLocalName() + ": proposal for contract " + accept.getConversationId() + " was accepted");

                SettableBuildingCell action = null;
                try {
                    // Call evaluateAction to convert cfp to SettableBuildingCell
                    action = evaluateAction(cfp);
                } catch (UnreadableException ex) {
                    Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (performAction(action)) {
                    System.out.println("7. " + getLocalName() + ": Action successfully performed on " + accept.getConversationId());
                    ACLMessage inform = accept.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent(accept.getContent());
                    return inform;
                } else {
                    System.out.println("7. " + getLocalName() + ": action execution failed on " + accept.getConversationId());
                    throw new FailureException("unexpected-error");
                }
            }

            protected void handleRejectProposal(ACLMessage reject) {
                System.out.println("5. " + getLocalName() + ": Proposal:" + reject.getConversationId() + " rejected");
            }
        });
    }
    
    public ACLMessage createMovementDone() {
        ACLMessage sendGarbageRequest = new ACLMessage(ACLMessage.REQUEST);
        sendGarbageRequest.clearAllReceiver();
        sendGarbageRequest.addReceiver(this.getCoordinatorAgent());
        sendGarbageRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);        
     
        try {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setType(MessageContent.GET_HARVESTER_STEPS_REPLY);
            sendGarbageRequest.setContentObject(wrapper);

            log("Notify to coordinator step finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        return sendGarbageRequest;
    }
    
    public Map<String,StreetCell> getNextPosition(AID harvester) {
        Map<String,StreetCell> message = new HashMap<>();
        ArrayList<StreetCell> path = (ArrayList<StreetCell>) this.getHarvesterGarbagePaths().get(harvester);
        ArrayList<StreetCell> path2 = (ArrayList<StreetCell>) this.getHarvesterRecyclePaths().get(harvester);
        if(path.isEmpty()){
            if(this.harvesterGarbageBuilding.get(harvester) != null){
                // Harvest garbage
                StreetCell nextPosition = path2.get(0);
                if(!nextPosition.isThereAnAgent()){
//                    TODO: path2.remove(0);
                    message.put("havest", nextPosition);
                }else
                    message.put("wait", null);
            }else{
                if(!path2.isEmpty()){
                    // Go to recycle center
                    StreetCell nextPosition = path2.get(0);
                    if(!nextPosition.isThereAnAgent()){
                        message.put("Recycle", nextPosition);
                    }else
                        message.put("wait", null);
                }else{
                    // you arrive
                    message.put("Recycle",null);
                }
            }
        }else{
            StreetCell nextPosition = path.get(0);
            if(!nextPosition.isThereAnAgent()){
                path.remove(0);
                if(path.isEmpty())
                    message.put("destination", nextPosition);
                else
                    message.put("movement", nextPosition);
            }else
                message.put("wait", null);
        }
        return message;
    }

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }
  
    private SettableBuildingCell evaluateAction(ACLMessage contract) throws UnreadableException {
  	// Convert ACLMessage to SettableBuildingCell
        MessageWrapper proposal1 = (MessageWrapper) contract.getContentObject();
        SettableBuildingCell proposal = (SettableBuildingCell) proposal1.getObject();
  	return proposal;
    }
  
    private boolean performAction(SettableBuildingCell action) { 
        
        addSettableBuildingCellList(action);
        System.out.println("6. "+getLocalName()+": formed a coalition");        
        
        return true;
    }  
    
    
    //INICIO DARIO
    
    
    
    
    
    
    private class ReceiveInfoBehaviour extends CyclicBehaviour {

        public ReceiveInfoBehaviour(HarvesterCoordinatorAgent agent) {
            super(agent);
        }
        
        @Override
        public void action() {
            
            HarvesterCoordinatorAgent agent = (HarvesterCoordinatorAgent)this.getAgent();
            
            ACLMessage msg= blockingReceive(ACLMessage.INFORM);
            if (msg!=null){
                try {
                    if (msg.getContentObject().getClass().equals(MessageWrapper.class)) {
                        MessageWrapper message = (MessageWrapper) msg.getContentObject();
                        switch (message.getType()) {
                            case MessageContent.SEND_GAME:
//                                agent.setGame((GameSettings) message.getObject());
                                //System.out.println(" Message Object Received " + " <----------- " + message.getType());
                                //Populate RecyclingCenter list
                                try {
                                    agent.clearRecyclingCenter();
                                    for (Cell[] cc : agent.game.getMap()) {
                                        for (Cell c : cc) {
                                            if (c.getCellType().equals(CellType.RECYCLING_CENTER)) {
                                                RecyclingCenter.add(c);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                }

                            case MessageContent.SETTABLE_BUILDING:
                                agent.send(msg);

                            default:

                        }
                    } else {
                        System.out.println(msg.getContentObject().getClass());
                    }
                } catch (UnreadableException ex) {
                    Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                
        
                
                
                
                
            }
        }
  	// Call coalition to collect garbage
//        MessageWrapper messageCoalition = new MessageWrapper.setObject();
        // coalition(cellBuilding)
       // return true;
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
                        agent.getAIDList().add(provider);
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
                    //Add element to harvesterGarbagePaths and harvesterRecyclePaths
                    int i = 0;
                    for (ServiceDescription e : agent.ServiceDescriptionList) {
                        agent.AgentStatus.add(0);
                        List<StreetCell> emptyList = new ArrayList<>();
                        agent.harvesterGarbagePaths.put(agent.getAIDList().get(i), emptyList);
                        agent.harvesterRecyclePaths.put(agent.getAIDList().get(i), emptyList);
                        i++;
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
    
    
    
    private class StartGraphBehaviour extends OneShotBehaviour{

        public StartGraphBehaviour(HarvesterCoordinatorAgent agent) {
            super(agent);
        }
        
        @Override
        public void action() {
            //Create distances matrix
            HarvesterCoordinatorAgent agent = (HarvesterCoordinatorAgent)this.getAgent();
            
            agent.getPfb().initGraph();
            agent.getPfb().buildGraphByMap();
            agent.setDistances(agent.getPfb().floydWarshall());
            
            agent.setDistances(agent.pfb.getDistances());
            /*
            for (int i=0;i<agent.getDistances().length;i++){
                for (int j=0;j<agent.getDistances()[0].length;j++){
                    System.out.println("Distance Graph created." + agent.getDistances()[i][j]);
                
                }
            }
            */      
        }
    }
    
    private class CheckEmptyBuildingBehaviour extends CyclicBehaviour{

        public CheckEmptyBuildingBehaviour(HarvesterCoordinatorAgent agent) {
            super(agent);
        }
        @Override
        public void action() {
            HarvesterCoordinatorAgent agent = (HarvesterCoordinatorAgent)this.getAgent();
            
            //Check if Building is empty and update AgentStatus
            for(int i=0;i<agent.getAgentStatus().size();i++){
                if(agent.getAgentStatus().get(i) > 0){
                    
                    SettableBuildingCell building = agent.getHarvesterGarbageBuilding().get(agent.getAIDList().get(i));
                    //If building empty means harvester took the trash to rc.
                    if(building.detectGarbage().isEmpty()){
                        //Remove agent from coalition
                        System.out.println("Removing agent "+agent.getAID().getLocalName()+" from coalition "+agent.getAgentStatus().get(i));
                        agent.getAgentStatus().set(i, 0);
                    }
                
                }
            }
        
        
        
        }

    }
    
    
    
}    
     



    
//FIN DARIO

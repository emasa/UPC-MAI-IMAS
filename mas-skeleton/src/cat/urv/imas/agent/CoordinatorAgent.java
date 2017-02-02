/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.coordinator.GarbageReciever;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviour;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.MessageWrapper;

import jade.core.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.*;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    private GameSettings game;

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }
    /**
     * System agent id.
     */
    private AID systemAgent;
    /**
     * Scout Coordinator agent id.
     */
     private AID scoutCoordinatorAgent;
    /**
     * Harvester Coordinator agent id.
     */
    private AID hcAgent;
    
    ArrayList<BuildingCell> garbageFound = new ArrayList<>();
    
    ArrayList<BuildingCell> garbageCollected = new ArrayList<>();
    
    ArrayList<BuildingCell> garbageCollecting = new ArrayList<>();

    private boolean scoutsFinished = false;
    private boolean harvestersFinished = false;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
        garbageFound = new ArrayList<>();
        garbageCollected = new ArrayList<>();
        garbageCollecting = new ArrayList<>();   
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription ca = new ServiceDescription();
        ca.setType(AgentType.COORDINATOR.toString());
        ca.setName(getLocalName());
        ca.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(ca);
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
        } else {
            // Make the agent terminate immediately
            doDelete();
        }        
        
        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);

        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        // search ScoutCoordinatorAgent
        searchCriterion.setType(AgentType.SCOUT_COORDINATOR.toString());
        this.scoutCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        // Start ContractNet message
//        ACLMessage gameRequest = new ACLMessage(ACLMessage.REQUEST);
//        gameRequest.clearAllReceiver();
//        gameRequest.addReceiver(this.systemAgent);
//        gameRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
//        log("Request message to agent");
//        try {
//            gameRequest.setContent(MessageContent.GET_MAP);
//            log("Request message content:" + gameRequest.getContent());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.addBehaviour(new GarbageReciever(this, mt));
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
        
        /* ********************************************************************/

        //INICIO DARIO
        //Add behaviour to inform basic info about game
        addBehaviour(new InitialInformToHarvesterCoordinatorAgentBehaviour(this));
        
        try {
//            if(senderID.equals(agent.getScoutCoordinatorAgent())){

                //ADD MANUALLY GARBAGE TO TEST COALITION
                
//                ArrayList<BuildingCell> SettableBuildingCellList = agent.getGarbageFound();
                
                ArrayList<BuildingCell> SettableBuildingCellList = new ArrayList<>();
                      
                SettableBuildingCell g1 = new SettableBuildingCell(3,3);
                g1.setGarbage(GarbageType.PAPER, 10);
                SettableBuildingCell g2 = new SettableBuildingCell(16,3);
                g2.setGarbage(GarbageType.GLASS, 20);
                SettableBuildingCell g3 = new SettableBuildingCell(3,7);
                g3.setGarbage(GarbageType.PLASTIC, 20);
                SettableBuildingCellList.add(g1);
                SettableBuildingCellList.add(g2);
                SettableBuildingCellList.add(g3);
                addGarbageFound(SettableBuildingCellList);
                
//                ArrayList<BuildingCell> garbageBuildings = (ArrayList<BuildingCell>) msg.getContentObject();
//                agent.addGarbageFound(garbageBuildings);
//                agent.log("total garbage to comunicate to Harvesters: "+garbageBuildings);
             
                /* ********************************************************************/
                // contract net system
                ServiceDescription searchHC = new ServiceDescription();     
                searchHC.setType(AgentType.HARVESTER_COORDINATOR.toString());
                this.hcAgent = UtilsAgents.searchAgent(this, searchHC);
//                this.setHarvesterCoordinatorAgent(UtilsAgents.searchAgent(this, searchHC));    
                
                // Cycle through garbageBuildings to get cell and start a ContractNet
                for (int i = 0; i < this.garbageFound.size(); i++) {
                    BuildingCell celda = this.garbageFound.get(i);
                    // Fill the CFP message
                    ACLMessage contract = new ACLMessage(ACLMessage.CFP);
                    contract.addReceiver(this.hcAgent);             // Receiver is HarvesterCoordinator
                    contract.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);  // ContractNet protocol
                    contract.setReplyByDate(new Date(System.currentTimeMillis() + 10000));  // We want to receive a reply in 10 secs
                    contract.setConversationId("Contract: "+i);                             // ConversationID                    
                    System.out.println("1. "+this.getLocalName()+": sent contract "+contract.getConversationId());
                    
                    try {
                        MessageWrapper mmm = new MessageWrapper();
                        mmm.setType(MessageContent.SETTABLE_BUILDING);
                        mmm.setObject(celda);
                        
                        contract.setContentObject(mmm);
                    } catch (IOException ex) {
                        Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    this.log("ContractNet Started");                    
                    
                    this.addBehaviour(new ContractNetInitiator(this, contract) {
                        @Override
                        protected void handlePropose(ACLMessage propose, Vector v) {
                            // Receive Proposal
                            System.out.println("3. "+propose.getSender().getName()+": proposed a coalition on "+propose.getConversationId());
                        }

                        @Override
                        protected void handleRefuse(ACLMessage refuse) {
                            System.out.println("3. "+refuse.getSender().getName()+": refused "+refuse.getConversationId());
                        }

                        @Override
                        protected void handleFailure(ACLMessage failure) {
                            if (failure.getSender().equals(myAgent.getAMS())) {
                                // FAILURE notification from the JADE runtime: the receiver
                                // does not exist
                                System.out.println("Responder does not exist");
                            }
                            else {
                                System.out.println("Agent "+failure.getSender().getName()+" failed "+failure.getConversationId());
                            }
                        }

                        @Override
                        protected void handleAllResponses(Vector responses, Vector acceptances) {
                            // Accept Proposal. CA always accepts proposal
                            Enumeration e = responses.elements();
                            while (e.hasMoreElements()) {
                                ACLMessage proposal = (ACLMessage) e.nextElement();                    
                                ACLMessage accept = proposal.createReply();
                                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                acceptances.addElement(accept);
                                accept.setContent(proposal.getContent()); 
                                System.out.println("4. "+myAgent.getLocalName()+": accepted proposal "+proposal.getContent() + " for "+ proposal.getConversationId());
                            }             
                        }

                        @Override
                        protected void handleInform(ACLMessage inform) {
                            System.out.println("8. "+inform.getSender().getName()+": successfully performed "+inform.getConversationId());
                        }
                    });
                }
            //}       
                } catch (Exception e) {
                    this.errorLog("Incorrect content: " + e.toString());
                }
    
        // comento esta linea, porque en este punto el coordinator ya debe tener seteada esa variable.
//         this.setGame(InitialGameSettings.load("game.settings"));
        
//        log("Initial configuration settings loaded");
        //FIN DARIO
    }

    
    
    
    //INICIO DARIO
    public class InitialInformToHarvesterCoordinatorAgentBehaviour extends OneShotBehaviour {
        
        public InitialInformToHarvesterCoordinatorAgentBehaviour(CoordinatorAgent agent) {
            super(agent);
        }
        
        @Override
        public void action() {
                       
            CoordinatorAgent agent = (CoordinatorAgent)this.getAgent();
            //Delay the search in DF
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // Search for services of name "HARVESTERCOORDINATOR"
            String serviceType = AgentType.HARVESTER_COORDINATOR.toString();
            
            System.out.println("Agent "+getLocalName()+" searching for services of type "+serviceType);
            try {
                // Build the description used as template for the search
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription templateSd = new ServiceDescription();
                templateSd.setType(serviceType);
                template.addServices(templateSd);
  		
                SearchConstraints sc = new SearchConstraints();
                // We want to receive 10 results at most
                sc.setMaxResults(new Long(10));
  	
                DFAgentDescription[] results = DFService.search(CoordinatorAgent.this, template, sc);
                
                if (results.length > 0) {
                    System.out.println("Agent "+getAID().getLocalName()+" found the service type " + serviceType );
                    for (int i = 0; i < results.length; ++i) {
  			DFAgentDescription dfd = results[i];
  			AID provider = dfd.getName();
  	
                        MessageWrapper message = new MessageWrapper();
                        message.setType(MessageContent.SEND_GAME);
                        message.setObject(agent.getGame());
                        ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
                        msg.setContentObject(message);
        
                        msg.clearAllReceiver();
                        msg.addReceiver(provider);

                        send(msg);
                    }
                }	
                else {
                    System.out.println("Agent "+getLocalName()+" did not find any "+serviceType+ " service");
                }
            
            }
            catch (FIPAException fe) {
  		fe.printStackTrace();
            } catch (IOException ex) {
                Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    //FIN DARIO
    
    public AID getScoutCoordinatorAgent() {
        return scoutCoordinatorAgent;
    }

    public void setScoutCoordinatorAgent(AID scoutCoordinatorAgent) {
        this.scoutCoordinatorAgent = scoutCoordinatorAgent;
    }

    public AID getSystemAgent() {
        return systemAgent;
    }

    public void setSystemAgent(AID systemAgent) {
        this.systemAgent = systemAgent;
    }

    public AID getHarvesterCoordinatorAgent() {
        return hcAgent;
    }

    public void setHarvesterCoordinatorAgent(AID harvesterCoordinatorAgent) {
        this.hcAgent = harvesterCoordinatorAgent;
    }

    public ArrayList<BuildingCell> getGarbageFound() {
        return garbageFound;
    }

    public void setGarbageFound(ArrayList<BuildingCell> garbageFound) {
        this.garbageFound = garbageFound;
    }

    public ArrayList<BuildingCell> getGarbageCollected() {
        return garbageCollected;
    }

    public void setGarbageCollected(ArrayList<BuildingCell> garbageCollected) {
        this.garbageCollected = garbageCollected;
    }

    public ArrayList<BuildingCell> getGarbageCollecting() {
        return garbageCollecting;
    }

    public void setGarbageCollecting(ArrayList<BuildingCell> garbageCollecting) {
        this.garbageCollecting = garbageCollecting;
    }

    public void addGarbageFound(ArrayList<BuildingCell> garbageFound) {
        this.garbageFound.addAll(garbageFound);
    }
    
    public void addGarbageCollecting(BuildingCell garbageCollecting) {
        this.garbageCollecting.add(garbageCollecting);
    }
    
    public void addGarbageCollected(BuildingCell garbageCollected) {
        this.garbageCollecting.add(garbageCollected);
    }
    
    public ACLMessage createGameRequest() {

        ACLMessage gameRequest = new ACLMessage(ACLMessage.REQUEST);
        gameRequest.clearAllReceiver();
        gameRequest.addReceiver(this.systemAgent);
        gameRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
        log("Request message to system");
        try {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setType(MessageContent.GET_MAP);
            gameRequest.setContentObject(wrapper);

            log("Request message content:" + wrapper.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return gameRequest;
    }
    
    public ACLMessage createNewHarvesterStepRequest() {
    
        ACLMessage stepsRequest = new ACLMessage(ACLMessage.REQUEST);
        stepsRequest.clearAllReceiver();
        stepsRequest.addReceiver(this.getHarvesterCoordinatorAgent());
        stepsRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);        
     
        try {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setType(MessageContent.GET_HARVESTER_STEPS);
            wrapper.setObject(this.getGame());
            stepsRequest.setContentObject(wrapper);

            log("Request message to harvester Coordinator content:" + wrapper.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        return stepsRequest;
    }
    
    public ACLMessage createNewScoutStepRequest() {
    
        ACLMessage stepsRequest = new ACLMessage(ACLMessage.REQUEST);
        stepsRequest.clearAllReceiver();
        stepsRequest.addReceiver(this.getScoutCoordinatorAgent());
        stepsRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);        
     
        try {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setType(MessageContent.GET_SCOUT_STEPS);
            wrapper.setObject(this.getGame());
            stepsRequest.setContentObject(wrapper);

            log("Request message to Scout Coordinator content:" + wrapper.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        return stepsRequest;
    }

    public ACLMessage createStepFinished() {
        ACLMessage stepsRequest = new ACLMessage(ACLMessage.REQUEST);
        stepsRequest.clearAllReceiver();
        stepsRequest.addReceiver(this.getSystemAgent());
        stepsRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);        
     
        try {
            MessageWrapper wrapper = new MessageWrapper();
            wrapper.setType(MessageContent.STEP_FINISHED);
            stepsRequest.setContentObject(wrapper);

            log("Notify to system step finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        return stepsRequest;

    }

    public void setScoutsFinished(boolean scoutsFinished) {
        this.scoutsFinished = scoutsFinished;
    }

    public boolean getScoutsFinished() {
        return scoutsFinished;
    }

    public boolean getHarvestersFinished() {
        return harvestersFinished;
    }

    public void setHarvestersFinished(boolean harvestersFinished) {
        this.harvestersFinished = harvestersFinished;
    }
    
}

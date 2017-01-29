/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.scout.ResponseGarbageBehaviour;
//import cat.urv.imas.behaviour.scout.RequesterBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dario, Angel, Pablo, Emanuel y Daniel
 */
public class ScoutAgent extends ImasAgent{
    
    private StreetCell position;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID scoutCoordinatorAgent;
    
     /**
     * Builds the coordinator agent.
     */
    public ScoutAgent() {
        super(AgentType.SCOUT);
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        
         // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SCOUT.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
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
            position = (StreetCell) args[0];
            position.getAgent().setAID(getAID());
            log("At (" + position.getRow() + " , " + position.getCol() + ")");
        } else {
            // Make the agent terminate immediately
            doDelete();
        
        }
        
        // search CoordinatorAgent
//        ServiceDescription searchCriterion = new ServiceDescription();
//        searchCriterion.setType(AgentType.SCOUT_COORDINATOR.toString());
//        this.scoutCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
//        // searchAgent is a blocking method, so we will obtain always a correct AID

        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new ResponseGarbageBehaviour(this, mt));
        
//        this.addBehaviour(new RequesterBehaviour(this));
        
//        try{
//            ACLMessage msg = receive();
//            ArrayList<Cell> adjacentCells = (ArrayList<Cell>) msg.getContentObject();
//            System.out.println( " - " + getLocalName() + " <- " + adjacentCells.size() );
//        } catch (UnreadableException ex) {
//            Logger.getLogger(ScoutCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NullPointerException nex){
//            Logger.getLogger(ScoutCoordinatorAgent.class.getName()).log(Level.SEVERE, null, nex);
//        }
        
//        if (msg!=null){
//            try {
//                
//            
//                
//            }
//        }
        
//        this.addBehaviour(new CyclicBehaviour(this) {
//            @Override
//            public void action() {
//                ACLMessage msg = receive();
//                if (msg!=null){
//                    try {
//                        ArrayList<Cell> adjacentCells = (ArrayList<Cell>) msg.getContentObject();
//                        System.out.println( " - " + myAgent.getLocalName() + " <- " + adjacentCells.size() );
//                    } catch (UnreadableException ex) {
//                        Logger.getLogger(ScoutCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                block();
//            }
//        });
    }

}

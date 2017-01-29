/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dario, Angel, Pablo, Emanuel y Daniel
 */
public class ScoutAgent extends ImasAgent{
     /**
     * Builds the coordinator agent.
     */
    public ScoutAgent() {
        super(AgentType.SCOUT);
    }
    private StreetCell position;
    
    @Override
    protected void setup() {
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
        
        this.addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg= receive();
                if (msg!=null){
                    try {
                        ArrayList<Cell> adjacentCells = (ArrayList<Cell>) msg.getContentObject();
                        System.out.println( " - " + myAgent.getLocalName() + " <- " + adjacentCells.size() );
                    } catch (UnreadableException ex) {
                        Logger.getLogger(ScoutCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                block();
            }
        });
    }

}

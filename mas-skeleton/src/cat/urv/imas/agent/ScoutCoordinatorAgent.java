/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.onthology.GameSettings;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dario, Angel, Pablo, Emanuel y Daniel
 */
public class ScoutCoordinatorAgent extends ImasAgent{
    
    /**
     * Game settings in use.
     */
    private GameSettings game;
    
     /**
     * Builds the scout coordinator agent.
     */
    public ScoutCoordinatorAgent() {
        super(AgentType.SCOUT_COORDINATOR);
    }

    @Override
    protected void setup() {
        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SCOUT_COORDINATOR.toString());
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
        addBehaviour(new CyclicBehaviour(this) 
            {
                public void action() {
                    ACLMessage msg= receive();
                    if (msg!=null){
                        GameSettings game;
                        try {
                            game = (GameSettings) msg.getContentObject();
//                            myAgent.setGame(game);
//                            myAgent.log(game.getShortString());
                            System.out.println( " - " + myAgent.getLocalName() + " <- " + game.getShortString() );
                        } catch (UnreadableException ex) {
                            Logger.getLogger(ScoutCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    block();
                }
            });
    }
    
    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

}

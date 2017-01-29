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
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * System agent id.
     */
    private List<AID> scoutsAgent = new ArrayList<AID>();
    
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
        
        this.addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg= receive();
                if (msg!=null){
                    GameSettings game;
                    try {
                        ScoutCoordinatorAgent currentAgent = (ScoutCoordinatorAgent) myAgent;
                        game = (GameSettings) msg.getContentObject();
                        currentAgent.setGame(game);
                        for (Map.Entry<AgentType, List<Cell>> en : game.getAgentList().entrySet()) {
                            AgentType agent = en.getKey();
                            List<Cell> agentsCells = en.getValue();
                            if(agent.equals(AgentType.SCOUT)){
                                for (int i = 0; i < agentsCells.size(); i++) {
                                    StreetCell cell = (StreetCell) agentsCells.get(i);
                                    ACLMessage cellsInform = new ACLMessage(ACLMessage.INFORM);
                                    cellsInform.addReceiver(cell.getAgent().getAID());
                                    try {
                                        cellsInform.setContentObject(getAdjacentCells(game, cell));
                                        currentAgent.send(cellsInform);
                                        currentAgent.log("Adjacent cells sent to Scout "+ cell.getAgent().getAID().getLocalName()+".");
                                    } catch (Exception e) {
                                        cellsInform.setPerformative(ACLMessage.FAILURE);
                                        currentAgent.errorLog(e.toString());
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (UnreadableException ex) {
                        Logger.getLogger(ScoutCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                block();
            }

            private ArrayList<Cell> getAdjacentCells(GameSettings game, StreetCell cell) {
                ArrayList<Cell> adjacentCells = new ArrayList<>();
                
                for (int i = cell.getCol()-1; i <= cell.getCol()+1; i++)
                    for (int j = cell.getRow()-1; j <= cell.getRow()+1; j++)
                        if((i >= 0 && i < game.getMap()[0].length) && (j >= 0 && j < game.getMap().length)&& (j != cell.getRow() || i != cell.getCol()))
                            adjacentCells.add(game.get(j, i));
                return adjacentCells;
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

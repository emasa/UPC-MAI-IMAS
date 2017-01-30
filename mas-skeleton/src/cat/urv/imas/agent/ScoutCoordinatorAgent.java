/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.scoutcoordinator.StepsResponseBehaviour;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.ArrayList;

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
     * System coordinator agent id.
     */
    private AID CoordinatorAgent;
    
    private HashMap<AID,ArrayList<Cell>> scoutAdjacentCells = new HashMap<>();
    
    private ArrayList<StreetCell> checkedCells = new ArrayList<>();
    
    private ArrayList<BuildingCell> garbageBuildings = new ArrayList<>();
    
     /**
     * Builds the scout coordinator agent.
     */
    public ScoutCoordinatorAgent() {
        super(AgentType.SCOUT_COORDINATOR);
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        
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
        
        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.CoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new StepsResponseBehaviour(this, mt));
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

    public AID getCoordinatorAgent() {
        return CoordinatorAgent;
    }

    public void setCoordinatorAgent(AID CoordinatorAgent) {
        this.CoordinatorAgent = CoordinatorAgent;
    }

    public StreetCell getNewPosition(AID sender) {
        // TODO: Add capability left or right whether odd or pair sender.
        ArrayList<Cell> adjacentCells = this.scoutAdjacentCells.get(sender);
        StreetCell possibleCell = null;
        for (int i = 0; i < adjacentCells.size(); i++) {
            Cell cell = adjacentCells.get(i);
            if(cell instanceof StreetCell){
                possibleCell = (StreetCell)cell;
                if(!possibleCell.isThereAnAgent()){
                    boolean discard = false;
                    for (int j = 0; j < this.checkedCells.size() && !discard; j++) {
                        StreetCell checked = this.checkedCells.get(j);
                        if(checked.equals(possibleCell))
                            discard = true;
                    }
                    if(!discard)
                        return possibleCell;
                }else{
                   possibleCell = null; 
                }
            }
        }
        return possibleCell;
    }

    public HashMap<AID, ArrayList<Cell>> getScoutAdjacentCells() {
        return scoutAdjacentCells;
    }

    public void setScoutAdjacentCells(HashMap<AID, ArrayList<Cell>> scoutAdjacentCells) {
        this.scoutAdjacentCells = scoutAdjacentCells;
    }
    
    public void addScoutAdjacentCells(AID Scout,ArrayList<Cell> adjacentCells) {
        this.scoutAdjacentCells.put(Scout, adjacentCells);
    }

    public ArrayList<BuildingCell> getGarbageBuildings() {
        return garbageBuildings;
    }

    public void setGarbageBuildings(ArrayList<BuildingCell> garbageBuildings) {
        this.garbageBuildings = garbageBuildings;
    }
    
    public void addGarbageBuildings(ArrayList<BuildingCell> garbageBuildings) {
        this.garbageBuildings.addAll(garbageBuildings);
    }

}

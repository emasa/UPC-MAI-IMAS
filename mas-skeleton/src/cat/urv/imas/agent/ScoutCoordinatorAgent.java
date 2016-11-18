/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

/**
 *
 * @author Dario
 */
public class ScoutCoordinatorAgent extends ImasAgent{
     /**
     * Builds the coordinator agent.
     */
    public ScoutCoordinatorAgent() {
        super(AgentType.SCOUT_COORDINATOR);
    }

    @Override
    protected void setup() {
        log("Creatad new Scout Coordinator: " + getLocalName());
        
    }

}

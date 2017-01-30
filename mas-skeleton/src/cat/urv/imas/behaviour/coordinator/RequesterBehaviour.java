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
package cat.urv.imas.behaviour.coordinator;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.domain.FIPANames;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Behaviour for the Coordinator agent to deal with AGREE messages.
 * The Coordinator Agent sends a REQUEST for the
 * information of the game settings. The System Agent sends an AGREE and 
 * then it informs of this information which is stored by the Coordinator Agent. 
 * 
 * NOTE: The game is processed by another behaviour that we add after the 
 * INFORM has been processed.
 */
public class RequesterBehaviour extends AchieveREInitiator {

    public RequesterBehaviour(CoordinatorAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
        agent.log("Started behaviour to deal with AGREEs");
    }

    /**
     * Handle AGREE messages
     *
     * @param msg Message to handle
     */
    @Override
    protected void handleAgree(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("AGREE received from " + ((AID) msg.getSender()).getLocalName());
    }

    /**
     * Handle INFORM messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        AID senderID = (AID) msg.getSender();
        agent.log("INFORM received from " + senderID.getLocalName());
        try {
            if(senderID.equals(agent.getSystemAgent())){
                GameSettings game = (GameSettings) msg.getContentObject();
                agent.setGame(game);
                agent.log(game.getShortString());
                /* ********************************************************************/
                ACLMessage stepsRequest = new ACLMessage(ACLMessage.REQUEST);
                stepsRequest.clearAllReceiver();
                stepsRequest.addReceiver(agent.getScoutCoordinatorAgent());
                stepsRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                agent.log("Request message to Scout Coordinator.");
                HashMap<String,GameSettings> content = new HashMap<String, GameSettings>();
                content.put(MessageContent.GET_SCOUT_STEPS, agent.getGame());
                stepsRequest.setContentObject(content);
                agent.addBehaviour(new RequesterBehaviour(agent, stepsRequest));
            }else if(senderID.equals(agent.getScoutCoordinatorAgent())){
                ArrayList<BuildingCell> garbageBuildings = (ArrayList<BuildingCell>) msg.getContentObject();
                agent.addGarbageFound(garbageBuildings);
                agent.log("total garbage to comunicate to Harvesters: "+garbageBuildings);
            }
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }

    /**
     * Handle NOT-UNDERSTOOD messages
     *
     * @param msg Message
     */
    @Override
    protected void handleNotUnderstood(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("This message NOT UNDERSTOOD.");
    }

    /**
     * Handle FAILURE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleFailure(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("The action has failed.");

    } //End of handleFailure

    /**
     * Handle REFUSE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleRefuse(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        agent.log("Action refused.");
    }

}

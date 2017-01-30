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
package cat.urv.imas.behaviour.scoutcoordinator;

import cat.urv.imas.agent.ScoutCoordinatorAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour for the Scout Coordinator agent to deal with AGREE messages.
 * The Scout Coordinator Agent sends a REQUEST with the adjacent cells for the
 * cells with garbage. The Scout Agent sends an AGREE and 
 * then it informs of this information which is resend to the Coordinator Agent. 
 * 
 * NOTE: The List of cell with garbage is processed by another behaviour that 
 * we add after the INFORM has been processed.
 */
public class RequestGarbageBehaviour extends AchieveREInitiator {

    public RequestGarbageBehaviour(ScoutCoordinatorAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
        AID elem = (AID)requestMsg.getAllReceiver().next();
        agent.log("Adjacent cells sent to Scout "+ elem.getLocalName()+".");
    }

    /**
     * Handle AGREE messages
     *
     * @param msg Message to handle
     */
    @Override
    protected void handleAgree(ACLMessage msg) {
        ScoutCoordinatorAgent agent = (ScoutCoordinatorAgent) this.getAgent();
        agent.log("AGREE received from " + ((AID) msg.getSender()).getLocalName());
    }

    /**
     * Handle INFORM messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
        ScoutCoordinatorAgent agent = (ScoutCoordinatorAgent) this.getAgent();
        try {
            agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
            // recieve garbage cells
//            GameSettings game = (GameSettings) msg.getContentObject();
//            agent.setGame(game);
//            agent.log(game.getShortString());
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
        ScoutCoordinatorAgent agent = (ScoutCoordinatorAgent) this.getAgent();
        agent.log("This message NOT UNDERSTOOD.");
    }

    /**
     * Handle FAILURE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleFailure(ACLMessage msg) {
        ScoutCoordinatorAgent agent = (ScoutCoordinatorAgent) this.getAgent();
        agent.log("The action has failed.");

    } //End of handleFailure

    /**
     * Handle REFUSE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleRefuse(ACLMessage msg) {
        ScoutCoordinatorAgent agent = (ScoutCoordinatorAgent) this.getAgent();
        agent.log("Action refused.");
    }

}

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
package cat.urv.imas.behaviour.scout;

import cat.urv.imas.agent.ScoutAgent;
import cat.urv.imas.behaviour.scoutcoordinator.RequestGarbageBehaviour;
import cat.urv.imas.map.Cell;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A request-responder behavior for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class ResponseGarbageBehaviour extends AchieveREResponder {

    /**
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behavior
     * @param mt Template to receive future responses in this conversation
     */
    public ResponseGarbageBehaviour(ScoutAgent agent, MessageTemplate mt) {
        super(agent, mt);
//        BehaviourFreezer freezer = new BehaviourFreezer(agent);
//        freezer.freeze(waitingBy, this);
        agent.log("Waiting REQUESTs from authorized agents");
    }

    /**
     * When System Agent receives a REQUEST message, it agrees. Only if
     * message type is AGREE, method prepareResultNotification() will be invoked.
     * 
     * @param msg message received.
     * @return AGREE message when all was OK, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResponse(ACLMessage msg) {
        ScoutAgent agent = (ScoutAgent) this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            HashMap<String,List<Cell>> content = (HashMap<String,List<Cell>>)msg.getContentObject();
            if (content.keySet().iterator().next().equals(MessageContent.GET_GARBAGE)) {
                ArrayList<Cell> adjacentCells = (ArrayList<Cell>) content.get(MessageContent.GET_GARBAGE);
                agent.setAdjacentCells(adjacentCells);
                agent.log( " - " + myAgent.getLocalName() + " <- " + adjacentCells.size() );
                // TODO: perform seach of garbage in BuildingCell
                // TODO: request movement
                ACLMessage newCell = new ACLMessage(ACLMessage.REQUEST);
                newCell.clearAllReceiver();
                newCell.addReceiver(msg.getSender());
                newCell.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                agent.log("Request new Cell to agent: " + ((AID)newCell.getAllReceiver().next()).getLocalName());
                newCell.setContent(MessageContent.NEW_CELL);
                agent.addBehaviour(new RequestMovementBehaviour(agent, newCell));
                reply.setPerformative(ACLMessage.AGREE);
            }
        } catch (UnreadableException e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.getMessage());
        }
        agent.log("Response being prepared");
        return reply;
    }

    /**
     * After sending an AGREE message on prepareResponse(), this behaviour
     * sends an INFORM message with the whole game settings.
     * 
     * NOTE: This method is called after the response has been sent and only when one
     * of the following two cases arise: the response was an agree message OR no
     * response message was sent. 
     *
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification, of type INFORM
     * when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {

        // it is important to make the createReply in order to keep the same context of
        // the conversation
        ScoutAgent agent = (ScoutAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        try {
            reply.setContentObject(agent.getGarbageCells());
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.toString());
            e.printStackTrace();
        }
        agent.log("Cells with Garbage sent from: " + agent.getLocalName());
        return reply;

    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

}

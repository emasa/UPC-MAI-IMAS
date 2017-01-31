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

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.ScoutCoordinatorAgent;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A request-responder behavior for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class StepsResponseBehaviour extends AchieveREResponder {

    /**
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behavior
     * @param mt Template to receive future responses in this conversation
     */
    public StepsResponseBehaviour(ScoutCoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
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
        ScoutCoordinatorAgent agent = (ScoutCoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            if(msg.getSender().equals(agent.getCoordinatorAgent())){
                HashMap<String,GameSettings> content = (HashMap<String,GameSettings>) msg.getContentObject();
                if (content.keySet().iterator().next().equals(MessageContent.GET_SCOUT_STEPS)) {
                    agent.log("Request Steps received");
                    GameSettings game = (GameSettings) content.get(MessageContent.GET_SCOUT_STEPS);
                    agent.setGame(game);
                    ParallelBehaviour scoutsSearch = new ParallelBehaviour(agent, ParallelBehaviour.WHEN_ALL){ 
                        @Override
                        public int onEnd() { 
                            System.out.println("Behavior finalized with success!"); 
                            return 0; 
                        } 
                    };
                    
                    // reset list of garbages
                    agent.setGarbageBuildings(new ArrayList<BuildingCell>());
                    
                    List<Cell> scoutsCells = game.getAgentList().get(AgentType.SCOUT);
                    for (int i = 0; i < scoutsCells.size(); i++) {
                        StreetCell scoutCell = (StreetCell) scoutsCells.get(i);
                        AID scout = scoutCell.getAgent().getAID();
                        ArrayList<Cell> adjacentCells = game.getAdjacentCells(scoutCell);
                        ACLMessage cellsInform = new ACLMessage(ACLMessage.REQUEST);
                        cellsInform.clearAllReceiver();
                        cellsInform.addReceiver(scout);
                        cellsInform.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                        agent.log("Request message to agent: " + scout.getLocalName());
                        agent.addScoutAdjacentCells(scout, adjacentCells);
                        HashMap<String,List<Cell>> contentRequest = new HashMap<>();
                        contentRequest.put(MessageContent.GET_GARBAGE, adjacentCells);
                        cellsInform.setContentObject(contentRequest);
                        scoutsSearch.addSubBehaviour(new RequestGarbageBehaviour(agent, cellsInform));
                    }
                    agent.addBehaviour(scoutsSearch);
                    
                    reply.setPerformative(ACLMessage.AGREE);
                }
            }else{
                agent.log(msg.getSender().getLocalName()+"Request new position");
                reply.setPerformative(ACLMessage.AGREE);
            }
        } catch (UnreadableException | IOException e) {
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
        ScoutCoordinatorAgent agent = (ScoutCoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);

        try {
            if(msg.getSender().equals(agent.getCoordinatorAgent())) {
                reply.setContentObject(agent.getGarbageBuildings());
                agent.log("reply with found garbage, and the scouts end their turns.");
            }else{
                StreetCell newPosition = agent.getNewPosition(msg.getSender());
//                oldPosition.removeAgent(Agent);
//                newPosition.addAgent(Agent);
                reply.setContentObject(newPosition);
            }
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.toString());
            e.printStackTrace();
        }
        agent.log("Response sent to:"+msg.getSender().getLocalName());
        return reply;
    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

}

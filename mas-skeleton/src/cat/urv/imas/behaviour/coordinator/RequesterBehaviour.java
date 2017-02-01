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

import cat.urv.imas.agent.AgentType;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.GarbageType;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.proto.ContractNetInitiator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

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
//                agent.log("venimos del scout, supuestamente acabo el paso.");

                //ADD MANUALLY GARBAGE TO TEST COALITION
                ArrayList<SettableBuildingCell> SettableBuildingCellList = new ArrayList<>();
                      
                SettableBuildingCell g1 = new SettableBuildingCell(3,3);
                g1.setGarbage(GarbageType.PAPER, 10);
                SettableBuildingCell g2 = new SettableBuildingCell(16,3);
                g2.setGarbage(GarbageType.GLASS, 20);
                SettableBuildingCell g3 = new SettableBuildingCell(3,7);
                g3.setGarbage(GarbageType.PLASTIC, 20);
                SettableBuildingCellList.add(g1);
                SettableBuildingCellList.add(g2);
                SettableBuildingCellList.add(g3);
                
//                ArrayList<BuildingCell> garbageBuildings = (ArrayList<BuildingCell>) msg.getContentObject();
//                agent.addGarbageFound(garbageBuildings);
//                agent.log("total garbage to comunicate to Harvesters: "+garbageBuildings);
             
                /* ********************************************************************/
                // contract net system
                ServiceDescription searchHC = new ServiceDescription();     
                searchHC.setType(AgentType.HARVESTER_COORDINATOR.toString());
                agent.setHarvesterCoordinatorAgent(UtilsAgents.searchAgent(agent, searchHC));    
                
                // Cycle through garbageBuildings to get cell and start a ContractNet
                for (int i = 0; i < SettableBuildingCellList.size(); i++) {
                    BuildingCell celda = SettableBuildingCellList.get(i);
                    // Fill the CFP message
                    ACLMessage contract = new ACLMessage(ACLMessage.CFP);
                    contract.addReceiver(agent.getHarvesterCoordinatorAgent());             // Receiver is HarvesterCoordinator
                    contract.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);  // ContractNet protocol
                    contract.setReplyByDate(new Date(System.currentTimeMillis() + 10000));  // We want to receive a reply in 10 secs
                    contract.setConversationId("Contract: "+i);                             // ConversationID                    
                    System.out.println("1. "+agent.getLocalName()+": sent contract "+contract.getConversationId());
                    
                    try {
                        MessageWrapper mmm = new MessageWrapper();
                        mmm.setType(MessageContent.SETTABLE_BUILDING);
                        mmm.setObject(celda);
                        
                        contract.setContentObject(mmm);
                    } catch (IOException ex) {
                        Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    agent.log("ContractNet Started");                    
                    
                    agent.addBehaviour(new ContractNetInitiator(agent, contract) {
                        @Override
                        protected void handlePropose(ACLMessage propose, Vector v) {
                            // Receive Proposal
                            System.out.println("3. "+propose.getSender().getName()+": proposed a coalition on "+propose.getConversationId());
                        }

                        @Override
                        protected void handleRefuse(ACLMessage refuse) {
                            System.out.println("3. "+refuse.getSender().getName()+": refused "+refuse.getConversationId());
                        }

                        @Override
                        protected void handleFailure(ACLMessage failure) {
                            if (failure.getSender().equals(myAgent.getAMS())) {
                                // FAILURE notification from the JADE runtime: the receiver
                                // does not exist
                                System.out.println("Responder does not exist");
                            }
                            else {
                                System.out.println("Agent "+failure.getSender().getName()+" failed "+failure.getConversationId());
                            }
                        }

                        @Override
                        protected void handleAllResponses(Vector responses, Vector acceptances) {
                            // Accept Proposal. CA always accepts proposal
                            Enumeration e = responses.elements();
                            while (e.hasMoreElements()) {
                                ACLMessage proposal = (ACLMessage) e.nextElement();                    
                                ACLMessage accept = proposal.createReply();
                                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                acceptances.addElement(accept);
                                accept.setContent(proposal.getContent()); 
                                System.out.println("4. "+myAgent.getLocalName()+": accepted proposal "+proposal.getContent() + " for "+ proposal.getConversationId());
                            }             
                        }

                        @Override
                        protected void handleInform(ACLMessage inform) {
                            System.out.println("8. "+inform.getSender().getName()+": successfully performed "+inform.getConversationId());
                        }
                    });
                }
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

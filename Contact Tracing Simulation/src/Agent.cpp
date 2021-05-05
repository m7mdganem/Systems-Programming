#include "Session.h"
#include "Agent.h"
#include "Tree.h"

Agent::Agent() {} //no fields to initialize

Agent::~Agent(){} //no resources to destruct

//****************** ContactTracer *********************
ContactTracer::ContactTracer() : Agent() {}

ContactTracer::ContactTracer(const ContactTracer &CT){}

void ContactTracer::act(Session &session)
{
    if(session.positiveNumOfInfected()) { //if there is still infected nodes that ContactTracers didnt act on them
        int infected = session.dequeueInfected(); //get the infected node
        Tree *BFSTree = session.getGraph().BFS(session, infected); //create its BFS tree
        int disconnectNode = BFSTree->traceTree(); //find the node to disconnect
        const Graph &g = session.getGraph(); //get the graph from the session
        Graph graph(g); //copy it since its const
        graph.disconnectNode(disconnectNode); //disconnect the node 
        session.setGraph(graph); //reSet the new graph to session
        delete BFSTree; //delete the BFS tree from heap
    }
}

Agent* ContactTracer::clone() const
{
    return new ContactTracer(*this);
}

//************************* Virus ************************
Virus::Virus(int nodeInd) : Agent(), nodeInd(nodeInd){}

Virus::Virus(const Virus& virus) : nodeInd(virus.nodeInd){}

Agent* Virus::clone() const
{
    return new Virus(*this);
}

void Virus::act(Session &session)
{
    const Graph& g = session.getGraph(); //get the graph from session
    Graph graph(g); //copy it (you cant make changes to const)
    if(!graph.isInfected(nodeInd)) { //if the virus did not infect the node then infect it and add it to the infected queue
        graph.infectNode(nodeInd);
        session.enqueueInfected(nodeInd);
    }
    std::vector<int> neighbors = graph.getNeighbors(nodeInd); //get neighbors of the node
    for(int neighbor: neighbors) { 
        if ((!graph.isCarrier(neighbor)) & (!graph.isInfected(neighbor))) {
            //if the neighbor is virus-free then create a new virus among him
            Virus *newVirus = new Virus(neighbor);
            graph.makeNodeCarrier(neighbor); //make him in a carrier status
            session.addAgent(newVirus); //add the new virus to agenrts list
            session.addDangerousNode(neighbor); 
			//add the node as a node who is dangerous (dangerous node is a node that can infect other nodes in present or future)
            break;
        }
    }
    session.setGraph(graph); //set the graph with the changes to session
}


#include "Session.h"
#include <fstream>
#include <Agent.h>
#include "json.hpp"

using json = nlohmann::json;

Session::Session(const std::string &path)
            :g(),treeType(Null), agents({}), infectedQueue(), dangerousNodes(), currCycle(0)
{
    std::ifstream file(path);
    json j;
    file >> j;
    std::vector<std::vector<int>> matrix = j["graph"];
    g = Graph(matrix);
    std::string treetype = j["tree"];
    if(treetype == "M") treeType = MaxRank;
    else if (treetype == "C") treeType = Cycle;
    else treeType = Root;
    for(auto a : j["agents"]){
        if(a[0] != "C"){
            Virus* A = new Virus(a[1]);
            addAgent(A);
            g.makeNodeCarrier(a[1]);
            dangerousNodes.push(a[1]);
        }else{
            ContactTracer* B = new ContactTracer();
            addAgent(B);
        }
    }
}

Session::Session(const Session& session)
        : g(session.g), treeType(session.treeType), agents({}), infectedQueue(session.infectedQueue),
		  dangerousNodes(session.dangerousNodes), currCycle(session.currCycle)
{
    for(Agent* agent : session.agents){
        Agent* newAgent = agent->clone();
        agents.push_back(newAgent);
    }
}

Session::Session(Session &&session)
    : g(session.g), treeType(session.treeType),agents(std::move(session.agents)),
	  infectedQueue(session.infectedQueue), dangerousNodes(session.dangerousNodes), currCycle(session.currCycle)

{
    session.agents.clear();
}

Session& Session::operator=(const Session& other)
{
    if(this == &other)
        return *this;
    clear();
    copy(other);
    for(Agent* agent : agents){
        Agent* newAgent = agent->clone();
        addAgent(newAgent);
    }
    return *this;
}

Session& Session::operator=(Session &&other)
{

    if(this != &other) {
        clear();
        copy(other);
        agents = std::move(other.agents);
        other.agents.clear();
    }
    return *this;
}

void Session::copy(const Session& other)
{
    g = other.g;
    treeType = other.treeType;
    infectedQueue = other.infectedQueue;
    dangerousNodes = other.dangerousNodes;
    currCycle = other.currCycle;
}

void Session::clear()
{
    for(Agent* agent : agents){
        if(agent)
             delete agent;
    }
    agents.clear();
}

Session::~Session()
{
    clear();
}

void Session::simulate()
{
    std::queue<int> helpingQueue;
    while(!dangerousNodes.empty()) { //while there are still nodes that can spread virus
        int length = agents.size(); //let all agents act on the graph
        for (int i = 0; i < length; i++) {
            Agent *agent = agents[i];
            agent->act(*this);
        }
        while (!dangerousNodes.empty()) { //go over all dangerousNodes
            int dangerousNode = dangerousNodes.front();
            dangerousNodes.pop();
            std::vector<int> neighbors = g.getNeighbors(dangerousNode);
            for (int neighbor : neighbors) {
                if (!g.isInfected(neighbor)) {
					//push the node to the helping queue in order to be able to get dangerous nodes in the end of the loop
                    helpingQueue.push(dangerousNode);
                    /*if that node have a virus-free neighbor then break (there is a connected 
					component that does not match terminating condition)*/
					break; 
                }
            }
            /*after this for loop, we have all dangerous nodes (for the next iteration)
			 *in the helping queue (nodes that was dangerous in the last iteration and does not
			 *have any more virus-free neighbors will not enter the if condition and thus they 
			 *will not be in the helping queue)*/
        }
		//return all the dangerous nodes to the dangerousNodes list to be able to handle the next iteration
        while (!helpingQueue.empty()) {
            dangerousNodes.push(helpingQueue.front());
            helpingQueue.pop();
        }
        currCycle++; //increase cycle
    }
    //when the termination condition holds the while loop will terminate and then we want to copy our results the output json file :-
    std::vector<int> infectedNodes = g.getInfectedNodes();
    json j;
    std::ofstream i("output.json");
    j["graph"] = g.getEdges();
    j["infected"] = infectedNodes;
    i<<j;
}

void Session::addDangerousNode(int dangerousNode)
{
    /*DangerousNodes list contains the nodes that may spread virus
	in the graph, in the present or future (Carrier and infected 
	nodes who still have virus-free neighbors)*/
    dangerousNodes.push(dangerousNode);
}

void Session::addAgent(const Agent &agent)
{
    Agent* newAgent = agent.clone();
    addAgent(newAgent);
}

void Session::addAgent(Agent* agent)
{
    agents.push_back(agent);
}

void Session::enqueueInfected(int nodeInd)
{
    infectedQueue.push(nodeInd);
}

bool Session::positiveNumOfInfected() const
{
    return !infectedQueue.empty();
}

int Session::dequeueInfected()
{
        int nextInfectedNode = infectedQueue.front();
        infectedQueue.pop();
        return nextInfectedNode;

}

TreeType Session::getTreeType() const
{
    return treeType;
}

const Graph& Session::getGraph() const
{
    return g;
}

int Session::getCycle() const
{
    return currCycle;
}


void Session::setGraph(const Graph &graph)
{
    g = graph;
}

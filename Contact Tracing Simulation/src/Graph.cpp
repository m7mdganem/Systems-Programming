#include "Graph.h"
#include <queue>
#include "Tree.h"

//indicate infection using indicators : * -1 = "healthy"
//                                      * 0 = "carrier"
//                                      * 1 = "infected"


//default constructor
Graph::Graph() : edges(), infectedNodes(), infectionIndicator() {}

//Constructor
Graph::Graph(std::vector<std::vector<int>> matrix)
             : edges(matrix), infectedNodes(), infectionIndicator(matrix.size(),-1){}
             
//copy constructor
Graph::Graph(const Graph& other)
        : edges(other.edges), infectedNodes(other.infectedNodes), infectionIndicator(other.infectionIndicator){}
        //the deafult copy constructor holds


//******************** Getters *****************************
const std::vector<std::vector<int>>& Graph::getEdges() const { return edges; }

const std::vector<int>& Graph::getInfectedNodes() const { return infectedNodes; }

bool Graph::isCarrier(int nodeInd) const { return infectionIndicator[nodeInd] == 0; }

bool Graph::isInfected(int nodeInd) const { return infectionIndicator[nodeInd]==1; }

std::vector<int> Graph::getNeighbors(int nodeInd) const
{
    std::vector<int> output;
    int numOfNodes = edges[nodeInd].size();
    for(int neighbor=0; neighbor < numOfNodes; neighbor++ ) {
        if (edges[nodeInd][neighbor] == 1)
            output.push_back(neighbor);
    }
    return output;
}

void Graph::infectNode(int nodeInd)
{
    // if not infected -> infect him and add him to the infected list
    if(!isInfected(nodeInd)) {
        infectedNodes.push_back(nodeInd);
        infectionIndicator[nodeInd] = 1;
    }
}

void Graph::makeNodeCarrier(int nodeInd)
{
    //if node not infected and not carrier make him in a carrier status
    if((!isInfected(nodeInd)) & (!isCarrier(nodeInd))){
        infectionIndicator[nodeInd] = 0;
    }
}

void Graph::disconnectNode(int node)
{
    edges[node] = std::vector<int>(edges.size(),0);
    for(std::vector<int>& incidentNodes : edges){
        incidentNodes[node] = 0 ;
    }
}

Tree* Graph::BFS(const Session& session, int rootLabel) const
{
    std::vector<bool> visited(edges.size(),false); //to prevent double visiting
    std::queue<Tree*> q;
    Tree* root = Tree::createTree(session,rootLabel);
    q.push(root);
    visited[rootLabel] = true;
    while(!q.empty()) {
        Tree* currRoot = q.front();
        q.pop();
        std::vector<int> neighbors = getNeighbors(currRoot->getNode());
        for(int neighbor : neighbors){
            if(!visited[neighbor]){
                visited[neighbor] = true;
                Tree* currTree = Tree::createTree(session,neighbor);
                q.push(currTree);
                currRoot->addChild(currTree);
            }
        }
    }
    return root;
}

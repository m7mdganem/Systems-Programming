#ifndef SESSION_H_
#define SESSION_H_

#include <vector>
#include <string>
#include "Graph.h"
#include <queue>

class Agent;

enum TreeType{
  Cycle,
  MaxRank,
  Root,
  Null
};

class Session{
public:
    Session(const std::string& path);
    Session(const Session& session);
    Session(Session&& session);
    Session& operator=(const Session& session);
    Session& operator=(Session&& other);
    virtual ~Session();

    TreeType getTreeType() const;
    bool positiveNumOfInfected() const;
    const Graph& getGraph () const;
    int getCycle() const;

    void simulate();
    void addAgent(const Agent& agent);
    void addAgent(Agent* agent);
    void setGraph(const Graph& graph);
    void addDangerousNode(int);
    void enqueueInfected(int);
    int dequeueInfected();

private:
    Graph g;
    TreeType treeType;
    std::vector<Agent*> agents;
    std::queue<int> infectedQueue;
    std::queue<int> dangerousNodes;
    int currCycle;

    void clear();
    void copy(const Session& other);
};

#endif

#ifndef GRAPH_H_
#define GRAPH_H_

#include <vector>

class Session;
class Tree;

class Graph{
public:
    Graph(std::vector<std::vector<int>> matrix);
    Graph();
    Graph(const Graph& other);

    void infectNode(int nodeInd);
    void makeNodeCarrier(int nodeInd);
    void disconnectNode(int);

    Tree* BFS(const Session&, int) const;
    bool isInfected(int nodeInd) const;
    bool isCarrier (int nodeInd) const;
    const std::vector<int>& getInfectedNodes() const;
    const std::vector<std::vector<int>>& getEdges() const;
    std::vector<int> getNeighbors(int nodeInd) const;

private:
    std::vector<std::vector<int>> edges;
    std::vector<int> infectedNodes;
    std::vector<int> infectionIndicator;
};

#endif

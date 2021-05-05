#include "Tree.h"
#include "Session.h"

//************************** Tree *****************************
//constructor
Tree::Tree(int rootLabel) : node(rootLabel), children({}) {}

Tree::Tree(const Tree &other) : node(other.node), children({})
{
    copy(other);
}

Tree::Tree(Tree&& other) : node(other.node), children(std::move(other.children))
{
}

Tree& Tree::operator=(const Tree& other) {
    if (this != &other) {
        clear();
        node = other.node;
        copy(other);
    }
    return *this;
}

Tree& Tree::operator=(Tree&& other) {
    if (this != &other) {
        clear();
        node = other.node;
        children = std::move(other.children);
    }
    return *this;
}

int Tree::getNode() const { return node; }

void Tree::addChild(const Tree &child)
{
    Tree* clone = child.clone();
    addChild(clone);
}

void Tree::addChild(Tree* child) { children.push_back(child); }


Tree* Tree::createTree(const Session& session, int rootLabel)
{
    if (session.getTreeType() == MaxRank) return new MaxRankTree(rootLabel);
    else if (session.getTreeType() == Cycle) return new CycleTree(rootLabel,session.getCycle());
    else return new RootTree(rootLabel); //Root
}

Tree::~Tree()
{
    clear();
}

void Tree::clear()
{
    for(Tree* tree : children) {
        if(tree) {
            delete tree;
        }
    }
    children.clear();
}

void Tree::copy(const Tree& other)
{
	for (Tree *child : other.children) {
            Tree *newChild = child->clone();
            addChild(newChild);
    }
}
//************************** CycleTree *****************************
CycleTree::CycleTree(int rootLabel, int currCycle) : Tree(rootLabel), currCycle(currCycle) {}


Tree* CycleTree::clone() const { return new CycleTree(*this); }

int CycleTree::traceTree() {
    CycleTree* curr = this;
    int counter = currCycle;
    std::vector<Tree*> childrenOfCurr = curr->children;
    while((counter > 0) & (childrenOfCurr.size()>0)){ //if we didnt reach the c'th node and didnt rach a leaf child
        curr = (CycleTree*) childrenOfCurr[0]; //take the left most child
        childrenOfCurr =curr->children;
        counter--;
    }
    return curr->node;
}

//************************** MaxRankTree *****************************
MaxRankTree::MaxRankTree(int rootLabel) : Tree(rootLabel) {}

Tree* MaxRankTree::clone() const { return new MaxRankTree(*this);}

int MaxRankTree::traceTree()
{
    int maxRank = node; //this int indicates the node that has the max num of children, strat it with the root
    std::queue<MaxRankTree*> q;
    q.push(this);
    int size = children.size(); //set the first max size as the number of the root children
    while (!q.empty()){
        MaxRankTree* currNode = q.front(); //pop a tree from the queue
        q.pop();
        int childrenSize = currNode->children.size(); //get the number of tree children
        if(childrenSize > size){ //if it is greater than the current max size then update it and update the nodeInd that has that max size
            size = currNode->children.size();
            maxRank = currNode->node;
            /*we wont get a higher depth node with the same num of children because we update only if
			number of children is greater (not greater, equals)*/
        }
        for (Tree* child : currNode->children) {
            q.push((MaxRankTree*)child);
            //push all tree's children to queue (like DFS traversal)
        }
    }
    return maxRank;
}

//************************** RootTree *****************************
RootTree::RootTree(int rootLabel) : Tree(rootLabel){}

Tree* RootTree::clone() const { return new RootTree(*this);}

int RootTree::traceTree() { return this->node;} // return the root node

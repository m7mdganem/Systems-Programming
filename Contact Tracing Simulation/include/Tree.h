#ifndef TREE_H_
#define TREE_H_

#include <vector>

class Session;

class Tree{
public:
    //constructor
    Tree(int rootLabel);
    Tree(const Tree& other);
    Tree(Tree&& other);
    Tree& operator=(const Tree& other);
    Tree& operator=(Tree&& other);
    virtual ~Tree();

    void addChild(const Tree& child);
    void addChild(Tree* child);
    int getNode() const;

    static Tree* createTree(const Session& session, int rootLabel);

    virtual int traceTree()=0;
    virtual Tree* clone() const=0;

protected:
    int node;
    std::vector<Tree*> children;
	
private: 
	void clear();
	void copy(const Tree& other);
};

class CycleTree: public Tree{
public:
    CycleTree(int rootLabel, int currCycle);

    virtual int traceTree();
    virtual Tree* clone() const;

private:
    int currCycle;
};

class MaxRankTree: public Tree{
public:
    MaxRankTree(int rootLabel);

    virtual int traceTree();
    virtual Tree* clone() const;
};

class RootTree: public Tree{
public:
    RootTree(int rootLabel);

    virtual int traceTree();
    virtual Tree* clone() const;
};

#endif

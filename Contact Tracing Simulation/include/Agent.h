#ifndef AGENT_H_
#define AGENT_H_

#include <vector>

class Agent{
public:
    Agent();

    virtual Agent* clone() const = 0;

    virtual void act(Session& session)=0;
    virtual ~Agent();
};

class ContactTracer: public Agent{
public:
    ContactTracer();
    ContactTracer(const ContactTracer& CT);
    virtual Agent* clone() const;
    
    virtual void act(Session& session);

};


class Virus: public Agent{
public:
    Virus(int nodeInd);
    Virus(const Virus& virus);
    virtual Agent* clone() const;

    virtual void act(Session& session);

private:
    const int nodeInd;
};

#endif

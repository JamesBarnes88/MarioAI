package ch.idsia.agents.controllers.behaviortree;

public class BehaviorTree implements Task{
    private Task head;

    public BehaviorTree() {
        // empty constructor
    }

    public BehaviorTree(Task head) {
        this.head = head;
    }

    public Task getHead() {
        return head;
    }

    public void setHead(Task head) {
        this.head = head;
    }

    @Override
    public boolean run(Object object) {
        if (head != null)
            return head.run(object);
        else
            throw new UnsupportedOperationException("cannot run a behavior tree without a head task");
    }
}

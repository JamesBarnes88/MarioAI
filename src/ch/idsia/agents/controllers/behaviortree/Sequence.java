package ch.idsia.agents.controllers.behaviortree;

public class Sequence extends Composite {
    @Override
    public boolean run(Object object) {
        for(Task task : getTasks()) {
           if (!task.run(object))
               return false;
        }

        return true;
    }
}

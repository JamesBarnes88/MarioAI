package ch.idsia.agents.controllers.behaviortree.composites;

import ch.idsia.agents.controllers.behaviortree.Task;

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

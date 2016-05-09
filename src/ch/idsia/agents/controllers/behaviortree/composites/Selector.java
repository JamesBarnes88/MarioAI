package ch.idsia.agents.controllers.behaviortree.composites;

import ch.idsia.agents.controllers.behaviortree.Task;

public class Selector extends Composite {
    @Override
    public boolean run(Object object) {
        for(Task task : getTasks()) {
            if (task.run(object))
                return true;
        }

        return false;
    }
}

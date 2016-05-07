package ch.idsia.agents.controllers.behaviortree;

import java.util.ArrayList;
import java.util.List;

public abstract class Composite implements Task {
    private ArrayList<Task> tasks;

    public Composite() {
        this.tasks = new ArrayList<>();
    }

    public Composite addTask(Task task) {
        tasks.add(task);
        return this;
    }

    public Composite removeTask(Task task) {
        tasks.remove(task);
        return this;
    }

    protected List<Task> getTasks() {
        return tasks;
    }

}

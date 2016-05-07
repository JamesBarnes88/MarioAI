package ch.idsia.agents.controllers.behaviortree;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class BehaviorTreeAIAgent extends BasicMarioAIAgent implements Agent{
    int trueJumpCounter = 0;
    int maxJumpCounter = 16;
    int trueSpeedCounter = 0;

    // Behavior Tree variable
    private BehaviorTree behaviorTree;


    // Actions
    private Task jumpAction = object -> {
//        if (isMarioOnGround && action[Mario.KEY_JUMP])
        System.out.println("attempting to jump");
        action[Mario.KEY_JUMP] = true;
        trueJumpCounter++;
        return true;
    };

    private Task moveRight = object -> {
        System.out.println("moving right");
        action[Mario.KEY_RIGHT] = true;
        return true;
    };

    private Task doNothing = object -> true;

    // Conditionals
    private Task isEnemyNotNear = object ->  {
        if (!isEnemyNear(marioEgoCol, marioEgoRow, 6)) {
            System.out.println("enemy not near");
            return true;
        } else {
            System.out.println("enemy found near");
            return false;
        }
    };

    private Task isNotJumping = object -> {
        if (isMarioAbleToJump) {
            System.out.println("not jumping");
            return true;
        } else {
            System.out.println("still jumping");
            return false;
        }
    };

    private Task continueJumping = object -> {
        if (trueJumpCounter > 16) {
            System.out.println("jump height acheived");
            action[Mario.KEY_JUMP] = false;
            trueJumpCounter = 0;
            maxJumpCounter = 16;
            return false;
        } else {
            System.out.println("jump still in progress");
            return true;
        }
    };

    Task isWallBlocking = object -> {
//        System.out.println("checking wall block");
        System.out.println("level obj: " + levelScene[marioEgoRow][marioEgoCol + 1]);
        if (levelScene[marioEgoRow][marioEgoCol + 1] != 0) {
            return true;
        } else {
            return false;
        }
    };

    public BehaviorTreeAIAgent() {
        super("Behavior Agent");
        // Create Behavior Tree
        this.behaviorTree = new BehaviorTree();

        Selector checkEnemy = new Selector();
        Selector checkForJump = new Selector();
        Sequence moveSequence = new Sequence();
        Sequence jumpSequence = new Sequence();

        checkEnemy.addTask(isEnemyNotNear);
        checkEnemy.addTask(jumpAction);

        checkForJump.addTask(isNotJumping);
        checkForJump.addTask(new Sequence().addTask(continueJumping).addTask(jumpAction));

        jumpSequence.addTask(isWallBlocking).addTask(moveRight).addTask(jumpAction);


        moveSequence.addTask(checkForJump).addTask(checkEnemy).addTask(moveRight).addTask(jumpSequence);
//        moveSequence.addTask(checkEnemy);

        behaviorTree.setHead(moveSequence);
    }

    @Override
    public void reset() {
        action = new boolean[Environment.numberOfKeys];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
        trueJumpCounter = 0;
        trueSpeedCounter = 0;
    }

    @Override
    public boolean[] getAction() {
        action = super.getAction();
//        action[Mario.KEY_SPEED] = true;
        behaviorTree.run(null);

        return action;
    }

    private boolean isEnemyNear(int x, int y, int distance) {
        for(int row = y - distance; row < y + distance; ++row) {
            for(int col = x - distance; col < x + distance; ++col) {
                if (enemies[row][col] != 0)
                    return true;
            }
        }

        return false;
    }
}

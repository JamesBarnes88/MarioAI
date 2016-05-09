package ch.idsia.agents.controllers.behaviortree;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.agents.controllers.behaviortree.composites.Selector;
import ch.idsia.agents.controllers.behaviortree.composites.Sequence;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class BehaviorTreeAIAgent extends BasicMarioAIAgent implements Agent{
    int trueJumpCounter = 0;
    int maxJumpCounter = 16;
    int trueSpeedCounter = 0;
    int trueMoveCounter = 0;
    int maxMoveCounter = 10;
    boolean isMovingSlow = false;

    // Behavior Tree variable
    private BehaviorTree behaviorTree;


    // Actions
    private Task lowJumpAction = object -> {
        System.out.println("low jumping");
        maxJumpCounter = 4;
        action[Mario.KEY_JUMP] = true;
        return true;
    };

    private Task highJumpAction = object -> {
        System.out.println("high jumping");
        maxJumpCounter = 16;
        action[Mario.KEY_JUMP] = true;
        return true;
    };

    private Task speed = object -> {
        System.out.println("speeding");
        action[Mario.KEY_SPEED] = true;
        return true;
    };

    private Task slowMoveRightAction = object -> {
        System.out.println("slow moving right");
        maxMoveCounter = 2;
        isMovingSlow = true;
        action[Mario.KEY_LEFT] = false;
        action[Mario.KEY_RIGHT] = true;
        return true;
    };

    private Task moveRightAction = object -> {
//        System.out.println("moving right");
        action[Mario.KEY_LEFT] = false;
        action[Mario.KEY_RIGHT] = true;
        return true;
    };

    private Task moveLeftAction = object -> {
//        System.out.println("moving right");
        action[Mario.KEY_RIGHT] = false;
        action[Mario.KEY_LEFT] = true;
        return true;
    };

    private Task doNothing = object -> true;

    // Conditionals
    private Task isEnemyNear = object ->  {
        if (isEnemyNear(marioEgoCol, marioEgoRow, 2)) {
            System.out.println("enemy found near");
            return true;
        } else {
//            System.out.println("enemy not near");
            return false;
        }
    };

    private Task isNotJumping = object -> {
        if (!action[Mario.KEY_JUMP] || !isMarioAbleToJump) {
            System.out.println("not jumping");
            return false;
        } else {
            System.out.println("still jumping");
            return true;
        }
    };

    private Task continueJumping = object -> {
        System.out.println("jump count: " + trueJumpCounter);
        if (action[Mario.KEY_JUMP]) {
            if (trueJumpCounter > maxJumpCounter) {
//                System.out.println("jump height achieved");
                action[Mario.KEY_JUMP] = false;
                trueJumpCounter = 0;
                maxJumpCounter = 16;
                return true;
            } else {
                trueJumpCounter++;
//                System.out.println("jump still in progress");
//                System.out.println("jumping");
                action[Mario.KEY_JUMP] = true;
                return true;
            }
        } else {
            return false;
        }
    };

    private Task continueMoving = object -> {
        System.out.println("move count: " + trueMoveCounter + "; maxCount: " + maxMoveCounter + "; ismovingslow: " + isMovingSlow);
        if (isMovingSlow && (action[Mario.KEY_LEFT] || action[Mario.KEY_RIGHT])) {
            if (trueMoveCounter > maxMoveCounter) {
                System.out.println("Moving Now! move count achieved");
                if (action[Mario.KEY_LEFT])
                    action[Mario.KEY_LEFT] = true;
                else
                    action[Mario.KEY_RIGHT] = true;
                trueMoveCounter = 0;
                maxMoveCounter = 10;
                isMovingSlow = false;

                return true;
            } else {
                trueMoveCounter++;
                System.out.println("Move pausing");
                action[Mario.KEY_LEFT] = false;
                action[Mario.KEY_RIGHT] = false;

                return true;
            }
        } else {
            return false;
        }
    };

    Task isWallBlocking = object -> {
        System.out.println("checking wall block");
        if (levelScene[marioEgoRow][marioEgoCol + 1] != 0) {
            System.out.println("level obj: " + levelScene[marioEgoRow][marioEgoCol + 1]);
            return true;
        } else {
            return false;
        }
    };

    public BehaviorTreeAIAgent() {
        super("Behavior Agent");
        // Create Behavior Tree
        this.behaviorTree = new BehaviorTree();

        Sequence headSequence = new Sequence();

        Selector jumpSelector = new Selector();
        Selector moveSelector = new Selector();

        Sequence continueJumpingSequence = new Sequence();
        Sequence jumpIfBlockingSequence = new Sequence();
        Sequence jumpIfEnemyNearSequence = new Sequence();

        Sequence moveIfEnemyNearSequence = new Sequence();
        Sequence moveRightSequence = new Sequence();

        // Jumping Branch

        // Construct continue jumping
        continueJumpingSequence.addTask(continueJumping);

        // Construct if blocking jumping
        jumpIfBlockingSequence.addTask(isWallBlocking).addTask(highJumpAction);

        // Construct enemy near jumping
        jumpIfEnemyNearSequence.addTask(isEnemyNear).addTask(lowJumpAction);

        // Movement Branch

        // If Enemy Near
        moveIfEnemyNearSequence.addTask(isEnemyNear).addTask(slowMoveRightAction);

        // Move Right with speed
        moveRightSequence.addTask(speed).addTask(moveRightAction);

        // Tree Construction

        // Construct Jumping Selection
        jumpSelector.addTask(continueJumpingSequence).addTask(jumpIfBlockingSequence).addTask(jumpIfEnemyNearSequence).addTask(moveRightSequence);

        // Construct Move Selection
        moveSelector.addTask(continueMoving).addTask(new Sequence().addTask(isWallBlocking).addTask(moveRightAction)).addTask(moveIfEnemyNearSequence).addTask(moveRightSequence);

        headSequence.addTask(jumpSelector).addTask(moveSelector);

        behaviorTree.setHead(headSequence);
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
//        action = super.getAction();
        action[Mario.KEY_SPEED] = false;
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

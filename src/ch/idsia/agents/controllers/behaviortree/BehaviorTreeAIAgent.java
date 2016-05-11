package ch.idsia.agents.controllers.behaviortree;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.agents.controllers.behaviortree.composites.Selector;
import ch.idsia.agents.controllers.behaviortree.composites.Sequence;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

import static ch.idsia.benchmark.mario.engine.GeneralizerLevelScene.BRICK;
import static ch.idsia.benchmark.mario.engine.GeneralizerLevelScene.COIN_ANIM;

public class BehaviorTreeAIAgent extends BasicMarioAIAgent implements Agent{
    private int trueJumpCounter = 0;
    private int maxJumpCounter = 16;

    private int trueSpeedCounter = 0;

    private int trueMoveCounter = 0;
    private int maxMoveCounter = 10;

    private boolean isMovingSlow = false;

    private int objectNear = 0;

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
        System.out.println("moving right");
        action[Mario.KEY_LEFT] = false;
        action[Mario.KEY_RIGHT] = true;
        return true;
    };

    private Task slowMoveLeftAction = object -> {
        System.out.println("slow moving left");
        maxMoveCounter = 2;
        isMovingSlow = true;
        action[Mario.KEY_LEFT] = true;
        action[Mario.KEY_RIGHT] = false;
        return true;
    };

    private Task moveLeftAction = object -> {
        System.out.println("moving left");
        action[Mario.KEY_RIGHT] = false;
        action[Mario.KEY_LEFT] = true;
        return true;
    };

    // Conditionals
    private Task isEnemyNear = object ->  {
        if (isEnemyNear(marioEgoCol, marioEgoRow, 2)) {
            System.out.println("enemy found near");
            return true;
        } else {
            System.out.println("enemy not near");
            return false;
        }
    };

    // Conditionals
    private Task isBrickNear = object ->  {
        int leftOrRight = isObjectLeftRight(marioEgoCol, marioEgoRow, 3, true, BRICK);
        if (leftOrRight != 0) {
            objectNear = leftOrRight;
            System.out.println("brick found near");
            return true;
        } else {
            System.out.println("brick not near");
            return false;
        }
    };

    // Conditionals
    private Task isCoinNear = object ->  {
        int leftOrRight = isObjectLeftRight(marioEgoCol, marioEgoRow, 2, false, COIN_ANIM);
        if (leftOrRight != 0) {
            objectNear = leftOrRight;
            System.out.println("enemy found near");
            return true;
        } else {
            System.out.println("enemy not near");
            return false;
        }
    };

    private Task moveToObject = object -> {
        if (objectNear != 0) {
            System.out.println("moving to coin: " + objectNear);
            if (objectNear < 0) {
                System.out.println("moving left");
                action[Mario.KEY_RIGHT] = false;
                action[Mario.KEY_LEFT] = true;
            } else {
                System.out.println("moving right");
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_LEFT] = false;
            }
            objectNear = 0;
            return true;
        }

        return false;
    };

    private Task slowMoveToObject = object -> {
        if (objectNear != 0) {
            System.out.println("moving to object: " + objectNear);
            if (objectNear < 0) {
                System.out.println("slow moving left");
                maxMoveCounter = 1;
                isMovingSlow = true;

                action[Mario.KEY_RIGHT] = false;
                action[Mario.KEY_LEFT] = true;
            } else {
                System.out.println("slow moving right");
                maxMoveCounter = 2;
                isMovingSlow = true;

                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_LEFT] = false;
            }
            objectNear = 0;
            return true;
        }

        return false;
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
                System.out.println("jump height achieved");
                action[Mario.KEY_JUMP] = false;
                trueJumpCounter = 0;
                maxJumpCounter = 16;
                return true;
            } else {
                trueJumpCounter++;
                System.out.println("jump still in progress");
                System.out.println("jumping");
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
        Sequence jumpIfCoinNearSequence = new Sequence();

        Sequence moveIfEnemyNearSequence = new Sequence();
        Sequence moveIfCoinNearSequence = new Sequence();
        Sequence moveRightSequence = new Sequence();

        // Jumping Branch

        // Construct continue jumping
        continueJumpingSequence.addTask(continueJumping);

        // Construct if blocking jumping
        jumpIfBlockingSequence.addTask(isWallBlocking).addTask(highJumpAction);

        // Construct enemy near jumping
        jumpIfEnemyNearSequence.addTask(isEnemyNear).addTask(lowJumpAction);

        jumpIfCoinNearSequence.addTask(isCoinNear).addTask(lowJumpAction);

        // Movement Branch

        // If Enemy Near
        moveIfEnemyNearSequence.addTask(isEnemyNear).addTask(slowMoveRightAction).addTask(speed);

        // If Coin Near
        moveIfCoinNearSequence.addTask(isCoinNear).addTask(speed).addTask(slowMoveToObject);

        // Move Right with speed
        moveRightSequence.addTask(speed).addTask(moveRightAction);

        // Tree Construction

        // Construct Jumping Selection
        jumpSelector.addTask(continueJumpingSequence).addTask(jumpIfBlockingSequence).
                addTask(jumpIfEnemyNearSequence).addTask(jumpIfCoinNearSequence).
                addTask(moveRightSequence);

        // Construct Move Selection
        moveSelector.addTask(continueMoving).addTask(new Sequence().addTask(isWallBlocking).
                addTask(moveRightAction)).addTask(moveIfEnemyNearSequence).
                addTask(moveIfCoinNearSequence).addTask(moveRightSequence);

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

    private int isObjectLeftRight(int x, int y, int distance, boolean isAbove, int object) {
        int toCol = x + distance, toRow;
        if (isAbove)
            toRow = y;
        else
            toRow = y + distance;

        for (int col = x - distance; col < toCol; ++col) {
            for (int row = y - distance;row < toRow; ++row) {
                if (levelScene[row][col] == object) {
                    if (col < marioEgoCol)
                        return -1;
                    else if (col > marioEgoCol)
                        return 1;
                }
            }
        }

        return 0;
    }
}

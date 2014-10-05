package lego.training;

import lego.robot.api.RobotEnvironment;
import lego.robot.api.RobotStrategy;
import lego.robot.api.constants.AbsoluteHeading;
import lego.util.Constants;
import lego.util.TupleIntInt;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class TrainingMap {

    private static final int mazeWidth = 9;
    private static final int mazeHeight = 6;

    private MazeCell[][] maze = new MazeCell[mazeWidth][mazeHeight];

    private TupleIntInt startPos = null;

    public TupleIntInt getStartPos(){
        return startPos;
    }

    public MazeCell[][] getMaze(){
        return maze;
    }

    private RobotEnvironment env = new RobotEnvironment();

    public RobotEnvironment getRobotEnvironment(){
        return env;
    }


    private int totalMovements = 0;
    private int totalTurns = 0;
    private int totalObstacleHinders = 0;

    AbsoluteHeading robotHeading;
    TupleIntInt robotPos = new TupleIntInt(0, 0);

    public TupleIntInt getRobotPos(){
        return robotPos;
    }

    private int blocksRemainingToCollect = 0;
    public void markAnotherBlockAsCollected(){
        blocksRemainingToCollect --;
    }

    public int getBlocksRemainingToCollect(){
        return blocksRemainingToCollect;
    }

    public TrainingMap(){
        generateMaze(maze, env);

    }

    public TrainingMap(BufferedReader file) throws Error{
        if(file == null){
            generateMaze(maze, env);
        }else{
            try {
                String line = file.readLine();
                while("+---------------------------+".equals(line)){}

                for (int lines = 0; lines < 6; lines++) {
                    line = file.readLine();
                    for (int symbols = 0; symbols < 9 * 3; symbols += 3) {
                        String symbol = line.substring(symbols + 1, symbols + 4);
                        if (symbol.equals(Constants.RENDER_BLOCK)) {
                            maze[symbols / 3][lines] = new MazeCell();
                            maze[symbols / 3][lines].isBlock = true;
                            maze[symbols / 3][lines].isStart = false;
                        } else if (symbol.equals(Constants.RENDER_PAC_DOT)) {
                            maze[symbols / 3][lines] = new MazeCell();
                            maze[symbols / 3][lines].isBlock = false;
                            maze[symbols / 3][lines].isStart = false;
                            blocksRemainingToCollect ++;
                        } else if (symbol.equals(Constants.RENDER_START)) {
                            maze[symbols / 3][lines] = new MazeCell();
                            maze[symbols / 3][lines].isStart = true;
                            maze[symbols / 3][lines].isBlock = false;
                            if(startPos != null){
                                throw new Error("Invalid file format: Too many starting positions.");
                            }
                            startPos = new TupleIntInt(symbols / 3, lines);

                        } else {
                            throw new Error("Invalid file format: Unknown block: "+symbol);
                        }
                    }
                }
                if(!"+---------------------------+".equals(file.readLine())){
                    throw new Error("Invalid file format: Expected end of lower and of map.");
                }
                if(startPos == null){
                    throw new Error("Invalid file format: No starting position.");
                }

            }catch (IOException e){
                throw new Error("Some problem reading file (IOException).");
            }
        }
    }

    private void generateMaze(MazeCell[][] maze, RobotEnvironment re){
        //TODO generate maze
    }

    public void reset(){
        env = new RobotEnvironment();
        totalMovements = 0;
        totalTurns = 0;
        totalObstacleHinders = 0;
        blocksRemainingToCollect = 0;
        for(int x = 0; x < mazeWidth; x ++){
            for(int y = 0; y < mazeHeight; y ++){
                if(!maze[x][y].isBlock && !maze[x][y].isStart){
                    maze[x][y].visitedTimes = 0;
                    blocksRemainingToCollect ++;
                }
            }
        }
    }

    public TrainingStatistics getStatistics(RobotStrategy robotStrategy){
        TrainingStatistics stats = new TrainingStatistics();

        boolean passed = true;
        int totalCollected = 0;
        int totalCollectible = 0;

        for(int x = 0; x < mazeWidth; x ++){
            for(int y = 0; y < mazeHeight; y ++){
                if(!maze[x][y].isBlock && !maze[x][y].isStart){
                    if(maze[x][y].visitedTimes == 0){
                        passed = false;
                    }else {
                        totalCollectible++;
                        totalCollected += maze[x][y].visitedTimes;
                    }
                }
            }
        }

        stats.fill(
            robotStrategy.getStrategyDescriptor(),
            passed,
            totalCollectible * 100 / totalCollected,
            totalMovements,
            totalTurns,
            totalObstacleHinders
        );

        return stats;
    }

    public void logMovement(){
        totalMovements ++;
    }
    public void logTurn(){
        totalTurns ++;
    }
    public void logObstacleHinders(){
        totalObstacleHinders ++;
    }

}

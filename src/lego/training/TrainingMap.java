package lego.training;

import lego.robot.api.RobotEnvironment;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class TrainingMap {

    private static final int mazeWidth = 9;
    private static final int mazeHeight = 6;

    private MazeCell[][] maze = new MazeCell[mazeWidth][mazeHeight];


    public MazeCell[][] getMaze(){
        return maze;
    }

    private RobotEnvironment env = new RobotEnvironment();

    public RobotEnvironment getRobotEnvironment(){
        return env;
    }

    public TrainingMap(){
        generateMaze(maze, env); //Don't forget to call deploy(x,y) method with coords of start

        //TODO start some stuff

    }

    private void generateMaze(MazeCell[][] maze, RobotEnvironment re){
        //TODO generate maze
    }


}

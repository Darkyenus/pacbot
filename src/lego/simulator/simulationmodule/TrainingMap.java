package lego.simulator.simulationmodule;

import lego.util.Constants;
import lego.util.TupleIntInt;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class TrainingMap {

    static final int mazeWidth = 9;
    static final int mazeHeight = 6;

    private MazeCell[][] maze = new MazeCell[mazeWidth][mazeHeight];

    private TupleIntInt startPos = null;

    public TupleIntInt getStartPos(){
        return startPos;
    }

    public MazeCell[][] getMaze(){
        return maze;
    }


    private int blocksRemainingToCollect = 0;
    public void markAnotherBlockAsCollected(){
        blocksRemainingToCollect --;
    }

    public int getBlocksRemainingToCollect(){
        return blocksRemainingToCollect;
    }

    public TrainingMap(){
        generateMaze();

    }

    public TrainingMap(BufferedReader file) throws Error{
        if(file == null){
            generateMaze();
        }else{
            try {

                while(!"+---------------------------+".equals(file.readLine())){}
                String line;

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
                            throw new Error("Invalid file format: Unknown block");
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

    private void generateMaze(){
        //TODO generate maze
    }

    public void reset(){
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




}

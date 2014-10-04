package lego.training;

/**
 * Created by jIRKA on 3.10.2014.
 */
public class TrainingStatistics {

    private boolean passed = false;
    private int efficiency = 0;
    private int totalMovements = 0;
    private int totalTurns = 0;
    private int totalObstacleHinders = 0;

    public void fill(
            boolean passed,
            int efficiency,
            int totalMovements,
            int totalTurns,
            int totalObstacleHinders
    ){

        this.passed = passed;
        this.efficiency = efficiency;
        this.totalMovements = totalMovements;
        this.totalTurns = totalTurns;

    }

    public boolean hasPassed(){
        return passed;
    }

    public int getEfficiency(){
        return efficiency;
    }

    public int getTotalMovements(){
        return totalMovements;
    }

    public int getTotalTurns(){
        return totalTurns;
    }

    public int getTotalObstacleHinders(){
        return totalObstacleHinders;
    }
}

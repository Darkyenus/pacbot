package lego.training;

/**
 * Private property.
 * User: jIRKA
 * Date: 3.10.2014
 * Time: 20:26
 */
public class TrainingStatistics {

    private String strategyDescriptor = "<No name>";
    private boolean passed = false;
    private int efficiency = 0;
    private int totalMovements = 0;
    private int totalTurns = 0;
    private int totalObstacleHinders = 0;

    public void fill(
            String strategyDescriptor,
            boolean passed,
            int efficiency,
            int totalMovements,
            int totalTurns,
            int totalObstacleHinders
    ){
        this.strategyDescriptor = strategyDescriptor;
        this.passed = passed;
        this.efficiency = efficiency;
        this.totalMovements = totalMovements;
        this.totalTurns = totalTurns;
        this.totalObstacleHinders = totalObstacleHinders;
    }

    public String getStrategyDescriptor() {
        return strategyDescriptor;
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

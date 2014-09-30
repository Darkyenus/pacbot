package lego.training;

/**
 * Created by jIRKA on 30.9.2014.
 */
public class TrainingMain {

    private static String RENDER_BLOCK = "[x]";
    private static String RENDER_EMPTY = " • ";
    private static String RENDER_EATEN = " o ";
    private static String RENDER_ROBOT = "(-)";
    private static String RENDER_START = " v ";





    public void render(TrainingMap map) {
        System.out.println("+---------------------------+");
        for(int y = 0; y < 9; y ++) {
            System.out.print("|");
            for (int x = 0; x < 9; x ++) {
                if(map.getMaze()[x][y].isStart){
                    System.out.print(RENDER_START);
                }else if(map.getRobotEnvironment().getX() == x && map.getRobotEnvironment().getY() == y){
                    System.out.print(RENDER_ROBOT);
                }else if(map.getMaze()[x][y].isBlock){
                    System.out.print(RENDER_BLOCK);
                }else if(map.getMaze()[x][y].visitedTimes == 0){
                    System.out.print(RENDER_EMPTY);
                }else{
                    System.out.print(RENDER_EATEN);
                }
            }
            System.out.println("|");
        }
        System.out.println("+---------------------------+");
    }
}

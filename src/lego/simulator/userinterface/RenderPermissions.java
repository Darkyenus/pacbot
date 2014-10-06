package lego.simulator.userinterface;

/**
 * Created by jIRKA on 5.10.2014.
 */
public class RenderPermissions {

    private static boolean renderErrors = true;
    private static boolean renderWarns = true;
    private static boolean renderSuccesses = true;
    private static boolean renderInfos = true;
    private static boolean renderGeneralText = true;

    private static boolean renderTrainingMaps = true;
    private static boolean renderStats = true;


    /**
     * Sets permission to all renderables at once.
     * @param render that mentioned value
     */
    public static void setRenderForAll(boolean render){
        setRenderErrors(render);
        setRenderWarns(render);
        setRenderSuccesses(render);
        setRenderInfos(render);
        setRenderGeneralText(render);

        setRenderTrainingMaps(render);
        setRenderStats(render);
    }




    /**
     * Errors means everything rendered using Print.error
     * @return whenever is error text rendered or not
     */
    public static boolean renderErrors(){
        return renderErrors;
    }

    /**
     * Warns means everything rendered using Print.warn
     * @return whenever is warning text rendered or not
     */
    public static boolean renderWarns(){
        return renderWarns;
    }

    /**
     * Successes means everything rendered using Print.success
     * @return whenever is success text rendered or not
     */
    public static boolean renderSuccesses(){
        return renderSuccesses;
    }

    /**
     * Infos means everything rendered using Print.info
     * @return whenever is info text rendered or not
     */
    public static boolean renderInfos(){
        return renderInfos;
    }

    /**
     * General texts means everything rendered using Print.line, Print.text and Print.color
     * @return whenever is general text rendered or not
     */
    public static boolean renderGeneralText(){
        return renderGeneralText;
    }


    /**
     * Training maps means everything rendered using Render.trainingMap
     * @return
     */
    public static boolean renderTrainingMaps(){
        return renderTrainingMaps;
    }

    /**
     * Stats means everything rendered using Render.statistics
     * @return
     */
    public static boolean renderStats(){
        return renderStats;
    }


    /**
     * Errors means everything rendered using Print.error
     * @param render true if it should be rendered.
     */
    public static void setRenderErrors(boolean render) {
        renderErrors = render;
    }

    /**
     * Warns means everything rendered using Print.warn
     * @param render true if it should be rendered.
     */
    public static void setRenderWarns(boolean render) {
        renderWarns = render;
    }

    /**
     * Successes means everything rendered using Print.success
     * @param render true if it should be rendered.
     */
    public static void setRenderSuccesses(boolean render) {
        renderSuccesses = render;
    }

    /**
     * Infos means everything rendered using Print.info
     * @param render true if it should be rendered.
     */
    public static void setRenderInfos(boolean render) {
        renderInfos = render;
    }

    /**
     * General texts means everything rendered using Print.line, Print.text and Print.color
     * @param render true if it should be rendered.
     */
    public static void setRenderGeneralText(boolean render) {
        renderGeneralText = render;
    }


    /**
     * Training maps means everything rendered using Render.trainingMap
     * @param render true if it should be rendered.
     */
    public static void setRenderTrainingMaps(boolean render) {
        renderTrainingMaps = render;
    }

    /**
     * Stats means everything rendered using Render.statistics
     * @param render true if it should be rendered.
     */
    public static void setRenderStats(boolean render) {
        renderStats = render;
    }
}

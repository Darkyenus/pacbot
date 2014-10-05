package lego.training.commands;

import lego.training.TrainingsMain;
import lego.training.userinterface.Print;
import lego.training.userinterface.UserInput;
import lego.util.BetterThread;

import java.util.LinkedList;
import java.util.Set;

/**
 * Private property.
 * User: jIRKA
 * Date: 5.10.2014
 * Time: 16:32
 */
public class Quit implements Command {

    @Override
    public void execute(String[] args) {

        boolean silent = false;
        boolean quit = false;

        if(args.length != 1){
            if(args.length != 0) {
                Print.error("Too many flags detected. Use only one flag at time.");
            }else{
                silent = false;
                quit = true;
            }
        }else{
            switch (args[0]) {
                case "-kt":
                case "--kill-threads":
                    stopOrKillThreads(scanForRunningThreads(true), false);
                    break;
                case "-st":
                case "--stop-stoppable-threads":
                    stopThreads(scanForRunningThreads(true));
                    break;
                case "-wt":
                case "--view-threads": {
                    Thread[] threads = scanForRunningThreads(true);
                    if (threads.length == 0) {
                        Print.info("No threads running. Nothing to show.");
                    } else {
                        showThreadList(threads);
                    }
                    break;
                }
                case "-wtd":
                case "--view-threads-daemon": {
                    Thread[] threads = scanForRunningThreads(false);
                    if (threads.length == 0) {
                        Print.info("No threads running. Nothing to show.");
                    } else {
                        showThreadList(threads);
                    }
                    break;
                }
                case "-sq":
                case "--silent-quit":
                    silent = true;
                    quit = true;
                    break;
                default:
                    Print.error("Unknown flag. "+messageTypos);
                    break;
            }
        }

        if(quit){
            boolean exit = false;

            Thread[] relevantThreads = scanForRunningThreads(true);

            if(relevantThreads.length != 0){
                if(!silent) {
                    Print.warn("There are some threads still running running:");
                    showThreadList(relevantThreads);
                }
                if(silent || UserInput.askQuestion("Stop these threads and quit application?")){
                    stopOrKillThreads(relevantThreads, silent);
                    exit = true;
                }else{
                    Print.info("Quitting aborted by user.");
                }
            }else{
                exit = true;
            }

            if(exit){

                //TODO clean resources (close streams, etc)

                TrainingsMain.quit();
            }
        }

    }

    private void stopOrKillThreads(Thread[] threads, boolean silent){
        for(Thread t:threads){
            if(t instanceof BetterThread){
                ((BetterThread)t).finish();
                long time = System.currentTimeMillis();
                while(t.isAlive() && System.currentTimeMillis() - time < 2500){
                    try {
                        Thread.sleep(200);
                    }catch (InterruptedException ignored){}
                }
                if(t.isAlive()){
                    if(!silent)
                        Print.info("Thread "+t.getName()+" seems not responding. Killing it.");
                    t.stop();
                }else{
                    if(!silent)
                        Print.info("Thread "+t.getName()+" stopped successfully.");
                }
            }else{
                if(!silent)
                    Print.info("Thread "+t.getName()+" is not stoppable. Killing it.");
                t.stop();
            }

        }
    }

    private void stopThreads(Thread[] threads){
        for(Thread t:threads){
            if(t instanceof BetterThread){
                ((BetterThread)t).finish();
                long time = System.currentTimeMillis();
                while(t.isAlive() && System.currentTimeMillis() - time < 2500){
                    try {
                        Thread.sleep(200);
                    }catch (InterruptedException ignored){}
                }
                if(t.isAlive()){
                    Print.info("Thread "+t.getName()+" seems not responding. Stopping request sent, maybe it stops sometimes...");
                }else{
                    Print.info("Thread "+t.getName()+" stopped successfully.");
                }
            }

        }
    }

    private Thread[] scanForRunningThreads(boolean exclDaemon){
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

        LinkedList<Thread> relevantThreads = new LinkedList<>();

        for(Thread t:threadArray){
            if(((!t.isDaemon() && exclDaemon) || !exclDaemon) && t.isAlive() && !t.getName().equals("main")){
                relevantThreads.add(t);
            }
        }
        return relevantThreads.toArray(new Thread[relevantThreads.size()]);
    }


    private void showThreadList(Thread[] threads){
        for(Thread t:threads){
            if(t instanceof BetterThread){
                Print.line("       Stoppable thread: "+t.getName()+" with " +
                        (t.getPriority()==1?"low":t.getPriority()==5?"normal":t.getPriority()==10?"high":t.getPriority()) +
                        " priority.");
            }else{
                Print.line("       Thread: "+t.getName()+" with " +
                        (t.getPriority()==1?"low":t.getPriority()==5?"normal":t.getPriority()==10?"high":t.getPriority()) +
                        " priority.");
            }

        }
    }


    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getShortDesc() {
        return "Quits the application";
    }

    @Override
    public String[] getManPage() {
        return new String[]{
                "Quits the application (unless otherwise specified). Known aliases: exit",
                "By default (without any flags) this command tries to quit an app.",
                "If any problem is detected (running threads,...) it ask user what to do.",
                "When using this command to quit app, it is guaranteed that all resources will end up cleaned",
                "",
                "Please use quit command instead of system's force quit.",
                "",
                "use: [flag]",
                "",
                messageFlagsTitle,
                "    -kt|--kill-threads             Tries to stop all stoppable threads.",
                "                                   Kills every thread that is running after",
                "                                   (unsuccessful) stopping request.",
                "    -st|--stop-stoppable-threads   Tries to stop all stoppable threads.",
                "    -sq|--silent-quit              Quits the application. If there are any",
                "                                   problems detected, automatically deals with them.",
                "    -wt|--view-threads             Shows up all running threads, that may cause a problem",
                "                                   When quitting application.",
                "    -wtd|--view-threads-daemon     Shows up all running threads, that may cause a problem",
                "                                   When quitting application, including daemon threads."
        };
    }
}

package lego.simulator.commands;

import lego.simulator.Main;
import lego.simulator.userinterface.Print;
import lego.simulator.userinterface.UserInput;
import lego.util.StoppableThread;

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
            if("-kt".equals(args[0]) || "--kill-threads".equals(args[0])){
                stopOrKillThreads(scanForRunningThreads(true), false);
            }else if("-st".equals(args[0]) || "--stop-stoppable-threads".equals(args[0])){
                stopThreads(scanForRunningThreads(true));
            }else if("-wt".equals(args[0]) || "--view-threads".equals(args[0])){
                Thread[] threads = scanForRunningThreads(true);
                if (threads.length == 0) {
                    Print.info("No threads running. Nothing to show.");
                } else {
                    showThreadList(threads);
                }
            }else if("-wtd".equals(args[0]) || "--view-threads-daemon".equals(args[0])){
                Thread[] threads = scanForRunningThreads(false);
                if (threads.length == 0) {
                    Print.info("No threads running. Nothing to show.");
                } else {
                    showThreadList(threads);
                }
            }else if("-sq".equals(args[0]) || "--silent-quit".equals(args[0])){
                silent = true;
                quit = true;
            }else{
                Print.error("Unknown flag. "+messageTypos);
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

                Main.quit();
            }
        }

    }

    private void stopOrKillThreads(Thread[] threads, boolean silent){
        for(Thread t:threads){
            if(t instanceof StoppableThread){
                ((StoppableThread)t).finish();
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
            if(t instanceof StoppableThread){
                ((StoppableThread)t).finish();
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

        LinkedList<Thread> relevantThreads = new LinkedList<Thread>();

        for(Thread t:threadArray){
            if(((!t.isDaemon() && exclDaemon) || !exclDaemon) && t.isAlive() && !t.getName().equals("main")){
                relevantThreads.add(t);
            }
        }
        return relevantThreads.toArray(new Thread[relevantThreads.size()]);
    }


    private void showThreadList(Thread[] threads){
        for(Thread t:threads){
            if(t instanceof StoppableThread){
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

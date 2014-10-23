package lego.nxt.util;

import lejos.util.Delay;

/**
 * Singleton responsible for scheduling tasks.
 * Task's create linked list structure that is being constantly consumed by Tasks themselves.
 * Every Task can have one successor task.
 * When active Task finishes, successor takes over. When there is no successor,
 * TaskProcessor waits for more actions from outside and resumes processing as soon as any task arrives.
 */
public class TaskProcessor {
    public TaskProcessor(){}

    /**
     * Starts thread that processes.
     */
    public static void initialize(){
        Thread driverThread = new Thread("Driver"){
            @Override
            public void run() {
                TaskProcessor.process();
            }
        };
        driverThread.setDaemon(false);
        driverThread.start();
    }

    private static final Object PROCESSOR_LOCK = new Object();
    private static Task stackHead = null;
    private static boolean running = true;

    /**
     * @return when no task is being processed
     */
    public static boolean isIdle(){
        return stackHead == null;
    }

    /**
     * Will wait using 200ms delays until TaskProcessor is idle, then returns.
     * Will return immediately when idle.
     */
    public static void waitUntilIdle(){
        while(!isIdle()){
            Delay.msDelay(200);
        }
    }

    /**
     * Will stop processing as soon as last task finishes.
     * Will not then continue to its successors nor wait for more.
     */
    public static void scheduleExit() {
        running = false;
        synchronized (PROCESSOR_LOCK) {
            PROCESSOR_LOCK.notifyAll();
        }
    }

    /**
     * Given task will be added to the end of queue.
     * Then it will be executed asynchronously.
     * Therefore, this method doesn't block.
     *
     * @param task to be added
     */
    public static void appendTask(Task task) {
        if (task == null) {
            return;
        }
        if (stackHead == null) {
            stackHead = task;
            synchronized (PROCESSOR_LOCK) {
                PROCESSOR_LOCK.notifyAll();
            }
        } else {
            stackHead.appendTask(task);
        }
    }

    /**
     * Do not call this from user code, this is called by Driver class to actually process Task's.
     */
    public static void process() {
        while (running) {
            if (stackHead != null) {
                stackHead.process();
                stackHead = stackHead.nextTask;
            } else {
                synchronized (PROCESSOR_LOCK) {
                    try {
                        PROCESSOR_LOCK.wait(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    /**
     * @return Active task. May be null.
     */
    public static Task getStackHead(){
        return stackHead;
    }

    /**
     * Task contains logic for program.
     * It has one optional successor Task that will be executed when this Task finishes.
     * This goes down recursively.
     */
    public static class Task {

        private Task nextTask;

        /**
         * Actual logic for task is here. Override this method.
         */
        protected void process() {
        }

        /**
         * Given task will be forcefully added right after this task.
         * So it will be this task's direct successor.
         * Any previous successors will be appended to this successor.
         * {@see appendTask(Task)} for how it will be done.
         */
        public Task pushTask(Task directSuccessor) {
            directSuccessor.appendTask(nextTask);
            nextTask = directSuccessor;
            return this;
        }

        /**
         * Given task will be added as successor to this Task.
         * If this task has no successor, it will be direct successor.
         * If this task has successor, it will be appended with same logic to him.
         *
         * So, given task will be at the end of this task queue.
         */
        public Task appendTask(Task successor) {
            Task head = this;
            while(head.nextTask != null){
                head = head.nextTask;
            }
            head.nextTask = successor;
            return this;
        }

        /**
         * Override this if your task runs for any measurable amount of time.
         * Returns true if robot doesn't move during this task.
         * Moving tasks can use this knowledge to brake/not accelerate and thus speeding up the robot.
         */
        public boolean isStationery() {
            return isNextStationery();
        }

        /**
         * @return Whether direct successor isStationery. When there is none, returns true.
         */
        protected boolean isNextStationery() {
            return nextTask == null || nextTask.isStationery();
        }

        /**
         * Returns debug info about this task. Please override this with custom info.
         */
        @Override
        public String toString() {
            return "T:Empty";
        }

        /**
         * @return direct successor. May be null.
         */
        public Task getNextTask() {
            return nextTask;
        }

        /**
         * Sets direct successor, discarding original one (if there was any).
         * So anything queued after this Task will be discarded by this.
         */
        protected void setNextTask(Task nextTask) {
            this.nextTask = nextTask;
        }
    }
}

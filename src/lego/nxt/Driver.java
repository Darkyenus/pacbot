package lego.nxt;

import lego.robot.api.RobotInterface;
import lego.robot.api.RobotStrategy;
import lego.robot.brain.Brain;
import lejos.nxt.*;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;
import lejos.util.Delay;

import java.io.*;
import java.util.HashMap;

/**
 * Private property.
 * User: Darkyen
 * Date: 04/12/13
 * Time: 22:02
 */
public class Driver {

    private static final boolean FLIP_DIRECTION = true;

    private static final int TRAY_BALLS = 8;

    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);

    public static final float GRAD_TO_RAD = TWO_PI / 400f;

    private static final RobotInterface robotInterface = new DifferentialRobotInterface();

    private static MotorController toolMotor = new MotorController(MotorPort.A);

    private static LightSensor lightSensor = new LightSensor(SensorPort.S1);
    private static TouchSensor leftClaw = new TouchSensor(SensorPort.S2);
    private static TouchSensor rightClaw = new TouchSensor(SensorPort.S3);
    private static UltrasonicSensor disruptor = new UltrasonicSensor(SensorPort.S4);

    public static Console console = new Console();
    private static boolean virtualEnter = false;
    private static boolean virtualEscape = false;

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        startSubsystems();
        MotorManager.stop(MotorManager.MAX_ACCELERATION);

        TaskProcessor.process();

    }

    /**
     * @return true if task should be done, false if cancelled
     */
    private static boolean waitForPress(int delay) {
        int delays = 0;
        while (!leftClaw.isPressed() && !rightClaw.isPressed() && Button.ENTER.isUp() && Button.ESCAPE.isUp() && !virtualEnter && !virtualEscape) {
            Delay.msDelay(100);//Wait for press
            delays++;
            if (delays % delay == 0) {
                Sound.playTone((int) (Math.random() * 1200) + 100, 150);
                Delay.msDelay(10);
                Sound.playTone((int) (Math.random() * 1200) + 100, 140);
            }
        }
        boolean result = Button.ESCAPE.isUp() && !virtualEscape;
        virtualEnter = false;
        virtualEscape = false;
        while (leftClaw.isPressed() || rightClaw.isPressed() || Button.ENTER.isDown() || Button.ESCAPE.isDown()) {
            Delay.msDelay(10);
        }
        return result;
    }

    private static void startSubsystems() {
        toolMotor.setStallThreshold(100, 2000);
        toolMotor.setAcceleration(10000);
        toolMotor.setSpeed(1000);

        disruptor.setContinuousInterval(1);
        disruptor.continuous();//Start disrupting!
    }

    private static void startToolMotor(){
        toolMotor.backward();//Start spinning!
    }

    private static void stopToolMotor(){
        toolMotor.flt(true);//Start spinning!
    }

    public static class TaskProcessor {
        private static final Object PROCESSOR_LOCK = new Object();
        private static Task stackHead = null;
        private static boolean running = true;

        public static boolean isIdle(){
            return stackHead == null;
        }

        public static void waitUntilIdle(){
            while(isIdle()){
                Delay.msDelay(200);
            }
        }

        public static void scheduleExit() {
            running = false;
            synchronized (PROCESSOR_LOCK) {
                PROCESSOR_LOCK.notifyAll();
            }
        }

        public static void appendHead(Task head) {
            if (head == null) {
                return;
            }
            if (stackHead == null) {
                stackHead = head;
                synchronized (PROCESSOR_LOCK) {
                    PROCESSOR_LOCK.notifyAll();
                }
            } else {
                stackHead.appendTask(head);
            }
        }

        public static void process() {
            while (running) {
                if (stackHead != null) {
                    //console.printTime("P: "+stackHead);
                    stackHead.process();
                    stackHead = stackHead.nextTask;
                    //console.printTime("P");
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

        public static class Task {

            private Task nextTask;

            protected void process() {
            }

            public Task pushNextTask(Task task) {
                task.appendTask(nextTask);
                nextTask = task;
                return this;
            }

            public Task appendNextTask(Task task) {
                appendTask(task);
                return this;
            }

            private void appendTask(Task toAppend) {
                if (nextTask == null) {
                    nextTask = toAppend;
                } else {
                    nextTask.appendTask(toAppend);
                }
            }

            public boolean isStationery() {
                return isNextStationery();
            }

            protected boolean isNextStationery() {
                return nextTask == null || nextTask.isStationery();
            }

            @Override
            public String toString() {
                return "T:Empty";
            }

            protected Task getNextTask() {
                return nextTask;
            }

            protected void setNextTask(Task nextTask) {
                this.nextTask = nextTask;
            }
        }
    }

    public static TaskProcessor.Task parseTask(String input) {
        /*
        Low level:
        f<cm>                            Forward
        r<grad>                          Rotate on spot
        v<cm> <cm>                       Vector drive (along circle) (x - left/right,y - front/back)
        t<cm> <grad>                     Turn: Circle drive (how far the center is, how far along the circle ride)
        c<+/->                           Calibrate (forward / backward)
        s<+/-><+/->                      Shooting sequence (Facing home / facing opponent, end facing home / end facing opponent)
        o                                Opportunity, shoot while having bad color
        b                                Block motors (stop, but S was taken)
        k<+/->                           (Start / Stop) tool motor

        High level:
        z<type>[<key><value>]...         Brain (out of letters). Creates brain of the given type with the given data.

        Debug:
        a[freq]                          Alert: Beep a frequency
        w<ms>                            Wait given amount of ms
        e<message>                       Prints message to console an to the screen
        i[freq]                          Starts a waiting for press with given beep frequency. If cancelled, the next command is skipped, if present. (Not guaranteed)
        l<file>                          Starts a FileExecutor.execute for given file. No thread safety guaranteed.
         */
        if (input == null || input.isEmpty()) {
            return null;
        } else {
            try {
                switch (input.charAt(0)) {
                    //--------------------------------- LOW LEVEL ONES
                    case 'f': {
                        float forward = Float.parseFloat(input.substring(1));
                        return constructStraightDrive(forward, MotorManager.MAX_SPEED());
                    }
                    case 'r': {
                        float rotateOnSpot = Float.parseFloat(input.substring(1)) * GRAD_TO_RAD;
                        return constructTurnOnSpot(rotateOnSpot);
                    }
                    case 'v': {
                        int spaceIndex = input.indexOf(' ');
                        final float xCm = Float.parseFloat(input.substring(1, spaceIndex));
                        final float yCm = Float.parseFloat(input.substring(spaceIndex + 1));
                        return constructVectorDrive(xCm, yCm);
                    }
                    case 't': {
                        int spaceIndex = input.indexOf(' ');
                        float centerDistance = Float.parseFloat(input.substring(1, spaceIndex));
                        float angleRad = Float.parseFloat(input.substring(spaceIndex + 1)) * GRAD_TO_RAD;
                        return constructCircleDrive(centerDistance, angleRad);
                    }
                    case 'c': {
                        if (input.charAt(1) == '+') {
                            return constructCalibrateForward(4);
                        } else {
                            return constructCalibrateBackward();
                        }
                    }
                    case 'k': {
                        if (input.charAt(1) == '+') {
                            return constructToolMotorStart();
                        } else {
                            return constructToolMotorStop();
                        }
                    }
                    case 'b':
                        return constructBlockMotors();
                    //--------------------------------- HIGH LEVEL ONES
                    case 'z':
                        return constructBrain(input.substring(1));
                    //--------------------------------- DEBUG ONES
                    case 'a':
                        if(input.length() == 1){
                            return new TaskProcessor.Task() {
                                @Override
                                protected void process() {
                                    Sound.playTone((int) (Math.random() * 1200) + 100, 500);
                                }

                                @Override
                                public String toString() {
                                    return "T:RAlert";
                                }
                            };
                        }else{
                            final int freq = Integer.parseInt(input.substring(1));
                            return new TaskProcessor.Task() {
                                @Override
                                protected void process() {
                                    Sound.playTone(freq, 500);
                                }

                                @Override
                                public String toString() {
                                    return "T:Alert " + freq;
                                }
                            };
                        }
                    case 'w':
                        final int delay = Integer.parseInt(input.substring(1));
                        return constructWait(delay);
                    case 'e':
                        final String alert = input.substring(1);
                        return new TaskProcessor.Task() {
                            @Override
                            protected void process() {
                                console.print(alert);
                            }

                            @Override
                            public String toString() {
                                return "T:Echo "+alert;
                            }
                        };
                    case 'i':
                        final int alertDelay = Integer.parseInt(input.substring(1));
                        return constructWaitForPress(alertDelay);
                    case 'l':
                        final String file = input.substring(1);
                        return new TaskProcessor.Task(){
                            @Override
                            protected void process() {
                                FileExecutor.execute(file);
                            }

                            @Override
                            public String toString() {
                                return "T:Load "+file;
                            }
                        };
                    default:
                        error("UNK: " + input);
                        return null;
                }
            } catch (Exception e) {
                console.print("SYN: " + input);
                error(e);
                return null;
            }
        }
    }

    public static TaskProcessor.Task constructWait(final int delay){
        return new TaskProcessor.Task() {
            @Override
            protected void process() {
                Delay.msDelay(delay);
            }

            @Override
            public boolean isStationery() {
                return delay >= 100 && isNextStationery();
            }

            @Override
            public String toString() {
                return "T:Wait " + delay;
            }
        };
    }

    public static TaskProcessor.Task constructWaitForPress(final int alertDelay){
        return new TaskProcessor.Task(){
            @Override
            protected void process() {
                if(!waitForPress(alertDelay)){
                    if(getNextTask() != null){
                        setNextTask(getNextTask().getNextTask());
                    }
                }
            }

            @Override
            public String toString() {
                return "T:WaitForPress";
            }
        };
    }

    public static TaskProcessor.Task constructStraightDrive(final float distanceCM, final float speed) {
        return new TaskProcessor.Task() {
            @Override
            protected void process() {
                MotorManager.move(distanceCM, distanceCM, speed, MotorManager.SMOOTH_ACCELERATION, isNextStationery() ? MotorManager.SMOOTH_ACCELERATION : MotorManager.NO_DECELERATION,isNextStationery());
            }

            @Override
            public boolean isStationery() {
                return false;
            }

            @Override
            public String toString() {
                return "T:Straight " + distanceCM;
            }
        };
    }

    public static TaskProcessor.Task constructTurnOnSpot(final float angleRad) {
        return new TaskProcessor.Task() {
            @Override
            protected void process() {
                MotorManager.turnRad(angleRad, MotorManager.MAX_SPEED(), MotorManager.SMOOTH_ACCELERATION, MotorManager.SMOOTH_ACCELERATION,isNextStationery());
            }

            @Override
            public boolean isStationery() {
                return false;
            }

            @Override
            public String toString() {
                return "T:TurnOnSpot " + angleRad;
            }
        };
    }

    public static TaskProcessor.Task constructVectorDrive(final float xCm, final float yCm) {
        if (xCm == 0) {
            if (yCm != 0) {
                //Driving straight
                return constructStraightDrive(yCm, MotorManager.MAX_SPEED());
            } else {
                return new TaskProcessor.Task();
            }
        } else {
            float distanceSquared = (xCm * xCm + yCm * yCm);
            float radius = distanceSquared / (2f * xCm);
            float angleRad = ((float) (2 * Math.asin(Math.sqrt(distanceSquared) / (2f * radius))));

            return constructCircleDrive(radius, angleRad);
        }
    }

    public static TaskProcessor.Task constructCircleDrive(final float centerDistance, final float angleRad) {
        final float leftMotor = (centerDistance + MotorManager.wheelDistanceCM) * angleRad;
        final float rightMotor = (centerDistance - MotorManager.wheelDistanceCM) * angleRad;

        return new TaskProcessor.Task() {
            @Override
            protected void process() {
                MotorManager.move(leftMotor, rightMotor, MotorManager.MAX_SPEED()*0.5f, MotorManager.MAX_ACCELERATION, isNextStationery() ? MotorManager.SMOOTH_ACCELERATION : MotorManager.NO_DECELERATION,isNextStationery());
            }

            @Override
            public boolean isStationery() {
                return false;
            }

            @Override
            public String toString() {
                return "T:CircleDrive " + centerDistance + ' ' + angleRad;
            }
        };
    }

    public static TaskProcessor.Task constructCalibrateForward(final int remainingTries) {
        final int delayMs = 50;
        final float turnOffset = 0.35f;

        return new TaskProcessor.Task() {
            @Override
            protected void process() {
                MotorManager.go(true, true, MotorManager.MAX_SPEED() / 2, MotorManager.MAX_SPEED() / 2, MotorManager.SMOOTH_ACCELERATION);
                int delaysToTimeout = 3000 / delayMs;
                int delaysToEmergencyTimeout = 1000 / delayMs;
                int delaysWithOnePressedToTimeout = 1000 / delayMs;

                while (!leftClaw.isPressed() || !rightClaw.isPressed()) {
                    //Wait
                    Delay.msDelay(delayMs);
                    delaysToTimeout--;
                    delaysToEmergencyTimeout--;
                    if(leftClaw.isPressed() || rightClaw.isPressed()){
                        delaysWithOnePressedToTimeout--;
                    }
                    if(delaysWithOnePressedToTimeout <= 0 && delaysToEmergencyTimeout <= 0 && (leftClaw.isPressed() || rightClaw.isPressed())){
                        if (remainingTries > 0) {
                            if(leftClaw.isPressed()){
                                TaskProcessor.Task getRidOfBallsProcedure = constructUnstuckerFast/*Classic*/(turnOffset, remainingTries);
                                pushNextTask(getRidOfBallsProcedure);
                            }else{
                                TaskProcessor.Task getRidOfBallsProcedure = constructUnstuckerFast/*Classic*/(-turnOffset, remainingTries);
                                pushNextTask(getRidOfBallsProcedure);
                            }

                        }
                        break;
                    } else if (delaysToTimeout <= 0) {
                        if (remainingTries > 0) {
                            TaskProcessor.Task getRidOfBallsProcedure = constructUnstuckerFast/*Classic*/(-turnOffset, remainingTries);
                            pushNextTask(getRidOfBallsProcedure);
                        }
                        break;
                    }
                }
                if (isNextStationery()) {
                    MotorManager.stop(MotorManager.MAX_ACCELERATION);
                } else {
                    MotorManager.relax();
                }
            }

            @Override
            public boolean isStationery() {
                return false;
            }

            @Override
            public String toString() {
                return "T:CalibrateFwd " + remainingTries;
            }
        };
    } 
    private static TaskProcessor.Task constructUnstuckerClassic(float turnOffset,int remainingTries){
        return constructStraightDrive(-1f, MotorManager.MAX_SPEED()).appendNextTask(constructTurnOnSpot(turnOffset)).appendNextTask(constructTurnOnSpot(-turnOffset*2)).appendNextTask(constructTurnOnSpot(turnOffset)).appendNextTask(constructCalibrateForward(remainingTries - 1));
    }

    private static TaskProcessor.Task constructUnstuckerFast(float turnOffset,int remainingTries){
        return constructStraightDrive(-2,MotorManager.MAX_SPEED()).appendNextTask(constructTurnOnSpot(turnOffset)).appendNextTask(constructWait(300)).appendNextTask(constructCalibrateForward(remainingTries - 1));
    }

    public static TaskProcessor.Task constructCalibrateBackward() {
        return constructStraightDrive(-11, MotorManager.MAX_SPEED() / 4f);
    }

    public static TaskProcessor.Task constructBlockMotors() {
        return new TaskProcessor.Task() {
            @Override
            protected void process() {
                MotorManager.stop(MotorManager.MAX_ACCELERATION);
            }

            @Override
            public boolean isStationery() {
                return true;
            }

            @Override
            public String toString() {
                return "T:BlockMotors";
            }
        };
    }

    public static TaskProcessor.Task constructToolMotorStart(){
        return new TaskProcessor.Task(){
            @Override
            protected void process() {
                startToolMotor();
            }

            @Override
            public String toString() {
                return "T:TM+";
            }
        };
    }

    public static TaskProcessor.Task constructToolMotorStop(){
        return new TaskProcessor.Task(){
            @Override
            protected void process() {
                stopToolMotor();
            }

            @Override
            public String toString() {
                return "T:TM-";
            }
        };
    }

    public static TaskProcessor.Task constructBrain(String input){
        String[] args = input.split("(?<!\\\\) ");
        if(args.length > 0){
            final String type = args[0];
            final HashMap<String, String> data = new HashMap<String, String>();
            String key = "";
            for(int i = 1; i < args.length; i++){
                if(key.isEmpty()){
                    key = args[i];
                }else{
                    data.put(key, args[i]);
                    key = "";
                }
            }
            if(key.isEmpty()){
                return new TaskProcessor.Task(){
                    @Override
                    protected void process() {
                        Brain b = new Brain(type);
                        b.setData(data);
                        RobotStrategy rs = b.getInstance(robotInterface);
                        rs.run();
                    }

                    @Override
                    public String toString() {
                        return "Z:"+type;
                    }
                };
            }
        }

        return new TaskProcessor.Task(){
            @Override
            protected void process() {}

            @Override
            public String toString() {
                return "Z:!";
            }
        };
    }


    public static void error(String error) {
        Sound.buzz();
        System.out.println(error);
        LCD.asyncRefresh();
        console.print(error);
    }

    public static void error(Throwable error) {
        Sound.buzz();
        error.printStackTrace();
        LCD.asyncRefresh();
        error.printStackTrace(console.stream);
    }

    public static class Console extends Thread {

        /*private final long start = System.currentTimeMillis();*/
        private StringBuilder outBuffer = new StringBuilder();
        private boolean connected = false;
        private int sleepTime = 200;
        private boolean shutdown = false;
        private DataInputStream in;
        private DataOutputStream out;

        private PrintStream stream = new PrintStream(new OutputStream() {

            private StringBuilder stringBuilder = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                if (b > 0)
                    stringBuilder.append((char) b);
            }

            @Override
            public void flush() throws IOException {
                print(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        });

        public Console() {
            LCD.setAutoRefresh(false);
            setDaemon(false);
            setName("Console");
            setPriority(Thread.MIN_PRIORITY);
            start();
        }

        @Override
        public void run() {
            display('-', '?', '-');
            NXTConnection connection = USB.waitForConnection();
            connected = true;
            in = connection.openDataInputStream();
            out = connection.openDataOutputStream();
            display('-', '-', '-');

            while (connected) {
                String incomingCache = null;
                try {
                    synchronized (this){
                        try {
                            if (outBuffer.length() == 0) {
                                out.writeShort(0);//A small hack, should work
                            } else {
                                out.writeUTF(outBuffer.toString());
                                outBuffer = new StringBuilder();
                            }
                        } catch (IOException e) {
                            Sound.buzz();
                            Sound.beep();
                        }
                        out.flush();
                    }

                    final String incoming = in.readUTF();

                    incomingCache = incoming;
                    if (!incoming.isEmpty()) {
                        if (incoming.charAt(0) == '-') {
                            if (incoming.length() == 1) {
                                StringBuilder stats = new StringBuilder();
                                stats.append("\nMemory: F").append(Runtime.getRuntime().freeMemory()).append(" T").append(Runtime.getRuntime().totalMemory()).append(' ').append((int) ((((float) Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().totalMemory()) * 100)).append('%');
                                stats.append("\nVoltage:").append(Battery.getVoltage());
                                stats.append("\nThreads\n");
                                for (VM.VMThread thread : VM.getVM().getVMThreads()) {
                                    stats.append(thread.threadId).append(' ').append(thread.getJavaThread().getName()).append(" w:").append(thread.waitingOn).append(" p:").append(thread.priority).append(" d:").append(thread.daemon).append('\n');
                                }
                                stats.append("\nStack\n");
                                TaskProcessor.Task head = TaskProcessor.stackHead;
                                while (head != null) {
                                    stats.append(head.toString()).append('\n');
                                    head = head.nextTask;
                                }
                                console.print(stats.toString());
                            } else if (incoming.charAt(1) == 'q') {//.equals("quit")
                                if (incoming.length() > 2 && incoming.charAt(2) == 'h') {
                                    Sound.beepSequence();
                                    connection.close();
                                    NXT.shutDown();
                                } else if (incoming.length() > 2 && incoming.charAt(2) == 'c') {
                                    connected = false;
                                } else if (incoming.length() > 2 && incoming.charAt(2) == 's') {
                                    TaskProcessor.scheduleExit();
                                } else {
                                    connected = false;
                                    shutdown = true;
                                }
                            } else if (incoming.charAt(1) == 'b') {//beep
                                Sound.beep();//For my own amusement
                            } else if (incoming.charAt(1) == 'k') { //stop tool motor
                                if (incoming.length() > 2 && incoming.charAt(2) == '+') {
                                    toolMotor.backward();
                                } else {
                                    toolMotor.flt(true);
                                }
                            } else if (incoming.charAt(1) == 'p') {
                                if(incoming.length() >= 3 && incoming.charAt(2) == '-'){
                                    virtualEscape = true;
                                }else{
                                    virtualEnter = true;
                                }
                            }
                        } else if (incoming.charAt(0) == '+') {
                            FileExecutor.execute(incoming.substring(1));
                        } else {
                            TaskProcessor.appendHead(parseTask(incoming));
                        }
                    }
                    Delay.msDelay(sleepTime);
                } catch (Exception e) {
                    console.print("Error for: " + incomingCache);
                    error(e);
                }
            }
            display('-', '/', '-');
            connection.close();
            if (shutdown) {
                Sound.beepSequence();
                System.exit(0);
            }
        }

        /*public void printTime(String data){
            print((System.currentTimeMillis()-start)+": "+data);
        }*/

        public void print(String data) {
            if (connected) {
                try {
                    synchronized (this){
                        outBuffer.append(data).append('\n');
                    }
                } catch (Exception ex) {
                    error(ex);
                }
            } else {
                Sound.beep();
            }
        }

    }

    public static void display(char one, char two, char three) {
        LCD.drawChar(one, 7, 4);
        LCD.drawChar(two, 8, 4);
        LCD.drawChar(three, 9, 4);
        LCD.asyncRefresh();
    }

    public static class MotorManager {
        private static final MotorController leftMotor = new MotorController(MotorPort.B);
        private static final MotorController rightMotor = new MotorController(MotorPort.C);
        static{
            leftMotor.setSynchronizedMotor(rightMotor);
            rightMotor.setSynchronizedMotor(leftMotor);
        }

        public static float MAX_SPEED() {
            return MotorController.getMaxSpeed()*0.825f;
        }

        public static final float MAX_ACCELERATION = 9000;
        public static final float SMOOTH_ACCELERATION = 3000;
        public static final float NO_DECELERATION = MAX_ACCELERATION;//Float.POSITIVE_INFINITY;//0;

        public static final float wheelDiameterCM = 4.2f;
        public static final float wheelCircumferenceCM = PI * wheelDiameterCM;
        public static final float wheelDistanceCM = 8f;

        public static void go(boolean leftForward, boolean rightForward, float leftSpeed, float rightSpeed, float acceleration) {
            if (FLIP_DIRECTION) {
                leftForward = !leftForward;
                rightForward = !rightForward;
            }
            leftMotor.setAcceleration(acceleration);
            rightMotor.setAcceleration(acceleration);
            leftMotor.setSpeed(leftSpeed);
            rightMotor.setSpeed(rightSpeed);
            if (leftForward) {
                leftMotor.forward();
            } else {
                leftMotor.backward();
            }
            if (rightForward) {
                rightMotor.forward();
            } else {
                rightMotor.backward();
            }
        }

        public static void turnRad(float angleRad, float speed, float acceleration, float deceleration,boolean hold) {
            move(-wheelDistanceCM * angleRad, wheelDistanceCM * angleRad, speed, acceleration, deceleration, hold);
        }

        /**
         * @param leftCM       cm to move forward with left wheel
         * @param rightCM      cm to move forward with right wheel
         * @param speed        the max speed of any wheel during the movement (will be scaled down for slower moving wheel)
         * @param acceleration the acceleration with which the first part of movement will be performed
         * @param deceleration the acceleration with which the second part of movement will be performed
         * @param hold         whether motors should float after movement
         */
        public static void move(float leftCM, float rightCM, float speed, float acceleration, float deceleration, boolean hold) {
            //console.print("move("+leftCM+','+rightCM+','+speed+','+acceleration+','+deceleration+','+hold+')');
            if (FLIP_DIRECTION) {
                leftCM = -leftCM;
                rightCM = -rightCM;
            }

            if (leftCM < rightCM) {
                rightMotor.setSpeed(speed);
                leftMotor.setSpeed((leftCM / rightCM) * speed);
            } else {
                leftMotor.setSpeed(speed);
                rightMotor.setSpeed((rightCM / leftCM) * speed);
            }
            //console.print("L: "+leftMotor.getSpeed()+" R: "+rightMotor.getSpeed());
            leftMotor.setAcceleration(acceleration);
            rightMotor.setAcceleration(acceleration);
            leftMotor.setDeceleration(deceleration);
            rightMotor.setDeceleration(deceleration);

            int toMoveLeft = (int) (leftCM / wheelCircumferenceCM * 360);
            int toMoveRight = (int) (rightCM / wheelCircumferenceCM * 360);

            leftMotor.rotate(toMoveLeft, hold, true);
            rightMotor.rotate(toMoveRight, hold, true);

            leftMotor.waitComplete();
            rightMotor.waitComplete();
        }

        public static void stop(float deceleration) {
            leftMotor.setAcceleration(deceleration);
            rightMotor.setAcceleration(deceleration);
            leftMotor.stop(true);
            rightMotor.stop(false);
            leftMotor.waitComplete();
        }

        public static void relax() {
            leftMotor.flt(true);
            rightMotor.flt(true);
        }
    }

    public static class FileExecutor extends Thread {
        private String file;

        private FileExecutor(String file) {
            setName("FileExec");
            this.file = file;
        }

        public static void execute(String file) {
            FileExecutor executor = new FileExecutor(file);
            executor.start();
        }

        public void run() {
            FileInputStream input;
            try {
                input = new FileInputStream(new File(file));
                console.print("Reading " + file);
            } catch (FileNotFoundException e) {
                console.print("File does not exist.");
                return;
            }
            boolean keepReading = true;
            StringBuilder lineContent = new StringBuilder();
            boolean commandLine = false;
            while (keepReading) {
                try {
                    int read = input.read();
                    if (read > 0) {
                        char toRead = (char) read;
                        if (toRead == '\n') {
                            commandLine = false;
                            if (lineContent.length() != 0) {
                                TaskProcessor.appendHead(parseTask(lineContent.toString()));
                                lineContent = new StringBuilder();
                            }
                        } else if (!commandLine) {
                            if (lineContent.length() == 0 && toRead == '#') {
                                commandLine = true;
                            } else {
                                lineContent.append(toRead);
                            }
                        }
                    } else {
                        if (lineContent.length() != 0) {
                            TaskProcessor.appendHead(parseTask(lineContent.toString()));
                        }
                        keepReading = false;
                    }
                } catch (IOException e) {
                    keepReading = false;
                }
            }
            try {
                input.close();
            } catch (Exception ignored) {
            }
            console.print("Whole file read.");
        }

    }
}

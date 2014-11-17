package lego.nxt;

import lego.nxt.util.TaskProcessor;
import lejos.nxt.*;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;
import lejos.util.Delay;

import java.io.*;

/**
* Private property.
* User: Darkyen
* Date: 23/10/14
* Time: 19:49
 *
 * This is here just for copy-paste convenience. Don't actually use.
 * @deprecated This only works in conjunction with Driver, witch is deprecated
*/
@Deprecated
public class Console extends Thread {

    private StringBuilder outBuffer = new StringBuilder();
    private boolean connected = false;
    private int sleepTime = 200;
    private boolean shutdown = false;
    private DataInputStream in;
    private DataOutputStream out;

    public Console() {
        LCD.setAutoRefresh(false);
        setDaemon(false);
        setName("Console");
        setPriority(Thread.MIN_PRIORITY);
        start();
    }

    @Override
    public void run() {
        Driver.display('-', '?', '-');
        NXTConnection connection = USB.waitForConnection();
        connected = true;
        in = connection.openDataInputStream();
        out = connection.openDataOutputStream();
        Driver.display('-', '-', '-');

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
                            TaskProcessor.Task head = TaskProcessor.getStackHead();
                            while (head != null) {
                                stats.append(head.toString()).append('\n');
                                head = head.getNextTask();
                            }
                            print(stats.toString());
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
                        }
                    } else if (incoming.charAt(0) == '+') {
                        Driver.FileExecutor.execute(incoming.substring(1));
                    } else {
                        TaskProcessor.appendTask(Driver.parseTask(incoming));
                    }
                }
                Delay.msDelay(sleepTime);
            } catch (Exception e) {
                Driver.console.print("Error for: " + incomingCache);
                Driver.error(e);
            }
        }
        Driver.display('-', '/', '-');
        connection.close();
        if (shutdown) {
            Sound.beepSequence();
            System.exit(0);
        }
    }

    public void print(String data) {
        if (connected) {
            try {
                synchronized (this){
                    outBuffer.append(data).append('\n');
                }
            } catch (Exception ex) {
                Driver.error(ex);
            }
        } else {
            Sound.beep();
        }
    }

}

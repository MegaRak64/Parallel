package net.roguelogic.mods.parallel.internal;

import net.roguelogic.mods.parallel.API.IThreaded;
import net.roguelogic.mods.parallel.API.MCThread;
import net.roguelogic.mods.parallel.internal.swing.ThreadMonitor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class Management{

    public static void init(){
        mainThread = Thread.currentThread();
        monitor = new ThreadMonitor();
        monitor.setResizable(false);
        monitor.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        monitor.pack();
        monitor.setVisible(true);
    }

    // Thread Ticking

    private static Thread mainThread;

    private static ArrayList<SubThread> threadsToExecute = new ArrayList<>();

    private static HashSet<MCThread> fullThreads = new HashSet<>();

    private static int threadsDone = 0;

    private static boolean allThreadsDone = true;

    private static boolean mainWaiting = false;


    public static boolean multithreded = false;

    private static int ticks=0;

    public static boolean done(){
        int threadsDone = 0;

        for (MCThread thread : fullThreads)
            if (thread.getDone())
                threadsDone++;

        for (SubThread thread : threadsToExecute)
            if (thread.getDone())
                threadsDone++;

        return threadsDone==threadsToExecute.size()+fullThreads.size();
    }

    public static void update() {
        checkThreads();
        if (multithreded) {
            ticks++;
            if (!done()) {
                mainWaiting=true;
                while (true)
                    try {
                        Thread.sleep(1);
                        if (done())
                            break;
                    } catch (InterruptedException ignored) {}
            }
            allThreadsDone = false;
            threadsToExecute.forEach(SubThread::update);
        } else
            allToExecute.forEach(IThreaded::update);
        LTT = System.nanoTime() - TT;
        TT = System.nanoTime();
        updateMonitor();
    }

    // Thread Monitoring

    private static ThreadMonitor monitor;

    private static long TT =0;
    private static long LTT = 0;

    static void updateMonitor(){
        switch (threadsToExecute.size()) {
            case 8:
                monitor.jProgressBar9.setValue((int) (threadsToExecute.get(7).getLastTickTime() / 1_000_000));
                monitor.jLabel9.setText(String.valueOf(threadsToExecute.get(7).getLastTickTime() / 1_000_000));
            case 7:
                monitor.jProgressBar8.setValue((int) (threadsToExecute.get(6).getLastTickTime() / 1_000_000));
                monitor.jLabel8.setText(String.valueOf(threadsToExecute.get(6).getLastTickTime() / 1_000_000));
            case 6:
                monitor.jProgressBar7.setValue((int) (threadsToExecute.get(5).getLastTickTime() / 1_000_000));
                monitor.jLabel7.setText(String.valueOf(threadsToExecute.get(5).getLastTickTime() / 1_000_000));
            case 5:
                monitor.jProgressBar6.setValue((int) (threadsToExecute.get(4).getLastTickTime() / 1_000_000));
                monitor.jLabel6.setText(String.valueOf(threadsToExecute.get(4).getLastTickTime() / 1_000_000));
            case 4:
                monitor.jProgressBar5.setValue((int) (threadsToExecute.get(3).getLastTickTime() / 1_000_000));
                monitor.jLabel5.setText(String.valueOf(threadsToExecute.get(3).getLastTickTime() / 1_000_000));
            case 3:
                monitor.jProgressBar4.setValue((int) (threadsToExecute.get(2).getLastTickTime() / 1_000_000));
                monitor.jLabel4.setText(String.valueOf(threadsToExecute.get(2).getLastTickTime() / 1_000_000));
            case 2:
                monitor.jProgressBar3.setValue((int) (threadsToExecute.get(1).getLastTickTime() / 1_000_000));
                monitor.jLabel3.setText(String.valueOf(threadsToExecute.get(1).getLastTickTime() / 1_000_000));
            case 1:
                monitor.jProgressBar2.setValue((int) (threadsToExecute.get(0).getLastTickTime() / 1_000_000));
                monitor.jLabel2.setText(String.valueOf(threadsToExecute.get(0).getLastTickTime() / 1_000_000));
            default:
                monitor.jProgressBar1.setValue((int) (LTT/1_000_000));
                monitor.jLabel1.setText(String.valueOf(LTT/1_000_000));
                monitor.jLabel12.setText(String.valueOf(allToExecute.size()));
                monitor.jLabel10.setText(multithreded ? "True" : "False");

        }
    }

    // Thread execution management

    private static boolean rebalance = true;

    private static HashMap<IThreaded, Long> TimeMap = new HashMap<>();

    private static HashSet<IThreaded> allToExecute = new HashSet<>();

    private static ArrayList<HashSet<IThreaded>> threadExecuting = new ArrayList<>();

    private static int totalThreads;

    private static HashSet<IThreaded> toBeAdded = new HashSet<>();
    private static HashSet<IThreaded> toBeRemoved = new HashSet<>();

    public static void removeThread(HashSet<IThreaded> iThreadedHashSet, SubThread thread) {
        threadExecuting.remove(iThreadedHashSet);
        threadsToExecute.remove(thread);
        forceRebalance();
    }

    private static void forceRebalance(){
        rebalance = true;
    }

    private static void checkThreads(){
        if (rebalance || totalThreads != Runtime.getRuntime().availableProcessors() || checkTimes()>0)
            rebalanceThreads();
        else
            addAndRemove();
        fullThreads.stream().filter(thread -> !thread.isAlive()).forEach(thread -> fullThreads.remove(thread));
    }

    private static int checkTimes(){
        int threadsOverTime =0;
        for (SubThread thread : threadsToExecute) {
            if ((thread.getLastTickTime()/1_000_000)>=50)
            threadsOverTime++;
        }
        return threadsOverTime;
    }

    private static void rebalanceThreads(){

        rebalance=false;

        if (totalThreads == Runtime.getRuntime().availableProcessors() && totalThreads == checkTimes())
            // CPU is maxed, get a better computer.
            return;

        // Somebody changed the number of cores i can use......
        threadExecuting.clear();
        if (totalThreads != Runtime.getRuntime().availableProcessors()){
            threadsToExecute.forEach(SubThread::kill);
            threadsToExecute.clear();

            totalThreads = Runtime.getRuntime().availableProcessors();
            for (int i = 0; i < totalThreads; i++) {
                threadExecuting.add(new HashSet<>());
                threadsToExecute.add(new SubThread(threadExecuting.get(i)).begin());
            }
        }

        ArrayList<IThreaded> sorted = new ArrayList<>();
        IThreaded currentLongest = null;
        long currentLongestTime = 0;

        for (int i = 0; i < allToExecute.size(); i++) {
            for (IThreaded iThreaded : allToExecute) {
                if (TimeMap.get(iThreaded) > currentLongestTime) {
                    currentLongest = iThreaded;
                    currentLongestTime = TimeMap.get(iThreaded);
                }
            }
            sorted.add(currentLongest);
            TimeMap.put(currentLongest, -1L);
        }

        boolean incresing = true;

        boolean secondEndPass = false;

        int currentThreadNum = 0;

        for (IThreaded iThreaded : sorted){
            threadExecuting.get(currentThreadNum).add(iThreaded);

            if (currentThreadNum==totalThreads){
                if (!secondEndPass)
                    secondEndPass = true;
                else{
                    secondEndPass=false;
                    incresing=false;
                }
            }else if (currentThreadNum==0){
                if (!secondEndPass)
                    secondEndPass = true;
                else{
                    secondEndPass=false;
                    incresing=true;
                }
            }

            if (incresing && !secondEndPass)
                currentThreadNum++;
            else
                currentThreadNum--;
        }
    }

    private static int iterator = 0;

    private static void addAndRemove(){
        if (toBeAdded.size()+toBeRemoved.size()>=200){
            rebalanceThreads();
            return;
        }

        toBeRemoved.forEach(iThreaded -> toBeAdded.remove(iThreaded));

        for (IThreaded add : toBeAdded){
            threadExecuting.get(iterator).add(add);
            if (iterator++ >= totalThreads)
                iterator = 0;
        }

    }

    static void setTickTime(IThreaded iThreaded, long time){
        TimeMap.put(iThreaded, time);
    }

    public static void register(IThreaded toRegister){
        if (allToExecute.contains(toRegister))
            return;
        allToExecute.add(toRegister);
        toBeAdded.add(toRegister);
    }

    public static void unregister(IThreaded toUnregister){
        if (!allToExecute.contains(toUnregister))
            return;
        allToExecute.remove(toUnregister);
        toBeRemoved.add(toUnregister);
    }
}

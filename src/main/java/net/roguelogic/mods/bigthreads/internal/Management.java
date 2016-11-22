package net.roguelogic.mods.bigthreads.internal;

import net.roguelogic.mods.bigthreads.API.IThreaded;
import net.roguelogic.mods.bigthreads.API.MCThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class Management {

    public static void init(){
        mainThread = Thread.currentThread();
    }

    // Thread Ticking

    private static Thread mainThread;

    private static ArrayList<SubThread> threadsToExecute = new ArrayList<>();

    private static HashSet<MCThread> fullThreads = new HashSet<>();

    private static int threadsDone = 0;

    private static boolean allThreadsDone = true;

    private static boolean mainWaiting = false;

    public static void done(){
        threadsDone++;
        if (threadsDone == threadsToExecute.size() + fullThreads.size()) {
            allThreadsDone = true;
            if (mainWaiting)
                mainThread.notify();
        }
    }

    public static void update(){
        if (!allThreadsDone){
            mainWaiting = true;
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException ignored) {}
        }
        checkThreads();
        threadsDone=0;
        allThreadsDone = mainWaiting = false;
        threadsToExecute.forEach(SubThread::update);
    }

    // Thread execution management

    private static boolean rebalance = false;

    private static HashMap<IThreaded, Long> TimeMap = new HashMap<>();

    private static HashSet<IThreaded> allToExecute = new HashSet<>();

    private static ArrayList<HashSet<IThreaded>> threadExecuting = new ArrayList<>();

    private static int totalThreads = Runtime.getRuntime().availableProcessors();

    private static HashSet<IThreaded> toBeAdded = new HashSet<>();
    private static HashSet<IThreaded> toBeRemoved = new HashSet<>();

    public static void removeThread(Thread subThread) {
        threadExecuting.remove(subThread);
        forceRebalance();
    }

    static void forceRebalance(){
        rebalance = true;
    }

    static void checkThreads(){
        if (rebalance || totalThreads != Runtime.getRuntime().availableProcessors() || checkTimes()>0)
            rebalanceThreads();
        else
            addAndRemove();
        fullThreads.stream().filter(thread -> !thread.isAlive()).forEach(thread -> fullThreads.remove(thread));
    }

    static int checkTimes(){
        int threadsOverTime =0;
        for (SubThread thread : threadsToExecute) {
            if ((thread.getLastTickTime()/1_000_000)>=50)
            threadsOverTime++;
        }
        return threadsOverTime;
    }

    static void rebalanceThreads(){

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
                threadExecuting.set(i, new HashSet<>());
                threadsToExecute.set(i, new SubThread(threadExecuting.get(i)));
                threadsToExecute.get(i).start();
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

    static int ittarator = 0;

    static void addAndRemove(){
        toBeRemoved.forEach(iThreaded -> toBeAdded.remove(iThreaded));

        for (IThreaded add : toBeAdded){
            threadExecuting.get(ittarator).add(add);
            if (ittarator++ >= totalThreads)
                ittarator=0;
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

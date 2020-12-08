package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    Lock lock = new Lock();
    private int speakerNum;
    private int listenerNum;
    private LinkedList<Integer> words = new LinkedList<Integer>();
    Condition2 listener = new Condition2(lock);
    Condition2 speaker = new Condition2(lock);
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param    word    the integer to transfer.
     */
    public void speak(int word) {
      
        lock.acquire();
        words.add(word);
        if(listenerNum == 0){
            speakerNum++;
            System.out.println("暂时没有收听者，等待收听");
            speaker.sleep();
            listenerNum--;
        }else{
            speakerNum++;
            listener.wake();
            listenerNum--;
        }
        lock.release();

    }
    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */
    public int listen() {
      
        lock.acquire();
        if(speakerNum==0){
            listenerNum++;
            System.out.println("暂时没有说话者，等待说话");
            listener.sleep();
            speakerNum--;
        }else{
            listenerNum++;
            speaker.wake();
            speakerNum--;
        }
        lock.release();
      
        return words.removeLast();
    }

    private static class Speaker implements Runnable {
        private Communicator c;
        Speaker(Communicator c) {
            this.c = c;
        }
        public void run() {
            for (int i = 0; i < 5; ++i) {
                System.out.println("speaker speaking " + i);
                c.speak(i);
                KThread.yield();
            }
        }
    }
    public static void SpeakTest() {
        System.out.println("测试Communicator类：");
        Communicator c = new Communicator();
        new KThread(new Speaker(c)).setName("Speaker").fork();
        for (int i = 0; i < 5; ++i) {
            System.out.println("listener listening " + i);
            int x = c.listen();
            System.out.println("listener listened, word = " + x);
            KThread.yield();
        }
    }
}

package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
    static BoatGrader bg;
    static boolean boatInO;
    static int num_children_O;
    static int num_adults_O;
    static int num_children_M;
    static int num_adults_M;
    static Lock lock;
    static Condition2 children_condition_o;
    static Condition2 children_condition_m;
    static Condition2 alduts_condition_o;
    static boolean gameover;
    static boolean is_pilot;
    static boolean is_adult_go;

    public static void selfTest() {
        BoatGrader b = new BoatGrader();

        //System.out.println("\n ***Testing Boats with only 2 children***");
        //begin(0, 2, b);

	    //System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
  	    //begin(1, 2, b);

  	    //System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
  	    //begin(3, 3, b);

        System.out.println("\n ***Testing Boats with 4 children, 2 adults***");
        begin(2, 4, b);
    }

    public static void begin(int adultsNum, int childrenNum, BoatGrader b) {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

//        Runnable r = new Runnable() {
//            public void run() {
//                SampleItinerary();
//            }
//        };
//        KThread t = new KThread(r);
//        t.setName("Sample Boat Thread");
//        t.fork();

        num_children_O = childrenNum;
        num_adults_O = adultsNum;
        num_adults_M = 0;
        num_children_M = 0;
        boatInO = true;
        lock = new Lock();
        children_condition_o = new Condition2(lock);
        children_condition_m = new Condition2(lock);
        alduts_condition_o = new Condition2(lock);
        gameover = false;
        is_pilot = true;
        is_adult_go = false;
        for(int i = 0;i<adultsNum;i++){
            new KThread(new Runnable(){
                public void run(){
                    AdultItinerary();
                }
            }).fork();
        }
        for(int i = 0;i<childrenNum;i++){
            new KThread(new Runnable(){
                public void run(){
                    ChildItinerary();
                }
            }).fork();
        }
    }

    static void AdultItinerary() {
        bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
        //DO NOT PUT ANYTHING ABOVE THIS LINE.

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
        lock.acquire();
        if(!(is_adult_go&&boatInO)){
            alduts_condition_o.sleep();
        }
        bg.AdultRowToMolokai();
        num_adults_M++;
        boatInO = false;
        num_adults_O--;
        children_condition_m.wake();
        is_adult_go = false;
        lock.release();
    }

    static void ChildItinerary() {
        bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
        //DO NOT PUT ANYTHING ABOVE THIS LINE.

        lock.acquire();
        while(!gameover){
            if(boatInO){
                if(is_adult_go){
                    alduts_condition_o.wake();
                    children_condition_o.sleep();
                }
                if(is_pilot){
                    bg.ChildRowToMolokai();
                    num_children_O--;
                    num_children_M++;
                    is_pilot = false;
                    children_condition_o.wake();
                    children_condition_m.sleep();
                }else{
                    bg.ChildRideToMolokai();
                    boatInO = false;
                    num_children_O--;
                    num_children_M++;
                    is_pilot=true;
                    if(num_adults_O==0&&num_children_O==0){
                        gameover = true;
                    }
                    if(gameover){
                        System.out.println("成功过河！！！");
                        children_condition_o.sleep();
                    }
                    if(num_adults_O!=0&&num_children_O==0){
                        is_adult_go = true;
                    }
                    children_condition_m.wake();
                    children_condition_m.sleep();
                }
            }else{
                bg.ChildRowToOahu();
                boatInO = true;
                num_children_O++;
                num_children_M--;
                if(is_adult_go){
                    alduts_condition_o.wake();
                }else{
                    children_condition_o.wake();
                }
                children_condition_o.sleep();
            }

        }
        lock.release();//释放锁
    }

    static void SampleItinerary() {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.
        System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();
    }

}

package my;

import base.Tester;

public class MyTester extends Tester {
    protected static final int STEP_DELAY = 500;
    public MyTester() throws Exception {
        env = new EventSimulator();
        makeSteps();
    }

    protected int getDelay()
    {
        return STEP_DELAY;
    }

    public static void main(String[] args) throws Exception {
        new MyTester();
    }
}


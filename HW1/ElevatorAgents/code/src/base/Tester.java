package base;

import my.EventSimulator;

public class Tester
{
    public EventSimulator env;

    public Tester() {
    }

    protected void makeSteps() throws InterruptedException {
        while(!env.goalsCompleted())
        {
            env.step();
            try
            {
                Thread.sleep(getDelay());
            } catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }


    @SuppressWarnings("static-method")
    protected int getDelay()
    {
        return 0;
    }
}

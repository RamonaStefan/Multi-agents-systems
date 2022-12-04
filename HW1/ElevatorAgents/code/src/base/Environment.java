package base;

public interface Environment
{
    boolean step() throws InterruptedException;

    @Override
    public String toString();
}

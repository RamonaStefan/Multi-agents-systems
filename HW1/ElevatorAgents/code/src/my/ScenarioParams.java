package my;

public enum ScenarioParams {
    T_FROM(0),
    T_TO(1),
    O_FROM(2),
    O_TO(3),
    D_FROM(4),
    D_TO(5),
    PERIOD(6);

    public int value;

    ScenarioParams(int position) {
        this.value = position;
    }
}

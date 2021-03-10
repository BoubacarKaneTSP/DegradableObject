package factories;

public abstract class counterfactory {

    public counter getcounter() { return createcounter(); }

    protected abstract counter createcounter();
}

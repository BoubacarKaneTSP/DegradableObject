package factories;

public class degradablecounterfactory extends counterfactory{
    @Override
    protected counter createcounter() { return new degradablecounter(); }
}

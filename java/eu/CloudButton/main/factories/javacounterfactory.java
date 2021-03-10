package factories;

public class javacounterfactory extends counterfactory {

    @Override
    protected counter createcounter() { return new javacounter();}
}

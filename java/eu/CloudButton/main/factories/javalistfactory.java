package factories;

public class javalistfactory extends listfactory{

    @Override
    protected javalist createjavalist() {
        return new javalist();
    }
}

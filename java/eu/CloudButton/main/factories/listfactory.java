package factories;

public abstract class listfactory {

    public javalist getjavalist(){
        return createjavalist();
    }

    protected abstract javalist createjavalist();
}

package factories;

public abstract class setfactory {

    public javaset getjavaset(){
        return createjavaset();
    }

    protected abstract javaset createjavaset();

}

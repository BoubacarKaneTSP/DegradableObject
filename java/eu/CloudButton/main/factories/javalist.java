package factories;

import java.util.ArrayList;
import java.util.List;

public class javalist extends list{

    private final List<String> list;

    public javalist() { list = new ArrayList<>(); }

    @Override
    public void append(String s) {
        list.add(s);
    }

    @Override
    public List<String> read() {
        return list;
    }

    @Override
    public void remove(String s) {
        list.remove(s);
    }

    @Override
    public void write(String s) {
        write(s);
    }
}

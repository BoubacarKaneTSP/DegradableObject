package eu.cloudbutton.dobj;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class Snapshot<T> {

    private HashMap<String ,Pair<T, ArrayList<T>>> obj;

    public Snapshot(HashMap<String, Pair<T, ArrayList<T>>> obj) {
        this.obj = obj;
    }


    private ArrayList<T> read(){

        ArrayList<T> read = new ArrayList<>();

        for (Pair<T, ArrayList<T>> p: obj.values()) {
            read.add(p.getValue0());
        }

        return read;
    }

    private ArrayList<T> snap(String process){
        
        ArrayList<T> read1, read2, read3, read4, embedded;
        
        read1 = read2 = read3 = read4 = null;
        embedded = null;
        int flag = 0;

        while ( flag == 0){

            read1 = read2;
            read2 = read3;
            read3 = read4;
            read4 = read();

            if ( !read1.equals(read2) && !read1.equals(read3) && !read1.equals(read4) &&
                 !read2.equals(read3) && !read2.equals(read4) &&
                 !read3.equals(read4)){
                
                flag = 2;
                embedded = read3;
            }
            
            if(read3.equals(read4))
                flag = 1;
        }
        
        if(flag == 1)
            return read4;
        else // flag == 2
            return embedded;
    }
}

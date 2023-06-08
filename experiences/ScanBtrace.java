import org.openjdk.btrace.core.annotations.BTrace;
import org.openjdk.btrace.core.annotations.Export;
import org.openjdk.btrace.core.annotations.Location;
import org.openjdk.btrace.core.annotations.OnMethod;
import org.openjdk.btrace.core.annotations.OnTimer;
import org.openjdk.btrace.core.annotations.ProbeClassName;
import org.openjdk.btrace.core.annotations.ProbeMethodName;
import org.openjdk.btrace.core.annotations.Property;
import org.openjdk.btrace.core.annotations.Self;
import org.openjdk.btrace.core.annotations.Duration;
import org.openjdk.btrace.core.annotations.Kind;
import org.openjdk.btrace.core.BTraceUtils.Aggregations;
import org.openjdk.btrace.core.aggregation.Aggregation;
import org.openjdk.btrace.core.aggregation.AggregationFunction;
import static org.openjdk.btrace.core.BTraceUtils.*;

@BTrace
public class ScanBtrace {

    private static Aggregation addMethodDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation followMethodDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation unfollowMethodDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation tweetMethodDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation readMethodDuration = Aggregations.newAggregation(AggregationFunction.AVERAGE);

    @Export
    private static long addMethodCalls;
    @Export
    private static long followMethodCalls;
    @Export
    private static long unfollowMethodCalls;
    @Export
    private static long tweetMethodCalls;
    @Export
    private static long readMethodCalls;

    @OnMethod(clazz = "eu.cloudbutton.dobj.benchmark.Database", method = "addUser", location = @Location(Kind.RETURN))
    public static void onAddMethod(@Duration long duration) {
        Aggregations.addToAggregation(addMethodDuration, duration / 1000000);
        addMethodCalls++;
    }

    @OnMethod(clazz = "eu.cloudbutton.dobj.benchmark.Database", method = "followUser", location = @Location(Kind.RETURN))
    public static void onFollowMethod(@Duration long duration) {
        Aggregations.addToAggregation(followMethodDuration, duration / 1000000);
        followMethodCalls++;
    }

    @OnMethod(clazz = "eu.cloudbutton.dobj.benchmark.Database", method = "unfollowUser", location = @Location(Kind.RETURN))
    public static void onUnfollowMethod(@Duration long duration) {
        Aggregations.addToAggregation(unfollowMethodDuration, duration / 1000000);
        unfollowMethodCalls++;
    }

    @OnMethod(clazz = "eu.cloudbutton.dobj.benchmark.Database", method = "tweet", location = @Location(Kind.RETURN))
    public static void onTweetMethod(@Duration long duration) {
        Aggregations.addToAggregation(tweetMethodDuration, duration / 1000000);
        tweetMethodCalls++;
    }

    @OnMethod(clazz = "eu.cloudbutton.dobj.benchmark.Database", method = "showTimeline", location = @Location(Kind.RETURN))
    public static void onReadMethod(@Duration long duration) {
        Aggregations.addToAggregation(readMethodDuration, duration / 1000000);
        readMethodCalls++;
    }

    @OnTimer(10000)
    public static void printMethodThroughput() {
        println("Débit de la méthode add : " + (addMethodCalls / 10) + " appels/s");
        println("Débit de la méthode follow : " + (followMethodCalls / 10) + " appels/s");
        println("Débit de la méthode unfollow : " + (unfollowMethodCalls / 10) + " appels/s");
        println("Débit de la méthode tweet : " + (tweetMethodCalls / 10) + " appels/s");
        println("Débit de la méthode read : " + (readMethodCalls / 10) + " appels/s");
    }
}

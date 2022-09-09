package art.comacreates.sevents;

import art.comacreates.sevents.seq.SeqChannel;

public class Main {

    public static void main(String[] args) {
        EventDispatcher dispatcher = Event.init(EventDispatcher.builder()
            .withChannelSupplier(() -> new SeqChannel())
            .withEnvironment(Environment.SEQUENTIAL)
            .withExceptionHandler((e) -> e.printStackTrace())
            .build());
        Event.Valued<String> test = Event.ROOT.valuedChild("test");
        Event.Valued<String> testChild = test.valuedChild("testChild");
        EventChannel channel = dispatcher.newChannel();
        channel.listenTo(Event.ROOT, () -> System.out.println("Root listeners called"));
        channel.listenTo(testChild, () -> System.out.println("This should be called first"));
        channel.listenTo(testChild, () -> System.out.println("This should be called second"));
        channel.listenTo(test, (e, v) -> System.out.println(e.name() + " called with value " + v));

        testChild.dispatch("testValue");
    }
    
}

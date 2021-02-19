import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.streaming.api.windowing.time.Time;
public class WordCount {
    public static void main(String[] args)throws Exception {
        int port;
        try{
            ParameterTool params = ParameterTool.fromArgs(args);
            port = params.getInt("port");

        }catch (Exception e){
            System.err.println("没有指定的port参数，使用默认值10000");
            port = 10000;
        }

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> text = env.socketTextStream("10.10.10.100", port, "\n");

        DataStream<WordWithCount> windowCount = text.flatMap(new FlatMapFunction<String, WordWithCount>() {
            public void flatMap(String value, Collector<WordWithCount> out) throws Exception {
                String[] splits = value.split("\\s");
                for (String word:splits) {
                    out.collect(new WordWithCount(word,1L));
                }
            }
        }).keyBy("word")
                .timeWindow(Time.seconds(2), Time.seconds(1))
                .sum("count");

        windowCount.print().setParallelism(1);
        env.execute("Streaming word count");
    }

    public static class WordWithCount{
        public String word;
        public long count;
        public WordWithCount(){}
        public WordWithCount(String word, long count){
            this.word = word;
            this.count = count;

        }
        @Override
        public String toString() {
            return "WordWithCount{" +
                    "word='" + word + '\'' +
                    ",count=" + count +
                    '}';
        }
    }
}


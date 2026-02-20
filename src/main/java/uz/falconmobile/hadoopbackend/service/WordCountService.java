package uz.falconmobile.hadoopbackend.service;

//import uz.falconmobile.hadoopbackend.JobResult;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.falconmobile.hadoopbackend.model.JobResult;

import java.io.*;
import java.util.*;

@Service
public class WordCountService {

    @Autowired
    private Configuration hadoopConfiguration;

    @Autowired
    private FileSystem fileSystem;

    // WordCount ishini boshlash
    public JobResult runWordCount(String inputPath, String outputPath) throws Exception {
        long startTime = System.currentTimeMillis();

        // Output papkasini tozalash
        Path outPath = new Path(outputPath);
        if (fileSystem.exists(outPath)) {
            fileSystem.delete(outPath, true);
        }

        // Job sozlash
        Job job = Job.getInstance(hadoopConfiguration, "word-count-" + System.currentTimeMillis());
        job.setJarByClass(WordCountService.class);

        job.setMapperClass(WordCountMapper.class);
        job.setCombinerClass(WordCountReducer.class);
        job.setReducerClass(WordCountReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        // Ishni bajarish
        boolean success = job.waitForCompletion(true);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Natijalarni o'qish
        Map<String, Integer> results = new HashMap<>();
        if (success) {
            results = readResults(outputPath + "/part-r-00000");
        }

        JobResult jobResult = new JobResult();
        jobResult.setJobId(job.getJobID().toString());
        jobResult.setStatus(success ? "SUCCESS" : "FAILED");
        jobResult.setInputPath(inputPath);
        jobResult.setOutputPath(outputPath);
        jobResult.setExecutionTime(executionTime);
        jobResult.setResults(results);

        return jobResult;
    }

    // Natijalarni o'qish
    private Map<String, Integer> readResults(String outputFile) throws IOException {
        Map<String, Integer> results = new LinkedHashMap<>();
        Path path = new Path(outputFile);

        if (!fileSystem.exists(path)) {
            return results;
        }

        try (FSDataInputStream inputStream = fileSystem.open(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            int count = 0;

            // Faqat eng ko'p uchraydigan 50 ta so'zni olish
            while ((line = reader.readLine()) != null && count < 50) {
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    results.put(parts[0], Integer.parseInt(parts[1]));
                    count++;
                }
            }
        }

        return results;
    }

    // Mapper sinfi
    public static class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // So'zlarni ajratish (lotin va kirill harflari, raqamlar)
            String[] words = line.split("[\\s\\p{Punct}]+");

            for (String w : words) {
                if (w.length() > 0) {
                    word.set(w.toLowerCase());
                    context.write(word, one);
                }
            }
        }
    }

    // Reducer sinfi
    public static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }

            context.write(key, new IntWritable(sum));
        }
    }
}
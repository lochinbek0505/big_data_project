package uz.falconmobile.hadoopbackend.config;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class HadoopConfig {

    @Bean
    public Configuration hadoopConfiguration() {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000");
        conf.set("hadoop.tmp.dir", "/tmp/hadoop-temp");
        conf.set("dfs.client.use.datanode.hostname", "true");
        return conf;
    }

    @Bean
    public FileSystem fileSystem(Configuration conf) {
        try {
            return FileSystem.get(new URI("hdfs://localhost:9000"), conf);
        } catch (Exception e) {
            throw new RuntimeException("HDFS ga ulanishda xatolik: " + e.getMessage());
        }
    }
}
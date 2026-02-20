package uz.falconmobile.hadoopbackend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobResult {
    private String jobId;
    private String status;
    private String inputPath;
    private String outputPath;
    private long executionTime;
    private Map<String, Integer> results;
}

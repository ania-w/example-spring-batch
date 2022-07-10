package service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import request.JobParamsRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobService {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("first")
    Job firstJob;

    @Autowired
    @Qualifier("second")
    Job secondJob;

    @Async
    public void startJob(String jobName, List<JobParamsRequest> jobParamsRequestList)
    {
        Map<String, JobParameter> params=new HashMap<>();
        params.put("currentTime",new JobParameter(System.currentTimeMillis()));

        jobParamsRequestList.stream().forEach(jobParamsRequest -> {
            params.put(jobParamsRequest.getParamKey(),
                    new JobParameter(jobParamsRequest.getParamValue()));
        });

        JobParameters jobParameters=new JobParameters(params);
try {
    JobExecution jobExecution=null;
    if (jobName.equals("First Job"))
        jobLauncher.run(firstJob, jobParameters);
    if (jobName.equals("Second Job"))
        jobLauncher.run(secondJob, jobParameters);
    System.out.println("job execution id: "+jobExecution.getId());
} catch (Exception e){
    System.out.println("Exception while starting job");
}
    }

}

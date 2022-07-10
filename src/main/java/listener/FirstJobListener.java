package listener;


import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FirstJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("Before: "+jobExecution.getJobInstance().getJobName());
        System.out.println("Params: "+jobExecution.getJobParameters());
        System.out.println("Context: "+jobExecution.getExecutionContext());

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("After: "+jobExecution.getJobInstance().getJobName());
        System.out.println("Params: "+jobExecution.getJobParameters());
        System.out.println("Context: "+jobExecution.getExecutionContext());

    }
}

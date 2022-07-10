package listener;


import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

@Component
public class FirstStepListener implements StepExecutionListener {


    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Before step: "+stepExecution.getStepName());
        System.out.println("Context: "+stepExecution.getExecutionContext());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("Before step: "+stepExecution.getStepName());
        System.out.println("Context: "+stepExecution.getExecutionContext());
        return stepExecution.getExitStatus();
    }
}

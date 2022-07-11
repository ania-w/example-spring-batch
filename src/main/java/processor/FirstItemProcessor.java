package processor;

import model.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FirstItemProcessor implements ItemProcessor<Student, StudentSQL> {
    @Override
    public StudentSQL process(Student item) throws Exception {

        StudentSQL student=new StudentSQL();

        student.setId(item.getId());
        student.setFirstName(item.getFirstName());
        student.setLastName(item.getLastName());
        student.setEmail(item.getEmail());
        student.setIsActive(item.getIsActive() != null ?
                Boolean.valueOf(item.getIsActive()) : false);

        return student;
    }
}

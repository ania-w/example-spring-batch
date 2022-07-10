package writer;


import model.*;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirstItemWriter implements ItemWriter<StudentJdbc> {
    @Override
    public void write(List<? extends StudentJdbc> list) throws Exception {
        System.out.println("Item Writer");
        list.stream().forEach(System.out::println);

    }
}

package listener;

import model.StudentCsv;
import model.StudentJson;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.FileWriter;

@Component
public class SkipListenerImpl implements SkipListener<StudentCsv, StudentJson> {
    @Override
    public void onSkipInRead(Throwable throwable) {
        if(throwable instanceof FlatFileParseException){
            createFile("C:\\Users\\Ania\\Desktop\\spring_batch_h2\\ChunkJob\\SecondJob\\Reader\\SkipInRead.txt"
                    ,((FlatFileParseException)throwable).getInput());
        }
    }

    @Override
    public void onSkipInWrite(StudentJson studentJson, Throwable throwable) {
        createFile("C:\\Users\\Ania\\Desktop\\spring_batch_h2\\ChunkJob\\SecondJob\\Processor\\SkipInProcess.txt"
                ,studentJson.toString());
    }

    @Override
    public void onSkipInProcess(StudentCsv studentCsv, Throwable throwable) {
        if(throwable instanceof NullPointerException){
            createFile("C:\\Users\\Ania\\Desktop\\spring_batch_h2\\ChunkJob\\SecondJob\\Processor\\SkipInProcess.txt"
                    ,studentCsv.toString());
        }
    }


    public void createFile(String path, String data){
        try(FileWriter fileWriter=new FileWriter(path,true)){
            fileWriter.write(data+"\n");
        } catch (Exception e){

        }
    }
}

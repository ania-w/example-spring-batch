package listener;

import model.StudentCsv;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.FileWriter;

@Component
public class SkipListener {

    @OnSkipInRead
    public void skipInRead(Throwable th){
        if(th instanceof FlatFileParseException){
            createFile("C:\\Users\\Ania\\Desktop\\spring_batch_h2\\ChunkJob\\SecondJob\\Reader\\SkipInRead.txt"
                    ,((FlatFileParseException)th).getInput());
        }
    }

    @OnSkipInProcess
    public void skipInRead(StudentCsv studentCsv,Throwable th){
        if(th instanceof NullPointerException){
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

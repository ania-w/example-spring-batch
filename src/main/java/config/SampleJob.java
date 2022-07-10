package config;

import listener.FirstJobListener;
import listener.FirstStepListener;
import listener.SkipListener;
import listener.SkipListenerImpl;
import model.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import processor.FirstItemProcessor;
import reader.FirstItemReader;
import service.StudentService;
import writer.FirstItemWriter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;

@Configuration
public class SampleJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("second")
    private Tasklet secondTask;

    @Autowired
    @Qualifier("first")
    private Tasklet firstTask;

    @Autowired
    private FirstJobListener firstJobListener;

    @Autowired
    private FirstStepListener firstStepListener;

    @Autowired
    private FirstItemReader reader;

    @Autowired
    private FirstItemProcessor processor;

    @Autowired
    private FirstItemWriter writer;

    @Autowired
    @Qualifier("datasource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("universityDatasource")
    private DataSource universityDataSource;


    @Autowired
    private StudentService studentService;

    @Autowired
    private SkipListener skipListener;

    @Autowired
    private SkipListenerImpl skipListenerImpl;

    @Autowired
    @Qualifier("postgresEntityManagerFactory")
    private EntityManagerFactory postgresEMF;

    @Autowired
    @Qualifier("sqlEntityManagerFactory")
    private EntityManagerFactory sqlEMF;

    @Autowired
    private JpaTransactionManager jpaTransactionManager;

    @Bean
    @Qualifier("first")
    public Job firstJob()
    {
        return jobBuilderFactory.get("First Job")
                .incrementer(new RunIdIncrementer())
                .start(firstStep())
                .next(secondStep())
                .listener(firstJobListener)
                .build();

    }

    @Bean
    @Qualifier("second")
    public Job secondJob()
    {
        return jobBuilderFactory.get("Second Job")
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .listener(firstJobListener)
                .build();

    }


    private Step firstChunkStep(){
        return stepBuilderFactory.get("First Chunk Step")
                //.<StudentCsv,StudentCsv>chunk(3)
                //.<StudentJson,StudentJson>chunk(3)
                //.<StudentXML,StudentXML>chunk(3)
                .transactionManager(jpaTransactionManager)
                .<Student,StudentSQL>chunk(5)
                //.<StudentResponse,StudentResponse>chunk(4)
                // reader(itemReaderAdapter())
                .reader(jpaCursorItemReader())
                //.reader(xmlItemReader(null))
                //.reader(jsonItemReader(null))
                //.reader(flatFileItemReader(null))
                .processor(processor)
                .writer(jpaItemWriter())
                //.faultTolerant()
                //.skip(Throwable.class)
                //.skipLimit(100) //skipuje 1 rekord
                //.retryLimit(5)
                //.retry(Throwable.class)
                //.skipPolicy(new AlwaysSkipItemSkipPolicy()) // skipuje zawsze
                //.listener(skipListenerImpl)
                .build();
    }

    private Step firstStep(){
        return stepBuilderFactory.get("First Step")
                .tasklet(firstTask)
                .listener(firstStepListener)
                .build();
    }

    private Step secondStep(){
        return stepBuilderFactory.get("Second Step")
                .tasklet(secondTask)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<StudentCsv> flatFileItemReader(@Value("#{jobParameters['inputFile']}") FileSystemResource file)
    {
        FlatFileItemReader<StudentCsv> flatFileItemReader=
                new FlatFileItemReader<>();

        flatFileItemReader.setResource(file);

        flatFileItemReader.setLineMapper(new DefaultLineMapper<>(){
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("ID","First Name","Last Name","Email");
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<StudentCsv>(){
                    {
                        setTargetType(StudentCsv.class);
                    }
                });
            }
        });

        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

    @StepScope
    @Bean
    public JsonItemReader<StudentJson> jsonItemReader(@Value("#{jobParameters['inputFile']}") FileSystemResource file)
    {
        JsonItemReader<StudentJson> jsonItemReader=
                new JsonItemReader<>();

        jsonItemReader.setResource(file);

        jsonItemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(StudentJson.class));


        // read items from 2 to 8
        jsonItemReader.setMaxItemCount(8);
        jsonItemReader.setCurrentItemCount(2);

        return jsonItemReader;
    }

    @StepScope
    @Bean
    public StaxEventItemReader<StudentXML> xmlItemReader(@Value("#{jobParameters['inputFile']}") FileSystemResource file)
    {
        StaxEventItemReader<StudentXML> xmlItemReader=
                new StaxEventItemReader<>();

        xmlItemReader.setResource(file);

        xmlItemReader.setFragmentRootElementName("student");
        xmlItemReader.setUnmarshaller(new Jaxb2Marshaller(){
            {
                setClassesToBeBound(StudentXML.class);
            }
        });

        return xmlItemReader;
    }

    public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader(){
        JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader=
                new JdbcCursorItemReader<>();

        jdbcCursorItemReader.setDataSource(universityDataSource);
        jdbcCursorItemReader.setSql(
                "select id, first_name as firstName, last_name as lastName, email from student"
        );


        jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<StudentJdbc>(){
            {
                setMappedClass(StudentJdbc.class);
            }
        });

        jdbcCursorItemReader.setCurrentItemCount(2);
        jdbcCursorItemReader.setMaxItemCount(8);
        return jdbcCursorItemReader;
    }

    public ItemReaderAdapter<StudentResponse> itemReaderAdapter() {
        ItemReaderAdapter<StudentResponse> itemReaderAdapter =
                new ItemReaderAdapter<>();

        itemReaderAdapter.setTargetObject(studentService);
        itemReaderAdapter.setTargetMethod("getStudent");

        return itemReaderAdapter;
    }


    @StepScope
    @Bean
    public FlatFileItemWriter<StudentJdbc> csvFlatFileItemWriter(@Value("#{jobParameters['outputFile']}") FileSystemResource file)
    {
        FlatFileItemWriter<StudentJdbc> flatFileItemWriter=new FlatFileItemWriter<>();

        flatFileItemWriter.setResource(file);
        flatFileItemWriter.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("Id,First Name,Last Name,Email");
            }
        });

        flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<StudentJdbc>(){
            {
                setFieldExtractor(new BeanWrapperFieldExtractor<StudentJdbc>(){
                    {
                        setNames(new String[]{"id","firstName","lastName","email"});
                    }
                });
                setDelimiter(";");
            }
        });

        return flatFileItemWriter;
    }

    @StepScope
    @Bean
    public JsonFileItemWriter<StudentJson> jsonItemWriter
            (@Value("#{jobParameters['outputFile']}") FileSystemResource file)
    {
        JsonFileItemWriter<StudentJson> jsonItemWriter
                =new JsonFileItemWriter<>(file,
        new JacksonJsonObjectMarshaller<StudentJson>());

        return jsonItemWriter;
    }

    @StepScope
    @Bean
    public StaxEventItemWriter<StudentJdbc> xmlItemWriter
            (@Value("#{jobParameters['outputFile']}") FileSystemResource file)
    {

        StaxEventItemWriter<StudentJdbc> xmlItemWriter=new StaxEventItemWriter<>();

        xmlItemWriter.setResource(file);
        xmlItemWriter.setRootTagName("student");
        xmlItemWriter.setMarshaller(new Jaxb2Marshaller(){
            {
                setClassesToBeBound(StudentJdbc.class);
            }
        });
        return xmlItemWriter;
    }

    @StepScope
    @Bean
    public JdbcBatchItemWriter<StudentJdbc> jdbcItemWriter()
    {
        JdbcBatchItemWriter<StudentJdbc> writer=new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);

        writer.setSql(
                "insert into student(id, first_name, last_name, email)"
                +"values(:id,:firstName,:lastName,:email)"
        );

        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<StudentJdbc>());

        return writer;
    }

    @StepScope
    @Bean
    public ItemWriterAdapter<StudentResponse> restItemWriter()
    {
       ItemWriterAdapter<StudentResponse> itemWriterAdapter=
               new ItemWriterAdapter<>();

       itemWriterAdapter.setTargetObject(studentService);
       itemWriterAdapter.setTargetMethod("restCallToCreateStudent");

        return itemWriterAdapter;
    }

    public JpaCursorItemReader<Student> jpaCursorItemReader(){
        JpaCursorItemReader<Student>  jpaCursorItemReader=
                new JpaCursorItemReader<>();

        jpaCursorItemReader.setEntityManagerFactory(postgresEMF);
        jpaCursorItemReader.setQueryString("From Student");

        return jpaCursorItemReader;
    }

    public JpaItemWriter<StudentSQL> jpaItemWriter() {
        JpaItemWriter<StudentSQL> jpaItemWriter =
                new JpaItemWriter<StudentSQL>();

        jpaItemWriter.setEntityManagerFactory(sqlEMF);

        return jpaItemWriter;
    }


}

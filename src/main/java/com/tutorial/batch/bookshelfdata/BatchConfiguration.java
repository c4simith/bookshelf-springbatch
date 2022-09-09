package com.tutorial.batch.bookshelfdata;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Value("${file.input}")
	private String inputFile;
	
	@Bean
	public FlatFileItemReader<Book> reader(){
		return new FlatFileItemReaderBuilder<Book>()
				.name("bookItemReader")
				.resource(new PathResource(inputFile))
				.delimited()
				.names(new String[] {"name","author", "genre"})
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Book>() {{
					setTargetType(Book.class);
				}})
				.build();
	}
	
	@Bean
    public BookItemProcessor processor() {
        return new BookItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Book> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Book>().itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO books (name, author, genre) VALUES (:name, :author, :genre)")
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public Job importUserJob( Step step1) {
        return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .flow(step1)
            .end()
            .build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<Book> writer) {
        return stepBuilderFactory.get("step1")
            .<Book, Book> chunk(10)
            .reader(reader())
            .processor((ItemProcessor<? super Book, ? extends Book>) processor())
            .writer(writer)
            .build();
    }
}

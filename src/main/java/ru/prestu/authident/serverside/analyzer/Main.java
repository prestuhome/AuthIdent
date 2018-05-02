package ru.prestu.authident.serverside.analyzer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import ru.prestu.authident.domain.model.entities.Book;
import ru.prestu.authident.domain.repositories.BookRepository;

import java.io.IOException;

@ComponentScan(basePackages = "ru.prestu.authident")
public class Main {

    private BookRepository repository;

    @Autowired
    public Main(BookRepository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        Main main = context.getBean(Main.class);

        main.start("C:\\tools\\idiot.txt", "Идиот");
    }

    public void start(String path, String name) throws IOException {
        Book book = new Book();
        TextAnalyzer analyzer = new TextAnalyzer();
        book.setBookInfo(analyzer.analyze(path));
        book.setName(name);
        repository.save(book);
    }
}

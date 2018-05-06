package ru.prestu.authident.web;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.springframework.beans.factory.annotation.Autowired;
import ru.prestu.authident.domain.model.entities.Author;
import ru.prestu.authident.domain.model.entities.Book;
import ru.prestu.authident.domain.repositories.AuthorRepository;
import ru.prestu.authident.domain.repositories.BookRepository;
import ru.prestu.authident.serverside.analyzer.TextAnalyzer;
import ru.prestu.authident.web.components.AuthorEditor;
import ru.prestu.authident.web.components.ClusterVisualizator;

import java.io.*;
import java.util.List;

@SpringUI
@Theme("authident")
@Title("AuthIdent")
public class VaadinUI extends UI {

    private final Button hiderForPreparingSpace = new Button();
    private final VerticalLayout preparingSpace = new VerticalLayout();
    private final Upload fileUploader = new Upload();
    private final TextField bookNameField = new TextField();
    private final ComboBox<Author> authorSelector = new ComboBox<>();
    private final Button addNewAuthorButton = new Button();
    private final Button startButton = new Button();
    private final Label fileIsReadyLabel = new Label();
    private final AuthorEditor authorEditor;

    private final Button hiderForResultSpace = new Button();
    private final VerticalLayout resultSpace = new VerticalLayout();
    private final ClusterVisualizator clusterVisualizator;


    private String fileName = "";

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public VaadinUI(AuthorRepository authorRepository, ClusterVisualizator clusterVisualizator, BookRepository bookRepository, AuthorEditor authorEditor) {
        this.authorEditor = authorEditor;
        this.clusterVisualizator = clusterVisualizator;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);

        hiderForPreparingSpace.setCaption("Подготовка");
        hiderForPreparingSpace.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        hiderForPreparingSpace.addStyleName("hider");
        hiderForPreparingSpace.setWidth(100, Unit.PERCENTAGE);
        hiderForPreparingSpace.setHeight(50 , Unit.PIXELS);
        hiderForPreparingSpace.addClickListener(clickEvent -> preparingSpace.setVisible(!preparingSpace.isVisible()));
        mainLayout.addComponent(hiderForPreparingSpace);

        preparingSpace.setMargin(true);
        preparingSpace.setSpacing(true);

        UploadReceiver receiver = new UploadReceiver();
        fileUploader.setReceiver(receiver);
        fileUploader.setErrorHandler((ErrorHandler) errorEvent -> {
            // nothing to do
        });
        fileUploader.setImmediateMode(false);
        fileUploader.setButtonCaption("Загрузите текстовый файл");
        fileUploader.addStartedListener(event -> {
            clusterVisualizator.setVisible(false);
            if (event.getContentLength() == 0) {
                Notifications.show("Выберите файл", "Для начала загрузки выберите текстовый файл в формате txt (кодировка UTF-8)", Notifications.SMALL_WINDOW);
                fileName = "";
            } else if (!event.getMIMEType().equals("text/plain")) {
                Notifications.show("Неверный формат файла", "Выберите текстовый файл в формате txt (кодировка UTF-8)", Notifications.SMALL_WINDOW);
                fileName = "";
            }
        });
        fileUploader.addFinishedListener(event -> update());
        preparingSpace.addComponent(fileUploader);

        HorizontalLayout bookFields = new HorizontalLayout();
        bookFields.setSpacing(true);
        bookFields.setMargin(false);
        bookNameField.setCaption("Название книги");
        authorSelector.setCaption("Автор");
        authorSelector.setItemCaptionGenerator(Author::toString);
        bookFields.addComponent(bookNameField);
        bookFields.addComponent(authorSelector);
        preparingSpace.addComponent(bookFields);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        addNewAuthorButton.setCaption("Добавить автора");
        addNewAuthorButton.setIcon(VaadinIcons.PLUS);
        startButton.setCaption("Старт");
        startButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        addNewAuthorButton.addClickListener((Button.ClickListener) event -> authorEditor.editAuthor(new Author()));
        startButton.addClickListener(event -> {
            if (!fileName.isEmpty()) {
                saveBook();
            }
            List<? extends Cluster<Book>> clusters = clusterBooks();
            if (clusters != null && !clusters.isEmpty()) clusterVisualizator.visualize(clusters);
            fileName = "";
            fileIsReadyLabel.setValue("");
        });

        buttonsLayout.addComponent(addNewAuthorButton);
        buttonsLayout.addComponent(startButton);
        buttonsLayout.addComponent(fileIsReadyLabel);
        preparingSpace.addComponent(buttonsLayout);
        preparingSpace.addComponent(authorEditor);
        mainLayout.addComponent(preparingSpace);

        hiderForResultSpace.setCaption("Результат");
        hiderForResultSpace.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        hiderForResultSpace.addStyleName("hider");
        hiderForResultSpace.setWidth(100, Unit.PERCENTAGE);
        hiderForResultSpace.setHeight(50 , Unit.PIXELS);
        hiderForResultSpace.addClickListener(clickEvent -> resultSpace.setVisible(!resultSpace.isVisible()));
        mainLayout.addComponent(hiderForResultSpace);

        resultSpace.setMargin(true);
        resultSpace.setSpacing(true);
        resultSpace.addComponent(clusterVisualizator);
        mainLayout.addComponent(resultSpace);

        setContent(mainLayout);
        listAuthors();
    }

    private List<? extends Cluster<Book>> clusterBooks() {
        List<Author> authors = authorRepository.findAll();
        List<Book> books = bookRepository.findAll();
        if (authors.size() == 0 || books.size() < authors.size()) {
            Notifications.show("Ошибка кластеризации", "Слишком мало входных параметров для кластеризации", Notifications.SMALL_WINDOW);
            return null;
        }
        KMeansPlusPlusClusterer<Book> clusterer = new KMeansPlusPlusClusterer<>(authors.size());
        return clusterer.cluster(books);
    }

    private void saveBook() {
        Book book = new Book();
        TextAnalyzer analyzer = new TextAnalyzer();
        try {
            book.setBookInfo(analyzer.analyze("C:\\Users\\prest\\tmp\\" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        book.setAuthor(authorSelector.getValue());
        book.setName(bookNameField.getValue());
        bookRepository.save(book);
    }

    public void listAuthors() {
        List<Author> authors = authorRepository.findAll();
        authorSelector.setItems(authors);
    }

    private void update() {
        fileIsReadyLabel.setValue("Файл " + fileName + " готов к обработке");
    }

    private class UploadReceiver implements Upload.Receiver {
        private static final long serialVersionUID = 2215337036540966711L;
        OutputStream outputFile = null;
        @Override
        public OutputStream receiveUpload(String fileName, String MIMEType) {
            File file;
            try {
                String path = "C:\\Users\\prest\\tmp\\" + fileName;
                file = new File(path);
                if(!file.exists()) {
                    file.createNewFile();
                }
                outputFile =  new FileOutputStream(file);
                VaadinUI.this.fileName = (fileName.isEmpty() || !MIMEType.equals("text/plain")) ? "" : fileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outputFile;
        }

        protected void finalize() {
            try {
                super.finalize();
                if(outputFile != null) {
                    outputFile.close();
                }
            } catch (Throwable exception) {
                exception.printStackTrace();
            }
        }
    }
}

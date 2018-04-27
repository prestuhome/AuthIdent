package ru.prestu.authident.web;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import ru.prestu.authident.domain.model.entities.Author;
import ru.prestu.authident.domain.repositories.AuthorRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringUI
public class VaadinUI extends UI {

    private final Upload fileUploader;
    private String fileName = "";

    private final TextField bookNameField;
    private final ComboBox<Author> authorSelector;

    private final VerticalLayout workingSpace;
    private final Button addNewAuthorButton;
    private final Button startButton;
    private final Label fileIsReadyLabel;
    private final AuthorEditor authorEditor;

    private final Label resultLabel;

    private final AuthorRepository authorRepository;

    @Autowired
    public VaadinUI(AuthorRepository authorRepository, AuthorEditor authorEditor) {
        fileUploader = new Upload();
        workingSpace = new VerticalLayout();
        bookNameField = new TextField("Название книги");
        authorSelector = new ComboBox<>("Автор");
        authorSelector.setItemCaptionGenerator(Author::toString);
        addNewAuthorButton = new Button("Добавить автора", VaadinIcons.PLUS);
        startButton = new Button("Старт");
        startButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        fileIsReadyLabel = new Label();
        resultLabel = new Label();
        this.authorEditor = authorEditor;
        this.authorRepository = authorRepository;
    }

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout mainLayout = new VerticalLayout();
        HorizontalLayout bookFields = new HorizontalLayout();
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        bookFields.setSpacing(true);
        buttonsLayout.setSpacing(true);
        workingSpace.setSpacing(true);
        mainLayout.setSpacing(true);

        UploadReceiver receiver = new UploadReceiver();
        fileUploader.setReceiver(receiver);
        fileUploader.setErrorHandler((ErrorHandler) errorEvent -> {
            // nothing to do
        });
        fileUploader.setImmediateMode(false);
        fileUploader.setButtonCaption("Загрузите текстовый файл");
        fileUploader.addStartedListener(event -> {
            if (event.getContentLength() == 0) {
                Notifications.show("Выберите файл", "Для начала загрузки выберите текстовый файл в формате txt (кодировка UTF-8)", Notifications.SMALL_WINDOW);
                fileName = "";
            } else if (!event.getMIMEType().equals("text/plain")) {
                Notifications.show("Неверный формат файла", "Выберите текстовый файл в формате txt (кодировка UTF-8)", Notifications.SMALL_WINDOW);
                fileName = "";
            }
        });
        fileUploader.addFinishedListener(event -> update());
        bookFields.addComponent(bookNameField);
        bookFields.addComponent(authorSelector);
        workingSpace.addComponent(bookFields);

        addNewAuthorButton.addClickListener((Button.ClickListener) event -> authorEditor.editAuthor(new Author()));
        startButton.addClickListener(event -> {
            try {
                resultLabel.setValue(Files.lines(Paths.get("C:\\Users\\prest\\tmp\\" + fileName), StandardCharsets.UTF_8).findFirst().get());
            } catch (UncheckedIOException e) {
                if (e.getMessage().equals("java.nio.charset.MalformedInputException: Input length = 1")) {
                    Notifications.show("Неверная кодировка", "Выберите текстовый файл в формате txt (кодировка UTF-8)", Notifications.SMALL_WINDOW);
                    fileName = "";
                    update();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        buttonsLayout.addComponent(addNewAuthorButton);
        buttonsLayout.addComponent(startButton);
        buttonsLayout.addComponent(fileIsReadyLabel);
        workingSpace.addComponent(buttonsLayout);
        workingSpace.addComponent(authorEditor);
        workingSpace.addComponent(resultLabel);

        mainLayout.addComponent(fileUploader);
        mainLayout.addComponent(workingSpace);
        setContent(mainLayout);

        workingSpace.setVisible(false);
        listAuthors();
    }

    void listAuthors() {
        List<Author> authors = authorRepository.findAll();
        authorSelector.setItems(authors);
    }

    private void update() {
        fileIsReadyLabel.setValue("Файл " + fileName + " готов к обработке");
        workingSpace.setVisible(!fileName.isEmpty());
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

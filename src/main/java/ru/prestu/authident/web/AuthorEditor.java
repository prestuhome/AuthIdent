package ru.prestu.authident.web;

import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import ru.prestu.authident.domain.model.entities.Author;
import ru.prestu.authident.domain.repositories.AuthorRepository;

import java.util.Optional;

@SpringComponent
@UIScope
public class AuthorEditor extends VerticalLayout {

    private final AuthorRepository repository;

    private TextField lastName = new TextField("Фамилия");
    private TextField firstName = new TextField("Имя");
    private TextField patronymic = new TextField("Отчество");

    private Button save = new Button("Сохранить", FontAwesome.SAVE);
    private Button cancel = new Button("Отмена");
    private CssLayout actions = new CssLayout(save, cancel);
    private Binder<Author> binder = new Binder<>(Author.class);

    private Author author;

    @Autowired
    public AuthorEditor(AuthorRepository repository) {
        this.repository = repository;

        addComponents(lastName, firstName, patronymic, actions);

        binder.bindInstanceFields(this);

        setSpacing(true);
        actions.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        save.addClickListener(e -> {
            repository.save(author);
            ((VaadinUI) UI.getCurrent()).listAuthors();
            AuthorEditor.this.setVisible(false);
        });
        cancel.addClickListener(e -> AuthorEditor.this.setVisible(false));
        setVisible(false);
    }

    public final void editAuthor(Author a) {
        if (a == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = a.getId() != null;
        if (persisted) {
            Optional<Author> authorOptional = repository.findById(a.getId());
            authorOptional.ifPresent(author -> this.author = author);
        } else {
            author = a;
        }

        binder.setBean(author);

        setVisible(true);

        save.focus();
        lastName.selectAll();
    }

    public void setChangeHandler(ChangeHandler h) {
        save.addClickListener(e -> h.onChange());
    }

    public interface ChangeHandler {
        void onChange();
    }

}

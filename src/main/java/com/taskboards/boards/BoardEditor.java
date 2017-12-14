package com.taskboards.boards;

import org.springframework.beans.factory.annotation.Autowired;

import com.taskboards.boards.BoardEditorFormConfigurator.ChangeHandler;
import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringComponent
@UIScope
public class BoardEditor extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private final BoardRepository repository;

	private Board board;

	private BoardEditorFormConfigurator formConfigurator;

	@Autowired
	public BoardEditor(BoardRepository repository) {
		this.repository = repository;
		formConfigurator = new BoardEditorFormConfigurator(this, new BoardEditorFormCompoenents()).initialConfiguration();
	}

	public final void editBoard(Board editedBoard) {
		if (isEditingBoard(editedBoard)) {
			loadBoardForEdition(editedBoard);
			configureFormForEditableBoardState(editedBoard);
		} else {
			configureFormForNonEditableBoardState();
		}
	}

	private void configureFormForEditableBoardState(Board editedBoard) {
		if (editedBoard.isPersisted()) {
			formConfigurator.setEstateForBoardBeingEdited();
		} else {
			formConfigurator.setEstateForBoardBeingAdded();
		}
	}

	private void configureFormForNonEditableBoardState() {
		formConfigurator.hideForm(true);
	}

	private void loadBoardForEdition(Board editedBoard) {
		if (editedBoard.isPersisted()) {
			board = repository.findOne(editedBoard.getId());
		} else {
			board = editedBoard;
		}
	}

	private boolean isEditingBoard(Board editedBoard) {
		return editedBoard != null;
	}

	public void setChangeHandler(ChangeHandler handler) {
		formConfigurator.setChangeHandler(handler);
	}

	public Board getBoard() {
		return board;
	}

	public BoardRepository getRepository() {
		return repository;
	}

	public BoardEditorFormConfigurator getFormConfigurator() {
		return formConfigurator;
	}
}

class BoardEditorFormCompoenents {
	TextField name = new TextField("Board name");
	TextField description = new TextField("Board description");

	Button save = new Button("Save", VaadinIcons.ARCHIVE);
	Button cancel = new Button("Cancel");
	Button delete = new Button("Delete", VaadinIcons.DEL);
	CssLayout actions = new CssLayout(save, cancel, delete);
}

class BoardEditorFormConfigurator {

	private BoardEditor editor;
	private BoardEditorFormCompoenents components;
	Binder<Board> binder = new Binder<>(Board.class);

	public BoardEditorFormConfigurator(BoardEditor editor, BoardEditorFormCompoenents components) {
		this.editor = editor;
		this.components = components;
	}

	public BoardEditorFormConfigurator initialConfiguration() {
		editor.addComponents(components.name, components.description, components.actions);
		bindFieldsUsingNameConvention();
		configureComponentsStyle();
		configureButtonsAction();
		hideForm(true);
		return this;
	}
	
	public void setEstateForBoardBeingAdded() {
		components.cancel.setVisible(false);
		configureEditableFormState();
	}

	public void setEstateForBoardBeingEdited() {
		components.cancel.setVisible(true);
		configureEditableFormState();
	}

	private void configureEditableFormState() {
		hideForm(false);
		binder.setBean(editor.getBoard());
		components.save.focus();
		components.name.selectAll();
	}

	public void hideForm(boolean visibility) {
		editor.setVisible(!visibility);
	}

	private void bindFieldsUsingNameConvention() {
		binder.bindInstanceFields(components);
	}

	private void configureComponentsStyle() {
		editor.setSpacing(true);
		components.actions.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		components.save.setStyleName(ValoTheme.BUTTON_PRIMARY);
		components.save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
	}

	private void configureButtonsAction() {
		configureListners(components.save, e -> editor.getRepository().save(editor.getBoard()));
		configureListners(components.delete, e -> editor.getRepository().delete(editor.getBoard()));
		configureListners(components.cancel, e -> editor.editBoard(editor.getBoard()));
	}

	private void configureListners(Button button, ClickListener listener) {
		button.addClickListener(listener);
	}
	
	public interface ChangeHandler {
		void onChange();
	}

	public void setChangeHandler(ChangeHandler handler) {
		components.save.addClickListener(e -> handler.onChange());
		components.delete.addClickListener(e -> handler.onChange());
	}

	public BoardEditorFormCompoenents getComponents() {
		return components;
	}
}

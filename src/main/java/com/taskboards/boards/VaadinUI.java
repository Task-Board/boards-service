package com.taskboards.boards;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
public class VaadinUI extends UI {

	private static final long serialVersionUID = 1L;

	private final BoardRepository repo;

	private final BoardEditor editor;

	final Grid<Board> grid;

	final TextField filter;

	private final Button addNewBtn;

	@Autowired
	public VaadinUI(BoardRepository repo, BoardEditor editor) {
		this.repo = repo;
		this.editor = editor;
		this.grid = new Grid<>(Board.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New board", VaadinIcons.PLUS);
	}

	@Override
	protected void init(VaadinRequest request) {
		buildLayout();
		configureLayout();
		configureBoardFilter();
		configureBoardEdition();
		configureBoardAddition();
		configureListnerForChangedBoards();
		initializeBoardList();
	}

	private void initializeBoardList() {
		listBoards(null);
	}

	private void configureListnerForChangedBoards() {
		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listBoards(filter.getValue());
		});
	}

	private void configureBoardAddition() {
		addNewBtn.addClickListener(e -> editor.editBoard(new Board("", "")));
	}

	private void configureBoardEdition() {
		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editBoard(e.getValue());
		});
	}

	private void configureBoardFilter() {
		filter.setValueChangeMode(ValueChangeMode.LAZY);
		filter.addValueChangeListener(e -> listBoards(e.getValue()));
	}

	private void configureLayout() {
		grid.setHeight(300, Unit.PIXELS);
		grid.setColumns("id", "name", "description");
		filter.setPlaceholder("Filter by name");
	}

	private void buildLayout() {
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		VerticalLayout mainLayout = new VerticalLayout(actions, grid, editor);
		setContent(mainLayout);
	}

	protected void listBoards(String filterText) {
		if (hasFilter(filterText)) {
			grid.setItems(repo.findAll());
		} else {
			grid.setItems(repo.findByNameStartsWithIgnoreCase(filterText));
		}
	}

	private boolean hasFilter(String filterText) {
		return filterText == null || filterText.isEmpty();
	}
}

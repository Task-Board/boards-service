package com.taskboards.boards;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.boot.VaadinAutoConfiguration;
import com.vaadin.ui.Button;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VaadinUITests.Config.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class VaadinUITests {

	@Autowired
	BoardRepository repository;

	VaadinRequest vaadinRequest = Mockito.mock(VaadinRequest.class);

	BoardEditor editor;

	VaadinUI vaadinUI;

	@Before
	public void setup() {
		this.editor = new BoardEditor(this.repository);
		this.vaadinUI = new VaadinUI(this.repository, editor);
	}

	@Test
	public void shouldInitializeTheGridWithBoardRepositoryData() {
		vaadinUI.init(this.vaadinRequest);

		then(vaadinUI.grid.getColumns()).hasSize(3);
		then(getBoardsInGrid()).hasSize(7);
	}

	@Test
	public void shouldFillOutTheGridWithNewData() {
		this.vaadinUI.init(this.vaadinRequest);
		boardDataWasFilled(editor, "Test board name", "A test board");

		getSaveButton().click();

		then(getBoardsInGrid()).hasSize(6);
		then(getBoardsInGrid().get(5)).extracting("name", "description").containsExactly("Test board name", "A test board");

	}

	@Test
	public void shouldFilterOutTheGridWithTheProvidedDescription() {
		this.vaadinUI.init(this.vaadinRequest);
		this.repository.save(new Board("A board", "A description"));

		vaadinUI.listBoards("A board");

		then(getBoardsInGrid()).hasSize(1);
		then(getBoardsInGrid().get(getBoardsInGrid().size() - 1)).extracting("name", "description").containsExactly("A board", "A description");
	}

	@Test
	public void shouldInitializeWithInvisibleEditor() {
		this.vaadinUI.init(this.vaadinRequest);

		then(this.editor.isVisible()).isFalse();
	}

	@Test
	public void shouldMakeEditorVisible() {
		this.vaadinUI.init(this.vaadinRequest);
		this.vaadinUI.grid.select(getBoardsInGrid().get(0));

		then(this.editor.isVisible()).isTrue();
	}

	private void boardDataWasFilled(BoardEditor editor, String name, String description) {
		setName(name);
		setDescription(description);
		editor.editBoard(new Board(name, description));
	}

	private void setDescription(String description) {
		this.editor.getFormConfigurator().getComponents().description.setValue(description);
	}

	private void setName(String name) {
		this.editor.getFormConfigurator().getComponents().name.setValue(name);
	}
	
	private Button getSaveButton() {
		return this.editor.getFormConfigurator().getComponents().save;
	}
	
	private List<Board> getBoardsInGrid() {
		ListDataProvider<Board> ldp = (ListDataProvider) vaadinUI.grid.getDataProvider();
		return new ArrayList<>(ldp.getItems());
	}

	@Configuration
	@EnableAutoConfiguration(exclude = VaadinAutoConfiguration.class)
	static class Config {

		@Autowired
		BoardRepository repository;

		@PostConstruct
		public void initializeData() {
			this.repository.save(new Board("First board", "First test board"));
			this.repository.save(new Board("Second board", ""));
			this.repository.save(new Board("Another board", "Not empty descrition"));
			this.repository.save(new Board("One more board", ""));
			this.repository.save(new Board("Last board", "Last board descrition"));
		}
	}
}

package com.taskboards.boards.web.ui;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.taskboards.boards.domain.Board;
import com.taskboards.boards.domain.BoardRepository;
import com.vaadin.ui.Button;

import static org.mockito.BDDMockito.*;
import static org.mockito.Matchers.argThat;

@RunWith(MockitoJUnitRunner.class)
public class BoardEditorTest {

	private static final String NAME = "Test Board";
	private static final String DESCRIPTION = "This is a test board.";

	@Mock BoardRepository customerRepository;
	@InjectMocks BoardEditor editor;

	@Test
	public void shouldStoreCustomerInRepoWhenEditorSaveClicked() {
		setName();
		setDescription();
		customerDataWasFilled();

		getSaveButton().click();

		then(this.customerRepository).should().save(argThat(customerMatchesEditorFields()));
	}

	@Test
	public void shouldDeleteCustomerFromRepoWhenEditorDeleteClicked() {
		setName();
		setDescription();
		customerDataWasFilled();

		getDeleteButton().click();

		then(this.customerRepository).should().delete(argThat(customerMatchesEditorFields()));
	}

	private Button getSaveButton() {
		return this.editor.getFormConfigurator().getComponents().save;
	}

	private void setDescription() {
		this.editor.getFormConfigurator().getComponents().description.setValue(DESCRIPTION);
	}

	private void setName() {
		this.editor.getFormConfigurator().getComponents().name.setValue(NAME);
	}
	
	private Button getDeleteButton() {
		return editor.getFormConfigurator().getComponents().delete;
	}

	private void customerDataWasFilled() {
		this.editor.editBoard(new Board(NAME, DESCRIPTION));
	}

	private TypeSafeMatcher<Board> customerMatchesEditorFields() {
		return new TypeSafeMatcher<Board>() {
			@Override
			public void describeTo(Description description) {}

			@Override
			protected boolean matchesSafely(Board item) {
				return NAME.equals(item.getName()) && DESCRIPTION.equals(item.getDescription());
			}
		};
	}

}

package com.taskboards.boards.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.taskboards.boards.BoardsApplication;
import com.taskboards.boards.domain.Board;
import com.taskboards.boards.domain.BoardRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BoardsApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class BoardRestControllerTest {
	
	private MockMvc mockMvc;
	
	@Autowired
	private BoardRepository repository;
	
	@Autowired
	private WebApplicationContext applicationContext;
	
	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
	
	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
		this.repository.deleteAllInBatch();
	}
	
	@Test
	public void shouldReturnNotFoundWhenThereIsNoBoard() throws Exception {
		mockMvc.perform(get("/boards"))
				.andExpect(status().isNotFound()); //Should be not found?
	}
	
	@Test
	public void shouldReturnAllBoard() throws Exception {
		String endpoint = "/boards";
		int firstPosition = 0;
		Board firstBoard = new Board("Test", "Test board");
		firstBoard = repository.save(firstBoard);
		
		assertSizeOfTheListReturned(endpoint, 1);
		assertBoardDataAtCorrectPosition(endpoint, firstBoard, firstPosition);
		
		int secondPosition = 1;
		Board secondBoard = new Board("Test 2", "Test board 2");
		secondBoard = repository.save(secondBoard);		
		
		assertSizeOfTheListReturned(endpoint, 2);
		assertBoardDataAtCorrectPosition(endpoint, firstBoard, firstPosition);
		assertBoardDataAtCorrectPosition(endpoint, secondBoard, secondPosition);
	}
	
	@Test
	public void shouldReturnNotFoundWhenThereIsNoBoardWithTheNameProvided() throws Exception {
		mockMvc.perform(get("/boards/name=anyName"))
				.andExpect(status().isNotFound()); //Should be not found?
	}
	
	@Test
	public void shouldReturnAllBoardsStartingWithTheNameProvided() throws Exception {
		String endpoint = "/boards/name=";
		Board testBoard = repository.save(new Board("Test", ""));
		Board anotherTest = repository.save(new Board("tested board", ""));
		Board anotherBoard = repository.save(new Board("another test", ""));	
		
		assertSizeOfTheListReturned(endpoint + "tESt", 2);
		assertBoardDataAtCorrectPosition(endpoint + "tESt", testBoard, 0);
		assertBoardDataAtCorrectPosition(endpoint + "tESt", anotherTest, 1);
		
		assertSizeOfTheListReturned(endpoint + "ANO", 1);
		assertBoardDataAtCorrectPosition(endpoint + "ANO", anotherBoard, 0);
	}
	
	@Test
	public void shouldReturnNotFoundWhenThereIsNoBoardWithTheIdProvided() throws Exception {
		mockMvc.perform(get("/boards/1"))
				.andExpect(status().isNotFound()); //Should be not found?
	}

	@Ignore
	@Test
	public void shouldReturnTheBoardWithTheIdProvided() throws Exception {
		String endpoint = "/boards/";
		Board testBoard = repository.save(new Board("Test", ""));
		Board anotherTest = repository.save(new Board("tested board", ""));
		Board anotherBoard = repository.save(new Board("another test", ""));	
		
		assertSizeOfTheListReturned(endpoint + "1", 1);
		assertSizeOfTheListReturned(endpoint + "2", 1);
		assertSizeOfTheListReturned(endpoint + "3", 1);
		
		assertBoardDataAtCorrectPosition(endpoint + "1", testBoard, 0);
		assertBoardDataAtCorrectPosition(endpoint + "2", anotherTest, 0);		
		assertBoardDataAtCorrectPosition(endpoint + "3", anotherBoard, 0);
	}
	
	//@PostMapping
	//@PutMapping("/{id}")
	//@DeleteMapping("/{id}")

	private void assertSizeOfTheListReturned(String endpoint, int expectedListSize) throws Exception {
		mockMvc.perform(get(endpoint))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
     	.andExpect(jsonPath("$", hasSize(expectedListSize)));
	}

	private void assertBoardDataAtCorrectPosition(String endpoint, Board firstBoard, int firstPosition) throws Exception {
		mockMvc.perform(get(endpoint))
                .andExpect(jsonPath("$[" + firstPosition + "].id", is(firstBoard.getId().intValue())))
                .andExpect(jsonPath("$[" + firstPosition + "].name", is(firstBoard.getName())))
                .andExpect(jsonPath("$[" + firstPosition + "].description", is(firstBoard.getDescription())))
                .andExpect(jsonPath("$[" + firstPosition + "].persisted", is(true)))
                .andExpect(jsonPath("$[" + firstPosition + "].links", hasSize(1)))
                .andExpect(jsonPath("$[" + firstPosition + "].links[0].rel", is("self")))
                .andExpect(jsonPath("$[" + firstPosition + "].links[0].href", is("http://localhost/boards/" + firstBoard.getId())));
	}

}

package com.taskboards.boards.web;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.nio.charset.Charset;

import org.junit.Before;
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
	public void shouldReturnEmptyWhenThereIsNoBoard() throws Exception {
		mockMvc.perform(get("/boards"))
				.andExpect(status().isNotFound()); //Should be not found?
	}
	
	@Test
	public void shouldReturnAllBoard() throws Exception {
		Board board = repository.save(new Board("Test", "Test board"));
		mockMvc.perform(get("/boards"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType))
             	.andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(board.getName())))
                .andExpect(jsonPath("$[0].description", is(board.getDescription())))
                .andExpect(jsonPath("$[0].persisted", is(true)))
                .andExpect(jsonPath("$[0].links", hasSize(1)))
                .andExpect(jsonPath("$[0].links[0].rel", is("self")))
                .andExpect(jsonPath("$[0].links[0].href", is("http://localhost/boards/" + board.getId())));
	}
	//@GetMapping
	//@GetMapping("/name={name}")
	//@GetMapping("/{id}")
	//@PostMapping
	//@PutMapping("/{id}")
	//@DeleteMapping("/{id}")

}

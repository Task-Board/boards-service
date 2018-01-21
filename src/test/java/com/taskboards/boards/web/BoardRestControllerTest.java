package com.taskboards.boards.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;
import com.taskboards.boards.BoardsApplication;
import com.taskboards.boards.domain.Board;
import com.taskboards.boards.domain.BoardRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BoardsApplication.class, properties = "spring.cloud.config.enabled=false")
@WebAppConfiguration
@ActiveProfiles("test")
public class BoardRestControllerTest {

	private static final String BOARDS_ROOT_ENDPOINT = "/boards";
	private static final String ALL_BOARDS_BY_NAME_FILTER_ENDPOINT = "/boards/name={name}";
	private static final String BOARD_BY_ID_ENDPOINT = "/boards/{id}";

	private MockMvc mockMvc;
	
	@SuppressWarnings("rawtypes")
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	private BoardRepository repository;

	@Autowired
	private WebApplicationContext applicationContext;
	
	@Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
            .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
            .findAny()
            .orElse(null);

        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
		this.repository.deleteAll();
	}

	@Test
	public void should_return_NotFound_when_no_board_is_persisted() throws Exception {
		ResultActions result = performGetOn(BOARDS_ROOT_ENDPOINT);

		result.andExpect(status().isNotFound());
	}

	@Test
	public void should_return_the_persisted_board() throws Exception {
		Board hrmTeam = repository.save(new Board("Team HRM", "Hypermatter Reactor Maintenance Team board"));

		ResultActions result = performGetOn(BOARDS_ROOT_ENDPOINT);

		assertNumberOfBoardsReturned(result, 1);
		assertBoardIsPresentWithCorrectData(result, hrmTeam);
	}

	@Test
	public void should_return_all_persisted_boards() throws Exception {
		Board hrmTeam = repository.save(new Board("Team HRM", "Hypermatter Reactor Maintenance Team board"));
		Board teamTIE = repository.save(new Board("Team TIE Wash and Wax Kanban", "Use is mandatory, Vader's orders."));
		Board vaderFist = repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));

		ResultActions result = performGetOn(BOARDS_ROOT_ENDPOINT);

		assertNumberOfBoardsReturned(result, 3);
		assertBoardIsPresentWithCorrectData(result, hrmTeam);
		assertBoardIsPresentWithCorrectData(result, teamTIE);
		assertBoardIsPresentWithCorrectData(result, vaderFist);
	}

	@Test
	public void should_return_NotFound_when_no_board_name_start_with_provided_parameter() throws Exception {
		repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));

		ResultActions result = performGetOnWithParameter(ALL_BOARDS_BY_NAME_FILTER_ENDPOINT, "Team TIE");

		result.andExpect(status().isNotFound());
	}

	@Test
	public void should_return_the_board_with_name_starting_as_provided_parameter_ignoring_case() throws Exception {
		repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));
		Board hrmTeam = repository.save(new Board("Team HRM", "Hypermatter Reactor Maintenance Team board"));

		ResultActions result = performGetOnWithParameter(ALL_BOARDS_BY_NAME_FILTER_ENDPOINT, "team");

		assertNumberOfBoardsReturned(result, 1);
		assertBoardIsPresentWithCorrectData(result, hrmTeam);
	}

	@Test
	public void should_return_all_boards_with_name_starting_as_provided_parameter_ignoring_case() throws Exception {
		repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));
		Board hrmTeam = repository.save(new Board("Team HRM", "Hypermatter Reactor Maintenance Team board"));
		Board teamTIE = repository.save(new Board("Team TIE Wash and Wax Kanban", "Use is mandatory, Vader's orders."));

		ResultActions result = performGetOnWithParameter(ALL_BOARDS_BY_NAME_FILTER_ENDPOINT, "TEAM");

		assertNumberOfBoardsReturned(result, 2);
		assertBoardIsPresentWithCorrectData(result, hrmTeam);
		assertBoardIsPresentWithCorrectData(result, teamTIE);
	}

	@Test
	public void should_return_NotFound_when_no_board_have_the_id_provided() throws Exception {
		repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));

		ResultActions result = performGetOnWithParameter(BOARD_BY_ID_ENDPOINT, "33");

		result.andExpect(status().isNotFound());
	}

	@Test
	public void should_return_the_board_with_id_equals_the_provided_parameter() throws Exception {
		repository.save(new Board("Team HRM", "Hypermatter Reactor Maintenance Team board"));
		repository.save(new Board("Team TIE Wash and Wax Kanban", "Use is mandatory, Vader's orders."));
		Board vaderFist = repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));

		ResultActions result = performGetOnWithParameter(BOARD_BY_ID_ENDPOINT, vaderFist.getId().toString());

		assertBoardDataIsCorrect(result, vaderFist);
	}
	
	@Test
	public void should_return_UnprocessableEntity_when_trying_to_create_a_board_with_no_name() throws Exception {
		ResultActions result = performPostOnWithBoard(BOARDS_ROOT_ENDPOINT, new Board("", ""));
		
		result.andExpect(status().isUnprocessableEntity()); 
	}
	
	@Test
	public void should_return_saved_board() throws Exception {
		Board teamTIE = new Board("Team TIE Wash and Wax Kanban", "Use is mandatory, Vader's orders.");
		
		ResultActions result = performPostOnWithBoard(BOARDS_ROOT_ENDPOINT, teamTIE);
		teamTIE.setId(getIdReturned(result));
		
		assertBoardDataIsCorrect(result, teamTIE);
		
	}
	
	@Test
	public void should_persist_saved_board() throws Exception {
		Board teamTIE = new Board("Team TIE Wash and Wax Kanban", "Use is mandatory, Vader's orders.");
		
		ResultActions  resultPost = performPostOnWithBoard(BOARDS_ROOT_ENDPOINT, teamTIE);
		teamTIE.setId(getIdReturned(resultPost));
				
		ResultActions resultGet = performGetOnWithParameter(BOARD_BY_ID_ENDPOINT, teamTIE.getId().toString());
		assertBoardDataIsCorrect(resultGet, teamTIE);
	}
	
	@Test
	public void should_return_UnprocessableEntity_when_the_edited_board_does_not_exists() throws Exception {
		Board editedTeamHRM = new Board("Team HRM TODO", "Hypermatter Reactor Maintenance Team TODO board");
		
		ResultActions result = performPutOnWithParameterAndBoard(BOARD_BY_ID_ENDPOINT, "501", editedTeamHRM);
		
		result.andExpect(status().isUnprocessableEntity()); 
	}
	
	@Test
	public void should_return_UnprocessableEntity_when_the_edition_removes_the_board_name() throws Exception {
		String hrmTeamBoardID = repository.save(new Board("HRM Team", "Hypermatter Reactor Maintenance Team board")).getId();		
		Board editedTeamHRM = new Board("", "Hypermatter Reactor Maintenance Team TODO board");

		ResultActions result = performPutOnWithParameterAndBoard(BOARD_BY_ID_ENDPOINT, hrmTeamBoardID.toString(), editedTeamHRM);
		
		result.andExpect(status().isUnprocessableEntity()); 
	}

	@Test
	public void should_return_the_edited_board() throws Exception {
		String hrmTeamBoardID = repository.save(new Board("HRM Team", "Hypermatter Reactor Maintenance Team board")).getId();		
		Board editedTeamHRM = new Board("Team HRM TODO", "Hypermatter Reactor Maintenance Team TODO board");

		ResultActions result = performPutOnWithParameterAndBoard(BOARD_BY_ID_ENDPOINT, hrmTeamBoardID.toString(), editedTeamHRM);
		editedTeamHRM.setId(hrmTeamBoardID);

		assertBoardDataIsCorrect(result, editedTeamHRM);
	}
	
	@Test
	public void should_persist_the_editions_on_board() throws Exception {
		String hrmTeamBoardID = repository.save(new Board("HRM Team", "Hypermatter Reactor Maintenance Team board")).getId();		
		Board editedTeamHRM = new Board("Team HRM TODO", "Hypermatter Reactor Maintenance Team TODO board");

		performPutOnWithParameterAndBoard(BOARD_BY_ID_ENDPOINT, hrmTeamBoardID.toString(), editedTeamHRM);
		editedTeamHRM.setId(hrmTeamBoardID);

		ResultActions resultGet = performGetOnWithParameter(BOARD_BY_ID_ENDPOINT, hrmTeamBoardID.toString());
		assertBoardDataIsCorrect(resultGet, editedTeamHRM);
	}

	@Test
	public void should_return_UnprocessableEntity_when_the_deleted_board_does_not_exists() throws Exception {
		ResultActions result = performDelete(BOARD_BY_ID_ENDPOINT, "501");
		
		result.andExpect(status().isUnprocessableEntity()); 
	}
	
	@Test
	public void should_return_deleted_board() throws Exception {
		Board vaderFist = repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));

		ResultActions result = performDelete(BOARD_BY_ID_ENDPOINT, vaderFist.getId().toString());

		assertBoardDataIsCorrect(result, vaderFist);
	}
	
	@Test
	public void should_not_return_deleted_boards_in_futures_requets() throws Exception {
		Board hrmTeam = repository.save(new Board("Team HRM", "Hypermatter Reactor Maintenance Team board"));
		Board teamTIE = repository.save(new Board("Team TIE Wash and Wax Kanban", "Use is mandatory, Vader's orders."));
		Board vaderFist = repository.save(new Board("501st Legion TODO", "Vader's Fist TODO. *Should not be updated during battle!"));

		performDelete(BOARD_BY_ID_ENDPOINT, vaderFist.getId().toString());
		
		ResultActions result = performGetOn(BOARDS_ROOT_ENDPOINT);
		assertNumberOfBoardsReturned(result, 2);
		assertBoardIsPresentWithCorrectData(result, hrmTeam);
		assertBoardIsPresentWithCorrectData(result, teamTIE);
		
		result = performGetOnWithParameter(BOARD_BY_ID_ENDPOINT, vaderFist.getId().toString());
		result.andExpect(status().isNotFound());
	}

	private ResultActions performGetOn(String endpoint) throws Exception {
		return mockMvc.perform(get(endpoint));
	}

	private ResultActions performGetOnWithParameter(String endpoint, String parameter) throws Exception {
		return mockMvc.perform(get(endpoint, parameter));
	}
	
	private ResultActions performPostOnWithBoard(String boardsRootEndpoint, Board parameter) throws Exception {
		return mockMvc.perform(post(boardsRootEndpoint).contentType(contentType).content(toJson(parameter)));
	}
	
	private ResultActions performPutOnWithParameterAndBoard(String boardsRootEndpoint, String parameter, Board board) throws Exception {
		return mockMvc.perform(put(boardsRootEndpoint, parameter).contentType(contentType).content(toJson(board)));
	}

	private void assertNumberOfBoardsReturned(ResultActions perform, int expectedListSize) throws Exception {
		perform.andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(contentType)).andExpect(jsonPath("$", hasSize(expectedListSize)));
	}
	
	private ResultActions performDelete(String endpoint, String parameter) throws Exception {
		return mockMvc.perform(delete(endpoint, parameter));
	}

	private void assertBoardIsPresentWithCorrectData(ResultActions perform, Board board) throws Exception {
		int index = getBoardIndex(perform, board);
		perform.andDo(print()).andExpect(jsonPath("$[" + index + "].id", is(board.getId())))
				.andExpect(jsonPath("$[" + index + "].name", is(board.getName())))
				.andExpect(jsonPath("$[" + index + "].description", is(board.getDescription())))
				.andExpect(jsonPath("$[" + index + "].persisted", is(true)))
				.andExpect(jsonPath("$[" + index + "].links", hasSize(1)))
				.andExpect(jsonPath("$[" + index + "].links[0].rel", is("self")))
				.andExpect(jsonPath("$[" + index + "].links[0].href", is("http://localhost/boards/" + board.getId())));
	}

	private void assertBoardDataIsCorrect(ResultActions perform, Board board) throws Exception {
		perform.andDo(print()).andExpect(jsonPath("name", is(board.getName())))
				.andExpect(jsonPath("description", is(board.getDescription())))
				.andExpect(jsonPath("persisted", is(true)))
				.andExpect(jsonPath("_links.self.href", is("http://localhost/boards/" + board.getId())));
	}

	private String getIdReturned(ResultActions result) throws UnsupportedEncodingException {
		return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
	}
	
	private int getBoardIndex(ResultActions perform, Board board) throws UnsupportedEncodingException {
		List<String> ids = JsonPath.read(perform.andReturn().getResponse().getContentAsString(), "$.[*].id");
		int index = 0;
		for (String id : ids) {
			if (id.equals(board.getId())) return index;
			index++;
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	private String toJson(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}

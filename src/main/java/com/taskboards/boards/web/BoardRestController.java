package com.taskboards.boards.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskboards.boards.domain.Board;
import com.taskboards.boards.domain.BoardRepository;

@RestController
@RequestMapping("/boards")
public class BoardRestController {

	@Autowired
	BoardRepository repository;
	
	BoardResourceAssembler assembler = new BoardResourceAssembler();
	
	@GetMapping
	public ResponseEntity<List<BoardResource>> getAll() {
		List<Board> boards = repository.findAll();
		if (!boards.isEmpty()) {			
			return new ResponseEntity<>(assembler.toResources(boards), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/name={name}")
	public ResponseEntity<List<BoardResource>> findByNome(@PathVariable String name) {
		List<Board> boards = repository.findByNameStartsWithIgnoreCase(name);
		if (!boards.isEmpty()) {			
			return new ResponseEntity<>(assembler.toResources(boards), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<BoardResource> get(@PathVariable Long id) {
		Board board = repository.findOne(id);
		if (board != null) {			
			return new ResponseEntity<>(assembler.toResource(board), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping
	public ResponseEntity<BoardResource> create(@RequestBody Board board) {
		board = repository.save(board);
		if (board != null) {
			return new ResponseEntity<>(assembler.toResource(board), HttpStatus.OK);					
		} else {
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<BoardResource> update(@PathVariable Long id, @RequestBody Board board) {
		if (board != null) {
			board.setId(id);
			board = repository.save(board);
			return new ResponseEntity<>(assembler.toResource(board), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<BoardResource> delete(@PathVariable Long id) {
		Board board = repository.findOne(id);
		if (board != null) {
			repository.delete(board);
			return new ResponseEntity<>(assembler.toResource(board), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
}
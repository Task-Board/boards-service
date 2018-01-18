package com.taskboards.boards.web;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import com.taskboards.boards.domain.Board;

public class BoardResource extends Resource<Board> {
	
	public BoardResource(Board board, Link... links) {
		super(board, links);
	}
	
}
package com.taskboards.boards.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import com.taskboards.boards.domain.Board;

public class BoardResourceAssembler extends ResourceAssemblerSupport<Board, BoardResource> {
	
	public BoardResourceAssembler() {
		super(Board.class, BoardResource.class);
	}

	@Override
	public BoardResource toResource(Board board) {
		return new BoardResource(board, linkTo(methodOn(BoardRestController.class).get(board.getId())).withSelfRel());
	}
	
	@Override
	protected BoardResource instantiateResource(Board board) {
		return new BoardResource(board);
	}
}
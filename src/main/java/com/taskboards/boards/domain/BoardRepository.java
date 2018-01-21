package com.taskboards.boards.domain;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BoardRepository extends MongoRepository<Board, String> {
	
	List<Board> findByNameStartsWithIgnoreCase(String name);

}

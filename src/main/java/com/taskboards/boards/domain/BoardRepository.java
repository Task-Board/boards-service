package com.taskboards.boards.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
	
	List<Board> findByNameStartsWithIgnoreCase(String name);

}

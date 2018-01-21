package com.taskboards.boards.domain;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class Board {

	@Id
	@Setter
	private String id;

	@Setter
	@NotBlank
	private String name;
	
	@Setter
	private String description;
	
	public Board(String name, String description) {
		this.name = name;
		this.description = description;
	}	
	
	public boolean isPersisted() {
		return getId() != null;
	}
	
	@Override
	public String toString() {
		return String.format("Board[id=%d, name='%s', description='%s']", id,
				name, description);
	}	
}

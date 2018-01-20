package com.taskboards.boards.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
public class Board {

	@Id
	@GeneratedValue
	@Setter
	private Long id;

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

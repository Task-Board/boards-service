package com.taskboards.boards;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BoardsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BoardsApplicationTest {	

		@Autowired
		private BoardRepository repository;

		@Ignore
		@Test
		public void shouldFillOutComponentsWithDataWhenTheApplicationIsStarted() {
			then(this.repository.count()).isEqualTo(5);
		}

		@Ignore
		@Test
		public void shouldFindTwoBauerCustomers() {
			then(this.repository.findByNameStartsWithIgnoreCase("Bauer")).hasSize(2);
		}

}

package org.examples.spring.databinding.jpa;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.examples.spring.databinding.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@Transactional
public class JPADataBindingControllerTest {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;
	
	@Autowired
	private JPADataBindingController controller;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void itAddsOneUserToAnEmptyMapWithKeyEqualToMinus1() throws Exception {		
		// POST one user data to be binded and added to the Map
		mockMvc.perform(post("/jpa")
				.param("users[-1].name", "John")
				.param("users[-1].email", "john@mail.com"))
				.andExpect(status().is3xxRedirection());
		
		// Let's check that's true
		IUserRepository repository = controller.getRepository();
		Iterable<Object> users = (Iterable<Object>)(Iterable)repository.findAll();
		assertThat(users, hasItem(hasProperty("name", equalTo("John"))));
		assertThat(users, hasItem(hasProperty("email", equalTo("john@mail.com"))));
	}

	@Test
	public void itAddsTwoUsersToAnEmptyMapWithKeysEqualToMinus1AndMinus2() throws Exception {
		// POST one user data to be binded and added to the Map
		mockMvc.perform(post("/jpa")
				.param("users[-1].name", "John")
				.param("users[-1].email", "john@mail.com")
				.param("users[-2].name", "Mike")
				.param("users[-2].email", "mike@mail.com"))
				.andExpect(status().is3xxRedirection());
		
		// Let's check that's true
		IUserRepository repository = controller.getRepository();
		assertEquals(2, repository.count());
		// Need this strange casting to make Hamcrest work; it does not with with Iterable<User>
		Iterable<Object> users = (Iterable<Object>)(Iterable)repository.findAll();
		assertThat(users, hasItem(hasProperty("name", equalTo("John"))));
		assertThat(users, hasItem(hasProperty("email", equalTo("john@mail.com"))));
		assertThat(users, hasItem(hasProperty("name", equalTo("Mike"))));
		assertThat(users, hasItem(hasProperty("email", equalTo("mike@mail.com"))));
	}

	@Test
	public void itAddsANewUserToAMapHoldingOneWithKeyEqualTo1() throws Exception {
		User mike = new User();
		mike.setName("Mike");
		mike.setEmail("mike@mail.com");
		IUserRepository repository = controller.getRepository();
		repository.save(mike);
		
		// Let's add the new user at key -1
		mockMvc.perform(post("/jpa")
				.param("users[-1].id", "")
				.param("users[-1].name", "John")
				.param("users[-1].email", "john@mail.com"))
				.andExpect(status().is3xxRedirection());
		
		// Let's check that's true
		assertEquals(2, repository.count());
		// Need this strange casting to make Hamcrest work; it does not with with Iterable<User>
		Iterable<Object> users = (Iterable<Object>)(Iterable)repository.findAll();
		assertThat(users, hasItem(hasProperty("name", equalTo("John"))));
		assertThat(users, hasItem(hasProperty("email", equalTo("john@mail.com"))));
		assertThat(users, hasItem(hasProperty("name", equalTo("Mike"))));
		assertThat(users, hasItem(hasProperty("email", equalTo("mike@mail.com"))));
	}

	@Test
	public void itModifiesAUser() throws Exception {
		// Initialize a Map of users
		User mike = new User();
		mike.setName("Mike");
		mike.setEmail("mike@mail.com");
		IUserRepository repository = controller.getRepository();
		repository.save(mike);
		Long id = mike.getId();
		
		// Modify Mike's email
		mockMvc.perform(post("/jpa")
				.param(String.format("users[%d].id", id), id.toString())
				.param(String.format("users[%d].name", id), "Mike")
				.param(String.format("users[%d].email", id), "mike@foo.bar"))
				.andExpect(status().is3xxRedirection());
		
		// Let's check that's true
		assertEquals(1, repository.count());
		// Need this strange casting to make Hamcrest work; it does not with with Iterable<User>
		Iterable<Object> users = (Iterable<Object>)(Iterable)repository.findAll();
		assertThat(users, hasItem(hasProperty("name", equalTo("Mike"))));
		assertThat(users, hasItem(hasProperty("email", equalTo("mike@foo.bar"))));
	}

	@Test
	public void itDeletesAUser() throws Exception {
		// Initialize a Map of users
		User mike = new User();
		mike.setName("Mike");
		mike.setEmail("mike@mail.com");
		IUserRepository repository = controller.getRepository();
		repository.save(mike);
		Long id = mike.getId();
		
		// Modify John's email
		mockMvc.perform(post("/jpa")
				.param(String.format("users[%d].id", id), "")
				.param(String.format("users[%d].name", id), "Mike")
				.param(String.format("users[%d].email", id), "mike@mail.com"))
				.andExpect(status().is3xxRedirection());
		
		// Let's check that's true
		assertEquals(0, repository.count());
	}
	
	/**
	 * @author jafamo
	 * @throws Exception
	 */
	@Test
	public void itDeletesAUserAfterOne() throws Exception {
		// Initialize a Map of users
		
		//create repository for save users
		IUserRepository repository = controller.getRepository();
		
		User mike = new User();
		mike.setName("Mike");
		mike.setEmail("mike@mail.com");
		repository.save(mike);
		
		User javier = new User();
		javier.setName("Javier");
		javier.setEmail("javier@mail.com");
		repository.save(javier);
		
		
		User mariano = new User();
		mariano.setName("Mariano");
		mariano.setEmail("mariano@mail.com");
		repository.save(mariano);
		
		Long id = mike.getId();
		Long id1 = javier.getId();
		//Long id2 = mariano.getId();
		
		// Modify John's email
		mockMvc.perform(post("/jpa")
		
				.param(String.format("users[%d].id", id1), "")
				.param(String.format("users[%d].name", id1), "javier")
				.param(String.format("users[%d].email", id1), "javier@mail.com"))
				
				.andExpect(status().is3xxRedirection());
		//Don't need
		//repository.delete(javier);
		
		// Let's check that's true
		assertEquals(2, repository.count());
	}
	
	
}

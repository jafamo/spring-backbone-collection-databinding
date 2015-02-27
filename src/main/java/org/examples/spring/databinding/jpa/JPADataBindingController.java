package org.examples.spring.databinding.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class JPADataBindingController {

	@Autowired
	private IUserRepository repository;

	@RequestMapping(value = "/jpa", method = RequestMethod.GET)
	public String getHome() {
		return "jpa";
	}

	@RequestMapping(value = "/jpa", method = RequestMethod.POST)
	public String updateUsers(@ModelAttribute("form") Form form) {
		// Save the binded data to our "Repository"
		//Set entrySet() Returns a collection view of the mappings contained in this map.
		Set<Entry<Long, User>> entrySet = form.getUsers().entrySet();
		for (Entry<Long, User> entry : entrySet) {
			Long key = entry.getKey();
			User user = entry.getValue();
			if (key > 0 && user.getId() == null) {
				this.repository.delete(user);
			} else {
				this.repository.save(user);
			}
		}
		return "redirect:/jpa";
	}

	@ModelAttribute("form")
	public Form getForm() {
		Form form = new Form();
		Map<Long, User> usersMap = new HashMap<Long, User>();
		Iterable<User> usersInRepository = this.repository.findAll();
		for (User user : usersInRepository) {
			usersMap.put(user.getId(), user);
		}
		form.setUsers(usersMap);
		return form;
	}

	public IUserRepository getRepository() {
		return repository;
	}

	public void setRepository(IUserRepository repository) {
		this.repository = repository;
	}


}

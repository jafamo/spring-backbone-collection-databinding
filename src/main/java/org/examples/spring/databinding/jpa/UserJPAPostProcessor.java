
package org.examples.spring.databinding.jpa;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.examples.spring.databinding.map.User;

/**
 * Assigns new ids for new Users and removes deleted users from the JPA
 * @author jafamo
 *
 */

public class UserJPAPostProcessor {

	private long userIdSequence = 1L;

	public void process(Map<Long, User> users) {
		
	}

	public void startIdSequenceAt(long userIdSequence) {
		this.userIdSequence = userIdSequence;
	}
	
	
	
	
}

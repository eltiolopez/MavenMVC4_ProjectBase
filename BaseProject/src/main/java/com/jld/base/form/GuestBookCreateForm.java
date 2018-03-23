package com.jld.base.form;

import java.io.Serializable;

public class GuestBookCreateForm implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String username;
	
	private String message;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

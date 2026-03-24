package com.estapar.teste.exceptions;

import java.time.LocalDateTime;

public class ErrorResponse {

	private int status;
	private String message;
	private String timestamp;

	public ErrorResponse(int status, String message) {
		this.status = status;
		this.message = message;
		this.timestamp = LocalDateTime.now().toString();
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}

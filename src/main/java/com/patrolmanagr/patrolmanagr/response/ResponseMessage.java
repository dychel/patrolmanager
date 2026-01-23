package com.patrolmanagr.patrolmanagr.response;

import lombok.Data;

@Data
public class ResponseMessage {
	
	private String code ;
	private String message;
	private Object data;

	public ResponseMessage() {
		
	}
	public ResponseMessage(String message) {
		this.message = message;
	}
	public ResponseMessage(String message, Object data) {
		this.message = message;
		this.data=data;
	}

	public ResponseMessage(String code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
}

package com.lovi.puppy.message.factory;

import com.lovi.puppy.message.FailResult;
import com.lovi.puppy.message.impl.FailResultImpl;

public class FailResultFactory{

	public FailResult create(){
		return new FailResultImpl();
	}
}

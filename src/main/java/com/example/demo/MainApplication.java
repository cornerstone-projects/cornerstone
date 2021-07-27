package com.example.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.core.DefaultApplication;

@SpringBootApplication
public class MainApplication extends DefaultApplication {

	public static void main(String[] args) throws Exception {
		start(args);
	}

}

package com.example.mathquiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.example.mathquiz",
        "com.example.mathquiz.adapter.in.web"  // Where handler resides
})
public class MathQuizApplication {

	public static void main(String[] args) {
		SpringApplication.run(MathQuizApplication.class, args);
	}

}

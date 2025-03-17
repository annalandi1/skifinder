package com.example.skifinder;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.skifinder.security.JwtService;

@SpringBootTest
class SkiFinderApplicationTests {

	@Mock
	private JwtService jwtService;

	@Mock
	private UserDetailsService userDetailsService;

	@InjectMocks
	private SkiFinderApplication skiFinderApplication;

	@Test
	void contextLoads() {
	}

}

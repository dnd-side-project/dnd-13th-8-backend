package com.example.demo;

import com.example.demo.dto.ChatUserProfile;
import com.example.demo.dto.chat.ChatInbound;
import com.example.demo.service.ChatProfileService;
import com.example.demo.service.ChatService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatServerApplicationTests {

	@LocalServerPort
	int port;

	@Value("${deulak.jwt.secret-base64}")
	String secretBase64;

	@Value("${deulak.jwt.issuer}")
	String issuer;

	WebSocketStompClient stompClient;

	WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();

	@TestConfiguration
	static class MockConfig {

		@Bean
		ChatService chatService() {
			return mock(ChatService.class);
		}

		@Bean
		ChatProfileService chatProfileService() {
			return mock(ChatProfileService.class);
		}
	}

	@Autowired
	ChatService chatService;

	@Autowired
	ChatProfileService chatProfileService;

	@BeforeEach
	void setup() {
		SockJsClient sockJsClient = new SockJsClient(
				List.of(new WebSocketTransport(new StandardWebSocketClient()))
		);
		stompClient = new WebSocketStompClient(sockJsClient);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	String mintAccessToken(String userId) {
		SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
		Instant now = Instant.now();
		return Jwts.builder()
				.issuer(issuer)
				.subject(userId)
				.claim("typ", "access")
				.issuedAt(java.util.Date.from(now))
				.expiration(java.util.Date.from(now.plusSeconds(600)))
				.signWith(key)
				.compact();
	}

	@Test
	void connect_send_shouldCallControllerFlow_withoutDb() throws Exception {
		String roomId = "room-1";
		String userId = "user-123";
		String token = mintAccessToken(userId);

		ChatUserProfile profile = ChatUserProfile.builder()
				.userId(userId)
				.username("kim")
				.profileImage("https://img.example/kim.png")
				.build();

		when(chatProfileService.getOrLoad(userId)).thenReturn(profile);

		StompHeaders connectHeaders = new StompHeaders();
		connectHeaders.add("Authorization", "Bearer " + token);

		String url = "ws://localhost:" + port + "/chat/ws";

		CompletableFuture<StompSession> future =
				stompClient.connectAsync(
						url,
						handshakeHeaders,
						connectHeaders,
						new StompSessionHandlerAdapter() {
							@Override
							public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
								ChatInbound inbound = ChatInbound.builder()
										.content("hello")
										.systemMessage(false)
										.build();
								session.send("/chat/app/rooms/" + roomId, inbound);
							}
						}
				);

		StompSession session = future.get(5, TimeUnit.SECONDS);

		verify(chatProfileService, timeout(2000)).getOrLoad(eq(userId));
		verify(chatService, timeout(2000)).handleInbound(eq(roomId), any(ChatInbound.class), eq(userId), any());

		session.disconnect();
	}

	@Test
	void connect_withoutToken_shouldFail() {
		String url = "ws://localhost:" + port + "/chat/ws";

		CompletableFuture<StompSession> future =
				stompClient.connectAsync(
						url,
						new WebSocketHttpHeaders(),
						new StompHeaders(),
						new StompSessionHandlerAdapter() {}
				);

		assertThrows(Exception.class, () -> future.get(3, TimeUnit.SECONDS));
	}

}

package com.devsuperior.dscatalog.config;

import com.devsuperior.dscatalog.components.JwtTokenEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.Arrays;

@Configuration
@EnableAuthorizationServer // Annotation muito importante , para dizer que a classe representa um
							// authorization server
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {


	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;
	@Value("${jwt.duration}")
	private Integer jwtDuration;


	// Vamos injetar os objetos que vamos precisar

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private JwtAccessTokenConverter accessTokenConverter;
	@Autowired
	private JwtTokenStore tokenStore;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtTokenEnhancer jwtTokenEnhancer;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
	}

	// Como que vai ser nossa autenticao e qual vai ser os dados do Cliente ( Credenciais da Aplica????o)
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
		.withClient(clientId) // nome da aplicacao
		.secret(passwordEncoder.encode(clientSecret)) // senha da aplicacao
		.scopes("read","write") // qual tipo de acesso q vou dar
		.authorizedGrantTypes("password") // o tipo padrao do oAuth
		.accessTokenValiditySeconds(jwtDuration);
	}
	
	// ?? Aqui que eu informo quem eu vou autorizar e o formato do token
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

		// Usado para informar as informacoes adicionars do token
		TokenEnhancerChain chain = new TokenEnhancerChain();
		chain.setTokenEnhancers(Arrays.asList(accessTokenConverter,jwtTokenEnhancer));

		endpoints.authenticationManager(authenticationManager)
		.tokenStore(tokenStore)
		.accessTokenConverter(accessTokenConverter)
		.tokenEnhancer(chain);
	}

}

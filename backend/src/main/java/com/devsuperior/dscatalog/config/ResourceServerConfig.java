package com.devsuperior.dscatalog.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {


    // Ambiente de configuracao da minha aplicacao
    @Autowired
    private Environment environment;

    // Preciso do bean do TokenStore
    @Autowired
    private JwtTokenStore tokenStore;

    // ENDPOINTS PUBLICOS
    private static final String[] PUBLIC = {"/oauth/token", "/h2-console/**"};
    // ENDPOINTS PADRAO PARA OPERATOR OU ADMIN
    private static final String[] OPERATOR_OR_ADMIN = {"/products/**", "/categories/**"};
    // ENDPOINTS SOMENTE PARA ADMIN
    private static final String[] ADMIN = {"/users/**"};


    // Aqui eu configuro o TokenStore, com isso o nosso resource server vai analizar se o token é valido
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore);
    }

    // Aqui vamos configurar as Rotas
    /*
     Paginacao , analizar produto = Liberado para navegar
     Cadastro de Produto,Categoria = Protegido por perfil ROLE_OPERATOR & ROLE_ADMIN (precisa de login)
     Cadastro de Usuario = Protegido por perfil ROLE_ADMIN (precisa de login)
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Se estou um rodando profile test , eu quero rodar o banco H2
        if(Arrays.asList(environment.getActiveProfiles()).contains("test")){
            http.headers().frameOptions().disable();
        }

       http.authorizeRequests()
               .antMatchers(PUBLIC).permitAll() // o endpoint /oauth/login publico
               .antMatchers(HttpMethod.GET,OPERATOR_OR_ADMIN).permitAll() // deixando publico so os GET de products e categories
               .antMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR","ADMIN") // Informando que so quem acessa os endpoints sao Operator e admin
               .antMatchers(ADMIN).hasAnyRole("ADMIN")// Informando que so quem acessa os endpoints Users é com role ADMIN
               .anyRequest().authenticated(); // Configurando que quem for acessar qualquer outra rota precisa estar logado nao importa o perfil de usuario
       http.cors().configurationSource(corsConfigurationSource());
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration corsConfig = new CorsConfiguration();
    	//Liberando todas os dominios pra usar.. futuramente posso deixar so um ex: (www.meudominio.com.br)
    	corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
    	corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH"));
    	corsConfig.setAllowCredentials(true);
    	corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

    	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    	source.registerCorsConfiguration("/**", corsConfig);
    	return source;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
    	FilterRegistrationBean<CorsFilter> bean 
    		= new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
    	bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    	return bean;
    }	


}

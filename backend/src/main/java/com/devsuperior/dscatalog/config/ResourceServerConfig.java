package com.devsuperior.dscatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {


    // Preciso do bean do TokenStore
    @Autowired
    private JwtTokenStore tokenStore;

    // ENDPOINTS PUBLICOS
    private static final String[] PUBLIC = {"/oauth/token"};
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
       http.authorizeRequests()
               .antMatchers(PUBLIC).permitAll() // o endpoint /oauth/login publico
               .antMatchers(HttpMethod.GET,OPERATOR_OR_ADMIN).permitAll() // deixando publico so os GET de products e categories
               .antMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR","ADMIN") // Informando que so quem acessa os endpoints sao Operator e admin
               .antMatchers(ADMIN).hasAnyRole("ADMIN")// Informando que so quem acessa os endpoints Users é com role ADMIN
               .anyRequest().authenticated(); // Configurando que quem for acessar qualquer outra rota precisa estar logado nao importa o perfil de usuario
    }

}

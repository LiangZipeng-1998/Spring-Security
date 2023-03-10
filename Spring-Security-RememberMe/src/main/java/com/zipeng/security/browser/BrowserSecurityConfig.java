package com.zipeng.security.browser;

import com.zipeng.handler.MyAuthenticationFailureHandler;
import com.zipeng.handler.MyAuthenticationSucessHandler;
import com.zipeng.validate.code.ValidateCodeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyAuthenticationSucessHandler authenticationSucessHandler;
    @Autowired
    private MyAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private ValidateCodeFilter validateCodeFilter;
    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private DataSource dataSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class) // ??????????????????????????????
                .formLogin() // ????????????
                // http.httpBasic() // HTTP Basic
                .loginPage("/authentication/require") // ???????????? URL
                .loginProcessingUrl("/login") // ?????????????????? URL
                .successHandler(authenticationSucessHandler) // ??????????????????
                .failureHandler(authenticationFailureHandler) // ??????????????????
                .and()
                .rememberMe()
                .tokenRepository(persistentTokenRepository()) // ?????? token ???????????????
                .tokenValiditySeconds(3600) // remember ????????????????????????
                .userDetailsService(userDetailService) // ????????????????????????
                .and()
                .authorizeRequests() // ????????????
                .antMatchers("/authentication/require",
                        "/login.html",
                        "/code/image").permitAll() // ???????????????????????????
                .anyRequest()  // ????????????
                .authenticated() // ???????????????
                .and()
                .csrf().disable();
    }
}

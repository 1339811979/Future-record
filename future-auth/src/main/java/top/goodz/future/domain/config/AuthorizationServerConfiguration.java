package top.goodz.future.domain.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import top.goodz.future.domain.config.support.JwtTokenEnhancer;
import top.goodz.future.domain.config.support.UserPasswordUpdateHandler;
import top.goodz.future.domain.constant.ConstantsOuath;
import top.goodz.future.domain.filter.SecurityTokenFilter;
import top.goodz.future.domain.service.SysUserService;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


/***
 *
 * future ??????????????????
 *
 * zhangyajun
 *
 */

@Order(6)
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthorizationCodeServices authorizationCodeServices;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private SecurityTokenFilter securityTokenFilter;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private TokenEnhancer jwtTokenEnhancer;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;
    /**
     * ???????????????????????????????????????
     *
     * @param clientDetails
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clientDetails) throws Exception {

        clientDetails.withClientDetails(clientDetailsService);
    }

    /**
     * ?????????????????????
     * @param dataSource
     * @return
     */
    @Bean
    public ClientDetailsService clientDetailsService(DataSource dataSource) {
        JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
        jdbcClientDetailsService.setPasswordEncoder(bCryptPasswordEncoder);
        return jdbcClientDetailsService;

    }

    /***
     *?????????????????? ???????????????
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore)
                .authenticationManager(authenticationManager)
                .authorizationCodeServices(authorizationCodeServices)
                .tokenServices(authorizationServerTokenServices())
                .userDetailsService(userDetailsService)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST)
                // ??????????????????api
                .pathMapping("/oauth/authorize", ConstantsOuath.URLPREFIX+"/authorize")
                .pathMapping("/oauth/token", ConstantsOuath.URLPREFIX+"/login")//????????????
                .pathMapping("/oauth/confirm_access", ConstantsOuath.URLPREFIX+"/confirm_access")
                .pathMapping("/oauth/error", ConstantsOuath.URLPREFIX+"/error")
                .pathMapping("/oauth/check_token", ConstantsOuath.URLPREFIX+"/check_token")
                .pathMapping("/oauth/token_key", ConstantsOuath.URLPREFIX+"/token_key")
        ;

    }

    /**
     * ?????????????????????????????????
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients()    //??????form?????????????????????,?????????????????????client_id???client_secret??????token
                .checkTokenAccess("permitAll()")     //??????????????????token??????
                .tokenKeyAccess("permitAll()");            // ??????token?????????????????????

    }

    /**
     * ????????????
     *
     * @return ???????????????????????? DefaultTokenServices??? ????????????RemoteTokenServices
     * @return
     */
    @Bean
    public AuthorizationServerTokenServices authorizationServerTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setTokenStore(tokenStore);
        tokenServices.setAccessTokenValiditySeconds(ConstantsOuath.ACCESS_TOKEN_VALIDITY_SECONDS);
        tokenServices.setRefreshTokenValiditySeconds(ConstantsOuath.REFRESH_TOKEN_VALIDITY_SECONDS);

        //????????????token????????????
        if (jwtAccessTokenConverter != null && jwtTokenEnhancer != null) {
            TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
            List<TokenEnhancer> enhancerList = new ArrayList<>();
            enhancerList.add(jwtTokenEnhancer);
            enhancerList.add(jwtAccessTokenConverter);
            tokenEnhancerChain.setTokenEnhancers(enhancerList);
            //jwt
            tokenServices.setTokenEnhancer(tokenEnhancerChain);
            //     tokenServices.accaccessTokenConverter(jwtAccessTokenConverter);
        }
        return tokenServices;

    }

    /**
     * ???????????????  ???????????????
     *
     * @return
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices(DataSource dataSource) {
        return new JdbcAuthorizationCodeServices(dataSource);
    }

}

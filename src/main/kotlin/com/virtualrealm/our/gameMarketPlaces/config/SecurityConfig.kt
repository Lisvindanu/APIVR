package com.virtualrealm.our.gameMarketPlaces.config

import com.virtualrealm.our.gameMarketPlaces.auth.ApiKeyFilter
import com.virtualrealm.our.gameMarketPlaces.auth.ApiKeyInterceptor
import com.virtualrealm.our.gameMarketPlaces.repository.ApiKeyRepository
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.AuthServices
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebSecurity

class SecurityConfig : WebMvcConfigurer {

    @Bean
    fun apiKeyFilter(apiKeyRepository: ApiKeyRepository):ApiKeyFilter{
        return ApiKeyFilter(apiKeyRepository)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, corsConfigurationSource : CorsConfigurationSource): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource) }
            .authorizeHttpRequests { authz ->

                authz

                    .requestMatchers("/api/auth/login", "/api/auth/login/oauth2/code/google",
                        "/api/auth/login/oauth2/code", "/api/auth/register", "/api/products", "/api/products/",
                        "/api/auth/logout", "/api/auth/login/oauth2/", "/api/purchases", "/api/**",
                        "/api/inventory/use", "api/inventory", "api/payments/", "/sftp/**",
                        "/runtime", "**/**", "actuator/**","/api/products/**",
                        "api/categories/**", "api/genres/**", "/api/users/**" ).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()
            }
            .csrf {
                it.disable()
            }

            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Tidak menggunakan sesi
            }
            .oauth2Login { oauth ->
                oauth.loginPage("/login") // Kalau perlu halaman login khusus
                oauth.defaultSuccessUrl("/dashboard", true) // Arahkan ke halaman utama setelah login berhasil
                oauth.failureUrl("/login?error=true") // Arahkan ke halaman login jika login gagal
                oauth.successHandler { request, response, authentication ->
                    println("Successfully authenticated: ${authentication.name}")
                }
                oauth.failureHandler { request, response, exception ->
                    println("OAuth2 login failed: ${exception.message}")
                }
            }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { request, response, authException ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
//            .oauth2Login (Customizer.withDefaults())

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder() // Menggunakan BCrypt untuk password encoder
    }
}

//
//@Configuration
//@EnableWebSecurity
//class SecurityConfig(
//    private val authServices: AuthServices,
//    private val userRepository: UserRepository,
//    private val apiKeyInterceptor: ApiKeyInterceptor,
//    private val apiKeyFilter: ApiKeyFilter
//) : WebMvcConfigurer {
//
//    @Bean
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            // Filter untuk API Key
//            .addFilterBefore(apiKeyFilter, AnonymousAuthenticationFilter::class.java)
//            .cors { cors ->
//                cors.configurationSource(corsConfigurationSource())
//            }
//            .authorizeHttpRequests { authz ->
//                authz
//                    // Endpoint yang tidak memerlukan autentikasi (registrasi, login, public API)
//                    .requestMatchers("/oauth2/authorization/google").permitAll()
//                    .requestMatchers("/auth/login", "/api/auth/register", "/api/auth/login", "/api/products/*", "/api/*").permitAll()
//                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                    .anyRequest().authenticated() // Endpoint lainnya memerlukan autentikasi
//            }
//            .csrf { csrf ->
//                csrf.disable()
//            }
//            // Konfigurasi OAuth2 untuk login Google
//            .oauth2Login { oauth2Login ->
//                oauth2Login
//                    .successHandler { _, response, authentication ->
//                        val user = authentication.principal as org.springframework.security.oauth2.core.oidc.user.OidcUser
//                        val email = user.email ?: "unknown"
//                        val username = user.getAttribute<String>("preferred_username") ?: email.split("@")[0]
//
//                        val userData = com.virtualrealm.our.gameMarketPlaces.model.authModel.UserDataResponse(
//                            username = username,
//                            email = email
//                        )
//                        val registeredUser = authServices.registerOrLoginWithGoogle(userData)
//                        val localUser = userRepository.findByUsername(registeredUser.username)
//                            ?: throw IllegalStateException("User not found")
//
//                        val token = authServices.generateAndStoreToken(localUser)
//                        response.contentType = "application/json"
//                        response.writer.write("""{"token": "${token.token}", "expiresAt": "${token.expiresAt}"}""")
//                    }
//                    .failureHandler { _, response, exception ->
//                        response.status = HttpServletResponse.SC_UNAUTHORIZED
//                        response.contentType = "application/json"
//                        response.writer.write(
//                            """{"error": "Authentication failed", "message": "${exception.message}"}"""
//                        )
//                    }
//            }
//            .sessionManagement { session ->
//                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            }
//
//        return http.build()
//    }
//
//    @Bean
//    fun passwordEncoder(): PasswordEncoder {
//        return BCryptPasswordEncoder()
//    }
//
//    private fun corsConfigurationSource(): CorsConfigurationSource {
//        val configuration = CorsConfiguration()
//        configuration.allowedOrigins = listOf("http://localhost:8080", "http://localhost:5501", "*")
//        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
//        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
//        configuration.allowCredentials = true
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", configuration)
//        return source
//    }
//
//    override fun addInterceptors(registry: InterceptorRegistry) {
//        registry.addInterceptor(apiKeyInterceptor)
//    }
//}

package com.virtualrealm.our.gameMarketPlaces.controller


import com.virtualrealm.our.gameMarketPlaces.entity.User
import com.virtualrealm.our.gameMarketPlaces.model.WebResponse
import com.virtualrealm.our.gameMarketPlaces.model.authModel.*
import com.virtualrealm.our.gameMarketPlaces.repository.TokenRepository
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.AuthServices
import com.virtualrealm.our.gameMarketPlaces.service.UserService
import com.virtualrealm.our.gameMarketPlaces.service.impl.AuthServicesImpl
import com.virtualrealm.our.gameMarketPlaces.service.impl.EmailService
import com.virtualrealm.our.gameMarketPlaces.service.impl.OtpService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Controller
import org.springframework.web.servlet.View
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.SignatureException
import java.time.LocalDateTime
import java.util.*


@Controller
@CrossOrigin
@RequestMapping("/api/auth")
class AuthController(
    private val authServices: AuthServices,
    private val userService: UserService,
    private val response: HttpServletResponse,
    private val userRepository: UserRepository,
    private val error: View,
    private val otpService: OtpService,
    private val emailService: EmailService,
    private val tokenRepository: TokenRepository,
) {
    private val logger = LoggerFactory.getLogger(AuthServicesImpl::class.java)

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterRequest): ResponseEntity<WebResponse<String>> {
        // Validasi password dan konfirmasi password
        if (request.password != request.password_confirmation) {
            logger.error("Password and confirm password do not match!")
            throw IllegalStateException("Password and confirm password must match!")
        }

        // Cek apakah email sudah terdaftar
        val existingUser = userRepository.findByEmail(request.email).orElse(null)
        if (existingUser != null) {
            logger.error("User email already exists: ${request.email}")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                WebResponse(400, "error", null, "User email already exists")
            )
        }

        // Proses pendaftaran pengguna
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = userRepository.save(
            User(
                username = request.username,
                fullname = request.fullname, // tambah fullname
                email = request.email,
                password = hashedPassword,
                imageUrl = request.imageUrl, // tambah imageUrl
                uuid = UUID.randomUUID().toString() // generate UUID
            )
        )

        // Pastikan user.id bukan null dan memiliki tipe Long
        val userId = user.id ?: throw IllegalStateException("User ID is missing")

        // Kirim OTP setelah registrasi
        val otp = otpService.generateOtp(userId)
        emailService.sendOtp(user.email, otp)

        return ResponseEntity.ok(WebResponse(
            code = 200,
            status = OtpStatus.OTP_SENT.status,
            data = "Please verify OTP sent to your email to complete the registration.",
            message = OtpStatus.OTP_SENT.message
        ))
    }



    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<WebResponse<Any>> {
        return try {
            // Cek apakah email ditemukan
            val user = userRepository.findByEmail(request.email).orElseThrow {
                IllegalStateException("Invalid credentials")
            }

            // Validasi password
            if (request.password.isNullOrBlank() || !checkPassword(request.password, user.password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    WebResponse(401, "error", null, "Invalid credentials")
                )
            }

            // Cek apakah pengguna sudah memiliki token yang masih aktif
            val existingToken = tokenRepository.findByUser(user)
            if (existingToken != null && existingToken.expiresAt.isAfter(LocalDateTime.now()) && existingToken.isLoggedIn) {
                logger.warn("User already logged in: ${user.username}")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    WebResponse(401, "error", null, "User is already logged in. Please logout first.")
                )
            }

            // Generate token baru
            val token = authServices.generateAndStoreToken(user)

            // Menyiapkan data respons
            val responseData = LoginResponseData(
                token = token.token,
                expiresAt = token.expiresAt,
                message = "Login successful",
                status = "success",
                role = user.role
            )

            ResponseEntity.ok(WebResponse(
                code = 200,
                status = "success",
                data = responseData,
                message = "Login successful"
            ))

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                WebResponse(500, "error", null, "An unexpected error occurred: ${e.message}")
            )
        }
    }


    @PostMapping("/send-otp")
    fun sendOtp(@RequestParam email: String): ResponseEntity<WebResponse<String>> {
        val user = userRepository.findByEmail(email).orElseThrow {
            IllegalStateException("Invalid credentials")
        }

        val existingOtp = otpService.getActiveOtpForUser(user.id!!)
        if (existingOtp != null) {
            otpService.deleteOtp(existingOtp)
        }

        // Generate OTP dan kirim ke email
        val otp = otpService.generateOtp(user.id!!)
        emailService.sendOtp(email, otp)

        return ResponseEntity.ok(WebResponse(
            code = 200,
            status = "success",
            data = "OTP sent to $email"
        ))
    }


    @PostMapping("/verify-otp-regis")
    fun verifyOtpRegis(@RequestBody @Valid request: OtpVerificationRequest): ResponseEntity<WebResponse<String>> {
        // Cari pengguna berdasarkan email
        val user = userRepository.findByEmail(request.email).orElseThrow {
            IllegalStateException("Invalid credentials")
        }

        // Validasi OTP
        val isValidOtp = otpService.validateOtp(user.id!!, request.otp)
        if (!isValidOtp) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                WebResponse(401, "error", null, OtpStatus.OTP_FAILED.message)
            )
        }

        // Tandai OTP sebagai terverifikasi
        userService.markOtpAsVerified(user.id!!, request.otp)

        // Finalisasi registrasi dan buat token
//        val token = authServices.generateAndStoreToken(user)

        return ResponseEntity.ok(WebResponse(
            code = 200,
            status = OtpStatus.OTP_VERIFIED.status,
            data = "User registered successfully. ",
            message = OtpStatus.OTP_VERIFIED.message
        ))
    }


    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<WebResponse<String>> {
        val token = authorization.removePrefix("Bearer ").trim() // Strip "Bearer " prefix
        authServices.logout(token)
        val response = WebResponse(
            code = 200,
            status = "success",
            data = "Successfully logged out"
        )
        return ResponseEntity.ok(response)
    }


    @GetMapping("/user")
    fun getUserData(@RequestHeader("Authorization") authorization: String): ResponseEntity<WebResponse<UserDataResponse>> {
        val token = authorization.removePrefix("Bearer ").trim()
        try {
            val userData = authServices.getUserData(token)
            return ResponseEntity.ok(WebResponse(
                code = 200,
                status = "success",
                data = userData
            ))
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid token format: ${e.message}")
            return ResponseEntity.status(400).body(WebResponse(
                code = 400,
                status = "error",
                data = null
            ))
        }
    }


    //coba 4
    @GetMapping("/login/oauth2/code/google")
    fun googleOAuth2Callback(@RequestParam("code") code: String): ResponseEntity<WebResponse<String>> {
        logger.info("Received Google OAuth2 callback with code: $code")
        try {
            val accessToken = authServices.exchangeAuthCodeForToken(code)
            logger.info("Exchanged auth code for token: $accessToken")

            val oauth2User = authServices.getOAuth2UserFromGoogleToken(accessToken)
            logger.info("Retrieved OAuth2 user from Google token: $oauth2User")

            // Handle Google authentication dengan field baru
            val user = authServices.handleGoogleAuthentication(oauth2User)
            logger.info("User handled and saved: $user")

            response.sendRedirect("http://localhost:5501/dashboard.html")
            return ResponseEntity.ok().build()

        } catch (e: Exception) {
            logger.error("OAuth2 authentication failed", e)
            return ResponseEntity.status(400).body(
                WebResponse(400, "error", null, "OAuth2 authentication failed: ${e.message}")
            )
        }
    }


    fun checkPassword(inputPassword: String, storedPassword: String): Boolean {
        // Contoh menggunakan bcrypt untuk membandingkan password
        return BCrypt.checkpw(inputPassword, storedPassword)
    }

    @GetMapping("/check-email")
    fun checkEmail(@RequestParam email: String): ResponseEntity<WebResponse<EmailCheckResponse>> {
        logger.info("Checking email existence: $email")
        val result = authServices.checkEmailExists(email)

        return ResponseEntity.ok(WebResponse(
            code = 200,
            status = "success",
            data = result,
            message = if (result.exists) "Email sudah terdaftar" else "Email tersedia"
        ))
    }

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<WebResponse<List<User>>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(
            WebResponse(
                code = 200,
                status = "success",
                data = users,
                message = "Successfully retrieved all users."
            )
        )
    }

    @GetMapping("/users/role")
    fun getUsersByRole(@RequestParam role: String): ResponseEntity<WebResponse<List<User>>> {
        val users = userService.getUsersByRole(role)
        return ResponseEntity.ok(
            WebResponse(
                code = 200,
                status = "success",
                data = users,
                message = "Successfully retrieved users with role: $role."
            )
        )
    }

//    @PostMapping("/google")
//    fun loginOrRegisterWithGoogle(@RequestBody request: GoogleLoginRequest): ResponseEntity<WebResponse<LoginResponseData>> {
//        // Periksa apakah pengguna sudah terdaftar berdasarkan Google ID
//        val existingUser = userRepository.findByGoogleId(request.googleId)
//
//        val user = if (existingUser != null) {
//            // Jika pengguna sudah ada, perbarui token Google jika diperlukan
//            existingUser.googleToken = request.googleToken
//            existingUser.googleRefreshToken = request.googleRefreshToken
//            userRepository.save(existingUser)
//        } else {
//            // Jika pengguna belum ada, lakukan registrasi pengguna baru
//            userRepository.save(
//                User(
//                    username = request.name,
//                    fullname = request.name, // Bisa diambil dari Google
//                    email = request.email,
//                    googleId = request.googleId,
//                    imageUrl = request.picture,
//                    googleToken = request.googleToken,
//                    googleRefreshToken = request.googleRefreshToken,
//                    password = "", // Kosongkan password untuk Google login
//                    role = "USER" // Default role
//                )
//            )
//        }
//
//        // Generate token untuk pengguna
//        val token = authServices.generateAndStoreToken(user)
//
//        // Kembalikan respons ke Laravel
//        return ResponseEntity.ok(
//            WebResponse(
//                code = 200,
//                status = "success",
//                data = LoginResponseData(
//                    token = token.token,
//                    expiresAt = token.expiresAt,
//                    status = "SUCCESS",
//                    role = user.role
//                ),
//                message = "Login/Registration successful"
//            )
//        )
//    }

    @PostMapping("/google")
    fun loginOrRegisterWithGoogle(@RequestBody request: GoogleLoginRequest): ResponseEntity<WebResponse<LoginResponseData>> {
        try {
            // Periksa apakah pengguna sudah terdaftar berdasarkan Google ID
            val existingUser = userRepository.findByGoogleId(request.googleId)

            // Jika pengguna sudah ada, perbarui token Google jika diperlukan
            val user = if (existingUser != null) {
                existingUser.apply {
                    googleToken = request.googleToken
                    googleRefreshToken = request.googleRefreshToken ?: "" // Nilai default jika null
                }.also { userRepository.save(it) }
            } else {
                // Jika pengguna belum ada, lakukan registrasi pengguna baru
                userRepository.save(
                    User(
                        username = request.name,
                        fullname = request.name, // Bisa diambil dari Google
                        email = request.email,
                        googleId = request.googleId,
                        imageUrl = request.picture,
                        googleToken = request.googleToken,
                        googleRefreshToken = request.googleRefreshToken ?: "", // Nilai default jika null
                        password = "", // Kosongkan password untuk Google login
                        role = "USER" // Default role
                    )
                )
            }

            // Generate token untuk pengguna
            val token = authServices.generateAndStoreToken(user)

            // Log token untuk debugging
            logger.info("Token berhasil dibuat untuk pengguna: ${user.username}")

            // Kembalikan respons ke Laravel
            return ResponseEntity.ok(
                WebResponse(
                    code = 200,
                    status = "success",
                    data = LoginResponseData(
                        token = token.token,
                        expiresAt = token.expiresAt,
                        status = "SUCCESS",
                        role = user.role
                    ),
                    message = "Login/Registration successful"
                )
            )
        } catch (e: Exception) {
            // Tangani error dan kembalikan respons
            logger.error("Error saat login/registrasi dengan Google: ${e.message}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                WebResponse(
                    code = 500,
                    status = "error",
                    data = null,
                    message = "An unexpected error occurred: ${e.message}"
                )
            )
        }
    }


}

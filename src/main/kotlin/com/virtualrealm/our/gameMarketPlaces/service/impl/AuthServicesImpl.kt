package com.virtualrealm.our.gameMarketPlaces.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.virtualrealm.our.gameMarketPlaces.entity.User
import com.virtualrealm.our.gameMarketPlaces.entity.UserToken
import com.virtualrealm.our.gameMarketPlaces.model.authModel.*
import com.virtualrealm.our.gameMarketPlaces.repository.OtpTokenRepository
import com.virtualrealm.our.gameMarketPlaces.repository.TokenRepository
import com.virtualrealm.our.gameMarketPlaces.repository.UserRepository
import com.virtualrealm.our.gameMarketPlaces.service.AuthServices
import com.virtualrealm.our.gameMarketPlaces.validation.ValidationUtil
import io.github.cdimascio.dotenv.Dotenv
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*


@Service
class AuthServicesImpl  (
    val userRepository: UserRepository,
    val validationUtil: ValidationUtil,
    private val tokenRepository: TokenRepository,
    private val otpService: OtpService,
    private val emailService: EmailService,
    private val otpTokenRepository: OtpTokenRepository

) : AuthServices {

    private val logger = LoggerFactory.getLogger(AuthServicesImpl::class.java)
    private val client = OkHttpClient()
    private val dotenv = Dotenv.load() // Load .env file

    private val googleClientId = dotenv["GOOGLE_CLIENT_ID"] ?: throw IllegalStateException("GOOGLE_CLIENT_ID not found in .env")
    private val googleClientSecret = dotenv["GOOGLE_CLIENT_SECRET"] ?: throw IllegalStateException("GOOGLE_CLIENT_SECRET not found in .env")
    private val redirectUri = dotenv["GOOGLE_REDIRECT_URI"] ?: throw IllegalStateException("REDIRECT_URI not found in .env")


    val SECRET_KEY: String = "test"



    @Transactional
    override fun register(registerRequest: RegisterRequest): RegisterResponseData {
        logger.info("Registering new user: ${registerRequest.username}")
        validationUtil.validate(registerRequest)

        val existingUserByEmail = userRepository.findByEmail(registerRequest.email)
        if (existingUserByEmail != null) {
            logger.error("User email already exists: ${registerRequest.email}")
            throw IllegalArgumentException("User email already exists")
        }

        val existingUserByUsername = userRepository.findByUsername(registerRequest.username)
        if (existingUserByUsername != null) {
            logger.error("User username already exists: ${registerRequest.username}")
            throw IllegalArgumentException("User username already exists")
        }

        val password = if (registerRequest.isGoogle) {
            logger.info("User is registering using Google account")
            null
        } else {
            registerRequest.password ?: throw IllegalArgumentException("Password cannot be null")
        }

        val hashedPassword = password?.let { hashPassword(it) }
        val user = User(
            username = registerRequest.username,
            fullname = registerRequest.fullname, // Add this
            email = registerRequest.email,
            password = hashedPassword ?: "",
            imageUrl = registerRequest.imageUrl, // Add this
            uuid = UUID.randomUUID().toString() // UUID will be auto-generated
        )

        logger.debug("Saving user: $user")
        val savedUser = userRepository.save(user)
        logger.info("User registered with ID: ${savedUser.id}")

        // Generate OTP for the user and send it via email
        val otp = savedUser.id?.let { otpService.generateOtp(it) }
        if (otp != null) {
            try {
                emailService.sendOtp(savedUser.email, otp)
                logger.info("OTP sent to ${savedUser.email}")
            } catch (e: Exception) {
                logger.error("Failed to send OTP to ${savedUser.email}: ${e.message}")
                throw RuntimeException("Failed to send OTP. Please try again.")
            }
        } else {
            logger.error("Failed to generate OTP for user ID: ${savedUser.id}")
            throw IllegalStateException("Failed to generate OTP for user")
        }

        return convertUserToResponseData(savedUser)
    }


    @Transactional
    override fun registerOrLoginWithGoogle(userData: UserDataResponse): RegisterResponseData {
        logger.info("Attempting to register or log in user via Google with email: ${userData.email}")
        logger.info("Registering or logging in user with Google ID: ${userData.googleId}")
        val existingUser = userRepository.findByUsername(userData.username)
        val user = if (existingUser != null) {
            logger.info("User already exists, logging in: ${userData.username}")
            existingUser
        } else {
            val newUser = User(
                username = userData.username,
                email = userData.email,
                password = "",
                googleId = userData.googleId,

            )
            logger.info("Creating new user: ${userData.username}")
            userRepository.save(newUser)
            newUser
        }
        return convertUserToResponseData(user)
    }

    @Transactional
    override fun login(loginRequest: LoginRequest): LoginResponseData {
        validationUtil.validate(loginRequest)

        // Cek login dengan Google
        if (loginRequest.isGoogle) {
            val googleToken = loginRequest.googleToken ?: throw IllegalArgumentException("Google token cannot be null")
            val userData = getUserDataFromGoogleToken(googleToken)
            logger.info("Google login successful for user: ${userData.username}")

            val existingUser = userRepository.findByUsername(userData.username)
            val user = existingUser ?: userRepository.save(User(username = userData.username, email = userData.email, password = ""))

            // Cek apakah pengguna sudah memiliki token yang masih berlaku
            val existingToken = tokenRepository.findByUser(user)
            if (existingToken != null && existingToken.expiresAt.isAfter(LocalDateTime.now())) {
                logger.warn("User already logged in with token: ${user.username}")
                return LoginResponseData(
                    message = "User is already logged in. Please logout first.",
                    status = "ERROR"
                )
            }

            // Jika token tidak ada atau sudah kedaluwarsa, buat token baru
            val token = generateAndStoreToken(user)

            logger.debug("Token generated: ${token.token}")
            return LoginResponseData(token = token.token, expiresAt = token.expiresAt, status = "SUCCESS")
        }

        // Login biasa dengan username dan password
        val user = userRepository.findByUsername(loginRequest.email)
            ?: throw IllegalArgumentException("Invalid Credentials")
        val password = loginRequest.password ?: throw IllegalArgumentException("Password cannot be null")

        if (!checkPassword(password, user.password)) {
            logger.warn("Invalid password for user: ${loginRequest.email}")
            return LoginResponseData(
                message = "Invalid Credentials",
                status = "ERROR"
            )
        }

        // Cek apakah pengguna sudah memiliki token yang masih berlaku
        val existingToken = tokenRepository.findByUser(user)
        if (existingToken != null && existingToken.expiresAt.isAfter(LocalDateTime.now())) {
            logger.warn("User already logged in with token: ${user.username}")
            return LoginResponseData(
                message = "User is already logged in. Please logout first.",
                status = "ERROR"
            )
        }

        // Jika token tidak ada atau sudah kedaluwarsa, buat token baru
        val token = generateAndStoreToken(user)
        return LoginResponseData(
            token = token.token,
            expiresAt = token.expiresAt,
            status = "SUCCESS"
        )
    }








    //tess
    @Transactional
    override fun handleGoogleAuthentication(oauth2User: OAuth2User): User {
        val googleId = oauth2User.getAttribute<String>("sub")
        val email = oauth2User.getAttribute<String>("email")
        val username = oauth2User.getAttribute<String>("name") ?: "Unknown"
        val picture = oauth2User.getAttribute<String>("picture")

        logger.info("Google authentication successful for user: $username, email: $email, googleId: $googleId")

        val existingUser = googleId?.let { userRepository.findByGoogleId(it) }

        return existingUser ?: run {
            val newUser = User(
                username = username,
                email = email ?: "No Email",
                password = "",
                googleId = googleId,
                createdAt = LocalDateTime.now(),

            )
            logger.info("Creating new user: $username with email: $email and googleId: $googleId")
            userRepository.save(newUser) // Save user to DB
            newUser
        }
    }

    override fun getOAuth2UserFromGoogleToken(googleToken: String): OAuth2User {
        val userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo"
        val request = Request.Builder()
            .url(userInfoEndpoint)
            .addHeader("Authorization", "Bearer $googleToken")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: throw IllegalArgumentException("Empty response body")
            val objectMapper = ObjectMapper()
            val jsonResponse = objectMapper.readTree(responseBody)
            val attributes = mapOf(
                "sub" to jsonResponse["sub"].asText(),
                "name" to jsonResponse["name"].asText(),
                "email" to jsonResponse["email"].asText(),
                "picture" to jsonResponse["picture"].asText()
            )
            return DefaultOAuth2User(
                listOf(SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
            )
        } else {
            throw IllegalArgumentException("Failed to fetch user data from Google: ${response.message}")
        }
    }

    override fun getUserData(token: String): UserDataResponse {
        val userToken = tokenRepository.findByToken(token) ?: throw IllegalArgumentException("Invalid Token")
        val user = userToken.user ?: throw IllegalArgumentException("User not found")
        return UserDataResponse(
            username = user.username,
            fullname = user.fullname,
            email = user.email,
            googleId = user.googleId ?: "N/A",
            imageUrl = user.imageUrl,
            uuid = user.uuid,
            address = user.address,
            phoneNumber = user.phoneNumber
        )
    }

    @Transactional
    override fun logout(token: String) {
        val userToken = tokenRepository.findByToken(token)
        if (userToken != null) {
            userToken.isLoggedIn = false
            tokenRepository.save(userToken)
            tokenRepository.delete(userToken)
        } else {
            throw IllegalArgumentException("Token not found")
        }
    }


    // Helper method to get user data from Google token
    override fun getUserDataFromGoogleToken(googleToken: String): UserDataResponse {
        val userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo"
        val request = Request.Builder()
            .url(userInfoEndpoint)
            .addHeader("Authorization", "Bearer $googleToken")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: throw IllegalArgumentException("Empty response body")
            val objectMapper = ObjectMapper()
            val jsonResponse = objectMapper.readTree(responseBody)

            val username = jsonResponse["name"].asText()
            val fullname = jsonResponse["name"].asText() // Google usually provides full name
            val email = jsonResponse["email"].asText()
            val googleId = jsonResponse["sub"].asText()
            val imageUrl = jsonResponse["picture"].asText() // Google provides profile picture URL
            val address = jsonResponse["address"].asText() // Google provides profile picture URL
            val phoneNumber = jsonResponse["phoneNumber"].asText() // Google provides profile picture URL

            logger.debug("Google token received: $googleToken")
            logger.debug("User data from Google: Username = $username, Email = $email, Google ID = $googleId")

            return UserDataResponse(
                username = username,
                fullname = fullname,
                email = email,
                googleId = googleId,
                imageUrl = imageUrl,
                uuid = UUID.randomUUID().toString(), // Generate a new UUID for Google users
                address = address,
                phoneNumber = phoneNumber
            )
        } else {
            throw IllegalArgumentException("Failed to fetch user data from Google: ${response.message}")
        }
    }

    override fun exchangeAuthCodeForToken(code: String): String {
        val client = OkHttpClient()
        val tokenEndpoint = "https://oauth2.googleapis.com/token"

        val formBody = FormBody.Builder()
            .add("code", code)
            .add("client_id", googleClientId)
            .add("client_secret", googleClientSecret)
            .add("redirect_uri", redirectUri)
            .add("grant_type", "authorization_code")
            .build()

        val request = Request.Builder()
            .url(tokenEndpoint)
            .post(formBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error("Failed to exchange auth code for token: ${response.message}")
                    throw IllegalArgumentException("Invalid response: ${response.code}")
                }
                val responseBody = response.body?.string() ?: throw IllegalArgumentException("Empty response body")
                val tokenResponse = ObjectMapper().readTree(responseBody)
                val accessToken = tokenResponse["access_token"].asText()

                // Log the access token
                logger.debug("Access token retrieved: $accessToken")

                return accessToken
            }
        } catch (e: Exception) {
            logger.error("Error exchanging auth code: ${e.message}")
            throw RuntimeException("Error during token exchange", e)
        }
    }


    @Transactional
    override fun verifyOtpForRegistration(otpVerificationRequest: OtpVerificationRequest): RegisterResponseData {
        val email = otpVerificationRequest.email
        val otp = otpVerificationRequest.otp

        // Find the user by email
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Validate OTP using OtpTokenRepository
        val otpToken = otpTokenRepository.findByUserIdAndOtp(user.id!!, otp)
            .orElseThrow { IllegalArgumentException("Invalid OTP") }

        // Check if OTP has expired
        if (otpToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("OTP has expired")
        }

        // Mark the OTP as verified in the User entity
        user.isOtpVerified = true
        userRepository.save(user)

        // Optionally, delete the OTP token after verification
        otpTokenRepository.deleteByUserId(user.id!!)

        // Return response
        return RegisterResponseData(
            id = user.id,
            username = user.username,
            email = user.email,
            created_at = user.createdAt,
            message = "Registration successful",
            status = "SUCCESS"
        )
    }



    private fun convertUserToResponseData(user: User): RegisterResponseData {
        val userId = user.id?.toString() ?: throw IllegalArgumentException("User ID cannot be null")
        return RegisterResponseData(
            id = user.id,
            username = user.username,
            email = user.email,
            created_at = user.createdAt,
            message = "Registration successful",
            status = "SUCCESS"
        )
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    private fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }

    override fun generateAndStoreToken(user: User): UserToken {
        val sub = UUID.randomUUID().toString()
        val tokenPayLoad = mapOf(
            "sub" to sub,
            "username" to user.username,
            "fullname" to (user.fullname ?: user.username) // Include fullname in token
        )
        val token = Base64.getEncoder().encodeToString(ObjectMapper().writeValueAsString(tokenPayLoad).toByteArray())
        val expiresAt = LocalDateTime.now().plusHours(1)

        val userToken = UserToken(
            username = user.username,
            token = token,
            expiresAt = expiresAt,
            user = user,
            sub = sub,
            isLoggedIn = true // Set initial login status
        )
        tokenRepository.save(userToken)
        return userToken
    }

    fun validateToken(token: String): Boolean {
        try {
            // Dekode token dari Base64
            val decodedBytes = Base64.getUrlDecoder().decode(token)
            val decodedString = String(decodedBytes)

            // Parse payload dari token (misalnya menggunakan JSON parsing)
            val payload = ObjectMapper().readValue(decodedString, Map::class.java)
            val sub = payload["sub"]
            val username = payload["username"]

            // Periksa apakah sub dan username sesuai dengan data yang ada di database
            val userToken = tokenRepository.findByToken(token)
            return userToken != null && userToken.expiresAt.isAfter(LocalDateTime.now())
        } catch (e: Exception) {
            return false
        }
    }


    @Transactional
    fun updateUserDetails(userId: Long, updateRequest: UpdateUserRequest): UserResponseData {
        logger.info("Updating user details for user ID: $userId")

        // Ambil user dari database
        val existingUser = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User not found")
        }

        // Periksa apakah username baru sudah ada
        if (updateRequest.username != existingUser.username) {
            val existingUsername = userRepository.findByUsername(updateRequest.username)
            if (existingUsername != null) {
                throw IllegalArgumentException("Username already exists")
            }
            existingUser.username = updateRequest.username
        }

        // Jika password baru ada, hash passwordnya
        if (updateRequest.password != null) {
            existingUser.password = hashPassword(updateRequest.password)
        }

        // Simpan perubahan
        val updatedUser = userRepository.save(existingUser)

        logger.info("User details updated for user ID: ${updatedUser.id}")
        return convertUserToResponseDataForUpdate(updatedUser)
    }

    fun convertUserToResponseDataForUpdate(user: User): UserResponseData {
        return UserResponseData(
            id = user.id!!,
            username = user.username,
            fullName = user.fullname!!,
            email = user.email,
            address = user.address!!,
            phoneNumber = user.phoneNumber!!,
            imageUrl = user.imageUrl!!
        )
    }

    override fun checkEmailExists(email: String): EmailCheckResponse {
        val user = userRepository.findByEmail(email).orElse(null)
        return EmailCheckResponse(
            exists = user != null,
            id = user?.id ?: -1,
            uuid = user?.id?.toString() ?: "",
            fullname = user?.username ?: "",
            username = user?.username ?: "",
            email = email,
            image = "", // You can add image handling if needed
            isLoggedIn = false
        )
    }

    @Transactional
    fun logoutAllSessions(userId: Long) {
        tokenRepository.deleteAllByUserId(userId)
        logger.info("All sessions for user ID $userId have been logged out.")
    }

    @Transactional
    override  fun updateProfile(userId: Long, updateRequest: UpdateUserRequest, token: String): UserResponseData {
        // Validate token first
        if (!validateToken(token)) {
            throw IllegalArgumentException("Invalid token")
        }

        logger.info("Updating profile for user ID: $userId")

        val existingUser = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User not found")
        }

        // Update fullname (terpisah dari username)
        updateRequest.fullname.let { existingUser.fullname = it }

        // Update address dan phone number jika ada
        updateRequest.address?.let { existingUser.address = it }
        updateRequest.phoneNumber?.let { existingUser.phoneNumber = it }

        // Update imageUrl jika ada foto baru
        updateRequest.imageUrl?.let { existingUser.imageUrl = it }

        val updatedUser = userRepository.save(existingUser)
        logger.info("Profile updated for user ID: ${updatedUser.id}")

        return UserResponseData(
            id = updatedUser.id!!,
            username = updatedUser.username,
            fullName = updatedUser.fullname ?: "",
            email = updatedUser.email,
            address = updatedUser.address,
            phoneNumber = updatedUser.phoneNumber,
            imageUrl = updatedUser.imageUrl // Tambahkan imageUrl ke response
        )
    }


}


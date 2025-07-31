# Validator 🔐

A full-stack email authentication system built with **Spring Boot**, **React**, and **JWT**, featuring **OTP-based password reset** via email.

---

## 🔧 Features

- 🔐 Login with JWT token stored in **HTTP-only cookies**
- 📧 Send OTP to email for password reset
- 🔄 Reset password using OTP
- ⚙️ Secure backend with Spring Security
- 📦 React frontend with Axios and protected routes
- ✅ CORS setup with credentials
- 💾 User details fetched from in-memory DB / JPA (customizable)

---

## 📁 Project Structure

```plaintext
validator/
├── src/ (Spring Boot)
│   ├── controller/
│   ├── service/
│   ├── config/
│   ├── util/
│   └── ValidatorApplication.java
├── client/ (React + Vite)
│   ├── src/
│   │   ├── pages/
│   │   ├── context/
│   │   └── components/
│   └── vite.config.js
└── README.md



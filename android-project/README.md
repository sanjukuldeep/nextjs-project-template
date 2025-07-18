# Employee Attendance Android App

A complete Android application for employee attendance tracking with punch in/out functionality, user registration/login, and daily email reports.

## Features

- **User Registration & Login**: Secure authentication with username/password
- **Punch In/Out**: Simple one-tap attendance marking
- **Attendance History**: View complete attendance records
- **Daily Email Reports**: Automated reports sent at 7 PM daily
- **Real-time Updates**: Live attendance tracking
- **User Management**: Support for 5001-6000 employee IDs
- **Cluster/Department Support**: Organize employees by department

## Project Structure

```
android-project/
├── app/                          # Android app source code
│   ├── src/main/java/           # Kotlin source files
│   │   └── com/yourcompany/employeeattendance/
│   │       ├── MainActivity.kt
│   │       ├── LoginActivity.kt
│   │       ├── RegisterActivity.kt
│   │       ├── DashboardActivity.kt
│   │       ├── AttendanceHistoryActivity.kt
│   │       ├── models/          # Data models
│   │       ├── network/         # API communication
│   │       ├── utils/           # Utility classes
│   │       ├── adapters/        # RecyclerView adapters
│   │       ├── services/        # Background services
│   │       └── receivers/       # Broadcast receivers
│   ├── src/main/res/            # UI resources
│   └── build.gradle            # Gradle configuration
├── backend/                     # Node.js backend API
│   ├── server.js               # Main server file
│   └── package.json            # Backend dependencies
└── README.md                   # This file
```

## Setup Instructions

### 1. Android Studio Setup

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **Import Project**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Choose the `android-project` folder

3. **Configure Project**
   - Update `BASE_URL` in `RetrofitClient.kt` to your server URL
   - Replace company logo in `res/drawable/company_logo.png`
   - Update email credentials in `EmailReportService.kt`

### 2. Backend Server Setup

1. **Install Node.js**
   - Download from: https://nodejs.org/
   - Install with default settings

2. **Setup Backend**
   ```bash
   cd android-project/backend
   npm install
   ```

3. **Configure Email**
   - Update email credentials in `server.js`:
     - Replace `your-email@gmail.com` with your Gmail
     - Replace `your-app-password` with your Gmail app password
     - Replace `your-company-email@company.com` with recipient email

4. **Start Server**
   ```bash
   npm start
   # or for development with auto-restart
   npm run dev
   ```

### 3. Database Setup (Optional)

For production, replace the in-memory database with a real database:

1. **MySQL Setup**
   ```sql
   CREATE DATABASE employee_attendance;
   USE employee_attendance;

   CREATE TABLE users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(10) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       name VARCHAR(100) NOT NULL,
       cluster VARCHAR(50) NOT NULL,
       phone_number VARCHAR(15) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );

   CREATE TABLE attendance_records (
       id INT AUTO_INCREMENT PRIMARY KEY,
       user_id INT NOT NULL,
       username VARCHAR(10) NOT NULL,
       name VARCHAR(100) NOT NULL,
       punch_in_time DATETIME NOT NULL,
       punch_out_time DATETIME,
       date DATE NOT NULL,
       cluster VARCHAR(50) NOT NULL,
       working_hours VARCHAR(20),
       FOREIGN KEY (user_id) REFERENCES users(id)
   );
   ```

2. **Update Backend**
   - Install MySQL package: `npm install mysql2`
   - Update `server.js` to use MySQL instead of in-memory storage

### 4. Testing the App

1. **Start Backend Server**
   ```bash
   cd android-project/backend
   npm start
   ```

2. **Run Android App**
   - Connect Android device or start emulator
   - Click "Run" in Android Studio
   - Test with username: `5001`, password: `kuldeep singh`

3. **Test Features**
   - Register new users (5001-6000)
   - Login with credentials
   - Punch in/out
   - View attendance history
   - Check daily email reports at 7 PM

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Attendance
- `POST /api/attendance/punch` - Punch in/out
- `GET /api/attendance/user/:userId` - Get user attendance history
- `GET /api/attendance/daily-report` - Get daily attendance report
- `POST /api/attendance/send-report` - Send daily email report

### Health Check
- `GET /api/health` - Server health check

## Default Credentials

For testing, use these default credentials:
- **Username**: 5001
- **Password**: kuldeep singh

## Email Configuration

To enable email reports:

1. **Gmail Setup**
   - Enable 2-factor authentication
   - Generate app password: https://support.google.com/accounts/answer/185833
   - Update credentials in `server.js`

2. **Custom SMTP**
   - Replace Gmail configuration with your SMTP server
   - Update `transporter` configuration in `server.js`

## Customization

### Colors
Edit `res/values/colors.xml` to change app colors

### Strings
Edit `res/values/strings.xml` to change text content

### Layouts
Edit XML files in `res/layout/` to modify UI

### Icons
Replace icons in `res/drawable/` with your company logo

## Troubleshooting

### Common Issues

1. **Network Error**
   - Check backend server is running
   - Verify `BASE_URL` in `RetrofitClient.kt`
   - Check internet connection

2. **Email Not Sending**
   - Verify Gmail app password
   - Check firewall settings
   - Ensure less secure apps is enabled

3. **Build Errors**
   - Clean and rebuild project
   - Update Gradle and dependencies
   - Check Android SDK version

### Debug Mode
Enable debug logging in `RetrofitClient.kt`:
```kotlin
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```

## Production Deployment

1. **Backend**
   - Deploy to cloud (Heroku, AWS, DigitalOcean)
   - Use environment variables for sensitive data
   - Set up SSL certificate

2. **Android App**
   - Build release APK: Build → Generate Signed Bundle/APK
   - Upload to Google Play Store
   - Configure app signing

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review Android Studio logs
3. Check backend server logs
4. Contact development team

## License

This project is proprietary software for internal company use.

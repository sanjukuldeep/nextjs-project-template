const express = require('express');
const cors = require('cors');
const nodemailer = require('nodemailer');
const cron = require('node-cron');
const bodyParser = require('body-parser');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// In-memory database (replace with MySQL/PostgreSQL in production)
let users = [];
let attendanceRecords = [];

// Initialize some test users
users.push({
    id: 1,
    username: "5001",
    password: "kuldeep singh",
    name: "Kuldeep Singh",
    cluster: "IT Department",
    phoneNumber: "9876543210",
    createdAt: new Date().toISOString()
});

// API Routes

// Login endpoint
app.post('/api/auth/login', (req, res) => {
    const { username, password } = req.body;
    
    const user = users.find(u => u.username === username && u.password === password);
    
    if (user) {
        res.json({
            success: true,
            message: "Login successful",
            user: user
        });
    } else {
        res.status(401).json({
            success: false,
            message: "Invalid username or password"
        });
    }
});

// Register endpoint
app.post('/api/auth/register', (req, res) => {
    const { username, password, name, cluster, phoneNumber } = req.body;
    
    // Check if username already exists
    const existingUser = users.find(u => u.username === username);
    if (existingUser) {
        return res.status(400).json({
            success: false,
            message: "Username already exists"
        });
    }
    
    // Validate username range
    const userId = parseInt(username);
    if (isNaN(userId) || userId < 5001 || userId > 6000) {
        return res.status(400).json({
            success: false,
            message: "Username must be between 5001-6000"
        });
    }
    
    const newUser = {
        id: users.length + 1,
        username,
        password,
        name,
        cluster,
        phoneNumber,
        createdAt: new Date().toISOString()
    };
    
    users.push(newUser);
    
    res.json({
        success: true,
        message: "Registration successful",
        user: newUser
    });
});

// Punch attendance endpoint
app.post('/api/attendance/punch', (req, res) => {
    const { userId, username, name, cluster, type, timestamp } = req.body;
    
    if (type === 'IN') {
        // Check if already punched in today
        const today = new Date().toISOString().split('T')[0];
        const existingRecord = attendanceRecords.find(
            r => r.userId === userId && r.date === today && !r.punchOutTime
        );
        
        if (existingRecord) {
            return res.status(400).json({
                success: false,
                message: "Already punched in today"
            });
        }
        
        const newRecord = {
            id: attendanceRecords.length + 1,
            userId,
            username,
            name,
            punchInTime: timestamp,
            punchOutTime: null,
            date: today,
            cluster,
            workingHours: null
        };
        
        attendanceRecords.push(newRecord);
        
        res.json({
            success: true,
            message: "Punch In successful",
            attendanceRecord: newRecord
        });
    } else if (type === 'OUT') {
        const today = new Date().toISOString().split('T')[0];
        const record = attendanceRecords.find(
            r => r.userId === userId && r.date === today && !r.punchOutTime
        );
        
        if (!record) {
            return res.status(404).json({
                success: false,
                message: "No active punch-in found for today"
            });
        }
        
        record.punchOutTime = timestamp;
        
        // Calculate working hours
        const punchInTime = new Date(record.punchInTime);
        const punchOutTime = new Date(timestamp);
        const diffInMillis = punchOutTime - punchInTime;
        const hours = Math.floor(diffInMillis / (1000 * 60 * 60));
        const minutes = Math.floor((diffInMillis % (1000 * 60 * 60)) / (1000 * 60));
        record.workingHours = `${hours}h ${minutes}m`;
        
        res.json({
            success: true,
            message: "Punch Out successful",
            attendanceRecord: record
        });
    }
});

// Get user attendance history
app.get('/api/attendance/user/:userId', (req, res) => {
    const userId = parseInt(req.params.userId);
    const userRecords = attendanceRecords.filter(r => r.userId === userId);
    
    res.json({
        success: true,
        message: "Attendance records retrieved successfully",
        records: userRecords.sort((a, b) => new Date(b.date) - new Date(a.date))
    });
});

// Get daily report
app.get('/api/attendance/daily-report', (req, res) => {
    const date = req.query.date || new Date().toISOString().split('T')[0];
    const dailyRecords = attendanceRecords.filter(r => r.date === date);
    
    const uniqueUsers = new Set(dailyRecords.map(r => r.userId));
    const presentUsers = new Set(dailyRecords.filter(r => r.punchInTime).map(r => r.userId));
    
    res.json({
        success: true,
        message: "Daily report retrieved successfully",
        date: date,
        totalEmployees: users.length,
        presentEmployees: presentUsers.size,
        absentEmployees: users.length - presentUsers.size,
        records: dailyRecords
    });
});

// Send daily report via email
app.post('/api/attendance/send-report', async (req, res) => {
    const { date, records } = req.body;
    
    try {
        // Configure email transporter
        const transporter = nodemailer.createTransporter({
            service: 'gmail',
            auth: {
                user: 'your-email@gmail.com', // Replace with your email
                pass: 'your-app-password' // Replace with your app password
            }
        });
        
        // Build email content
        let emailContent = `DAILY ATTENDANCE REPORT\n`;
        emailContent += `========================\n\n`;
        emailContent += `Date: ${date}\n`;
        emailContent += `Total Employees: ${users.length}\n`;
        emailContent += `Present: ${records.filter(r => r.punchInTime).length}\n`;
        emailContent += `Absent: ${users.length - records.filter(r => r.punchInTime).length}\n\n`;
        emailContent += `ATTENDANCE DETAILS:\n`;
        emailContent += `-------------------\n`;
        emailContent += `Employee\t\tID\t\tPunch In\t\tPunch Out\t\tCluster\n`;
        emailContent += `--------\t\t--\t\t--------\t\t---------\t\t-------\n`;
        
        records.forEach(record => {
            const punchIn = record.punchInTime ? record.punchInTime.substring(11, 16) : 'N/A';
            const punchOut = record.punchOutTime ? record.punchOutTime.substring(11, 16) : 'N/A';
            emailContent += `${record.name}\t\t${record.username}\t\t${punchIn}\t\t${punchOut}\t\t${record.cluster}\n`;
        });
        
        emailContent += `\n\nReport generated at: ${new Date().toLocaleString()}`;
        emailContent += `\n\nThis is an automated report from Employee Attendance System.`;
        
        const mailOptions = {
            from: 'your-email@gmail.com',
            to: 'your-company-email@company.com', // Replace with your company email
            subject: `Daily Attendance Report - ${date}`,
            text: emailContent
        };
        
        await transporter.sendMail(mailOptions);
        
        res.json({
            success: true,
            message: "Daily report sent successfully"
        });
    } catch (error) {
        console.error('Error sending email:', error);
        res.status(500).json({
            success: false,
            message: "Failed to send email report"
        });
    }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
    res.json({
        success: true,
        message: "Server is running",
        timestamp: new Date().toISOString()
    });
});

// Schedule daily report at 7 PM
cron.schedule('0 19 * * *', async () => {
    console.log('Sending daily attendance report...');
    const today = new Date().toISOString().split('T')[0];
    const dailyRecords = attendanceRecords.filter(r => r.date === today);
    
    try {
        await fetch(`http://localhost:${PORT}/api/attendance/send-report`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                date: today,
                records: dailyRecords
            })
        });
        console.log('Daily report sent successfully');
    } catch (error) {
        console.error('Error sending daily report:', error);
    }
});

// Start server
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
    console.log(`Health check: http://localhost:${PORT}/api/health`);
});

// Export for testing
module.exports = app;

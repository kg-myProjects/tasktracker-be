<#-- @ftlvariable name="link" type="String" -->
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>TrackerApp | Password Reset</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            background-color: #020617;
        }

        .outer-table {
            width: 100%;
	        height: 100%;
   	        min-height: 100vh;
            background-color: #020617;
            background-image: url('https://tracker-app.online/background.png');
            background-size: cover;
            background-position: center;
        }

        .outer-td {
            padding: 75px 20px 75px 20px;
            background-color: rgba(2, 6, 23, 0.75);
        }

        .container {
            width: 100%;
            max-width: 600px;
            margin: 0 auto;
            background-color: #1e293b;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            border: 2px solid #06b6d433;
            border-collapse: separate;
        }

        .header {
            background: linear-gradient(90deg, #0f172a 0%, #083344 50%, #0f172a 100%);
            padding: 20px;
            text-align: center;
            font-family: Arial, sans-serif;
            border-bottom: 1px solid rgba(6, 182, 212, 0.2);
            border-radius: 8px 8px 0 0;
        }

        .header h1 {
            margin: 0 0 8px 0;
            font-size: 24px;
            text-transform: uppercase;
            color: #22d3ee;
        }

        .content {
            background-color: rgba(2, 6, 23, 0.9);
            padding: 20px;
            font-family: Arial, sans-serif;
            color: #9ca3af;
            font-size: 16px;
            line-height: 1.5;
        }

        .content h2 {
            margin: 0;
            color: #9ca3af;
        }

        .content a {
            color: #22d3ee;
            text-decoration: underline;
        }

        .logo {
            font-family: Arial, sans-serif;
            font-size: 42px;
            font-weight: 900;
            letter-spacing: -1px;
            color: #ffffff;
            text-shadow: 0 0 10px rgba(6, 182, 212, 1);
        }

        .logo-accent {
            color: #22d3ee;
        }

        .button-wrapper {
            text-align: center;
            margin: 30px 0;
        }

        .button {
            display: inline-block;
            background-color: #06b6d4;
            color: #ffffff !important;
            text-decoration: none !important;
            padding: 10px 16px;
            border-radius: 12px;
            font-weight: bold;
            font-size: 14px;
            line-height: 20px;
            font-family: Arial, sans-serif;
        }

        .footer {
            background: linear-gradient(90deg, #0f172a 0%, #083344 50%, #0f172a 100%);
            padding: 20px;
            text-align: center;
            font-family: Arial, sans-serif;
            font-size: 12px;
            color: #22d3ee;
            border-radius: 0 0 8px 8px;
        }
    </style>
</head>
    <body>
        <table class="outer-table">
            <tr>
                <td class="outer-td">
                    <table class="container">
                    <tr>
                        <td class="header">
                            <span class="logo">Tracker<span class="logo-accent">App</span></span>
                            <h1>Password Reset</h1>
                        </td>
                    </tr>
                    <tr>
                        <td class="content">
                            <h2>Dear User,</h2>
                            <p>you requested a password reset!</p>
                            <p>Please, click the button below to set a new password:</p>
                            <p class="button-wrapper">
                                <a href="${link}" class="button">Password Reset</a>
                            </p>
                            <p>If the button above doesn't work, please copy and paste the following link into your browser:</p>
                            <p><a href="${link}">${link}</a></p>
                        </td>
                    </tr>
                    <tr>
                        <td class="footer">
                            © 2026 TrackerApp. All rights reserved.
                        </td>
                    </tr>
                    </table>
                </td>
            </tr>
        </table>
    </body>
</html>
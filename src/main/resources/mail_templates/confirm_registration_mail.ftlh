<#-- @ftlvariable name="link" type="String" -->
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>TrackerApp | Email Confirmation</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            background-color: #0D1117;
            font-family: "Segoe UI", Arial, sans-serif;
        }

        .outer-table {
            width: 100%;
            background-color: #0D1117;
            border-spacing: 0;
        }

        .outer-td {
            padding: 75px 20px 75px 20px;
            background-color: #0D1117;
        }

        .container {
            width: 100%;
            max-width: 600px;
            margin: 0 auto;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 4px #0E7490;
            border: 2px solid #0E7490;
            border-collapse: separate;
            border-spacing: 0;
        }

        .header {
            background: linear-gradient(90deg, #0D1117 0%, #0E7490 50%, #0D1117 100%);
            padding: 20px;
            text-align: center;
            border-bottom: 1px solid #06B6D433;
            border-radius: 8px 8px 0 0;
        }

        .header h1 {
            margin: 0 0 8px 0;
            font-size: 24px;
            text-transform: uppercase;
            color: #22D3EE;
        }

        .content {
            background-color: #161B22;
            padding: 20px;
            color: #9CA3AF;
            font-size: 16px;
            line-height: 1.5;
        }

        .content h2 {
            margin: 0;
            color: #9CA3AF;
        }

        .content a {
            color: #22D3EE;
            text-decoration: underline;
        }

        .logo {
            font-family: Arial, sans-serif;
            font-size: 42px;
            font-weight: 900;
            letter-spacing: -1px;
            color: #ffffff;
            text-shadow: 0 0 10px #06B6D4;
        }

        .logo-accent {
            color: #22D3EE;
        }

        .button-wrapper {
            text-align: center;
            margin: 30px 0;
        }

        .button {
            display: inline-block;
            background-color: #0E7490;
            color: #ffffff !important;
            text-decoration: none !important;
            padding: 10px 16px;
            border-radius: 5px;
            font-weight: bold;
            font-size: 14px;
            line-height: 20px;
        }

        .footer {
            background: linear-gradient(90deg, #0D1117 0%, #0E7490 50%, #0D1117 100%);
            padding: 20px;
            text-align: center;
            font-size: 12px;
            color: #22D3EE;
            border-radius: 0 0 8px 8px;
        }
    </style>
</head>
    <body>
        <table class="outer-table">
            <tr>
                <td class="outer-td" align="center">
                    <table class="container">
                    <tr>
                        <td class="header">
                            <span class="logo">Tracker<span class="logo-accent">App</span></span>
                            <h1>E-mail Confirmation</h1>
                        </td>
                    </tr>
                    <tr>
                        <td class="content">
                            <h2>Dear User,</h2>
                            <p>thank you for registering!</p>
                            <p>Please confirm your registration by clicking the button below:</p>
                            <p class="button-wrapper">
                                <a href="${link}" class="button">Confirm Registration</a>
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
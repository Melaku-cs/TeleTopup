Ethio Telecom Mobile Top-up Integration with Wegagen Bank
Overview
This project implements the integration of Ethio Telecom's Mobile Top-up service with Wegagen Bank through a USSD interface. It allows users to perform mobile phone balance recharges using their Wegagen Bank accounts, following a multi-step flow that ensures secure and accurate top-up transactions.

Table of Contents
1 Features

2 System Requirements

 3 How to Use

4 Code Overview

5 Transaction Flow

6 Error Handling

7 Integration with Ethio Telecom and Wegagen Bank

8 Database Integration

Features
===============
* USSD Interaction: The system leverages USSD  codes to guide users through the recharge process.

* Iterative Process: The integration uses an iterative process for gathering data and processing requests.

* Phone Number Validation: Ensures that phone numbers entered by users are valid.

* Bank Number Selection: Users can select their preferred bank account for the recharge.

* Amount Validation: Ensures that the amount entered by the user is valid and meets the necessary requirements.

* Transaction Success/Failure Handling: The system provides appropriate responses based on the transaction result.

System Requirements
================================================================================================================
* Java Development Kit (JDK) 8 or higher.

* Integrated Development Environment (IDE) like IntelliJ IDEA, Eclipse, or similar.

* A compatible web service to handle USSD and API calls for both Ethio Telecom and Wegagen Bank.

* Database integration for saving transaction details (MySQL or another relational database).

How to Use
============================================================================================================
1 Start the USSD Service: Initiate a session using a USSD code on the mobile device.

2 Choose the Action: Users are presented with a series of options to select their preferred action (e.g., recharge mobile or view account balance).

3 Phone Number and Bank Account: Users enter their phone number and choose a bank account for the recharge.

4 Amount Entry: The user specifies the amount to be topped up.

5 PIN Authentication: The user enters a PIN for authentication.

6 Transaction Confirmation: The system processes the transaction and confirms the result.

Code Overview
==============================================================================================================
The code implements an interactive session for the user to complete a mobile recharge through a multi-step process. Each step involves the following:

* Session Management: SessionManager handles user sessions, storing and retrieving session data as required.

* Transaction Processing: The system interacts with Ethio Telecom and Wegagen Bank through the appropriate methods.

* Error Handling: The system checks for invalid inputs, session timeouts, and transaction failures.

Session Management
=================================================================================================================
* The code uses SessionManager to maintain session states. A session state is tracked for each user, ensuring that each step in the process is properly followed:

* The session is updated each time the user performs an action, such as selecting a bank or inputting an amount.

* The session is removed after the transaction is completed or if an error occurs.

Flow Handling
========================================================================================================================
The flow consists of multiple iterations (steps):

1 Iteration 1: User is prompted to select an action (e.g., top-up or check account balance).

2  Iteration 2: User is prompted to select a bank account.

3 Iteration 3-4: User selects the phone number to recharge and the amount to be topped up.

4 Iteration 5-6: User is asked to authenticate with a PIN.

5 Iteration 7-8: The system validates and processes the transaction.

Transaction Validation
===============================================================================================================
The system ensures:

* Phone numbers entered are valid.

* Bank account numbers are selected properly.

* Amount entered is positive and within the valid range.

* PIN authentication is successfully completed before proceeding with the transaction.

Error Handling
==========================================================================================================
Errors are handled in a variety of ways:

* Invalid Input: Users are notified if their input is invalid (e.g., incorrect phone number format).

* Session Expiry: Sessions that are too long or have invalid actions are terminated.

* Transaction Failure: If the top-up fails, the user is informed with an appropriate message.

Transaction Flow
=========================================================================================================
1 Start Session: The system begins with creating a new session for the user.

2 Request Handling: Based on user input, a specific response is built, such as prompting for phone numbers or amount.

3 Top-up Request: If all inputs are validated, the system proceeds with sending a top-up request to Ethio Telecom.

4 Final Response: The transaction status is returned, and the system either confirms the top-up or provides an error message.

Integration with Ethio Telecom and Wegagen Bank
===================================================================================================================
* The system assumes that Ethio Telecom provides an API or USSD interface for mobile top-up and balance inquiries.

* The integration assumes Wegagen Bank has a compatible mobile banking or API service for interacting with the user's account.

The system will send top-up requests to Ethio Telecom and receive responses indicating whether the transaction was successful or failed.

Database Integration
===============================================================================================================
 The system integrates with a database to persist transaction details. For instance:

* The transactionsDao is responsible for saving the top-up transaction.

* Transaction data includes phone number, bank account, amount, transaction ID, and timestamp.

This allows the system to keep track of all transactions for auditing and verification purposes.

To Run the Project
==================================================================================================================
1 Set up a Java environment (JDK 8 or higher).

2 Clone the repository.

3 Configure database connections (if using).

4 Build and run the project on your preferred IDE or using the command line (mvn clean install).

5 Ensure that Ethio Telecom and Wegagen Bank APIs are properly integrated.

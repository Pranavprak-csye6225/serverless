# Serverless Cloud Functions

This repository contains serverless functions built using Google Cloud Functions for sending emails when a user is created and updating a Cloud SQL database with the email sent time.



## Cloud Functions

### 1. SendEmailFunction

This function sends an email notification when a user is created.

#### Trigger

- Pub/Sub message from the SendEmailFunction.

#### Dependencies

- Google Cloud Pub/Sub for triggering the function.
- Mailgun API for sending emails.

### 2. UpdateEmailSentTimeFunction

This function updates the Cloud SQL database with the email sent time when a user is created.

#### Trigger

- Pub/Sub message from the SendEmailFunction.

#### Dependencies

- Google Cloud SQL for storing user data.

## Prerequisites

Before setting up the Cloud Functions, make sure you have:

- A Google Cloud Platform (GCP) account.
- Enabled Google Cloud Functions, Google Cloud Pub/Sub, Google Cloud SQL, and Google Cloud Storage APIs.
- Set up Mailgun API credentials for sending emails.

## Setup

1. Clone this repository to your local machine:

```bash
git clone https://github.com/your-username/serverless.git

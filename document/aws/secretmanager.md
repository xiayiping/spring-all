
1. create IAM user, assign user as SecretsManagerReadWrite
2. in KMS, bound key persmmission to IAM user
3. in secret manager, bound permission to IAM user
4. create access key in the IAM user page.

5. create IAM role in role
6. attach IAM role to EC2
7. create user in KMS for the new IAM role


create sdk credential
https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html


https://docs.aws.amazon.com/emr/latest/ManagementGuide/emr-ranger-tls-certificates.html

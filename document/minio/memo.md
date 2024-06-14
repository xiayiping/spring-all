```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Effect": "Allow",
      "Resource": [
        "arn:aws:s3:::kyc-qa/*"
      ]
    }
  ]
}
```

```shell
mc
mc alias set kycqa https://minio.tcghl.com matt admin123sh
mc admin info kycqa
mv
mc
mc ls
mc list --help
mc list kycqa
mc ls
mc ls kycqa
mc kycqa
mc mb
mc mb --with-versioning kycqa/kyc-qa
mc ls kycqa/kyc-qa
mc admin policy ls
mc admin policy ls kycqa
mc admin policy ls info kycqa/readwrite
mc admin policy info kycqa/readwrite
mc admin policy info kycqa readwrite
mc ls kycqa/kyc-qa
mc ls kycqa kyc-qa
mc info kycqa kyc-qa
mc admin info kycqa kyc-qa
mc admin info kycqa
mc stat kycqa/kyc-qa
mc admin policy info kycqa readwrite
```
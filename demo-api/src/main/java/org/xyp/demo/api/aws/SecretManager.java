package org.xyp.demo.api.aws;

// Make sure to import the following packages in your code

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretManager {

    public static void main(String[] args) {
        getSecret();
//mvn compile exec:java -Dexec.mainClass="org.xyp.demo.api.aws.SecretManager"
    }

    public static void getSecret() {


        String secretName = "key_store";
        Region region = Region.of("ap-southeast-1");
        // Create a Secrets Manager client
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                // credential chain https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html
//                .credentialsProvider( DefaultCredentialsProvider.create())
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build()
        ) {

            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse getSecretValueResponse;

            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);


            String secret = getSecretValueResponse.secretString();
            System.out.println(secret);
        }
        // Your code goes here.
    }
}

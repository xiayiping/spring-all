package org.xyp.demo.api.aws;

// Make sure to import the following packages in your code
 import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
 import software.amazon.awssdk.regions.Region;
 import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
 import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
 import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
public class SecretManager {

    public static void main(String[] args) {
        getSecret();

    }
    public static void getSecret() {

        String secretName = "key_store";
        Region region = Region.of("ap-southeast-1");

        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
//                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            // For a list of exceptions thrown, see
            // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
            throw e;
        }

        String secret = getSecretValueResponse.secretString();
        System.out.println(secret);
        // Your code goes here.
    }
}

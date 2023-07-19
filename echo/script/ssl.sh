
# Create CertificateSigningRequest file
keytool -list -v -keystore .\domain.key
keytool -certreq -alias caller-cert -file caller.crt -keystore truststore.caller.p12 -storepass 123456
keytool -export  -alias caller-cert -file caller.crt -keystore keystore.caller.p12 -storepass 123456 -rfc
keytool -printcertreq -file caller.csr

# Import Certificate file to a keystore
keytool -import -trustcacerts -keystore keystore.caller.p12 -storepass p@ssw0rd -alias caller-crt -file caller.csr

keytool -import -trustcacerts -keystore keystore.caller.p12 -storepass 123456   -alias caller-crt -file caller.crt -keypass 123456 -storetype PKCS12

keytool -import -alias caller-cert -file ./caller.crt -storetype PKCS12 -keystore ./trust.caller.p12

keytool -genkeypair -alias caller-key -keyalg RSA -keysize 4096 -validity 90 -dname "CN=localhost" -keypass 123456 -keystore keystore.caller.p12  -storeType PKCS12 -storepass 123456


https://unix.stackexchange.com/questions/347116/how-to-create-keystore-and-truststore-using-self-signed-certificate

openssl x509 -in ./diagserverCA.pem -nout -text

# https://serverfault.com/questions/9708/what-is-a-pem-file-and-how-does-it-differ-from-other-openssl-generated-key-file
# https://www.youtube.com/watch?v=kAaIYRJoJkc
# https://www.youtube.com/watch?v=b6KHuJYJl3Q
#####################################
# https://unix.stackexchange.com/questions/347116/how-to-create-keystore-and-truststore-using-self-signed-certificate
#####################################
openssl genrsa -out diagserverCA.key 2048

openssl req -x509 -new -nocert -key diagserverCA.key \
  -sha256 -days 1024 -out diagserverCA.pem

openssl pkcs12 -export -name server-cert \
  -in diagserverCA.pem -inkey diagserverCA.key \
  -out serverkeystore.p12

keytool -importkeystore -destkeystore server.keystore \
  -srckeystore serverkeystore.p12 -srcstoretype pkcs12 \
  -alias server-cert

keytool -import -alias client-cert \
  -file ../../../../../caller/src/main/resources/keystore/diagclientCA.pem -keystore server.truststore

keytool -import -alias server-cert \
  -file diagserverCA.pem -keystore server.truststore



## ---------------------------



openssl genrsa -out diagclientCA.key 2048

openssl req -x509 -new -nodes -key diagclientCA.key \
-sha256 -days 1024 -out diagclientCA.pem

openssl pkcs12 -export -name client-cert \
  -in diagclientCA.pem -inkey diagclientCA.key \
  -out clientkeystore.p12

keytool -importkeystore -destkeystore client.keystore \
  -srckeystore clientkeystore.p12 -srcstoretype pkcs12 \
  -alias client-cert

keytool -import -alias server-cert  \
  -file ../../../../../echo/src/main/resources/keystore/diagserverCA.pem \
  -keystore client.truststore

keytool -import -alias client-cert -file diagclientCA.pem \
  -keystore client.truststore



package sun.security.provider.certpath.ForwardBuilder.java:721
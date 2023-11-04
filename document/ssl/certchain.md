
```shell

openssl genrsa -aes256 -out ca-key.pem 4096  # generate private key.

[ -f ./ca.pem ] && rm ./ca.pem
openssl req -new -x509 -sha256 -days 3650 -key ca-key.pem -out ca.pem \
  -config ./ca.conf -extensions v3_req 
  
cat ca.pem 
openssl x509 -in ca.pem -text -noout

####### intermediate certificate ########
openssl genrsa -out cert-int-key.pem 4096 

[ -f ./cert-int.csr ] && rm ./cert-int.csr
openssl req -new -sha256  -key cert-int-key.pem -out cert-int.csr \
  -config ./req-int.conf -extensions v3_req 
  
openssl req -in ./cert-int.csr -text -noout

[ -f ./cert-int.pem ] && rm ./cert-int.pem
openssl x509 -req -sha256 -days 3650 -in ./cert-int.csr \
  -CA ./ca.pem -CAkey ./ca-key.pem \
  -out ./cert-int.pem \
  -extfile ./req-int.conf -extensions v3_req \
  -CAcreateserial -clrext

openssl x509 -in cert-int.pem -text -noout

######## leaf certificate ########
openssl genrsa -out cert-leaf-key.pem 4096 

[ -f ./cert-leaf.csr ] && rm ./cert-leaf.csr
openssl req -new -sha256  -key cert-leaf-key.pem -out cert-leaf.csr \
  -config ./req-leaf.conf  -extensions v3_req 
  
openssl req -in ./cert-leaf.csr -text -noout

[ -f ./cert-leaf.pem ] && rm ./cert-leaf.pem
openssl x509 -req -sha256 -days 3650 -in ./cert-leaf.csr \
  -CA ./cert-int.pem -CAkey ./cert-int-key.pem \
  -out ./cert-leaf.pem \
  -extfile ./req-leaf.conf -extensions v3_req \
  -CAcreateserial -clrext 

openssl x509 -in cert-leaf.pem -text -noout

######## trust store chain ########
#cat cert-leaf.pem > trust-root.pem
#cat cert-int.pem >> trust-root.pem
cat ca.pem > trust-root.pem

[ -f ./paradise.truststore.p12 ] && rm ./paradise.truststore.p12
keytool -J-Duser.language=en \
  -import -alias paradise-certificate \
  -file ./trust-root.pem -keystore ./paradise.truststore.p12

keytool -list -v -keystore ./paradise.truststore.p12 

######## key store ########
cat cert-leaf.pem > cert-chain.pem
cat cert-int.pem >> cert-chain.pem
#cat ca.pem >> cert-chain.pem

cat cert-leaf-key.pem > key-leaf.pem
cat cert-chain.pem >> key-leaf.pem

openssl x509 -in  key-leaf.pem -text -noout

[ -f ./paradise.keystore.p12 ] && rm ./paradise.keystore.p12 
openssl pkcs12 -export -inkey ./cert-leaf-key.pem -in ./key-leaf.pem \
  -name paradise-key -out ./paradise.keystore.p12

keytool -list -v -keystore ./paradise.keystore.p12 

```
PKI Generator for vitamui environment
==================================

For a custom environment remove all files inside:
/pki/ca/
/pki/certs/ subdirectories
/pki/vitam/client-external/

Recreate your ca, certs, and stores by executing the following commands.

## Create the root CA

Fill the right configuration files from ./config directory
and execute the following command:
~~~
 ./scripts/generate_ca.sh
~~~

This script create inside config file and index.txt file
and a serial.txt file (the serial number is randomly
generated), then create ca_root.key and ca_root.crt inside
ca directory.


## Create certificates for each VitamUI service

Create certificates for each service (each directory inside /pki/certs/ ) and create also vitam certs for
external client ( /pki/vitam/client-external/ ). You need to change the values at the script end for
your custom needs.
~~~
./scripts/generate_certs.sh
~~~

## Create stores for each VitamUI service

Create p12 certificate and store (jks) for each service (each directory inside /pki/certs/ )
and also vitam client external ( /pki/vitam/client-external/ ).

~~~
./scrips/generate_stores.sh
~~~



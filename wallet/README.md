# Bac 3.0

- issue new public entity
- that entity should issue a diploma
- then check the diploma

-> check if the issuer already exists -> if not
-> register issuer (gigel) -> sign the diploma with the private key -> check the diploma with the public key (verify credential)

---
flow 1: call api to sign the diploma with the diploma and the user -> get the hash

flow to participate to admission: scan qr code (rest call to poli) -> uploads the doc and the signature

---

## Build locally

1. Get the dependencies: `flutter pub get`.
2. Generate the code: `dart run build_runner build`. (and watch: `dart run build_runner watch`)
3. Start the app: `flutter run lib/main.dart`.

### Other stuff

The wallet is heavily inspired from https://github.com/allanclempe/ether-wallet-flutter/tree/master

### Users

Regular user - 'Student'

test@test.com
T@5ttest

Ministry user

ministry@ministry.com
M1nistry@@
